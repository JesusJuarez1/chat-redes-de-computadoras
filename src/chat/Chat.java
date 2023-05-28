package chat;

import cliente.tcp.ClienteTCP;
import cliente.udp.ClienteUDP;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Chat {
    public Chat(){
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            // Mostrar el menú de opciones
            System.out.println("------ Menú ------");
            System.out.println("1. Mensajes de texto");
            System.out.println("2. Enviar archivo");
            System.out.println("3. Realizar videollamada");
            System.out.println("4. Salir");
            System.out.println("------------------");
            System.out.print("Seleccione una opción: ");

            // Leer la opción seleccionada por el usuario
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de línea después de leer el número

            switch (opcion) {
                case 1:
                    mensajeriaTexto();
                    break;
                case 2:
                    enviarArchivos();
                    break;
                case 3:
                    // Opción de realizar videollamada
                    System.out.println("Iniciando videollamada...");
                    // Lógica para iniciar la videollamada
                    break;
                case 4:
                    // Opción de salir
                    exit = true;
                    System.out.println("Saliendo del programa...");
                    break;
                default:
                    System.out.println("Opción inválida. Intente nuevamente.");
                    break;
            }
            System.out.println();
        }
        scanner.close();
    }

    private void enviarArchivos() {
        String servidor = obtenerDireccionIPDestinatario();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingresa la ruta del archivo: ");
        String rutaArchivo = scanner.nextLine();

        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            System.err.println("El archivo especificado no existe.");
            return;
        }

        ClienteTCP cliente = new ClienteTCP(servidor, 20, archivo);
        try {
            cliente.inicia();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Esperar a que los hilos del cliente finalicen
        while (cliente.isActivo()) {
            try {
                Thread.sleep(100); // Esperar 100 milisegundos antes de verificar nuevamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Llama las clases necesarias para enviar y recibir mensajes
     */
    private void mensajeriaTexto() {
        String servidor = obtenerDireccionIPDestinatario();
        ClienteUDP cliente = new ClienteUDP(servidor, 50000);
        try {
            cliente.inicia();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Esperar a que los hilos del cliente finalicen
        while (cliente.isActivo()) {
            try {
                Thread.sleep(100); // Esperar 100 milisegundos antes de verificar nuevamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return La dirección ip validada
     */
    public static String obtenerDireccionIPDestinatario() {
        Scanner scanner = new Scanner(System.in);
        String ipAddress = null;

        System.out.print("Ingrese la dirección IP del cliente destinatario: ");
        ipAddress = scanner.nextLine();

        // Validar la dirección IP ingresada
        while (!validarDireccionIP(ipAddress)) {
            System.out.println("La dirección IP ingresada no es válida. Inténtelo nuevamente.");
            System.out.print("Ingrese la dirección IP del cliente destinatario: ");
            ipAddress = scanner.nextLine();
        }

        return ipAddress;
    }

    /**
     * @param ipAddress Direccion ip a validar
     * @return True si es correcta, false en caso contrario
     */
    public static boolean validarDireccionIP(String ipAddress) {
        try {
            InetAddress.getByName(ipAddress);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
