package servidor.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ServidorEscuchaUDP extends Thread {
    protected DatagramSocket socket;
    protected final int PUERTO_SERVER;
    protected final int MAX_BUFFER = 256;
    protected Map<String, Integer> clientes;

    public ServidorEscuchaUDP(int puertoS) throws Exception {
        PUERTO_SERVER = puertoS;
        socket = new DatagramSocket(puertoS);
        clientes = new HashMap<>();
    }

    public void run() {
        try {
            byte[] mensaje_bytes;
            DatagramPacket paquete;

            // Iniciamos el bucle
            while (true) {
                // Recibimos el paquete
                mensaje_bytes = new byte[MAX_BUFFER];
                paquete = new DatagramPacket(mensaje_bytes, MAX_BUFFER);
                socket.receive(paquete);

                // Obtenemos la dirección IP y el puerto del remitente
                String direccionIPRemitente = paquete.getAddress().getHostAddress();
                int puertoRemitente = paquete.getPort();

                // Lo formateamos
                mensaje_bytes=new byte[paquete.getLength()];
                mensaje_bytes=paquete.getData();
                String mensaje = new String(mensaje_bytes,0,paquete.getLength()).trim();

                // Lo mostramos por pantalla
                System.out.println("Mensaje recibido \""+mensaje+"\" del cliente "+
                        paquete.getAddress()+"#"+paquete.getPort());

                // Verificamos si el remitente está registrado como cliente
                if (clientes.containsKey(direccionIPRemitente) && clientes.get(direccionIPRemitente) == puertoRemitente) {
                    // El remitente está registrado, enviamos el mensaje al otro cliente
                    enviarMensajeACliente(opuestoCliente(direccionIPRemitente), mensaje_bytes);
                } else {
                    // El remitente no está registrado, lo registramos como nuevo cliente
                    registrarCliente(direccionIPRemitente, puertoRemitente);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void registrarCliente(String direccionIP, int puerto) {
        clientes.put(direccionIP, puerto);
        System.out.println("Cliente registrado: " + direccionIP + ":" + puerto);
    }

    private void enviarMensajeACliente(String direccionIP, int puerto, byte[] mensaje) throws Exception {
        InetAddress addressCliente = InetAddress.getByName(direccionIP);
        DatagramPacket paquete = new DatagramPacket(mensaje, mensaje.length, addressCliente, puerto);
        socket.send(paquete);
    }

    private void enviarMensajeACliente(String direccionIP, byte[] mensaje) throws Exception {
        int puerto = clientes.get(direccionIP);
        enviarMensajeACliente(direccionIP, puerto, mensaje);
    }

    private String opuestoCliente(String direccionIP) {
        for (String cliente : clientes.keySet()) {
            if (!cliente.equals(direccionIP)) {
                return cliente;
            }
        }
        return null;
    }
}