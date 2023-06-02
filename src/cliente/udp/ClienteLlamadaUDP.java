package cliente.udp;

import cliente.udp.video.Video;

import java.net.*;
import java.io.*;

//declaramos la clase udp
public class ClienteLlamadaUDP{
    protected final int PUERTO_SERVER;
    protected final String SERVER;

    public ClienteLlamadaUDP(String servidor, int puertoS){
        PUERTO_SERVER=puertoS;
        SERVER=servidor;
    }

    public void inicia()throws Exception{
        DatagramSocket socket=new DatagramSocket();
        Video videoLlamada = new Video();
        videoLlamada.setServer(SERVER);
        videoLlamada.setPuertoServer(PUERTO_SERVER);
        videoLlamada.launch();
    }
}
