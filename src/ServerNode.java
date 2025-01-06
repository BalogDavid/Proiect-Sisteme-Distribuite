import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.*;

public class ServerNode {
    private final String clientHost;
    private int clientPort;
    private static final int MAX_RETRIES = 10; // Numărul maxim de încercări
    private boolean running = true;

    public ServerNode(String clientHost, int clientPort) {
        this.clientHost = clientHost;
        this.clientPort = clientPort;
    }

    public void start() {
        int retries = 0;

        while (retries < MAX_RETRIES && running) {
            try (Socket socket = new Socket(clientHost, clientPort);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Server conectat la clientul " + clientHost + ":" + clientPort);

                // Trimitem mesaje către client
                sendMessage(writer);

                return; // Dacă conexiunea este reușită, ieșim din funcție

            } catch (IOException e) {
                System.err.println("Nu s-a putut conecta la " + clientHost + ":" + clientPort);
                clientPort++; // Incrementăm portul și încercăm din nou
                retries++;
                System.out.println("Încerc să mă conectez la portul " + clientPort + " (încercarea " + retries + ")");
            }
        }

        System.err.println("Eșuat după " + MAX_RETRIES + " încercări. Serverul se oprește.");
    }

    private void sendMessage(PrintWriter writer) {
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            ObjectMapper objectMapper = new ObjectMapper();

            System.out.println("Introduceți mesaje pentru a le trimite către client (scrieți STOP pentru a opri):");

            String messageContent;
            while ((messageContent = consoleReader.readLine()) != null) {
                if ("STOP".equalsIgnoreCase(messageContent)) {
                    // Creăm un mesaj de tip STOP
                    Message stopMessage = new Message("STOP", "Server", "Oprire server");
                    String jsonMessage = objectMapper.writeValueAsString(stopMessage);

                    writer.println(jsonMessage);
                    System.out.println("Mesaj STOP trimis: " + jsonMessage);
                    running = false;
                    break;
                }

                // Creăm un mesaj de tip data
                Message dataMessage = new Message("data", "Server", messageContent);
                String jsonMessage = objectMapper.writeValueAsString(dataMessage);

                writer.println(jsonMessage);
                System.out.println("Mesaj trimis: " + jsonMessage);
            }
        } catch (IOException e) {
            System.err.println("Eroare la trimiterea mesajului: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Utilizare: java ServerNode <adresa_client> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ServerNode server = new ServerNode(host, port);
        server.start();
    }
}
