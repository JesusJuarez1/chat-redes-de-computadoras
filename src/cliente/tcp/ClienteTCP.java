package cliente.tcp;

import java.io.File;

public  class ClienteTCP{
    protected final String SERVER;
    protected final int PUERTO_SERVER;
    protected boolean activo;
    protected final File archivo;
    
    public ClienteTCP(String servidor,int puertoS, File archivo){
        SERVER=servidor;
        PUERTO_SERVER=puertoS;
        activo = false;
        this.archivo = archivo;
    }
    public void inicia()throws Exception{
        ClienteEnviaTCP clienteTCP= new ClienteEnviaTCP(SERVER,PUERTO_SERVER, archivo);
        
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
