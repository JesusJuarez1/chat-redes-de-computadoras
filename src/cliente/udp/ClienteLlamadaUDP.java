package cliente.udp;

import java.net.*;
import java.io.*;

//declaramos la clase udp
public class ClienteLlamadaUDP{
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected boolean activo;

    public ClienteLlamadaUDP(String servidor, int puertoS){
        PUERTO_SERVER=puertoS;
        SERVER=servidor;
        activo = false;
    }

    public void inicia()throws Exception{
        DatagramSocket socket=new DatagramSocket();

        ClienteEscuchaLlamadaUDP clienteEnvLlUDP=new ClienteEscuchaLlamadaUDP(socket);
        ClienteEnviaLlamadaUDP clienteEscLlUDP=new ClienteEnviaLlamadaUDP(socket, SERVER, PUERTO_SERVER);

        clienteEnvLlUDP.start();
        clienteEscLlUDP.start();

        // Indicar que los hilos están activos
        activo = true;

        // Esperar a que los hilos finalicen
        clienteEnvLlUDP.join();
        clienteEscLlUDP.join();

        // Indicar que los hilos han finalizado
        activo = false;
    }

    public boolean isActivo() {
        return activo;
    }
}
