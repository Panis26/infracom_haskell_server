package UDP;
//Se crea una clase que permita generar un archivo bin de tamaño 100MB y 250MB

import java.io.*;
import java.util.*;

class file{
    public static void main(String[] args) throws IOException{
        //Se crea un archivo de 100MB
        File file = new File("100MB.bin");
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        Random random = new Random();
        for (int i = 0; i < 1024 * 100; i++) {
            random.nextBytes(buffer);
            fos.write(buffer);
        }
        fos.close();
        //Se crea un archivo de 250MB
        File file2 = new File("250MB.bin");
        FileOutputStream fos2 = new FileOutputStream(file2);
        byte[] buffer2 = new byte[1024];
        Random random2 = new Random();
        for (int i = 0; i < 1024 * 250; i++) {
            random2.nextBytes(buffer2);
            fos2.write(buffer2);
        }
        fos2.close();
    }
}
