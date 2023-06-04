package cliente.udp;

import cliente.udp.video.ClienteEnviaVideoLlamadaUDP;
import org.opencv.core.Core;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;

//declaramos la clase udp
public class ClienteLlamadaUDP extends Thread{
    protected String SERVER;
    protected boolean activo;

    public ClienteLlamadaUDP(String SERVER) {
        this.SERVER = SERVER;
        activo = false;
    }

    public void inicia() throws Exception{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DatagramSocket videoSocket = null;
        DatagramSocket audioSocket = null;
        try {
            videoSocket = new DatagramSocket();
            audioSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        ClienteEnviaVideoLlamadaUDP videoLlamada = null;
        videoLlamada = new ClienteEnviaVideoLlamadaUDP(SERVER, videoSocket, audioSocket);
        videoLlamada.start();

        activo = true;

        videoLlamada.join();
    }

    public boolean isActivo() {
        return activo;
    }
}
