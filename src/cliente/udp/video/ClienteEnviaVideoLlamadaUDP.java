package cliente.udp.video;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.sound.sampled.*;

public class ClienteEnviaVideoLlamadaUDP extends Application {

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

        // Crear el botón y ajustar su tamaño
        Button stopButton = new Button("Terminar Videollamada");
        stopButton.setPrefSize(200, 40);

        // Ubicar el botón en la esquina inferior derecha
        StackPane.setAlignment(stopButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(stopButton, new Insets(10));
        stopButton.setOnAction(e -> stopVideoCall());
        root.getChildren().add(stopButton);

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
        new Thread(this::receiveAndDisplayVideoAudio).start();
    }

    private void stopVideoCall() {
        capturing = false; // Detener la captura de video y audio

        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }

        if (socket != null) {
            socket.close();
        }

        // Cerrar la aplicación
        Platform.exit();
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
                    new Thread(() -> {
                        sendCompressedData(compressedImageData);
                    }).start();

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
        int maxFragmentSize = 1400; // Tamaño máximo para cada fragmento
        int offset = 0;
        int retryCount = 0;
        boolean sentSuccessfully = false;

        while (!sentSuccessfully && offset < compressedData.length) {
            int fragmentSize = Math.min(maxFragmentSize, compressedData.length - offset);
            byte[] fragment = new byte[fragmentSize];
            System.arraycopy(compressedData, offset, fragment, 0, fragmentSize);
            Thread sendThread = null;

            try {
                DatagramPacket packet = new DatagramPacket(fragment, fragment.length, serverAddress, serverPort);
                sendThread = new Thread(() -> {
                    try {
                        socket.send(packet);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                sendThread.start();

                // Esperar 2 segundos
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // El hilo fue interrumpido, se detiene el envío
                    sendThread.interrupt();
                    break;
                }

                // Verificar si el hilo de envío ha terminado
                if (sendThread.isAlive()) {
                    // El tiempo de espera de 2 segundos ha transcurrido, se interrumpe el hilo de envío
                    sendThread.interrupt();
                    retryCount++;
                    System.out.println("Reintentando envío de datos (" + retryCount + ")");
                } else {
                    // El envío se completó exitosamente
                    sentSuccessfully = true;
                }
            } catch (Exception e){

            }

            offset += fragmentSize;
        }

        if (!sentSuccessfully) {
            System.out.println("No se pudo enviar los datos después de " + retryCount + " intentos.");
        }
    }


    private void receiveAndDisplayVideoAudio() {
        try {
            byte[] buffer = new byte[65536];

            while (capturing) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] receivedData = packet.getData();
                int receivedDataLength = packet.getLength();

                // Descomprimir los datos de video y audio recibidos
                byte[] decompressedVideoData = decompressData(receivedData, receivedDataLength);
                byte[] decompressedAudioData = decompressData(receivedData, receivedDataLength);

                // Mostrar el video y reproducir el audio
                displayVideo(decompressedVideoData);
                playAudio(decompressedAudioData);
            }
        } catch (SocketException e){

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] decompressData(byte[] data, int length) {
        // Realizar la descompresión de datos aquí
        // Reemplaza este código con el algoritmo de descompresión de tu elección
        // Retorna los datos descomprimidos en un arreglo de bytes
        return data;
    }

    private void displayVideo(byte[] videoData) {
        // Convertir los datos de video a una matriz OpenCV
        Mat frame = byteArrayToMat(videoData);

        // Realizar cualquier procesamiento de imagen necesario aquí
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

        // Convertir la imagen de Mat a Image
        Image image = matToImage(frame);

        // Mostrar la imagen en el componente ImageView
        imageView.setImage(image);
    }

    private Mat byteArrayToMat(byte[] byteArray) {
        MatOfByte matOfByte = new MatOfByte(byteArray);
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);
    }

    private void playAudio(byte[] audioData) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(audioData, 0, audioData.length);
            line.drain();
            line.stop();
            line.close();
        } catch (LineUnavailableException e) {
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
