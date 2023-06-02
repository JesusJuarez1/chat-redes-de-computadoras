package cliente.udp;

import cliente.udp.video.ClienteEnviaVideoLlamadaUDP;
import javafx.application.Application;
import javafx.stage.Stage;

import java.net.*;

//declaramos la clase udp
public class ClienteLlamadaUDP extends Application {
    protected int PUERTO_SERVER;
    protected String SERVER;

    public void setPUERTO_SERVER(int puerto) {
        PUERTO_SERVER = puerto;
    }

    public void setSERVER(String server) {
        SERVER = server;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        ClienteEnviaVideoLlamadaUDP videoLlamada = new ClienteEnviaVideoLlamadaUDP();
        videoLlamada.setServer(SERVER);
        videoLlamada.setPuertoServer(PUERTO_SERVER);
        videoLlamada.start(primaryStage);
    }
}
