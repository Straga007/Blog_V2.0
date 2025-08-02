package Blog_V2.repository;

import Blog_V2.dao.repository.PostRepository;
import Blog_V2.model.Comment;
import Blog_V2.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;
    public JdbcPostRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            // Вставка нового поста
            String sql = "INSERT INTO posts (title, text, image_path, likes_count) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, post.getTitle());
                ps.setString(2, post.getText());
                ps.setString(3, post.getImagePath());
                ps.setInt(4, post.getLikesCount());
                return ps;
            }, keyHolder);
            post.setId(keyHolder.getKey().intValue());
        } else {
            // Обновление существующего поста
            String sql = "UPDATE posts SET title = ?, text = ?, image_path = ?, likes_count = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    post.getTitle(),
                    post.getText(),
                    post.getImagePath(),
                    post.getLikesCount(),
                    post.getId());
        }

        // Обновление тегов
        updateTags(post);
        // Обновление комментариев
        updateComments(post);
    }

    private void updateTags(Post post) {
        // Удаляем старые теги для поста
        String deleteSql = "DELETE FROM tags WHERE post_id = ?";
        jdbcTemplate.update(deleteSql, post.getId());

        // Затем добавляем новые теги
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            String insertSql = "INSERT INTO tags (post_id, tag) VALUES (?, ?)";
            for (String tag : post.getTags()) {
                jdbcTemplate.update(insertSql, post.getId(), tag);
            }
        }
    }

    @Override
    public Optional<Post> findById(Integer id) {
        String postSql = "SELECT * FROM posts WHERE id = ?";
        try {
            Post post = jdbcTemplate.queryForObject(postSql,
                    new Object[]{id},
                    (rs, rowNum) -> {
                        Post p = new Post();
                        p.setId(rs.getInt("id"));
                        p.setTitle(rs.getString("title"));
                        p.setText(rs.getString("text"));
                        p.setImagePath(rs.getString("image_path"));
                        p.setLikesCount(rs.getInt("likes_count"));
                        return p;
                    }
            );

            // Загружаем теги для поста
            if (post != null) {
                String tagsSql = "SELECT tag FROM tags WHERE post_id = ?";
                List<String> tags = jdbcTemplate.queryForList(tagsSql, String.class, id);
                post.setTags(tags);
            }
            // Загружаем комментарии для поста
            String commentsSql = "SELECT id, text FROM comments WHERE post_id = ?";
            List<Comment> comments = jdbcTemplate.query(commentsSql,
                    new Object[]{id},
                    (rs, rowNum) -> {
                        Comment comment = new Comment();
                        comment.setId(rs.getInt("id"));
                        comment.setText(rs.getString("text"));
                        return comment;
                    });
            post.setComments(comments);

            return Optional.ofNullable(post);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    private void updateComments(Post post) {

        if (post.getComments() != null && !post.getComments().isEmpty()) {
            for (Comment comment : post.getComments()) {
                if (comment.getId() == 0) { // if new
                    String insertSql = "INSERT INTO comments (post_id, text) VALUES (?, ?)";
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{"id"});
                        ps.setInt(1, post.getId());
                        ps.setString(2, comment.getText());
                        return ps;
                    }, keyHolder);
                    comment.setId(keyHolder.getKey().intValue());
                }
            }
        }
    }
    @Override
    public List<Post> getPosts(String search, int pageSize, int pageNumber) {
        // Реализация пагинации
        int offset = (pageNumber - 1) * pageSize;
        String sql = "SELECT * FROM posts ORDER BY id DESC LIMIT ? OFFSET ?";

        List<Post> posts = jdbcTemplate.query(sql,
                new Object[]{pageSize, offset},
                (rs, rowNum) -> {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setTitle(rs.getString("title"));
                    post.setText(rs.getString("text"));
                    post.setImagePath(rs.getString("image_path"));
                    post.setLikesCount(rs.getInt("likes_count"));
                    return post;
                });
        // tags and comment(s)
        for (Post post : posts) {
            String tagsSql = "SELECT tag FROM tags WHERE post_id = ?";
            List<String> tags = jdbcTemplate.queryForList(tagsSql, String.class, post.getId());
            post.setTags(tags);

            String commentsSql = "SELECT id, text FROM comments WHERE post_id = ?";
            List<Comment> comments = jdbcTemplate.query(commentsSql,
                    new Object[]{post.getId()},
                    (rs, rowNum) -> {
                        Comment comment = new Comment();
                        comment.setId(rs.getInt("id"));
                        comment.setText(rs.getString("text"));
                        return comment;
                    });
            post.setComments(comments);
        }

        return posts;
    }
    @Override
    public boolean hasMorePosts(String search, int pageSize, int pageNumber) {
        int offset = pageNumber * pageSize;
        String sql = "SELECT 1 FROM posts LIMIT 1 OFFSET ?";
        try {
            jdbcTemplate.queryForObject(sql, Integer.class, offset);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public void deleteComment(int postId, int commentId) {
        String sql = "DELETE FROM comments WHERE id = ? AND post_id = ?";
        jdbcTemplate.update(sql, commentId, postId);
    }

    @Override
    public void editComment(int postId, int commentId, String text) {
        String sql = "UPDATE comments SET text = ? WHERE id = ? AND post_id = ?";
        jdbcTemplate.update(sql, text, commentId, postId);
    }

    @Override
    public void deletePost(int postId) {
        String deleteTagsSql = "DELETE FROM tags WHERE post_id = ?";
        jdbcTemplate.update(deleteTagsSql, postId);

        String deleteCommentsSql = "DELETE FROM comments WHERE post_id = ?";
        jdbcTemplate.update(deleteCommentsSql, postId);

        String deletePostSql = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(deletePostSql, postId);
    }

}
