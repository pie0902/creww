package org.example.creww.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.creww.user.entity.User;

@Getter
@NoArgsConstructor
public class UserLoginResponse {
    private String token;
    private User user;
    private String username;
    public UserLoginResponse(String token,User user,String username){
        this.token = token;
        this.user = user;
        this.username = username;
    }
}
