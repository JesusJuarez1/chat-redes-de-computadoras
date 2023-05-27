package cliente.tcp;

public  class ClienteTCP{
    protected final String SERVER;
    protected final int PUERTO_SERVER;
    protected boolean activo;
    
    public ClienteTCP(String servidor,int puertoS){
        SERVER=servidor;
        PUERTO_SERVER=puertoS;
        activo = false;
    }
    public void inicia()throws Exception{
        ClienteEnviaTCP clienteTCP= new ClienteEnviaTCP(SERVER,PUERTO_SERVER);
        
        clienteTCP.start();

        activo = true;

        // Esperar a que los hilos finalicen
        clienteTCP.join();

        // Indicar que los hilos han finalizado
        activo = false;
    }

    public boolean isActivo() {
        return activo;
    }
}
