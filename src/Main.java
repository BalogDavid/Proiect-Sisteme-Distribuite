import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Introduceți portul curent: ");
        int myPort = scanner.nextInt();

        System.out.print("Introduceți portul următor: ");
        int nextPort = scanner.nextInt();

        String myHost = "localhost"; // Gazda implicită
        String nextHost = "localhost"; // Gazda implicită pentru nodul următor
        String nodeId = "Node_" + myPort; // Generăm un ID bazat pe port

        // Creăm și pornim nodul
        RingNode node = new RingNode(myHost, myPort, nextHost, nextPort, nodeId);
        node.start();
    }
}
