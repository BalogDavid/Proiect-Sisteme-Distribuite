import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("type")
    private String type; // Tipul mesajului (ex. "heartbeat", "data")
    @JsonProperty("sender")
    private String sender; // ID-ul nodului care trimite mesajul
    @JsonProperty("content")
    private String content; // Conținutul mesajului

    public Message() {
        // Constructor gol pentru deserializare JSON
    }

    public Message(String type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
