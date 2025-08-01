package Blog_V2.service;


import Blog_V2.dao.repository.PostRepository;
import Blog_V2.model.Comment;
import Blog_V2.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

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

        // Убедимся, что лайки не могут быть отрицательными
        post.setLikesCount(Math.max(newLikes, 0));

        // Обновляем пост в базе данных
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

        //image
        String imagePath = saveImage(image);

        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setImagePath(imagePath);
        post.setTags(Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList()));
        post.setLikesCount(0);

        //H2
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


}
