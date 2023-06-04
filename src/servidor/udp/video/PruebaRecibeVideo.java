package servidor.udp.video;

import cliente.udp.video.ClienteEnviaVideoLlamadaUDP;
import org.opencv.core.Core;

import java.net.DatagramSocket;
import java.net.SocketException;

public class PruebaRecibeVideo {
    public static void main(String[] args) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ServidorRecibeVideoLlamadaUDP servidor = new ServidorRecibeVideoLlamadaUDP();
        servidor.start();
        while(servidor.isRunning()){
            if(servidor.getClienteIP() != ""){
                break;
            }
        }
        DatagramSocket videoSocket = null;
        DatagramSocket audioSocket = null;
        try {
            videoSocket = new DatagramSocket();
            audioSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        ClienteEnviaVideoLlamadaUDP videoLlamada = null;
        videoLlamada = new ClienteEnviaVideoLlamadaUDP(servidor.getClienteIP(), videoSocket, audioSocket);
        videoLlamada.start();

        while(videoLlamada.isRunning() || servidor.isRunning() ){

        }
        if(!videoLlamada.isRunning()){
            servidor.interrupt();
        }else{
            videoLlamada.interrupt();
        }
    }
}
