package cliente.tcp;

public  class ClienteTCP{
    protected final String SERVER;
    protected final int PUERTO_SERVER;
    
    public ClienteTCP(String servidor,int puertoS){
        SERVER=servidor;
        PUERTO_SERVER=puertoS;
    }
    public void inicia()throws Exception{
        ClienteEnviaTCP clienteTCP= new ClienteEnviaTCP(SERVER,PUERTO_SERVER);
        
        clienteTCP.start();
    }
}
