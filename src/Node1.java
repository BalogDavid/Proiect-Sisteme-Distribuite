import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Node1 {
    public static void main(String[] args) throws IOException {
        // Adresa IP și porturile pentru nodurile 2 și 3
        String node2Address = "localhost";
        int node2Port = 5001;
        int node1Port = 5003;

        // Creează un socket pentru a trimite mesajul către nodul 2
        Socket socketToNode2 = new Socket(node2Address, node2Port);
        PrintWriter out = new PrintWriter(socketToNode2.getOutputStream(), true);

        // Citește mesajul de la tastatură
        Scanner scanner = new Scanner(System.in);
        System.out.println("Introdu mesajul de trimis către Nod 2: ");
        String message = scanner.nextLine();

        // Trimite mesajul către nodul 2
        out.println(message);
        System.out.println("Mesajul trimis Nodului 2: " + message);

        // Închide socket-ul către nodul 2
        socketToNode2.close();

        // Creează un ServerSocket pentru a primi mesajul de la nodul 3
        ServerSocket serverSocket = new ServerSocket(node1Port);
        Socket socketFromNode3 = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socketFromNode3.getInputStream()));

        // Primește mesajul de la nodul 3
        String receivedMessage = in.readLine();
        System.out.println("Mesajul primit de la Nodul 3: " + receivedMessage);

        // Închide socket-ul și server-ul
        socketFromNode3.close();
        serverSocket.close();
    }
}
