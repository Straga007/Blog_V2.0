package Blog_V2.controller;

import Blog_V2.model.Paging;
import Blog_V2.model.Post;
import Blog_V2.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/")
    public String redirectToPosts() {
        return "redirect:/posts";
    }

    @GetMapping
    public String posts(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            Model model) {

        List<Post> posts = postService.getPosts(search, pageSize, pageNumber);

        Paging paging = new Paging();
        paging.setPageNumber(pageNumber);
        paging.setPageSize(pageSize);
        paging.setHasNext(postService.hasMorePosts(search, pageSize, pageNumber));
        paging.setHasPrevious(pageNumber > 1);

        // Add attributes to model
        model.addAttribute("posts", posts);
        model.addAttribute("search", search);
        model.addAttribute("paging", paging);

        return "posts";
    }
    @PostMapping("/{id}/comments")
    public String addComment(
            @PathVariable int id,
            @RequestParam("text") String text) {

        postService.addComment(id, text);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}")
    public String updatePost(
            @PathVariable int id,
            @RequestParam("title") String title,
            @RequestParam("text") String text,
            @RequestParam("tags") String tags,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        Post post = postService.getPostById(id);
        post.setTitle(title);
        post.setText(text);

        // Преобразуем строку тегов в список
        List<String> tagsList = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
        post.setTags(tagsList);

        if (image != null && !image.isEmpty()) {
            String imagePath = postService.saveImage(image);
            post.setImagePath(imagePath);
        }

        postService.savePost(post);
        return "redirect:/posts/" + id;
    }
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) throws IOException {
        byte[] imageBytes = postService.getImageByPostId(id);
        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }

        // Определяем тип содержимого (Content-Type) на основе расширения файла
        String imagePath = postService.getPostById(id).getImagePath();
        String contentType = Files.probeContentType(Paths.get(imagePath));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageBytes);
    }

    @GetMapping("/{id}")
    public String post(@PathVariable int id, Model model) {
        Post post = postService.getPostById(id);
        // Add post to model
        model.addAttribute("post", post);
        return "post";
    }
    @PostMapping("/{id}/like")
    public String handleLike(
            @PathVariable int id,
            @RequestParam("like") boolean like) {
        postService.updateLikes(id, like);
        return "redirect:/posts/" + id;
    }
    @GetMapping("/{id}/edit")
    public String editPost(@PathVariable int id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        return "add-post";
    }
    @GetMapping("/add")
    public String addPost() {
        return "add-post";
    }
    @PostMapping
    public String createNewPost(    @RequestParam("title") String title,
                                    @RequestParam("text") String text,
                                    @RequestParam("tags") String tags,
                                    @RequestParam("image") MultipartFile image) throws IOException {
        int postId = postService.createPost(title, text, tags, image);

        return "redirect:/posts/" + postId;
    }
    @PostMapping("/{postId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable int postId,
            @PathVariable int commentId) {

        postService.deleteComment(postId, commentId);
        return "redirect:/posts/" + postId;
    }
    @PostMapping("/{postId}/comments/{commentId}")
    public String editComment(
            @PathVariable int postId,
            @RequestParam("text")String text,
            @PathVariable int commentId) {
        postService.editComment(postId, commentId, text);
        return "redirect:/posts/" + postId;
    }
    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable int id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }
}
