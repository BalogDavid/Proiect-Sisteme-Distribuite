import java.io.*;
import java.net.*;

public class Client {
    private final String serverHost;
    private int serverPort;
    private static final int MAX_RETRIES = 10; // Numărul maxim de încercări

    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void start() {
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try (Socket clientSocket = new Socket(serverHost, serverPort);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                System.out.println("Client conectat la nodul " + serverHost + ":" + serverPort);
                System.out.println("Aștept mesaje...");

                String receivedJson;
                while ((receivedJson = reader.readLine()) != null) {
                    System.out.println("Client a primit mesaj: " + receivedJson);
                }
                return; // Ieșim din funcție dacă conexiunea este reușită

            } catch (IOException e) {
                System.err.println("Nu s-a putut conecta la " + serverHost + ":" + serverPort);
                serverPort++; // Incrementăm portul și încercăm din nou
                retries++;
                System.out.println("Încerc să mă conectez la portul " + serverPort + " (încercarea " + retries + ")");
            }
        }

        System.err.println("Eșuat după " + MAX_RETRIES + " încercări. Clientul se oprește.");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Utilizare: java ClientNode <adresa_server> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        Client client = new Client(host, port);
        client.start();
    }
}
