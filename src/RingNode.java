import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class RingNode {
    private String myHost;
    private int myPort;
    private String nextHost;
    private int nextPort;
    private String nodeId;

    private String clientHost;
    private int clientPort;

    private ServerSocket serverSocket;
    private volatile boolean running = true;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Utilitar pentru JSON

    public RingNode(String myHost, int myPort, String nextHost, int nextPort, String nodeId, String clientHost, int clientPort) {
        this.myHost = myHost;
        this.myPort = myPort;
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.nodeId = nodeId;
        this.clientHost = clientHost;
        this.clientPort = clientPort;
    }

    public void start() {
        // Crearea unui executor pentru thread-uri
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(this::listenForMessages); // Ascultă mesaje
        executor.submit(this::sendHeartbeat); // Trimite heartbeat-uri către următorul nod

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
            executor.shutdown();
        }));

        System.out.println("Nodul " + nodeId + " este funcțional. Așteaptă mesaje...");
    }

    // Funcția de ascultare a mesajelor
    private void listenForMessages() {
        try {
            serverSocket = new ServerSocket(myPort);  // Crează serverul pe portul local
            System.out.println("Nodul " + nodeId + " ascultă pe portul " + myPort);

            while (running) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String receivedJson;
                    while ((receivedJson = in.readLine()) != null) {
                        // Deserializăm mesajul JSON
                        Message message = objectMapper.readValue(receivedJson, Message.class);
                        System.out.println("Nodul " + nodeId + " a primit: " + message);
                        // Verificăm tipul mesajului
                        if ("STOP".equalsIgnoreCase(message.getType()) || "STOP".equalsIgnoreCase(message.getContent())) {
                            System.out.println("Comanda STOP primită. Oprirea nodului...");
                            stop();
                            return; // Ieșim imediat din buclă
                        } else if ("heartbeat".equals(message.getType())) {
                            continue; // Ignorăm heartbeat-uri
                        } else if ("data".equals(message.getType())) {
                            // Procesăm mesajul
                            if (!nodeId.equals(message.getSender())) {
                                forwardMessage(message); // Retrimiterea mesajului
                                sendToClient(message);   // Trimite mesajul către client
                            }
                        }
                    }
                } catch (IOException e) {
                    if (running) System.err.println("Eroare în recepție: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Nu pot asculta pe portul " + myPort + ": " + e.getMessage());
        }
    }

    // Funcție pentru a trimite mesaje către client
    private void sendToClient(Message message) {
        try (Socket clientSocket = new Socket(clientHost, clientPort);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String jsonMessage = objectMapper.writeValueAsString(message);
            out.println(jsonMessage);
            System.out.println("Mesaj trimis către client: " + jsonMessage);

        } catch (IOException e) {
            System.err.println("Nu s-a putut trimite mesajul către client: " + e.getMessage());
        }
    }

    // Trimite heartbeat-uri
    private void sendHeartbeat() {
        // În cazul în care nu există alt nod, nu face nimic
        while (running && nextHost != null && !nextHost.isEmpty()) {
            try (Socket socket = new Socket(nextHost, nextPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                Message heartbeat = new Message("heartbeat", nodeId, "Verificare conexiune");
                String heartbeatJson = objectMapper.writeValueAsString(heartbeat);

                out.println(heartbeatJson);
                System.out.println("Trimis heartbeat către " + nextHost + ":" + nextPort);
                Thread.sleep(2000); // Trimitem un mesaj la fiecare 2 secunde

            } catch (IOException e) {
                System.err.println("Nu pot trimite către " + nextHost + ":" + nextPort + ". Încerc reconectarea...");
                findNextNode();
            } catch (InterruptedException ignored) {
            }
        }
    }

    // Retrimite mesajul către următorul nod
    private void forwardMessage(Message message) {
        try (Socket socket = new Socket(nextHost, nextPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String jsonMessage = objectMapper.writeValueAsString(message);
            out.println(jsonMessage);

            System.out.println("Nodul " + nodeId + " a transmis mesajul mai departe: " + message);

        } catch (IOException e) {
            System.err.println("Nu pot trimite mesajul către " + nextHost + ":" + nextPort);
        }
    }

    // Dacă nodul nu poate să găsească următorul nod, încearcă să se reconecteze
    private void findNextNode() {
        if (nextHost == null || nextHost.isEmpty()) {
            System.out.println("Niciun nod următor configurat. Nodul funcționează izolat.");
            return;
        }

        for (int i = 1; i <= 5; i++) {
            try (Socket testSocket = new Socket(nextHost, nextPort)) {
                System.out.println("Nodul " + nextHost + ":" + nextPort + " este din nou accesibil.");
                return;
            } catch (IOException e) {
                System.err.println("Nodul " + nextHost + ":" + nextPort + " inaccesibil. Încercăm următorul...");
                nextPort += 1;
                if (nextPort >= myPort + 100) nextPort = myPort + 1;
            }
        }
    }

    // Oprește nodul
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Eroare la închiderea serverului: " + e.getMessage());
        }
        System.out.println("Nodul " + nodeId + " a fost oprit.");
    }

    public static void main(String[] args) {
        if (args.length != 7) {
            System.err.println("Utilizare: java RingNode <myHost> <myPort> <nextHost> <nextPort> <nodeId> <clientHost> <clientPort>");
            System.exit(1);
        }

        String myHost = args[0];
        int myPort = Integer.parseInt(args[1]);
        String nextHost = args[2];
        int nextPort = Integer.parseInt(args[3]);
        String nodeId = args[4];
        String clientHost = args[5];
        int clientPort = Integer.parseInt(args[6]);

        RingNode node = new RingNode(myHost, myPort, nextHost, nextPort, nodeId, clientHost, clientPort);
        node.start();
    }
}
