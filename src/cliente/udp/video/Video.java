package cliente.udp.video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Video extends Application {

    private ImageView imageView;
    private VideoCapture videoCapture;
    private TargetDataLine audioLine;
    private boolean capturing;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    @Override
    public void start(Stage primaryStage) {
        // Cargar la biblioteca OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Crear el componente ImageView para mostrar la vista previa del cliente.udp.video
        imageView = new ImageView();

        // Crear el contenedor de la interfaz de usuario
        StackPane root = new StackPane();
        root.getChildren().add(imageView);

        // Crear la escena
        Scene scene = new Scene(root, 640, 480);

        // Configurar y mostrar la ventana
        primaryStage.setTitle("Video and Audio Capture");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Iniciar la captura de cliente.udp.video y audio
        videoCapture = new VideoCapture(0);
        audioLine = getAudioLine();
        capturing = true;

        // Crear el socket UDP
        try {
            socket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(this::captureVideo).start();
        new Thread(this::captureAudio).start();
    }

    public void setServer(String server) throws UnknownHostException {
        this.serverAddress = InetAddress.getByName(server);
    }

    public void setPuertoServer(int puertoServer) {
        this.serverPort = puertoServer;
    }

    private void captureVideo() {
        videoCapture.open(0); // Abrir la cámara con el índice 0 (cámara predeterminada)

        if (videoCapture.isOpened()) {
            while (capturing) {
                Mat frame = new Mat();
                videoCapture.read(frame);

                if (!frame.empty()) {
                    // Realizar cualquier procesamiento de imagen necesario aquí
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

                    // Convertir la imagen de Mat a Image
                    Image image = matToImage(frame);

                    // Mostrar la imagen en el componente ImageView
                    imageView.setImage(image);

                    // Obtener los datos de imagen en un arreglo de bytes
                    byte[] imageData = matToByteArray(frame);

                    // Comprimir los datos de imagen
                    byte[] compressedImageData = compressData(imageData);

                    // Enviar los datos comprimidos por UDP
                    sendCompressedData(compressedImageData);
                }
            }
        }

        videoCapture.release();
    }

    private Image matToImage(Mat frame) {
        Mat convertedFrame = new Mat();
        Imgproc.cvtColor(frame, convertedFrame, Imgproc.COLOR_BGR2RGB);

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", convertedFrame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    private byte[] matToByteArray(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return buffer.toArray();
    }

    private TargetDataLine getAudioLine() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            return line;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void captureAudio() {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            while (capturing) {
                int count = audioLine.read(buffer, 0, buffer.length);
                if (count > 0) {
                    outputStream.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Obtener los datos de audio capturados en un arreglo de bytes
        byte[] audioData = outputStream.toByteArray();

        // Comprimir los datos de audio
        byte[] compressedAudioData = compressData(audioData);

        // Enviar los datos comprimidos por UDP
        sendCompressedData(compressedAudioData);
    }

    private byte[] compressData(byte[] data) {
        // Realizar la compresión de datos aquí
        // Reemplaza este código con el algoritmo de compresión de tu elección
        // Retorna los datos comprimidos en un arreglo de bytes
        return data;
    }

    private void sendCompressedData(byte[] compressedData) {
        try {
            DatagramPacket packet = new DatagramPacket(compressedData, compressedData.length, serverAddress, serverPort);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        capturing = false;
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }
        if (socket != null) {
            socket.close();
        }
        super.stop();
    }
}
