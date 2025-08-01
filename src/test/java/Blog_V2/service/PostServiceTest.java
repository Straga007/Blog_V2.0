package Blog_V2.service;

import Blog_V2.dao.repository.PostRepository;
import Blog_V2.model.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

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
    public void testSavePost() {
        Post post = new Post();
        post.setId(1);
        post.setTitle("Test Post");

        postService.savePost(post);

        verify(postRepository).save(post);
    }
}
