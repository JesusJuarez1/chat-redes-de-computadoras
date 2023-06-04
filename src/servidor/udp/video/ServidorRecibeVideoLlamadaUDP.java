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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ServidorRecibeVideoLlamadaUDP extends Thread {
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    private static final int PACKET_SIZE = 65507;
    private static final int VIDEO_PORT = 50000;
    private static final int AUDIO_PORT = 50001;

    private DatagramSocket videoSocket;
    private DatagramSocket audioSocket;

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

        audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    private int receiveDataSize(DatagramSocket socket) {
        byte[] sizeData = new byte[4];
        DatagramPacket sizePacket = new DatagramPacket(sizeData, sizeData.length);
        try {
            socket.receive(sizePacket);
            return ByteBuffer.wrap(sizeData).getInt();
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            return 0;
        }
    }

    private byte[] receiveData(DatagramSocket socket, int totalBytes) throws Exception {
        int receivedBytes = 0;
        int packetSize = 1400;
        byte[] receivedData = new byte[totalBytes];

        while (receivedBytes < totalBytes) {
            int remainingBytes = totalBytes - receivedBytes;
            int packetBytes = Math.min(packetSize, remainingBytes);

            byte[] buffer = new byte[packetBytes];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.arraycopy(buffer, 0, receivedData, receivedBytes, packetBytes);

            receivedBytes += packetBytes;
        }
        return receivedData;
    }

    public void start() {
        isRunning = true;
        while (isRunning) {
            // Receive video frame
            int totalBytes = receiveDataSize(videoSocket);
            byte[] receivedData = new byte[0];
            try {
                receivedData = receiveData(videoSocket,totalBytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Mat frame = decodeFrame(receivedData);
            BufferedImage image = matToBufferedImage(frame);
            // Display video frame
            ImageIcon icon = new ImageIcon(image);
            videoLabel.setIcon(icon);
            videoLabel.repaint();

            // Receive audio frame
            int totalBytesAudio = receiveDataSize(audioSocket);
            byte[] receivedDataAudio = new byte[0];
            try {
                receivedDataAudio = receiveData(audioSocket,totalBytesAudio);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Last packet received, play the audio
            sourceDataLine.write(receivedDataAudio, 0, receivedDataAudio.length);
        }

        // Cleanup resources
        videoSocket.close();
        audioSocket.close();
        sourceDataLine.stop();
        sourceDataLine.close();
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
            System.err.println("Invalid image dimensions: width=" + width + ", height=" + height);
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
}