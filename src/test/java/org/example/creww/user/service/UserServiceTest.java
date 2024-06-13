package org.example.creww.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.dto.UserLoginRequest;
import org.example.creww.user.dto.UserLoginResponse;
import org.example.creww.user.dto.UserSearchResponse;
import org.example.creww.user.dto.UserSignUpRequest;
import org.example.creww.user.entity.User;
import org.example.creww.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;

    private String token;
    private User user;
    private String encodedPassword;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @BeforeEach
    void setUp() {
        token = "testToken";
        encodedPassword = passwordEncoder.encode("1234");
        user = new User("test@test.com","tester",encodedPassword);
        ReflectionTestUtils.setField(user,"id",1L);



    }
    @Test
    @DisplayName("현재 로그인한 상태 테스트")
    void me_test() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.getTokenFromRequest(request)).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(user.getId()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //when
        UserLoginResponse userLoginResponse = userService.me(request);

        //then
        assertEquals(userLoginResponse.getUser().getUsername(), user.getUsername());

    }
    @Test
    @DisplayName("회원가입 테스트")
    void signup_test(){
        //given
        UserSignUpRequest userSignUpRequest = new UserSignUpRequest(user.getUsername(), user.getEmail(), user.getPassword());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail(userSignUpRequest.getEmail())).thenReturn(Optional.empty());

        //when
        userService.signup(userSignUpRequest);

        //then
        verify(userRepository, times(1)).save(any(User.class));
    }




    @Test
    @DisplayName("로그인 테스트")
    void login_test(){
        //given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(user.getId())).thenReturn(token);
        UserLoginRequest userLoginRequest = new UserLoginRequest(user.getEmail(),user.getPassword());
        when(passwordEncoder.matches(userLoginRequest.getPassword(),user.getPassword())).thenReturn(true);

        //when
        UserLoginResponse userLoginResponse = userService.login(userLoginRequest);

        //then
        assertEquals(user.getUsername(),userLoginResponse.getUsername());
        assertEquals(user.getEmail(),userLoginResponse.getUser().getEmail());
        assertEquals(token,userLoginResponse.getToken());
    }

    @Test
    @DisplayName("유저 검색 테스트")
    void searchUserByEmail_test(){
        //given
        List<User> users = Arrays.asList(
          user,
          new User("test2@test.com","tester2",encodedPassword)
        );
        when(userRepository.findByEmailContaining(user.getEmail())).thenReturn(users);
        //when
        List<UserSearchResponse> userSearchResponse = userService.searchUsersByEmail(user.getEmail());

        //then
        assertEquals(2, userSearchResponse .size());
        assertEquals("tester", userSearchResponse .get(0).getUsername());
        assertEquals("test@test.com", userSearchResponse .get(0).getEmail());
        assertEquals("tester2", userSearchResponse .get(1).getUsername());
        assertEquals("test2@test.com", userSearchResponse .get(1).getEmail());

        verify(userRepository, times(1)).findByEmailContaining(user.getEmail());
    }


}