package org.example.creww.comment.repository;

import java.util.List;
import org.example.creww.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPostId(Long postId);
}
