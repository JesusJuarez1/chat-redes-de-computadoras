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
        // Un bucle para que al terminar de recibir un archivo no termine de escuchar el servidor
        while (true) {
            // Declaramos un bloque try y catch para controlar la ejecución del subprograma
            try {
                System.out.println("\nServidor escuchando en el puerto " + PUERTO_SERVER + "...");
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                DataInputStream dataInputStream = new DataInputStream(clienteSocket.getInputStream());

                // Recibir el nombre del archivo enviado por el cliente
                String nombreArchivo = dataInputStream.readUTF();
                System.out.println("Nombre del archivo a recibir: " + nombreArchivo);

                //String directorioActual = System.getProperty("user.dir");
                //System.out.println("Directorio actual: " + directorioActual);

                // Crear el flujo de salida para escribir los datos en el archivo
                String rutaArchivo = "archivos/" + nombreArchivo;
                FileOutputStream fileOutputStream = new FileOutputStream(rutaArchivo);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long bytesRecibidosPorSegundo = 0;
                long startTime = System.nanoTime();
                long tiempoActualizacion = startTime;

                while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    bytesRecibidosPorSegundo += bytesRead;

                    long tiempoActual = System.nanoTime();
                    double tiempoTranscurrido = (tiempoActual - startTime)/1000000000.0;
                    double intervaloTiempo = (tiempoActual - tiempoActualizacion)/1000000000.0;

                    if (intervaloTiempo >= 1.0) {
                        long tasaTransferenciaBps = bytesRecibidosPorSegundo*8;

                        System.out.println("Tasa de recibimiento: " + tasaTransferenciaBps + " bps");
                        System.out.println("Tasa de recibimiento: " + (bytesRecibidosPorSegundo/1024) + " Kps");
                        System.out.println("Tiempo transcurrido: " + tiempoTranscurrido + " segundos");
                        System.out.println("----------------------------------------------");

                        bytesRecibidosPorSegundo = 0;
                        tiempoActualizacion = tiempoActual;
                    }
                }

                System.out.println("Archivo recibido y guardado correctamente.");

                fileOutputStream.close();
                dataInputStream.close();
                clienteSocket.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}
