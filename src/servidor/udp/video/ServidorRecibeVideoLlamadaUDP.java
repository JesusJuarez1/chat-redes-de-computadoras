package servidor.udp.video;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServidorRecibeVideoLlamadaUDP extends JFrame {

    private JLabel videoLabel;

    public ServidorRecibeVideoLlamadaUDP() {
        // Cargar la biblioteca OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Crear la etiqueta para mostrar el cliente.udp.video
        videoLabel = new JLabel();

        // Configurar la ventana
        setTitle("Video Call Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        add(videoLabel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Iniciar el servidor UDP
        try {
            DatagramSocket serverSocket = new DatagramSocket(5000); // Puerto 5000 para recibir los datos del cliente

            // Crear el búfer para recibir los datos del cliente.udp.video
            byte[] buffer = new byte[65507];

            // Crear una matriz OpenCV para procesar los datos del cliente.udp.video
            Mat frame = new Mat();

            // Obtener la dirección IP del cliente
            InetAddress clientAddress;

            // Obtener el puerto del cliente
            int clientPort;

            // Iniciar el bucle principal del servidor
            while (true) {
                // Recibir los datos del cliente.udp.video
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                // Obtener la dirección IP y el puerto del cliente
                clientAddress = packet.getAddress();
                clientPort = packet.getPort();

                // Convertir los datos recibidos en una matriz OpenCV
                frame.create(480, 640, CvType.CV_8UC3);
                frame.put(0, 0, packet.getData(), 0, frame.cols() * frame.rows() * frame.channels());

                // Mostrar la imagen en la ventana
                showFrame(frame);

                // Enviar una confirmación al cliente
                byte[] confirmationData = "Received".getBytes();
                DatagramPacket confirmationPacket = new DatagramPacket(confirmationData, confirmationData.length, clientAddress, clientPort);
                serverSocket.send(confirmationPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFrame(Mat frame) {
        // Convertir la matriz OpenCV en una imagen BufferedImage
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, buffer);
        byte[] imageBytes = buffer.toArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        BufferedImage image;
        try {
            image = ImageIO.read(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Mostrar la imagen en la etiqueta
        videoLabel.setIcon(new ImageIcon(image));
        videoLabel.repaint();
    }

    public static void main(String[] args) {
        new ServidorRecibeVideoLlamadaUDP();
    }
}

