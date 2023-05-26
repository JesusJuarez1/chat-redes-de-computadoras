package cliente.udp;

import java.net.*;
import java.io.*;
 
//declaramos la clase udp escucha
public class ClienteEscuchaUDP extends Thread{
    protected BufferedReader in;
    //Definimos el sockets, número de bytes del buffer, y mensaje.
    protected final int MAX_BUFFER=256;
    protected final int PUERTO_CLIENTE;
    protected DatagramSocket socket;
    protected InetAddress address;
    protected DatagramPacket servPaquete;
    //protected String SERVER;
    
    public  ClienteEscuchaUDP(DatagramSocket socketNuevo){
        socket=socketNuevo;
        //SERVER=servidor;
        PUERTO_CLIENTE=socket.getLocalPort();
    }
    public void run() {
        byte[] mensaje_bytes;
        String mensaje="";
        mensaje_bytes=mensaje.getBytes();
        
        String cadenaMensaje="";

        byte[] recogerServidor_bytes;

        try {
            do {
                recogerServidor_bytes = new byte[MAX_BUFFER];

                //Esperamos a recibir un paquete
                servPaquete = new DatagramPacket(recogerServidor_bytes,MAX_BUFFER);
                socket.receive(servPaquete);

                //Convertimos el mensaje recibido en un string
                cadenaMensaje = new String(recogerServidor_bytes).trim();

                //Imprimimos el paquete recibido
                System.out.println("Mensaje recibido \""+cadenaMensaje +"\" de "+
                        servPaquete.getAddress()+"#"+servPaquete.getPort());
            } while (!cadenaMensaje.startsWith("fin"));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("Excepcion C: "+e.getMessage());
            System.exit(1);
        }
    }
}
