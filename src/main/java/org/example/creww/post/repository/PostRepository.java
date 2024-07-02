package org.example.creww.post.repository;

import java.util.List;
import java.util.Optional;
import org.example.creww.post.dto.PostWithUser;
import org.example.creww.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post,Long> {
    //List<Post> findByBoardId(Long boardId);
    Page<Post> findByBoardId(Long boardId, Pageable pageable);
    @Query("SELECT new org.example.creww.post.dto.PostWithUser(p.id, p.title, u.id, u.username) " +
        "FROM Post p JOIN User u ON p.userId = u.id WHERE p.id = :postId")
    Optional<PostWithUser> findPostWithUserById(@Param("postId") Long postId);
}
