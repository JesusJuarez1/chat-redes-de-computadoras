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
    protected final File archivo;
    
    public ClienteEnviaTCP(String servidor, int puertoS, File archivo)throws Exception{
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
        this.archivo=archivo;
    }
    
    public void run () {
        // Código para enviar archivo
        try {
            FileInputStream fis = new FileInputStream(archivo);

            // Enviar el nombre del archivo al servidor
            out.writeUTF(archivo.getName());

            byte[] buffer = new byte[1024];
            int bytesRead;
            long startTime = System.currentTimeMillis(); // Tiempo de inicio de la transmisión

            long totalBytesSent = 0;
            long fileSize = archivo.length(); // Tamaño del archivo en bytes

            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;

                // Calcular tiempo transcurrido
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;

                // Calcular tasa de transferencia
                double transferRate = (totalBytesSent * 8) / (elapsedTime / 1000.0); // Tasa de transferencia en bps

                // Calcular tiempo restante
                double remainingTime = (fileSize - totalBytesSent) / transferRate;

                System.out.println("Bytes enviados: " + totalBytesSent);
                System.out.println("Tasa de transferencia: " + transferRate + " bps");
                System.out.println("Tiempo transcurrido: " + elapsedTime + " ms");
                System.out.println("Tiempo restante: " + remainingTime + " s");
            }

            long endTime = System.currentTimeMillis(); // Tiempo de finalización de la transmisión
            long totalTime = endTime - startTime; // Tiempo total de transmisión en milisegundos

            double transferRate = (fileSize * 8) / (totalTime / 1000.0); // Tasa de transferencia en bps

            System.out.println("Transmisión completada.");
            System.out.println("Tasa de transferencia: " + transferRate + " bps");
            System.out.println("Tiempo total de transmisión: " + totalTime + " ms");
            fis.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
