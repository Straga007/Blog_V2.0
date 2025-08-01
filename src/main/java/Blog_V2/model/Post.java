package Blog_V2.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String text;
    private String imagePath;
    private int likesCount;
    @ElementCollection
    private List<String> tags;
    private List<Comment> comments;
    public Post() {
        this.comments = new ArrayList<>();
    }
    public Post(int id, String title, String text, String imagePath,List<String> tags){
        this.id = id;
        this.title = title;
        this.text = text;
        this.imagePath = imagePath;
        this.tags = tags;
        this.comments = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    // Методы для Thymeleaf
    public String getTextPreview() {
        return text != null && text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    public String getTagsAsText() {
        return tags != null ? String.join(", ", tags) : "";
    }

    public List<String> getTextParts() {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(text.split("\\r?\\n"));
    }

    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }

    // Методы без get
    public List<String> tags() {
        return getTags();
    }

    public List<Comment> comments() {
        return getComments();
    }

    public String textPreview() {
        return getTextPreview();
    }

    public List<String> textParts() {
        return getTextParts();
    }

    public String tagsAsText() {
        return getTagsAsText();
    }
}
