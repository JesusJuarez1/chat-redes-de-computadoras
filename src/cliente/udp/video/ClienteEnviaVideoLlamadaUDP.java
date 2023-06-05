package cliente.udp.video;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

import javax.sound.sampled.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public class ClienteEnviaVideoLlamadaUDP extends Thread {
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    private static final int FPS = 30;
    private static final int AUDIO_BUFFER_SIZE = 20000;
    private static final int BUFFER_SIZE = 48000;
    private static final int VIDEO_PORT = 5000;
    private static final int AUDIO_PORT = 5001;
    private static final int TIMEOUT = 300;
    private volatile boolean isRunning;

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private TargetDataLine targetDataLine;
    private VideoCapture videoCapture;
    private AudioFormat audioFormat;
    private JFrame frame;
    private JLabel videoLabel;
    private JButton stopButton;

    public ClienteEnviaVideoLlamadaUDP(String server, DatagramSocket socket) {
        this.socket = socket;
        try {
            serverAddress = InetAddress.getByName(server);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        videoCapture = new VideoCapture(0);

        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 2,
                4, BUFFER_SIZE, true);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        frame = new JFrame("Cliente");
        videoLabel = new JLabel();
        stopButton = new JButton("Detener envío");

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopSending();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(videoLabel, BorderLayout.CENTER);
        panel.add(stopButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setVisible(true);
    }

    private void displayVideo() {
        SwingWorker<Void, BufferedImage> worker = new SwingWorker<Void, BufferedImage>() {
            @Override
            protected Void doInBackground() {
                try {
                    while (isRunning) {
                        // Capture video frame
                        Mat frame = new Mat();
                        videoCapture.read(frame);
                        Imgproc.resize(frame, frame, new Size(FRAME_WIDTH, FRAME_HEIGHT));
                        BufferedImage image = matToBufferedImage(frame);

                        // Publish the image for display
                        publish(image);

                        // Delay between frames
                        Thread.sleep(1000 / FPS);
                    }
                } catch (Exception e) {
                    System.err.println("Exception: " + e.getMessage());
                    System.exit(1);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<BufferedImage> chunks) {
                // Update the UI with the latest image
                BufferedImage image = chunks.get(chunks.size() - 1);
                ImageIcon icon = new ImageIcon(image);
                videoLabel.setIcon(icon);
                videoLabel.repaint();
            }
        };

        worker.execute();
    }

    public void run() {
        try {
            byte[] audioBuffer = null;
            targetDataLine.start();

            // Start displaying the video
            displayVideo();
            isRunning = true;
            while (isRunning) {
                // Capture video frame
                Mat frame = new Mat();
                videoCapture.read(frame);

                // Convert video frame to compressed bytes
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, matOfByte);
                byte[] compressedVideo = matOfByte.toArray();


                audioBuffer = new byte[AUDIO_BUFFER_SIZE];
                // Capturar audio frame
                int bytesRead = targetDataLine.read(audioBuffer, 0, AUDIO_BUFFER_SIZE);
                // Compress audio buffer
                byte[] compressedAudio = compressAudio(audioBuffer);

                // Send video frame
                sendData(compressedVideo, VIDEO_PORT);

                // Send audio buffer
                sendData(compressedAudio, AUDIO_PORT);
            }
            // Cleanup resources
            videoCapture.release();
            targetDataLine.stop();
            targetDataLine.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    private byte[] compressAudio(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] compressedData = new byte[data.length];
        int compressedSize = deflater.deflate(compressedData);
        deflater.end();

        // Create a new array with the exact size of the compressed data
        byte[] compressedBytes = new byte[compressedSize];
        System.arraycopy(compressedData, 0, compressedBytes, 0, compressedSize);

        return compressedBytes;
    }

    private void stopSending() {
        isRunning = false;
        frame.dispose();
    }

    private void isPacketReceived(DatagramPacket sizePacket, int port) {
        boolean isPacketReceived = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 3; // Número máximo de intentos permitidos

        while (!isPacketReceived && attempts < MAX_ATTEMPTS) {
            // Enviar el paquete
            try {
                socket.send(sizePacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] confirmationData = new byte[1];
            DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length);
            long startTime = System.currentTimeMillis(); // Obtener el tiempo de inicio

            while (true) {
                try {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long remainingTime = TIMEOUT - elapsedTime;

                    if (remainingTime <= 0) {
                        break; // Se superó el tiempo de espera, salir del bucle interno
                    }

                    socket.setSoTimeout((int) remainingTime);
                    socket.receive(confirmationPacket);

                    // Verificar si se recibió la confirmación del paquete
                    if (confirmationPacket.getAddress() != null) {
                        if (confirmationPacket.getAddress().equals(serverAddress) && confirmationPacket.getPort() == port) {
                            isPacketReceived = true;
                            break; // Confirmación recibida, salir del bucle interno
                        }
                    }
                } catch (SocketTimeoutException e) {
                    // Se superó el tiempo de espera, continuar con el siguiente intento
                    break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            attempts++;
        }
        if (!isPacketReceived) {
            // No se recibió la confirmación después de los intentos máximos permitidos
            //throw new RuntimeException("No se recibió la confirmación del paquete después de varios intentos.");
        }
    }

    private void sendData(byte[] data, int port) {
        int totalBytes = data.length;
        int sentBytes = 0;
        int packetSize = 1460;

        // Convertir el tamaño total en un arreglo de bytes
        byte[] totalBytesData = ByteBuffer.allocate(4).putInt(totalBytes).array();

        // Enviar el tamaño total primero
        DatagramPacket sizePacket = new DatagramPacket(totalBytesData, totalBytesData.length, serverAddress, port);
        isPacketReceived(sizePacket, port);

        while (sentBytes < totalBytes) {
            int remainingBytes = totalBytes - sentBytes;
            int packetBytes = Math.min(packetSize, remainingBytes);

            DatagramPacket packet = new DatagramPacket(data, sentBytes, packetBytes, serverAddress, port);

            isPacketReceived(packet, port);

            sentBytes += packetBytes;
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);

        return image;
    }
}