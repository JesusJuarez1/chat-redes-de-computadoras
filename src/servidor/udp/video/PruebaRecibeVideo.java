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
        ClienteEnviaVideoLlamadaUDP cliente = null;
        while(servidor.isRunning()){
            if (servidor.getAddress() != "") {
                try {
                    cliente = new ClienteEnviaVideoLlamadaUDP(servidor.getAddress(), new DatagramSocket());
                    break;
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        cliente.start();

        servidor.join();
        cliente.interrupt();

    }
}
