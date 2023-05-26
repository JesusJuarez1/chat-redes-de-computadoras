package cliente.tcp;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        ClienteTCP clienteTCP =new ClienteTCP("192.168.100.3",60000);
             
        clienteTCP.inicia();
    }
}
