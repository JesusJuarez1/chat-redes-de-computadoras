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
import java.net.*;
import java.nio.ByteBuffer;

public class ClienteEnviaVideoLlamadaUDP extends Thread {
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    private static final int FPS = 30;
    private static final int AUDIO_BUFFER_SIZE = 4096;
    private static final int PACKET_SIZE = 65507;
    private static final int VIDEO_PORT = 50000;
    private static final int AUDIO_PORT = 50001;
    private volatile boolean isRunning;

    private DatagramSocket videoSocket;
    private DatagramSocket audioSocket;
    private InetAddress serverAddress;

    private VideoCapture videoCapture;
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;

    private JFrame frame;
    private JLabel videoLabel;
    private JButton stopButton;

    public ClienteEnviaVideoLlamadaUDP(String server, DatagramSocket videoSocket, DatagramSocket audioSocket) {
        this.videoSocket = videoSocket;
        this.audioSocket = audioSocket;
        try {
            serverAddress = InetAddress.getByName(server);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        videoCapture = new VideoCapture(0);
        audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
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
            byte[] audioBuffer = new byte[AUDIO_BUFFER_SIZE];

            // Start displaying the video
            displayVideo();
            isRunning = true;
            while (isRunning) {
                // Capture video frame
                Mat frame = new Mat();
                videoCapture.read(frame);
                BufferedImage image = matToBufferedImage(frame);

                // Display video frame
                ImageIcon icon = new ImageIcon(image);
                videoLabel.setIcon(icon);
                videoLabel.repaint();

                // Convert video frame to compressed bytes
                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg", frame, matOfByte);
                byte[] compressedVideo = matOfByte.toArray();

                // Send video frame
                sendData(compressedVideo, VIDEO_PORT);

                // Capture audio frame
                targetDataLine.open(audioFormat);
                targetDataLine.start();

                // Send audio frame
                sendData(audioBuffer, AUDIO_PORT);
            }
            // Cleanup resources
            videoCapture.release();
            targetDataLine.stop();
            targetDataLine.close();
            videoSocket.close();
            audioSocket.close();
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            System.exit(1);
        }
    }

    private void stopSending() {
        isRunning = false;
        frame.dispose();
    }

    private void sendData(byte[] data, int port) throws Exception {
        int totalBytes = data.length;
        int sentBytes = 0;
        int packetSize = 1400;

        // Convertir el tamaño total en un arreglo de bytes
        byte[] totalBytesData = ByteBuffer.allocate(4).putInt(totalBytes).array();

        // Enviar el tamaño total primero
        DatagramPacket sizePacket = new DatagramPacket(totalBytesData, totalBytesData.length, serverAddress, port);
        videoSocket.send(sizePacket);

        while (sentBytes < totalBytes) {
            int remainingBytes = totalBytes - sentBytes;
            int packetBytes = Math.min(packetSize, remainingBytes);

            DatagramPacket packet;
            packet = new DatagramPacket(data, sentBytes, packetBytes, serverAddress, port);

            videoSocket.send(packet);
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

    public boolean isRunning(){
        return isRunning;
    }
}
