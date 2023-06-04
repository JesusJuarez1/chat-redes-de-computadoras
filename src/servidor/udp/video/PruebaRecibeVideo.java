package servidor.udp.video;

import org.opencv.core.Core;

public class PruebaRecibeVideo {
    public static void main(String[] args) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ServidorRecibeVideoLlamadaUDP servidor = new ServidorRecibeVideoLlamadaUDP();
        servidor.start();
        servidor.join();
    }
}
