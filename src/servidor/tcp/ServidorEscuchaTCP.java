package servidor.tcp;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorEscuchaTCP extends Thread {
    // declaramos un objeto ServerSocket para realizar la comunicación
    protected ServerSocket serverSocket;
    protected final int PUERTO_SERVER;
    
    public ServidorEscuchaTCP(int puertoS)throws Exception{
        PUERTO_SERVER=puertoS;
        // Instanciamos un ServerSocket con la dirección del destino y el
        // puerto que vamos a utilizar para la comunicación

        serverSocket = new ServerSocket(PUERTO_SERVER);
    }
    // método principal main de la clase
    public void run() {
        // Declaramos un bloque try y catch para controlar la ejecución del subprograma
        try {
            System.out.println("Servidor escuchando en el puerto " + PUERTO_SERVER + "...");
            Socket clienteSocket = serverSocket.accept();
            System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

            // Creamos el flujo de entrada para recibir los datos del cliente
            DataInputStream dataInputStream = new DataInputStream(clienteSocket.getInputStream());

            // Leemos el nombre del archivo enviado por el cliente
            String nombreArchivo = dataInputStream.readUTF();
            System.out.println("Nombre del archivo recibido: " + nombreArchivo);

            // Creamos el flujo de salida para escribir los datos en el archivo
            String rutaArchivo = "../archivos/" + nombreArchivo;
            FileOutputStream fileOutputStream = new FileOutputStream(rutaArchivo);

            // Creamos un buffer para leer los datos del flujo de entrada
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Leemos los datos del flujo de entrada y los escribimos en el archivo
            while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("Archivo recibido y guardado correctamente.");

            // Cerramos los flujos y el socket
            fileOutputStream.close();
            dataInputStream.close();
            clienteSocket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
