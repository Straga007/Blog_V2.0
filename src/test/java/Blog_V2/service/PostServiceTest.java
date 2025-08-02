package Blog_V2.service;

import Blog_V2.dao.repository.PostRepository;
import Blog_V2.model.Comment;
import Blog_V2.model.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;


    @Test
    public void testGetPosts() {
        Post post1 = new Post();
        post1.setId(1);
        post1.setTitle("Post 1");
        
        Post post2 = new Post();
        post2.setId(2);
        post2.setTitle("Post 2");
        
        List<Post> posts = Arrays.asList(post1, post2);
        
        when(postRepository.getPosts("test", 10, 1)).thenReturn(posts);
        
        List<Post> result = postService.getPosts("test", 10, 1);
        
        // Проверки
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Post 1", result.get(0).getTitle());
        assertEquals("Post 2", result.get(1).getTitle());
        
        verify(postRepository).getPosts("test", 10, 1);
    }

    @Test
    public void testHasMorePosts() {
        when(postRepository.hasMorePosts("test", 5, 2)).thenReturn(true);
        
        boolean result = postService.hasMorePosts("test", 5, 2);
        
        assertTrue(result);
        
        verify(postRepository).hasMorePosts("test", 5, 2);
    }

    @Test
    public void testGetPostById() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        Post result = postService.getPostById(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Post", result.getTitle());
        
        verify(postRepository).findById(1);
    }

    @Test
    public void testGetPostByIdNotFound() {
        when(postRepository.findById(999)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.getPostById(999);
        });
        
        assertEquals("Post not found", exception.getMessage());
        
        verify(postRepository).findById(999);
    }


    @Test
    public void testAddComment() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        postService.addComment(1, "Test comment");
        
        assertEquals(1, post.getComments().size());
        assertEquals("Test comment", post.getComments().get(0).getText());
        
        verify(postRepository).findById(1);
        verify(postRepository).save(post);
    }

    @Test
    public void testDeleteComment() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        
        Comment comment1 = new Comment();
        comment1.setId(1);
        comment1.setText("Comment 1");
        
        Comment comment2 = new Comment();
        comment2.setId(2);
        comment2.setText("Comment 2");

        post.setComments(new ArrayList<>(Arrays.asList(comment1, comment2)));

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        postService.deleteComment(1, 2);
        
        assertEquals(1, post.getComments().size());
        assertEquals(1, post.getComments().get(0).getId());
        assertEquals("Comment 1", post.getComments().get(0).getText());
        
        verify(postRepository).findById(1);
        verify(postRepository).deleteComment(1, 2);
        verify(postRepository).save(post);
    }

    @Test
    public void testEditComment() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        
        Comment comment = new Comment();
        comment.setId(1);
        comment.setText("Original comment");
        
        post.setComments(Arrays.asList(comment));
        
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        postService.editComment(1, 1, "Updated comment");
        
        assertEquals(1, post.getComments().size());
        assertEquals("Updated comment", post.getComments().get(0).getText());
        
        verify(postRepository).findById(1);
        verify(postRepository).editComment(1, 1, "Updated comment");
    }


    @Test
    public void testDeletePost() {
        postService.deletePost(1);
        
        verify(postRepository).deletePost(1);
    }

    @Test
    public void testSavePost() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        
        postService.savePost(post);
        
        verify(postRepository).save(post);
    }

    @Test
    public void testUpdateLikesIncrement() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        post.setLikesCount(5);
        
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        postService.updateLikes(1, true);
        
        assertEquals(6, post.getLikesCount());
        
        verify(postRepository).findById(1);
        verify(postRepository).save(post);
    }

    @Test
    public void testUpdateLikesDecrement() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        post.setLikesCount(5);
        
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        postService.updateLikes(1, false);
        
        assertEquals(4, post.getLikesCount());
        
        verify(postRepository).findById(1);
        verify(postRepository).save(post);
    }

    @Test
    public void testUpdateLikesPreventNegative() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        post.setLikesCount(0);
        
        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        postService.updateLikes(1, false);
        
        assertEquals(0, post.getLikesCount()); // Не должно стать отрицательным
        
        verify(postRepository).findById(1);
        verify(postRepository).save(post);
    }

    @Test
    public void testCreatePost() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        int postId = postService.createPost("Test Title", "Test Text", "tag1, tag2", image);

        assertTrue(postId == 0);

        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void testCreatePostWithEmptyImage() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new byte[0]); // Пустой файл

        int postId = postService.createPost("Test Title", "Test Text", "tag1, tag2", image);

        assertTrue(postId == 0);

        verify(postRepository).save(any(Post.class));
    }


    @Test
    public void testSaveImage() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image", 
                "test.jpg", 
                "image/jpeg", 
                "test image content".getBytes());
        
        String imagePath = postService.saveImage(image);
        
        assertNotNull(imagePath);
        assertTrue(imagePath.startsWith("/images/"));
        assertTrue(imagePath.contains("_test.jpg"));
    }

    @Test
    public void testSaveEmptyImage() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image", 
                "test.jpg", 
                "image/jpeg", 
                new byte[0]);
        
        String imagePath = postService.saveImage(image);
        
        assertNull(imagePath);
    }


    @Test
    public void testGetImageByPostIdNoImage() throws IOException {
        Post post = new Post();
        post.setId(1);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        
        byte[] imageBytes = postService.getImageByPostId(1);
        
        assertNull(imageBytes);
        
        verify(postRepository).findById(1);
    }
    @Test
    public void testGetImageByPostId() throws IOException {
        Post post = new Post();
        post.setId(1);
        post.setImagePath("/images/test.jpg");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        Path uploadDir = Paths.get("uploads/images");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path testImage = uploadDir.resolve("test.jpg");
        Files.write(testImage, "test image content".getBytes());

        try {
            byte[] imageBytes = postService.getImageByPostId(1);

            assertNotNull(imageBytes);
            assertEquals("test image content", new String(imageBytes));
        } finally {
            Files.deleteIfExists(testImage);
        }

        verify(postRepository).findById(1);
    }


    @Test
    public void testCreatePostWithNullTags() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        int postId = postService.createPost("Test Title", "Test Text", "", image);

        assertTrue(postId == 0);

        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void testCreatePostWithWhitespaceTags() throws IOException {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());

        int postId = postService.createPost("Test Title", "Test Text", " ,  , tag ", image);

        assertTrue(postId == 0);

        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void testSaveImageIOException() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(image.getOriginalFilename()).thenReturn("test.jpg");
        when(image.getInputStream()).thenThrow(new IOException("Test IO exception"));
        
        assertThrows(IOException.class, () -> {
            postService.saveImage(image);
        });
    }
}
