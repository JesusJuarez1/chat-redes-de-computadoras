package cliente.tcp;
import java.net.*;
// importar la libreria java.net
import java.io.*;
// importar la libreria java.io
 
// declararamos la clase clientetcp
public class ClienteEnviaTCP extends Thread{
    protected BufferedReader in;
    // declaramos un objeto socket para realizar la comunicación
    protected Socket socket;
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected DataOutputStream out;
    
    public ClienteEnviaTCP(String servidor, int puertoS)throws Exception{
        PUERTO_SERVER=puertoS;
        SERVER=servidor;
        
        // Creamos una instancia BuffererReader en la
        // que guardamos los datos introducido por el usuario
        in = new BufferedReader(new InputStreamReader(System.in));
        
        // Instanciamos un socket con la dirección del destino y el
        // puerto que vamos a utilizar para la comunicación
        socket = new Socket(SERVER,PUERTO_SERVER);
        
        // Declaramos e instanciamos el objeto DataOutputStream
        // que nos valdrá para enviar datos al servidor destino
        out =new DataOutputStream(socket.getOutputStream());
    }
    
    public void run () {
        // declaramos una variable de tipo string
        String mensaje="";

        // Declaramos un bloque try y catch para controlar la ejecución del subprograma
        try {
            // Creamos un bucle do while en el que enviamos al servidor el mensaje
            // los datos que hemos obtenido despues de ejecutar la función
            // "readLine" en la instancia "in"
            do {
                mensaje = in.readLine();
                // enviamos el mensaje codificado en UTF
                out.writeUTF(mensaje);
                // mientras el mensaje no encuentre la cadena fin, seguiremos ejecutando
                // el bucle do-while
            } while (!mensaje.startsWith("fin"));
        }
        // utilizamos el catch para capturar los errores que puedan surgir
        catch (Exception e) {
            // si existen errores los mostrará en la consola y después saldrá del
            // programa
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
