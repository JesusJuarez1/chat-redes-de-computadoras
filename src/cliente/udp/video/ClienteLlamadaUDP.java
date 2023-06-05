package cliente.udp.video;

import cliente.udp.video.ClienteEnviaVideoLlamadaUDP;
import org.opencv.core.Core;
import servidor.udp.video.ServidorRecibeVideoLlamadaUDP;

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
        try {
            videoSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        ClienteEnviaVideoLlamadaUDP videoLlamada = null;
        videoLlamada = new ClienteEnviaVideoLlamadaUDP(SERVER, videoSocket);
        videoLlamada.start();

        ServidorRecibeVideoLlamadaUDP servidor = new ServidorRecibeVideoLlamadaUDP();
        servidor.start();

        activo = true;

        videoLlamada.join();
        servidor.interrupt();
        activo = false;
    }

    public boolean isActivo() {
        return activo;
    }
}
