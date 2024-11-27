import java.io.*;
import java.net.*;

public class Node3 {
    public static void main(String[] args) throws IOException {
        // Adresa IP și porturile pentru nodul 1 și nodul 2
        String node1Address = "localhost";
        int node1Port = 5003;
        int node3Port = 5002;

        // Creează un ServerSocket pentru a primi mesajul de la nodul 2
        ServerSocket serverSocket = new ServerSocket(node3Port);
        Socket socketFromNode2 = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socketFromNode2.getInputStream()));

        // Primește mesajul de la nodul 2
        String message = in.readLine();
        System.out.println("Mesajul primit de la Nodul 2: " + message);

        // Trimite mesajul înapoi către nodul 1
        Socket socketToNode1 = new Socket(node1Address, node1Port);
        PrintWriter out = new PrintWriter(socketToNode1.getOutputStream(), true);
        out.println(message);
        System.out.println("Mesajul trimis Nodului 1: " + message);

        // Închide toate conexiunile
        socketFromNode2.close();
        socketToNode1.close();
        serverSocket.close();
    }
}
