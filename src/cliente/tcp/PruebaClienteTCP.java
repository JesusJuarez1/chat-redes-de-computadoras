package cliente.tcp;

import java.io.File;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        ClienteTCP clienteTCP =new ClienteTCP("",60000,
                new File(""));
        clienteTCP.inicia();
    }
}
