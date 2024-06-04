package org.example.creww.post.repository;

import java.util.List;
import org.example.creww.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {
    //List<Post> findByBoardId(Long boardId);
    Page<Post> findByBoardId(Long boardId, Pageable pageable);

}
