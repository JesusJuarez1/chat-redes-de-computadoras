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
            out.writeUTF(archivo.getName());

            byte[] buffer = new byte[1024];
            int bytesRead;
            long bytesEnviados = 0;
            long bytesEnviadosPorSegundo = 0;
            long startTime = System.nanoTime();
            long tiempoActualizacion = startTime;
            double tiempoTotal = 0.0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                bytesEnviados += bytesRead;
                bytesEnviadosPorSegundo += bytesRead;

                long tiempoActual = System.nanoTime();
                double tiempoTranscurrido = (tiempoActual - startTime)/1000000000.0;
                double intervaloTiempo = (tiempoActual-tiempoActualizacion)/1000000000.0;

                if (intervaloTiempo >= 1.0) {
                    double tasaTransferencia = bytesEnviadosPorSegundo*8;
                    double tiempoEstimado = ((double)(archivo.length()-bytesEnviados))/bytesEnviadosPorSegundo;

                    System.out.println("Tasa de transferencia: " + tasaTransferencia + " b/s");
                    System.out.println("Tasa de transferencia: " + bytesEnviadosPorSegundo/1024 + " KB/s");
                    System.out.println("Tiempo transcurrido: " + tiempoTranscurrido + " segundos");
                    System.out.println("Tiempo restante estimado: " + tiempoEstimado + " segundos");
                    System.out.println("----------------------------------------------");
                    bytesEnviadosPorSegundo = 0;
                    tiempoActualizacion = tiempoActual;
                }
            }

            fis.close();
            System.out.println("Archivo enviado correctamente.");
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
