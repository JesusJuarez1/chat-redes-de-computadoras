package cliente.udp;

import java.net.*;
 
//declaramos la clase udp
public class ClienteUDP{
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected boolean activo;
    
    public ClienteUDP(String servidor, int puertoS){
        PUERTO_SERVER=puertoS;
        SERVER=servidor;
        activo = false;
    }
    
    public void inicia()throws Exception{
        DatagramSocket socket=new DatagramSocket();
        
        ClienteEscuchaUDP clienteEnvUDP=new ClienteEscuchaUDP(socket);
        ClienteEnviaUDP clienteEscUDP=new ClienteEnviaUDP(socket, SERVER, PUERTO_SERVER);
        
        clienteEnvUDP.start();
        clienteEscUDP.start();

        // Indicar que los hilos están activos
        activo = true;

        // Esperar a que los hilos finalicen
        clienteEnvUDP.join();
        clienteEscUDP.join();

        // Indicar que los hilos han finalizado
        activo = false;
    }

    public boolean isActivo() {
        return activo;
    }
}
