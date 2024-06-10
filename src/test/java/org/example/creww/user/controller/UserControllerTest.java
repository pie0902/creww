package org.example.creww.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.jwt.JwtUtils;
import org.example.creww.user.dto.UserLoginRequest;
import org.example.creww.user.dto.UserLoginResponse;
import org.example.creww.user.dto.UserSearchResponse;
import org.example.creww.user.dto.UserSignUpRequest;
import org.example.creww.user.dto.UserSignupResponse;
import org.example.creww.user.entity.User;
import org.example.creww.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;
    private Long userId;
    public UserLoginResponse userLoginResponse;
    public UserLoginRequest userLoginRequest;
    public UserSearchResponse userSearchResponse;
    public UserSignUpRequest userSignUpRequest;
    public UserSignupResponse userSignupResponse;
    public String username;
    public User user;
    public String email;
    @BeforeEach
    void setUp() {
        token = "mockToken";
        userId = 1L;
        username = "testUser";
        email = "test@test.com";
        user = new User(email,username,"1234");
        userLoginResponse = new UserLoginResponse(token,user,username);
        userLoginRequest = new UserLoginRequest(email,"1234");
        userSearchResponse = new UserSearchResponse(userId,username,email);
        userSignUpRequest = new UserSignUpRequest(username,email,"1234");
        userSignupResponse = new UserSignupResponse(username,email);


        when(jwtUtils.getTokenFromRequest(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(String.valueOf(userId));
    }

    @Test
    @WithMockUser
    @DisplayName("회원가입 테스트")
    void signup_test() throws Exception {

        objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(userSignUpRequest);
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().string("회원가입 성공"));

        verify(userService).signup(any(UserSignUpRequest.class));
    }
    @Test
    @WithMockUser
    @DisplayName("로그인 테스트")
    void login_test() throws Exception {
        String jsonContent = objectMapper.writeValueAsString(userSignupResponse);


        when(userService.login(any(UserLoginRequest.class))).thenReturn(userLoginResponse);
        String jsonResponseContent = objectMapper.writeValueAsString(userLoginResponse);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().json(jsonResponseContent))
            .andReturn();
        // 응답 내용을 확인
        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response Content: " + responseContent);
        verify(userService).login(any(UserLoginRequest.class));

    }
    @Test
    @WithMockUser
    @DisplayName("유저 검색 테스트")
    void search_test() throws Exception {
        //given


        List<UserSearchResponse> searchResponseList = Arrays.asList(
            new UserSearchResponse(userId, username, email)
        );
        String jsonResponseContent = objectMapper.writeValueAsString(searchResponseList);

        //when
        when(userService.searchUsersByEmail(anyString())).thenReturn(searchResponseList);

        //then
        String emailQueryParam = "test@test.com";
        MvcResult result = mockMvc.perform(get("/api/auth/search")
                .param("email", emailQueryParam)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().json(jsonResponseContent))
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response Content: " + responseContent);

        verify(userService).searchUsersByEmail(anyString());
    }
    @Test
    @WithMockUser
    @DisplayName("로그인한 상태 조회")
    void me_test() throws Exception{
        //when
        when(userService.me(any(HttpServletRequest.class))).thenReturn(userLoginResponse);
        String jsonResponseContent = objectMapper.writeValueAsString(userLoginResponse);
        //then
        MvcResult result = mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().json(jsonResponseContent))
            .andReturn();
        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Response Content: " + responseContent);

        verify(userService).me(any(HttpServletRequest.class));
    }
}
