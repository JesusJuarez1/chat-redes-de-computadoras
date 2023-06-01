package video;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javax.sound.sampled.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Video extends Application {

    private ImageView imageView;
    private VideoCapture videoCapture;
    private TargetDataLine audioLine;
    private boolean capturing;

    @Override
    public void start(Stage primaryStage) {
        // Cargar la biblioteca OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Crear el componente ImageView para mostrar la vista previa del video
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

        // Iniciar la captura de video y audio
        videoCapture = new VideoCapture(0);
        audioLine = getAudioLine();
        capturing = true;
        new Thread(this::captureVideo).start();
        new Thread(this::captureAudio).start();
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

                    // Realizar cualquier procesamiento adicional con los datos de imagen
                    // ...
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

        // Realizar cualquier procesamiento adicional con los datos de audio
        // ...
    }

    @Override
    public void stop() throws Exception {
        capturing = false;
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
