package cliente.udp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClienteEnviaLlamadaUDP extends Thread{
    protected BufferedReader in;
    //Definimos el sockets, número de bytes del buffer, y mensaje.
    protected final int MAX_BUFFER=256;
    protected final int PUERTO_SERVER;
    protected DatagramSocket socket;
    protected InetAddress address;
    protected DatagramPacket paquete;

    public ClienteEnviaLlamadaUDP(DatagramSocket nuevoSocket, String servidor, int puertoServidor) {
        socket = nuevoSocket;
        PUERTO_SERVER = puertoServidor;
        try {
            address = InetAddress.getByName(servidor);
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
            System.exit(1);
        }
    }

    public void run() {
        try {

            do {

            } while (true);
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
            System.exit(1);
        }
    }
}
