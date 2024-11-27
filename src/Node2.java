import java.io.*;
import java.net.*;

public class Node2 {
    public static void main(String[] args) throws IOException {
        // Adresa IP și porturile pentru nodurile 1 și 3
        String node3Address = "localhost";
        int node3Port = 5002;
        int node2Port = 5001;

        // Creează un ServerSocket pentru a primi mesajul de la nodul 1
        ServerSocket serverSocket = new ServerSocket(node2Port);
        Socket socketFromNode1 = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socketFromNode1.getInputStream()));

        // Primește mesajul de la nodul 1
        String message = in.readLine();
        System.out.println("Mesajul primit de la Nodul 1: " + message);

        // Trimite mesajul mai departe către nodul 3
        Socket socketToNode3 = new Socket(node3Address, node3Port);
        PrintWriter out = new PrintWriter(socketToNode3.getOutputStream(), true);
        out.println(message);
        System.out.println("Mesajul trimis Nodului 3: " + message);

        // Închide toate conexiunile
        socketFromNode1.close();
        socketToNode3.close();
        serverSocket.close();
    }
}
