package cliente.tcp;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        ClienteTCP clienteTCP =new ClienteTCP("192.168.0.121",60000,"gola");
        clienteTCP.inicia();
    }
}
