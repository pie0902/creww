package org.example.creww.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.creww.post.entity.Post;
import org.example.creww.user.entity.User;
@Getter
@AllArgsConstructor
public class PostWithUser {
    private Long postId;
    private String postTitle;
    private Long userId;
    private String username;
}
