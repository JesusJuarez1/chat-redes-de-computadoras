package servidor.tcp;

import java.net.*;
//importar la libreria java.net
 
import java.io.*;
//importar la libreria java.io
// declaramos la clase servidortcp
 
public class ServidorEscuchaTCP extends Thread {
    // declaramos un objeto ServerSocket para realizar la comunicación
    protected ServerSocket socket;
    protected DataInputStream in;
    protected Socket socket_cli;
    protected final int PUERTO_SERVER;
    
    public ServidorEscuchaTCP(int puertoS)throws Exception{
        PUERTO_SERVER=puertoS;
        // Instanciamos un ServerSocket con la dirección del destino y el
        // puerto que vamos a utilizar para la comunicación

        socket = new ServerSocket(PUERTO_SERVER);
    }
    // método principal main de la clase
    public void run() {
        // Declaramos un bloque try y catch para controlar la ejecución del subprograma
        try {
            // Creamos un socket_cli al que le pasamos el contenido del objeto socket después
            // de ejecutar la función accept que nos permitirá aceptar conexiones de clientes
            socket_cli = socket.accept();

            // Declaramos e instanciamos el objeto DataInputStream
            // que nos valdrá para recibir datos del cliente

            in =new DataInputStream(socket_cli.getInputStream());

            // Creamos un bucle do while en el que recogemos el mensaje
            // que nos ha enviado el cliente y después lo mostramos
            // por consola
            do {
                String mensaje ="";
                mensaje = in.readUTF();
                System.out.println(mensaje);
            } while (true);
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
