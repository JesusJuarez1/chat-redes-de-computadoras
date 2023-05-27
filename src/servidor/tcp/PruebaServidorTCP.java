package servidor.tcp;

public class PruebaServidorTCP{
    public static void main(String args[])throws Exception{
        ServidorTCP servidorTCP=new ServidorTCP(20);
        
        servidorTCP.inicia();
    }
}
