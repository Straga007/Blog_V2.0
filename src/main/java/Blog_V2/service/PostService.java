package Blog_V2.service;


import Blog_V2.dao.repository.PostRepository;
import Blog_V2.model.Comment;
import Blog_V2.model.Paging;
import Blog_V2.model.Post;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getPosts(String search, int pageSize, int pageNumber) {
        return postRepository.getPosts(search, pageSize, pageNumber);
    }
    public void addComment(int postId, String text) {
        Post post = getPostById(postId);
        Comment comment = new Comment(text);
        post.addComment(comment);
        postRepository.save(post);
    }
    public void deleteComment(int postId, int commentId) {
        Post post = getPostById(postId);
        post.getComments().removeIf(comment -> comment.getId() == commentId);
        postRepository.deleteComment(postId,commentId);
        postRepository.save(post);
    }
    public void editComment(int postId, int commentId, String text) {
        Post post = getPostById(postId);
        post.getComments().stream()
                .filter(comment -> comment.getId() == commentId)
                .findFirst()
                .ifPresent(comment -> {
                    comment.setText(text);
                    postRepository.editComment(postId, commentId, text);
                });
    }
    public void deletePost(int postId){
        postRepository.deletePost(postId);
    }
    public boolean hasMorePosts(String search, int pageSize, int pageNumber) {
        return postRepository.hasMorePosts(search, pageSize, pageNumber);
    }
    public void savePost(Post post) {
        postRepository.save(post);
    }
    public void updateLikes(int postId, boolean like) {
        Post post = getPostById(postId);
        int currentLikes = post.getLikesCount();
        int newLikes = like ? currentLikes + 1 : currentLikes - 1;

        post.setLikesCount(Math.max(newLikes, 0));

        postRepository.save(post);
    }
    public Post getPostById(int id) {
        return postRepository.findById(id).orElseThrow(()
                -> new RuntimeException("Post not found"));
    }
    public byte[] getImageByPostId(int id) throws IOException {
        Post post = getPostById(id);
        if (post == null || post.getImagePath() == null) {
            return null;
        }
        String fileName = post.getImagePath().replace("/images/", "");
        Path imagePath = Paths.get("uploads/images/" + fileName);

        if (!Files.exists(imagePath)) {
            return null;
        }

        return Files.readAllBytes(imagePath);
    }
    public int createPost(
            String title,
            String text,
            String tags,
            MultipartFile image) throws IOException {

        String imagePath = saveImage(image);

        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setImagePath(imagePath);
        post.setTags(parseTags(tags));
        post.setLikesCount(0);

        postRepository.save(post);
        return post.getId();
    }

    public String saveImage(MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            return null;
        }

        String uploadDir = "uploads/images/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(image.getInputStream(), filePath);

        return "/images/" + fileName;
    }

    public void updatePost(int id, String title, String text, String tags, MultipartFile image) throws IOException {
        Post post = getPostById(id);
        post.setTitle(title);
        post.setText(text);
        post.setTags(parseTags(tags));

        if (image != null && !image.isEmpty()) {
            String imagePath = saveImage(image);
            post.setImagePath(imagePath);
        }
        savePost(post);
    }
    public Paging createPaging(String search, int pageSize, int pageNumber) {
        Paging paging = new Paging();
        paging.setPageNumber(pageNumber);
        paging.setPageSize(pageSize);
        paging.setHasNext(hasMorePosts(search, pageSize, pageNumber));
        paging.setHasPrevious(pageNumber > 1);
        return paging;
    }
    private List<String> parseTags(String tagsString) {
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
    }
}
