package UDP;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class client {

    //Tamaño del buffer
    private static final int bufferSize = 1024;

    //Tamaño máximo de un fragmento UDP
    private static final int maxPacketSize = 65507;

    //Cantidad de clientes a enviar
    private static int numClientsConnected;

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Ingrese el nombre del archivo: ");
            String fileName = scanner.nextLine();

            System.out.print("Ingrese la cantidad de clientes a enviar: ");
            int numClients = scanner.nextInt();
            numClientsConnected = numClients;

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("El archivo no existe");
                return;
            }
            //Obtener el número de cliente
            
            int clientNumber = numClientsConnected + 1 - numClients;
            //Construir el nombre del archivo con el formato solicitado
            String newFileName = "UDP/archivosRecibidos/Cliente"+ clientNumber + "-Prueba-" + numClients + ".txt";
            File newFile = new File(newFileName);

            try (DatagramSocket socket = new DatagramSocket()) {

                //Envío del nombre del archivo al servidor
                byte[] buffer = fileName.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 5000);
                socket.send(packet);

                //Envío de la cantidad de clientes al servidor
                buffer = String.valueOf(numClients).getBytes();
                packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 5000);
                socket.send(packet);

                //Recepción del tamaño del archivo
                byte[] sizeBuffer = new byte[bufferSize];
                packet = new DatagramPacket(sizeBuffer, sizeBuffer.length);
                socket.receive(packet);

                long fileSize = Long.parseLong(new String(sizeBuffer, 0, packet.getLength()));
                System.out.println("Tamaño del archivo: " + fileSize + " bytes");

                //Escritura del archivo
                FileOutputStream fos = new FileOutputStream(newFile);
                byte[] fileBuffer = new byte[maxPacketSize];
                

                long bytesReceived = 0;
                long startTime = System.currentTimeMillis();
                while (bytesReceived < fileSize) {

                    packet = new DatagramPacket(fileBuffer, fileBuffer.length);
                    socket.receive(packet);

                    fos.write(packet.getData(), 0, packet.getLength());
                    bytesReceived += packet.getLength();
                }

                fos.close();
                long transferTime = System.currentTimeMillis() - startTime;
                System.out.println("Archivo recibido correctamente");

                //Obtener la fecha y hora actual
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date date = new Date();
                String dateTimeString = formatter.format(date);

                //Construir el nombre del archivo log con el formato solicitado
                String logFileName = dateTimeString + "-log.txt";
                File logFile = new File("UDP/Logs/" + logFileName);

                //Crear el archivo log
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }

                //Escribir el registro en el archivo log
                String logEntry = "Archivo recibido: " + fileName + " (" + fileSize + " bytes)\n";
                if (bytesReceived == fileSize) {
                    logEntry += "Entrega exitosa\n";
                } else {
                    logEntry += "Entrega fallida\n";
                }
                logEntry += "Tiempo de transferencia: " + transferTime + " ms\n";
                Files.write(logFile.toPath(), logEntry.getBytes(), StandardOpenOption.APPEND);


            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}


