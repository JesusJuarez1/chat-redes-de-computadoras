package chat;

import servidor.tcp.ServidorTCP;

public class Servidor {
    public static void main(String[] args) throws Exception {
        ServidorTCP servidor = new ServidorTCP(30000);
        servidor.inicia();
    }
}
