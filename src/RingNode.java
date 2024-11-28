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

    private ServerSocket serverSocket;
    private volatile boolean running = true;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Utilitar pentru JSON

    public RingNode(String myHost, int myPort, String nextHost, int nextPort, String nodeId) {
        this.myHost = myHost;
        this.myPort = myPort;
        this.nextHost = nextHost;
        this.nextPort = nextPort;
        this.nodeId = nodeId;
    }

    public void start() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(this::listenForMessages); // Ascultare mesaje
        executor.submit(this::sendHeartbeat); // Monitorizare noduri

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
            executor.shutdown();
        }));

        System.out.println("Nodul " + nodeId + " este funcțional. Așteaptă mesaje...");
    }

    private void listenForMessages() {
        try {
            serverSocket = new ServerSocket(myPort);
            System.out.println("Nodul " + nodeId + " ascultă pe portul " + myPort);

            while (running) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    String receivedJson;
                    while ((receivedJson = in.readLine()) != null) {
                        // Deserializăm mesajul JSON
                        Message message = objectMapper.readValue(receivedJson, Message.class);

                        System.out.println("Nodul " + nodeId + " a primit: " + message);

                        if ("STOP".equalsIgnoreCase(message.getType()) || "STOP".equalsIgnoreCase(message.getContent())) {
                            System.out.println("Comanda STOP primită. Oprirea nodului...");
                            stop();
                            return; // Ieșim imediat din buclă
                        } else if ("heartbeat".equals(message.getType())) {
                            // Ignorăm heartbeat-uri
                            continue;
                        } else if ("data".equals(message.getType())) {
                            // Procesăm și trimitem mai departe mesajele personalizate
                            if (!nodeId.equals(message.getSender())) {
                                forwardMessage(message);
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

    private void sendHeartbeat() {
        while (running) {
            try (Socket socket = new Socket(nextHost, nextPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Creăm un mesaj de tip heartbeat
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

    private void findNextNode() {
        for (int i = 1; i <= 5; i++) {
            try (Socket testSocket = new Socket(nextHost, nextPort)) {
                System.out.println("Nodul " + nextHost + ":" + nextPort + " este din nou accesibil.");
                return;
            } catch (IOException e) {
                System.err.println("Nodul " + nextHost + ":" + nextPort + " inaccesibil. Încercăm următorul...");
                nextPort += 1;
            }
        }
    }

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
}
