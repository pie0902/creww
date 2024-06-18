package org.example.creww.user.service;

//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.creww.global.globalException.ApplicationException;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.dto.UserLoginRequest;
import org.example.creww.user.dto.UserLoginResponse;
import org.example.creww.user.dto.UserSearchResponse;
import org.example.creww.user.dto.UserSignUpRequest;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserLoginResponse me(HttpServletRequest request){
        String token = jwtUtils.validateTokenOrThrow(request);
        Long userId = Long.parseLong(jwtUtils.getUserIdFromToken(token));
        User user = userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저"));
        UserLoginResponse userLoginResponse = new UserLoginResponse(token,user,user.getUsername());
        return userLoginResponse;
    }


    public void signup(
        UserSignUpRequest userSignUpRequest
    ) {
        // 유저가 이미 존재하는지 확인
        boolean userExists = userRepository.findByEmail(userSignUpRequest.getEmail()).isPresent();
        if (userExists) {
            throw new ApplicationException("이미 존재하는 유저", HttpStatus.CONFLICT);
        }
        String encodedPassword = passwordEncoder.encode(userSignUpRequest.getPassword());

        User user = new User(
            userSignUpRequest.getEmail(),
            userSignUpRequest.getUsername(),
            encodedPassword
        );
        userRepository.save(user);
    }

    public UserLoginResponse login(UserLoginRequest userLoginRequest) {
        // 유저 검증
        User user = userRepository.findByEmail(userLoginRequest.getEmail())
            .orElseThrow(() -> new ApplicationException("존재하지 않는 유저", HttpStatus.NOT_FOUND));
        // 비밀번호 검증
        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new ApplicationException("비밀번호가 일치하지 않습니다", HttpStatus.BAD_REQUEST);
        }
        String token = jwtUtils.generateToken(user.getId());
        return new UserLoginResponse(token, user,user.getUsername());
    }

    public List<UserSearchResponse> searchUsersByEmail(String email) {
        logger.info("Searching for users with email containing: {}", email);
        List<User> users = userRepository.findByEmailContaining(email);
        logger.info("Found {} users", users.size());
        return users.stream()
            .map(user -> new UserSearchResponse(user.getId(),user.getUsername(),user.getEmail()))
            .collect(Collectors.toList());
    }



//    private void setTokenCookie(
//        HttpServletResponse response,
//        String token
//    ){
//        Cookie cookie = new Cookie("token",token);
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath("/");
//        cookie.setMaxAge(86400);
//        response.addCookie(cookie);
//    }

}
