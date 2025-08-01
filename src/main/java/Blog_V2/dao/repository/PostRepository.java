package Blog_V2.dao.repository;



import Blog_V2.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    public void save(Post post);
    public Optional<Post> findById(Integer id);
    public List<Post> getPosts(String search, int pageSize, int pageNumber);
    public boolean hasMorePosts(String search, int pageSize, int pageNumber);
    public void deleteComment(int postId, int commentId);
    public void editComment(int postId, int commentId, String text);
    public void deletePost(int postId);
}
