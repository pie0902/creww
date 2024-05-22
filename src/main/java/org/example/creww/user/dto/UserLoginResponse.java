package org.example.creww.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.creww.user.entity.User;

@Getter
@NoArgsConstructor
public class UserLoginResponse {
    private String token;
    private User user;
    public UserLoginResponse(String token,User user){
        this.token = token;
        this.user = user;
    }
}
