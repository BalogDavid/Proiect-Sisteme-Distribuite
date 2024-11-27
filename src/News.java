
public class News {

    private String[] filters;
    private String title;
    private String[] author;
    private String content;
    private String url;
    private String date;
    private String language;

    public String[] getFilters() {
        return filters;
    }

    public String getTitle() {
        return title;
    }

    public String[] getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public void setFilters(String[] filters) {
        this.filters = filters;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public String getDate() {
        return date;
    }

    public void setAuthor(String[] author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
}
