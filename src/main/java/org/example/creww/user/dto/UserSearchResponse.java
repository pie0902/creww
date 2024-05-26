package org.example.creww.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {
    private Long userId;
    private String username;
    private String email;
}
