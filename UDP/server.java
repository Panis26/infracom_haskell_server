package UDP;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class server {

    //tamaño fragmento UDP
    private static final int maxSizeUdp = 65507;  

    //Puerto de comunicación
    private static final int port = 5000;

    //Numero máximo de conexiones
    private static final int maxConnections = 1;

    //Tamaño del buffer
    private static final int bufferSize = 1024;

    //Carpeta logs
    private static final String logsFolder = "UDP/Logs";
    
    
    private static DatagramSocket socket;
    private static DatagramPacket packet;
    private static byte[] buffer = new byte[bufferSize];
    private static int currentConnections = 0;

    public static void main(String[] args) {
        try{
            System.out.println("Servidor iniciado");
            System.out.println("Esperando peticiones...");
            //Cración del ditrectiorio Logs
            File logs = new File(logsFolder);
            if(!logs.exists()){
                logs.mkdir();
            }

            //Se inicia el socket del servidor
            socket = new DatagramSocket(port);

            while(true){
                //Recepción de paquete
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                //Obtener la direccióin Ip y el puerto del cliente
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                //Limite de conexiones
                if(currentConnections >= maxConnections){
                    
                    //Se envia un mensaje de error
                    String error = "Error: Maximo de conexiones alcanzado";
                    DatagramPacket errorPacket = new DatagramPacket(error.getBytes(), error.getBytes().length, clientAddress, clientPort);
                    socket.send(errorPacket);
                    continue;
                }   

                //Cración de thread para procesar la petición del cliente
                System.out.println(packet.getData());
                ConnectionHandler handler = new ConnectionHandler(clientAddress, clientPort, packet.getData(),packet.getLength());
                Thread thread = new Thread(handler);
                thread.start();
            }

        }catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }finally{
            if(socket != null){
                socket.close();
            }
        }
    }

    //Clase para manejar la conexión con el cliente
    private static class ConnectionHandler implements Runnable {
        private InetAddress clientAddress;
        private int clientPort;
        private byte[] data;
        private int length;

        public ConnectionHandler(InetAddress clientAddress, int clientPort, byte[] data, int length){
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
            this.data = data;
            this.length = length;
        }

        @Override
        public void run(){
            try {
                // Se incrementa el número de conexiones
                currentConnections++;
        
                // Se obtiene el nombre del archivo
                String fileName = new String(data, 0, length);
                System.out.println("Archivo solicitado: " + fileName);
        
                // Verificar si el archivo existe
                File file = new File(fileName);
                if(!file.exists()){
                    // Se envia un mensaje de error
                    String error = "Error: El archivo no existe";
                    DatagramPacket errorPacket = new DatagramPacket(error.getBytes(), error.getBytes().length, clientAddress, clientPort);
                    socket.send(errorPacket);
                    return;
                }
        
                // Obtener el tamaño del archivo
                long fileSize = file.length();
        
                // Se envia el tamaño del archivo
                String size = String.valueOf(fileSize);
                DatagramPacket sizePacket = new DatagramPacket(size.getBytes(), size.getBytes().length, clientAddress, clientPort);
                socket.send(sizePacket);
        
                // Se prepara para la transferencia de datos
                long startTime = System.currentTimeMillis();
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] fileBuffer = new byte[maxSizeUdp];
                    int bytesRead = 0;
                    while((bytesRead = fis.read(fileBuffer)) > 0){
                        DatagramPacket filePacket = new DatagramPacket(fileBuffer, bytesRead, clientAddress, clientPort);
                        socket.send(filePacket);
                    }
                }
                long endTime = System.currentTimeMillis();
                long transferTime = endTime - startTime;
        
                // Crear archivo de log
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                String logFileName = dateFormat.format(new Date()) + "-log.txt";
                File logFile = new File(logsFolder, logFileName);
                if(!logFile.exists()){
                    logFile.createNewFile();
                }
        
                // Escribir en el archivo de log
                BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
                writer.write("Archivo enviado: " + fileName + "\n");
                writer.write("Tamaño del archivo: " + fileSize + " bytes\n");
                writer.write("Tiempo de transferencia: " + transferTime + " ms\n");
                writer.close();
        
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Se decrementa el número de conexiones
                currentConnections--;
            }
        }
    }
}
