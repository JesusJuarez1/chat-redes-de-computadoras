package cliente.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClienteEscuchaLlamadaUDP extends Thread{
    protected final int MAX_BUFFER = 256;
    protected DatagramSocket socket;

    public ClienteEscuchaLlamadaUDP(DatagramSocket nuevoSocket) {
        socket = nuevoSocket;
    }

    public void run() {
        try {
            byte[] buffer = new byte[MAX_BUFFER];
            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(paquete);
                String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                System.out.println("Mensaje recibido: " + mensaje);
            }
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
            System.exit(1);
        }
    }
}
