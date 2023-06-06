package servidor.udp.video;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ServidorRecibeVideoLlamadaUDP extends Thread {
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    private static final int BUFFER_SIZE = 40000;
    private static final int VIDEO_PORT = 5000;
    private static final int AUDIO_PORT = 5001;

    protected String address = null;

    private final DatagramSocket videoSocket;
    private final DatagramSocket audioSocket;

    private JFrame frame;
    private JLabel videoLabel;

    private AudioFormat audioFormat;
    private SourceDataLine sourceDataLine;
    private boolean isRunning;

    public ServidorRecibeVideoLlamadaUDP() {
        try {
            videoSocket = new DatagramSocket(VIDEO_PORT);
            audioSocket = new DatagramSocket(AUDIO_PORT);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        frame = new JFrame("Servidor");
        videoLabel = new JLabel();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(videoLabel, BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);

        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 2, 4, BUFFER_SIZE, true);
        SourceDataLine.Info sourceDataLineInfo = new SourceDataLine.Info(SourceDataLine.class, audioFormat);
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceDataLineInfo);
            sourceDataLine.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        isRunning = true;
        sourceDataLine.start();
        while (isRunning) {

            // Receive video frame
            byte[] receivedData = null;
            try {
                receivedData = receiveData(videoSocket);
            } catch (Exception e) {
                //System.err.println("Video " + e.getMessage());
                continue; // Salta a la siguiente iteración del bucle
                //throw new RuntimeException(e);
            }
            if(receivedData != null){
                Mat frame = decodeFrame(receivedData);
                if(frame != null){
                    BufferedImage image = matToBufferedImage(frame);

                    if(image != null){
                        // Display video frame
                        ImageIcon icon = new ImageIcon(image);
                        videoLabel.setIcon(icon);
                        videoLabel.repaint();
                    }
                }
            }

            // Receive audio frame
            byte[] receivedDataAudio = new byte[0];
            try {
                receivedDataAudio = receiveData(audioSocket);
            } catch (Exception e) {
                //System.err.println("Audio " + e.getMessage());
                continue;
                //throw new RuntimeException(e);
            }
            if(receivedDataAudio != null){
                receivedDataAudio = decompressAudio(receivedDataAudio);

                if(receivedDataAudio != null){
                    // Last packet received, play the audio
                    try {
                        sourceDataLine.write(receivedDataAudio, 0, receivedDataAudio.length);
                    }catch (IllegalArgumentException e){

                    }catch (Exception e ){
                        continue;
                    }
                }
            }
        }

        // Cleanup resources
        videoSocket.close();
        audioSocket.close();
        sourceDataLine.stop();
        sourceDataLine.close();
    }

    private int receiveDataSize(DatagramSocket socket) {
        byte[] sizeData = new byte[4];
        DatagramPacket sizePacket = new DatagramPacket(sizeData, sizeData.length);
        try {
            socket.setSoTimeout(200);
            socket.receive(sizePacket);
            InetSocketAddress senderAddress = new InetSocketAddress(sizePacket.getAddress(), sizePacket.getPort());
            if(address == null){
                address = String.valueOf(senderAddress.getAddress());
            }
            byte[] confirmationData = {1};
            DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length, senderAddress.getAddress(), senderAddress.getPort());
            socket.send(confirmationPacket);
            return ByteBuffer.wrap(sizeData).getInt();
        } catch (Exception e) {
            //System.err.println("Error al recibir el tamaño de datos: " + e.getMessage());
            return 0;
        }
    }

    private byte[] receiveData(DatagramSocket socket) {
        int totalBytes = receiveDataSize(socket);
        while(totalBytes == 0){
            totalBytes = receiveDataSize(socket);
        }
        int receivedBytes = 0;
        int packetSize = 1460;
        byte[] receivedData = new byte[totalBytes];

        while (receivedBytes < totalBytes && receivedData.length >= receivedBytes) {
            int remainingBytes = totalBytes - receivedBytes;
            int packetBytes = Math.min(packetSize, remainingBytes);

            byte[] buffer = new byte[packetBytes];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            byte[] prevBuffer = new byte[buffer.length];
            DatagramPacket prevPacket = new DatagramPacket(prevBuffer, buffer.length);
            try {
                socket.setSoTimeout(900);
                socket.receive(packet);

                byte[] confirmationData = {1};
                DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length,
                        packet.getAddress(), packet.getPort());
                socket.send(confirmationPacket);
                prevPacket = packet;
                prevBuffer = buffer;
            } catch (IOException e) {
                byte[] confirmationData = {1};
                DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length,
                        prevPacket.getAddress(), prevPacket.getPort());
                try {
                    socket.send(confirmationPacket);
                } catch (IOException ex) {}
            }

            if(packet.getData() != null){
                boolean isEmpty = true;
                for (byte b : prevBuffer) {
                    if (b != 0) {
                        isEmpty = false;
                        break;
                    }
                }
                if (!isEmpty) {
                    int bytesToCopy = Math.min(packetBytes, receivedData.length - receivedBytes);
                    System.arraycopy(prevBuffer, 0, receivedData, receivedBytes, bytesToCopy);
                } else {
                    System.arraycopy(buffer, 0, receivedData, receivedBytes, packetBytes);
                }
            }else {
                // El paquete se perdió, se rellena el buffer con datos de relleno
                byte[] fillerData = new byte[packetBytes];
                // Rellenar el buffer con datos de relleno (por ejemplo, ceros)
                Arrays.fill(fillerData, (byte) 0);
                int bytesToCopy = Math.min(packetBytes, receivedData.length - receivedBytes);
                System.arraycopy(fillerData, 0, receivedData, receivedBytes, bytesToCopy);
            }
            receivedBytes += packetSize;

        }
        return receivedData;
    }

    private byte[] decompressAudio(byte[] compressedData) {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);

        byte[] decompressedData = new byte[compressedData.length * 4]; // Estimate initial size
        int decompressedSize;
        try {
            decompressedSize = inflater.inflate(decompressedData);
        } catch (DataFormatException e) {
            return null;
        }
        inflater.end();

        // Create a new array with the exact size of the decompressed data
        byte[] decompressedBytes = new byte[decompressedSize];
        System.arraycopy(decompressedData, 0, decompressedBytes, 0, decompressedSize);

        return decompressedBytes;
    }

    private Mat decodeFrame(byte[] compressedData) {
        MatOfByte matOfByte = new MatOfByte(compressedData);
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        int width = mat.cols();
        int height = mat.rows();

        // Check if the dimensions are valid
        if (width <= 0 || height <= 0) {
            //System.err.println("Invalid image dimensions: width=" + width + ", height=" + height);
            return null;
        }

        int bufferSize = mat.channels() * width * height;
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(width, height, type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);

        return image;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getAddress() {
        return address;
    }
}