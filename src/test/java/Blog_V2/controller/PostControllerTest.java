package Blog_V2.controller;

import Blog_V2.model.Post;
import Blog_V2.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public PostService postService() {
            return Mockito.mock(PostService.class);
        }
    }

    @Autowired
    private PostService postService;

    @BeforeEach
    void setUp() {
        reset(postService);
    }

    @Test
    public void testRedirectToPosts() throws Exception {
        mockMvc.perform(get("/posts/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));
    }

    @Test
    public void testGetPosts() throws Exception {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        post.setText("Test Content");

        List<Post> posts = Arrays.asList(post);

        when(postService.getPosts(anyString(), anyInt(), anyInt())).thenReturn(posts);
        when(postService.hasMorePosts(anyString(), anyInt(), anyInt())).thenReturn(false);

        mockMvc.perform(get("/posts")
                        .param("search", "")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("paging"));

        verify(postService).getPosts("", 10, 1);
        verify(postService).hasMorePosts("", 10, 1);
    }

    @Test
    public void testGetPostById() throws Exception {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        post.setText("Test Content");

        when(postService.getPostById(1)).thenReturn(post);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("post"))
                .andExpect(model().attribute("post", post));

        verify(postService).getPostById(1);
    }

    @Test
    public void testAddComment() throws Exception {
        doNothing().when(postService).addComment(1, "Test comment");

        mockMvc.perform(post("/posts/1/comments")
                        .param("text", "Test comment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).addComment(1, "Test comment");
    }

    @Test
    public void testCreateNewPost() throws Exception {
        when(postService.createPost(anyString(), anyString(), anyString(), any()))
                .thenReturn(1);

        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart("/posts")
                        .file(image)
                        .param("title", "Test Title")
                        .param("text", "Test Text")
                        .param("tags", "tag1, tag2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).createPost("Test Title", "Test Text", "tag1, tag2", image);
    }


    @Test
    public void testDeletePost() throws Exception {
        doNothing().when(postService).deletePost(1);

        mockMvc.perform(post("/posts/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"));

        verify(postService).deletePost(1);
    }

    @Test
    public void testUpdateLikes() throws Exception {
        doNothing().when(postService).updateLikes(1, true);

        mockMvc.perform(post("/posts/1/like")
                        .param("like", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).updateLikes(1, true);
    }

    @Test
    public void testEditPost() throws Exception {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");
        post.setText("Test Content");

        when(postService.getPostById(1)).thenReturn(post);

        mockMvc.perform(get("/posts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"))
                .andExpect(model().attribute("post", post));

        verify(postService, times(1)).getPostById(1);
    }

    @Test
    public void testAddPost() throws Exception {
        mockMvc.perform(get("/posts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-post"));
    }

    @Test
    public void testDeleteComment() throws Exception {
        doNothing().when(postService).deleteComment(1, 2);

        mockMvc.perform(post("/posts/1/comments/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).deleteComment(1, 2);
    }

    @Test
    public void testEditComment() throws Exception {
        doNothing().when(postService).editComment(1, 2, "Updated comment");

        mockMvc.perform(post("/posts/1/comments/2")
                        .param("text", "Updated comment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1"));

        verify(postService).editComment(1, 2, "Updated comment");
    }
}
