package org.example.creww.post.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.creww.global.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post")
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Long boardId;
    private int views = 0;
    public Post(String title, String content,Long userId,Long boardId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.boardId = boardId;
    }
    public void setViews(int views){
        this.views = views;
    }
    public void updatePost(String title, String content,Long userId,Long boardId){
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.boardId = boardId;
    }

}
