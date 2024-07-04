package org.example.creww.comment.repository;

import java.util.List;
import org.example.creww.comment.dto.CommentResponse;
import org.example.creww.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment,Long> {
//    List<Comment> findByPostId(Long postId);
    @Query("SELECT new org.example.creww.comment.dto.CommentResponse(c.id, c.content, c.username) " +
        "FROM Comment c " +
        "WHERE c.postId = :postId")
    List<CommentResponse> findByPostId(@Param("postId") Long postId);
}
