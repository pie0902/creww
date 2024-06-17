package org.example.creww.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.example.creww.global.globalException.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;
    private String Secret_key;
    private Long userId;
    private HttpServletRequest request;
    @BeforeEach
    public void setUp() {
        // JWT_SECRET 값을 설정합니다.
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "testSecret");
        Secret_key = "testTokenSecretKey";
        jwtUtils.init();
        userId = 12345L;
        request = mock(HttpServletRequest.class);
    }

    @Test
    public void testGenerateToken() {
        // 토큰 생성
        String token = jwtUtils.generateToken(userId);

        // 토큰이 null이 아닌지 확인
        assertNotNull(token);
    }
    @Test
    @DisplayName("토큰 검증 성공 테스트")
    public void validateTokenTrue_test() {
        //given
        String token = jwtUtils.generateToken(userId);
        //when
        Boolean test = jwtUtils.validateToken(token);

        //then
        assertEquals(test,true);
    }
    @Test
    @DisplayName("토큰 검증 실패 테스트")
    public void validateTokenFalse_test() {
        //given
        String token = "falseToken";
        //when
        Boolean test = jwtUtils.validateToken(token);

        //then
        assertEquals(test,false);
    }
    @Test
    @DisplayName("토큰 유저 id값 가져오기 테스트")
    public void getUserIdFromToken_test() {
        //given
        String token = jwtUtils.generateToken(userId);
        //when
        String result = String.valueOf(jwtUtils.getUserIdFromToken(token));
        String StrUserId = String.valueOf(userId);
        //then
        assertEquals(result,StrUserId);
    }
    @Test
    @DisplayName("토큰 Http request 값 가져오기 테스트")
    public void getTokenFromRequest_test() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String bearerToken = "Bearer validToken";
        when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // when
        String result = jwtUtils.getTokenFromRequest(request);

        // then
        assertEquals("validToken", result);
    }
    @Test
    @DisplayName("토큰 Http request 값 가져오기 테스트 - 유효하지 않은 토큰")
    public void getTokenFromRequest_invalidToken_test() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        String result = jwtUtils.getTokenFromRequest(request);

        // then
        assertEquals(null, result);
    }

    @Test
    @DisplayName("토큰 Http request 값 가져오기 테스트 - Bearer 접두사 없음")
    public void getTokenFromRequest_noBearerPrefix_test() {
        // given

        String bearerToken = "InvalidPrefix validToken";
        when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // when
        String result = jwtUtils.getTokenFromRequest(request);

        // then
        assertEquals(null, result);
    }
    @Test
    @DisplayName("유효한 토큰 테스트")
    public void validateTokenOrThrow_validToken_test() {
        // given
        String token = jwtUtils.generateToken(userId);
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn("Bearer " + token);

        // when & then
        assertDoesNotThrow(() -> jwtUtils.validateTokenOrThrow(mockRequest));
    }
    @Test
    @DisplayName("유효하지 않은 토큰 테스트 - null 토큰")
    public void validateTokenOrThrow_nullToken_test() {
        // given
        String token = null;
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getHeader("Authorization")).thenReturn(token);

        // when & then
        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            jwtUtils.validateTokenOrThrow(mockRequest);
        });
        assertEquals("Invalid token", exception.getMessage());
    }
}
