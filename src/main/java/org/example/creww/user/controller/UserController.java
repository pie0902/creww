package org.example.creww.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.creww.user.dto.UserLoginRequest;
import org.example.creww.user.dto.UserLoginResponse;
import org.example.creww.user.dto.UserSignUpRequest;
import org.example.creww.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
        @RequestBody UserSignUpRequest userSignUpRequest
    ) {
        userService.signup(userSignUpRequest);
        return ResponseEntity.ok().body("회원가입 성공");
    }
//    @PostMapping("/login")
//    public ResponseEntity<UserLoginResponse> login(
//        @RequestBody UserLoginRequest userLoginRequest,
//        HttpServletResponse response
//    ){
//      UserLoginResponse userLoginResponse = userService.login(userLoginRequest,response);
//      return ResponseEntity.ok().body(userLoginResponse);
//}
    @PostMapping("/login")
    public UserLoginResponse login(
        @RequestBody UserLoginRequest userLoginRequest,
        HttpServletResponse response
    ) {
        UserLoginResponse userLoginResponse = userService.login(userLoginRequest);
        response.setHeader("Authorization", "Bearer " + userLoginResponse.getToken());
        return userLoginResponse;
    }
}
