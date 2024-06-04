package org.example.creww.user.controller;

//import jakarta.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.creww.user.dto.UserLoginRequest;
import org.example.creww.user.dto.UserLoginResponse;
import org.example.creww.user.dto.UserSearchResponse;
import org.example.creww.user.dto.UserSignUpRequest;
import org.example.creww.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/auth")
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @PostMapping("/signup")
    @ApiOperation(value = "회원가입", notes = "회원가입을 진행 합니다.", tags = {"user-controller"})
    public ResponseEntity<String> signup(
        @RequestBody UserSignUpRequest userSignUpRequest
    ) {
        userService.signup(userSignUpRequest);
        return ResponseEntity.ok().body("회원가입 성공");
    }
    @PostMapping("/login")
    @ApiOperation(value = "로그인", notes = "로그인을 진행 합니다.", tags = {"user-controller"})
    public UserLoginResponse login(
        @RequestBody UserLoginRequest userLoginRequest,
        HttpServletResponse response
    ) {
        UserLoginResponse userLoginResponse = userService.login(userLoginRequest);
        response.setHeader("Authorization", "Bearer " + userLoginResponse.getToken());
        return userLoginResponse;
    }
    @GetMapping("/search")
    @ApiOperation(value = "회원조회", notes = "회원을 조회 합니다.", tags = {"user-controller"})
    public ResponseEntity<List<UserSearchResponse>> searchUsersByEmail(@RequestParam String email) {
        logger.info("Received search request for email: {}", email);
        List<UserSearchResponse> users = userService.searchUsersByEmail(email);
        if (users.isEmpty()) {
            logger.info("No users found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logger.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }
    @GetMapping("/me")
    @ApiOperation(value = "로그인 회원 조회", notes = "로그인 회원을 조회 합니다.", tags = {"user-controller"})
    public ResponseEntity<UserLoginResponse> me(HttpServletRequest request) {
        UserLoginResponse userLoginResponse = userService.me(request);
        return ResponseEntity.ok().body(userLoginResponse);
    }


}
