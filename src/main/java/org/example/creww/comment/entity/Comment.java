package org.example.creww.comment.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String username;
    private Long postId;
    private Long userId;
    public Comment(String content,String username,Long postId,Long userId){
        this.content = content;
        this.username = username;
        this.postId = postId;
        this.userId = userId;
    }
}
