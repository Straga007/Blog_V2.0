package Blog_V2.integration;


import Blog_V2.BlogGradleSpringWebappApplication;
import Blog_V2.dao.repository.PostRepository;
import Blog_V2.model.Comment;
import Blog_V2.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BlogGradleSpringWebappApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class JdbcPostRepositoryIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private DataSource dataSource;
    @BeforeEach
    void setUp() throws SQLException {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);
    }
    @Test
    public void testSaveNewPostWithTagsAndComments() {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setText("Test Content");
        post.setTags(Arrays.asList("tag1", "tag2"));

        Comment comment = new Comment("Test comment");
        post.addComment(comment);

        postRepository.save(post);

        assertTrue(post.getId() > 0);

        Optional<Post> found = postRepository.findById(post.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Post", found.get().getTitle());
        assertEquals("Test Content", found.get().getText());

        List<String> tags = found.get().getTags();
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));

        List<Comment> comments = found.get().getComments();
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Test comment", comments.get(0).getText());
        assertTrue(comments.get(0).getId() > 0);
    }

    @Test
    public void testUpdateExistingPost() {
        Post post = new Post();
        post.setTitle("Original Title");
        post.setText("Original Content");
        post.setTags(Arrays.asList("original"));
        postRepository.save(post);

        int postId = post.getId();
        post.setTitle("Updated Title");
        post.setText("Updated Content");
        post.setTags(Arrays.asList("updated", "tag"));
        postRepository.save(post);

        Optional<Post> updated = postRepository.findById(postId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals("Updated Content", updated.get().getText());
        assertEquals(postId, updated.get().getId());

        List<String> tags = updated.get().getTags();
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertTrue(tags.contains("updated"));
        assertTrue(tags.contains("tag"));
    }

    @Test
    public void testSavePostWithOnlyTags() {
        Post post = new Post();
        post.setTitle("Post with Tags");
        post.setText("Content");
        post.setTags(Arrays.asList("tag1", "tag2", "tag3"));

        postRepository.save(post);

        Optional<Post> found = postRepository.findById(post.getId());
        assertTrue(found.isPresent());
        assertEquals("Post with Tags", found.get().getTitle());
        assertEquals("Content", found.get().getText());

        List<String> tags = found.get().getTags();
        assertNotNull(tags);
        assertEquals(3, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
        assertTrue(tags.contains("tag3"));

        List<Comment> comments = found.get().getComments();
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }

    @Test
    public void testSavePostWithOnlyComments() {
        Post post = new Post();
        post.setTitle("Post with Comments");
        post.setText("Content");

        Comment comment1 = new Comment("First comment");
        Comment comment2 = new Comment("Second comment");
        post.addComment(comment1);
        post.addComment(comment2);

        postRepository.save(post);

        Optional<Post> found = postRepository.findById(post.getId());
        assertTrue(found.isPresent());
        assertEquals("Post with Comments", found.get().getTitle());
        assertEquals("Content", found.get().getText());

        List<String> tags = found.get().getTags();
        assertNotNull(tags);

        List<Comment> comments = found.get().getComments();
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("First comment", comments.get(0).getText());
        assertEquals("Second comment", comments.get(1).getText());
    }

    @Test
    public void testFindByIdExistingPost() {
        Post post = new Post();
        post.setTitle("Find Test");
        post.setText("Find Content");
        post.setTags(Arrays.asList("find", "test"));

        Comment comment = new Comment("Find comment");
        post.addComment(comment);

        postRepository.save(post);

        Optional<Post> found = postRepository.findById(post.getId());

        assertTrue(found.isPresent());
        assertEquals(post.getId(), found.get().getId());
        assertEquals("Find Test", found.get().getTitle());
        assertEquals("Find Content", found.get().getText());
    }

    @Test
    public void testFindByIdNonExistingPost() {
        Optional<Post> found = postRepository.findById(99999);

        assertFalse(found.isPresent());
    }

    @Test
    public void testFindByIdLoadsTags() {
        Post post = new Post();
        post.setTitle("Tags Test");
        post.setText("Content");
        post.setTags(Arrays.asList("tag1", "tag2", "tag3"));
        postRepository.save(post);

        Optional<Post> found = postRepository.findById(post.getId());

        assertTrue(found.isPresent());
        List<String> tags = found.get().getTags();
        assertNotNull(tags);
        assertEquals(3, tags.size());
        assertTrue(tags.contains("tag1"));
        assertTrue(tags.contains("tag2"));
        assertTrue(tags.contains("tag3"));
    }

    @Test
    public void testFindByIdLoadsComments() {
        Post post = new Post();
        post.setTitle("Comments Test");
        post.setText("Content");

        Comment comment1 = new Comment("Comment 1");
        Comment comment2 = new Comment("Comment 2");
        post.addComment(comment1);
        post.addComment(comment2);

        postRepository.save(post);

        Optional<Post> found = postRepository.findById(post.getId());

        assertTrue(found.isPresent());
        List<Comment> comments = found.get().getComments();
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("Comment 1", comments.get(0).getText());
        assertEquals("Comment 2", comments.get(1).getText());
        assertTrue(comments.get(0).getId() > 0);
        assertTrue(comments.get(1).getId() > 0);
    }

    @Test
    public void testGetPostsReturnsListOfPosts() {
        Post post1 = new Post();
        post1.setTitle("Post 1");
        post1.setText("Content 1");
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Post 2");
        post2.setText("Content 2");
        postRepository.save(post2);

        List<Post> posts = postRepository.getPosts("", 10, 1);

        assertNotNull(posts);
        assertFalse(posts.isEmpty());
        assertTrue(posts.size() >= 2);

        boolean foundPost1 = posts.stream().anyMatch(p -> "Post 1".equals(p.getTitle()));
        boolean foundPost2 = posts.stream().anyMatch(p -> "Post 2".equals(p.getTitle()));
        assertTrue(foundPost1);
        assertTrue(foundPost2);
    }

    @Test
    public void testGetPostsPagination() {
        for (int i = 1; i <= 5; i++) {
            Post post = new Post();
            post.setTitle("Post " + i);
            post.setText("Content " + i);
            postRepository.save(post);
        }

        List<Post> firstPage = postRepository.getPosts("", 3, 1);
        assertEquals(3, firstPage.size());

        List<Post> secondPage = postRepository.getPosts("", 3, 2);
        assertEquals(2, secondPage.size());
    }

    @Test
    public void testGetPostsOrderByIdDesc() {
        Post post1 = new Post();
        post1.setTitle("First");
        post1.setText("Content");
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Second");
        post2.setText("Content");
        postRepository.save(post2);

        Post post3 = new Post();
        post3.setTitle("Third");
        post3.setText("Content");
        postRepository.save(post3);

        List<Post> posts = postRepository.getPosts("", 10, 1);

        assertNotNull(posts);
        assertTrue(posts.size() >= 3);

        assertEquals("Third", posts.get(0).getTitle());
        assertEquals("First", posts.get(posts.size() - 1).getTitle());
    }
    @Test
    public void testHasMorePosts() {
        for (int i = 1; i <= 5; i++) {
            Post post = new Post();
            post.setTitle("Post " + i);
            post.setText("Content " + i);
            postRepository.save(post);
        }

        boolean hasMore = postRepository.hasMorePosts("", 3, 1);
        assertTrue(hasMore);

        boolean hasMoreSecondPage = postRepository.hasMorePosts("", 3, 2);
        assertFalse(hasMoreSecondPage);

        boolean hasMoreThirdPage = postRepository.hasMorePosts("", 3, 3);
        assertFalse(hasMoreThirdPage);
    }
    @Test
    public void testDeletePostWithCascadeDeleteTagsAndComments() {
        Post post = new Post();
        post.setTitle("To Delete");
        post.setText("Content");
        post.setTags(Arrays.asList("tag1", "tag2"));

        Comment comment = new Comment("Delete comment");
        post.addComment(comment);

        postRepository.save(post);
        int postId = post.getId();

        postRepository.deletePost(postId);

        Optional<Post> found = postRepository.findById(postId);
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteNonExistingPost() {
        assertDoesNotThrow(() -> {
            postRepository.deletePost(99999);
        });
    }

    @Test
    public void testDeleteComment() {
        Post post = new Post();
        post.setTitle("Comments Test");
        post.setText("Content");

        Comment comment1 = new Comment("Keep this comment");
        Comment comment2 = new Comment("Delete this comment");
        post.addComment(comment1);
        post.addComment(comment2);

        postRepository.save(post);
        int postId = post.getId();
        int commentIdToDelete = comment2.getId();

        postRepository.deleteComment(postId, commentIdToDelete);

        Optional<Post> updatedPost = postRepository.findById(postId);
        assertTrue(updatedPost.isPresent());

        List<Comment> comments = updatedPost.get().getComments();
        assertEquals(1, comments.size());
        assertEquals("Keep this comment", comments.get(0).getText());
    }

    @Test
    public void testEditComment() {
        Post post = new Post();
        post.setTitle("Edit Comment Test");
        post.setText("Content");

        Comment comment = new Comment("Original comment");
        post.addComment(comment);

        postRepository.save(post);
        int postId = post.getId();
        int commentId = comment.getId();

        String newText = "Updated comment";
        postRepository.editComment(postId, commentId, newText);

        Optional<Post> updatedPost = postRepository.findById(postId);
        assertTrue(updatedPost.isPresent());

        List<Comment> comments = updatedPost.get().getComments();
        assertEquals(1, comments.size());
        assertEquals(newText, comments.get(0).getText());
        assertEquals(commentId, comments.get(0).getId());
    }
}
