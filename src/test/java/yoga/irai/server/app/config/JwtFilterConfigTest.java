package yoga.irai.server.app.config;

import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import yoga.irai.server.app.AppUtils;
import yoga.irai.server.authentication.entity.UserEntity;
import yoga.irai.server.authentication.entity.UserPrincipalEntity;
import yoga.irai.server.authentication.service.JwtService;
import yoga.irai.server.authentication.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;

class JwtFilterConfigTest {

    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private Jwt jwt;
    @Mock
    private UserEntity userEntity;
    @Mock
    private UserDetails userDetails;

    @Mock
    private UserPrincipalEntity userPrincipalEntity;

    @InjectMocks
    private JwtFilterConfig jwtFilterConfig;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        MockitoAnnotations.openMocks(this);
        jwtFilterConfig = new JwtFilterConfig(jwtDecoder, jwtService, userService);
        userEntity = UserEntity.builder()
                .userId(userId)
                .userFirstName("Hilton")
                .userLastName("Paul")
                .userEmail("hilton.p@terv.pro")
                .passwordHash("Hil@1234")
                .userType(AppUtils.UserType.PORTAL_USER)
                .userAoi("1:2;2:1;3:1")
                .build();
        userEntity.setCreatedBy(userId);
        userEntity.setUpdatedBy(userId);
        userDetails = new UserPrincipalEntity(userEntity);
        ReflectionTestUtils.setField(jwtFilterConfig, "issuerUrl", "https://keycloak.cloudops.terv.pro/auth/realms/terv-pro-realm");
    }

    @Test
    void testDoFilterInternal_KeycloakToken() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpYUNMaTNmNnltMXFuYUhmWEViWldBZ1FWWHNzQWVldGFleHBPSXpWRHRRIn0.eyJleHAiOjE3NjAwNzQ2NTIsImlhdCI6MTc1OTU1NjI1MiwiYXV0aF90aW1lIjoxNzU5NTU2MjUxLCJqdGkiOiJkZmEzZjBiMy1lN2JlLTQyMjktODBjMS1lZDY1Yzc5MTIwNmMiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmNsb3Vkb3BzLnRlcnYucHJvL2F1dGgvcmVhbG1zL3RlcnYtcHJvLXJlYWxtIiwic3ViIjoiOGEzYTBhNzgtNmYyZi00ZDMxLWE3ODEtODFmMzhhYWNkMGU5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiaXJhaS15b2dhLXYxLXNlcnZpY2UtYWNjb3VudC1jbGllbnQiLCJub25jZSI6IjI0NjgzNTBiLTE4OTctNDBmMy04NzMyLTY1ZjA0MDEyMGQyYyIsInNlc3Npb25fc3RhdGUiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJhY3IiOiIxIiwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSBncm91cHMiLCJzaWQiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibW9iaWxlIjoiKzkxODEyMjM0OTQzOSIsIm5hbWUiOiJKb3NodWEgRGFuIiwiZ3JvdXBzIjpbImNsb3Vkb3BzLWdyb3VwIiwiaXJhaS15b2dhLXYxLWdyb3VwIiwic2VuaW9yLWdyb3VwIl0sInByZWZlcnJlZF91c2VybmFtZSI6Impvc2h1YS5kQHRlcnYucHJvIiwiZ2l2ZW5fbmFtZSI6Ikpvc2h1YSIsImZhbWlseV9uYW1lIjoiRGFuIiwiZW1haWwiOiJqb3NodWEuZEB0ZXJ2LnBybyJ9.W4JfdRY7BsUSF7pGqosxxIqr6iqTorEuUev8eGbX3xqiJtElCX4SMpzZj69oh34PG0A5QUT9HdxS4_uLTkAqsNl4TmuP790gYZ8jolaasb3uMG33zIyCcXWx4ViH9Ee5XtKL80jVQ-Bal1jzQII25BMUIYYjmCtrjELwvewVn30UM8mxFcGsgzPaIQ_qHwxi3koeETbFluXJkjXaepIKY4_LTLttMQXv0CrN6OFqD8hTbK4mFj9xLeW7ZqV34kcj5pxWdVh-jghq--H9ePm7zU5RUKVpi_mDD1IGh2Z9on578dLbkBTHacUtLnnEtJgj_KgBhanXV--TJMd6dL0WKw");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("userSub");
        when(jwt.getClaimAsString("email")).thenReturn("user@email.com");
        when(userService.checkCredentials(anyString())).thenReturn(userEntity);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);

        jwtFilterConfig.doFilterInternal(request, response, filterChain);
    }

    @Test
    void testDoFilterInternal_KeycloakToken_EmptyJwt() throws ServletException, IOException {
        jwt = null;
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpYUNMaTNmNnltMXFuYUhmWEViWldBZ1FWWHNzQWVldGFleHBPSXpWRHRRIn0.eyJleHAiOjE3NjAwNzQ2NTIsImlhdCI6MTc1OTU1NjI1MiwiYXV0aF90aW1lIjoxNzU5NTU2MjUxLCJqdGkiOiJkZmEzZjBiMy1lN2JlLTQyMjktODBjMS1lZDY1Yzc5MTIwNmMiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmNsb3Vkb3BzLnRlcnYucHJvL2F1dGgvcmVhbG1zL3RlcnYtcHJvLXJlYWxtIiwic3ViIjoiOGEzYTBhNzgtNmYyZi00ZDMxLWE3ODEtODFmMzhhYWNkMGU5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiaXJhaS15b2dhLXYxLXNlcnZpY2UtYWNjb3VudC1jbGllbnQiLCJub25jZSI6IjI0NjgzNTBiLTE4OTctNDBmMy04NzMyLTY1ZjA0MDEyMGQyYyIsInNlc3Npb25fc3RhdGUiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJhY3IiOiIxIiwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSBncm91cHMiLCJzaWQiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibW9iaWxlIjoiKzkxODEyMjM0OTQzOSIsIm5hbWUiOiJKb3NodWEgRGFuIiwiZ3JvdXBzIjpbImNsb3Vkb3BzLWdyb3VwIiwiaXJhaS15b2dhLXYxLWdyb3VwIiwic2VuaW9yLWdyb3VwIl0sInByZWZlcnJlZF91c2VybmFtZSI6Impvc2h1YS5kQHRlcnYucHJvIiwiZ2l2ZW5fbmFtZSI6Ikpvc2h1YSIsImZhbWlseV9uYW1lIjoiRGFuIiwiZW1haWwiOiJqb3NodWEuZEB0ZXJ2LnBybyJ9.W4JfdRY7BsUSF7pGqosxxIqr6iqTorEuUev8eGbX3xqiJtElCX4SMpzZj69oh34PG0A5QUT9HdxS4_uLTkAqsNl4TmuP790gYZ8jolaasb3uMG33zIyCcXWx4ViH9Ee5XtKL80jVQ-Bal1jzQII25BMUIYYjmCtrjELwvewVn30UM8mxFcGsgzPaIQ_qHwxi3koeETbFluXJkjXaepIKY4_LTLttMQXv0CrN6OFqD8hTbK4mFj9xLeW7ZqV34kcj5pxWdVh-jghq--H9ePm7zU5RUKVpi_mDD1IGh2Z9on578dLbkBTHacUtLnnEtJgj_KgBhanXV--TJMd6dL0WKw");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
        verify(jwtDecoder, times(1)).decode(anyString());
    }

    @Test
    void testDoFilterInternal_KeycloakToken_withoutUser() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpYUNMaTNmNnltMXFuYUhmWEViWldBZ1FWWHNzQWVldGFleHBPSXpWRHRRIn0.eyJleHAiOjE3NjAwNzQ2NTIsImlhdCI6MTc1OTU1NjI1MiwiYXV0aF90aW1lIjoxNzU5NTU2MjUxLCJqdGkiOiJkZmEzZjBiMy1lN2JlLTQyMjktODBjMS1lZDY1Yzc5MTIwNmMiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmNsb3Vkb3BzLnRlcnYucHJvL2F1dGgvcmVhbG1zL3RlcnYtcHJvLXJlYWxtIiwic3ViIjoiOGEzYTBhNzgtNmYyZi00ZDMxLWE3ODEtODFmMzhhYWNkMGU5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiaXJhaS15b2dhLXYxLXNlcnZpY2UtYWNjb3VudC1jbGllbnQiLCJub25jZSI6IjI0NjgzNTBiLTE4OTctNDBmMy04NzMyLTY1ZjA0MDEyMGQyYyIsInNlc3Npb25fc3RhdGUiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJhY3IiOiIxIiwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSBncm91cHMiLCJzaWQiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibW9iaWxlIjoiKzkxODEyMjM0OTQzOSIsIm5hbWUiOiJKb3NodWEgRGFuIiwiZ3JvdXBzIjpbImNsb3Vkb3BzLWdyb3VwIiwiaXJhaS15b2dhLXYxLWdyb3VwIiwic2VuaW9yLWdyb3VwIl0sInByZWZlcnJlZF91c2VybmFtZSI6Impvc2h1YS5kQHRlcnYucHJvIiwiZ2l2ZW5fbmFtZSI6Ikpvc2h1YSIsImZhbWlseV9uYW1lIjoiRGFuIiwiZW1haWwiOiJqb3NodWEuZEB0ZXJ2LnBybyJ9.W4JfdRY7BsUSF7pGqosxxIqr6iqTorEuUev8eGbX3xqiJtElCX4SMpzZj69oh34PG0A5QUT9HdxS4_uLTkAqsNl4TmuP790gYZ8jolaasb3uMG33zIyCcXWx4ViH9Ee5XtKL80jVQ-Bal1jzQII25BMUIYYjmCtrjELwvewVn30UM8mxFcGsgzPaIQ_qHwxi3koeETbFluXJkjXaepIKY4_LTLttMQXv0CrN6OFqD8hTbK4mFj9xLeW7ZqV34kcj5pxWdVh-jghq--H9ePm7zU5RUKVpi_mDD1IGh2Z9on578dLbkBTHacUtLnnEtJgj_KgBhanXV--TJMd6dL0WKw");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn(null);
        when(jwt.getClaimAsString("email")).thenReturn(null);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
        verify(jwtDecoder, times(1)).decode(anyString());
    }

    @Test
    void testDoFilterInternal_KeycloakToken_User_withoutEntity() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpYUNMaTNmNnltMXFuYUhmWEViWldBZ1FWWHNzQWVldGFleHBPSXpWRHRRIn0.eyJleHAiOjE3NjAwNzQ2NTIsImlhdCI6MTc1OTU1NjI1MiwiYXV0aF90aW1lIjoxNzU5NTU2MjUxLCJqdGkiOiJkZmEzZjBiMy1lN2JlLTQyMjktODBjMS1lZDY1Yzc5MTIwNmMiLCJpc3MiOiJodHRwczovL2tleWNsb2FrLmNsb3Vkb3BzLnRlcnYucHJvL2F1dGgvcmVhbG1zL3RlcnYtcHJvLXJlYWxtIiwic3ViIjoiOGEzYTBhNzgtNmYyZi00ZDMxLWE3ODEtODFmMzhhYWNkMGU5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiaXJhaS15b2dhLXYxLXNlcnZpY2UtYWNjb3VudC1jbGllbnQiLCJub25jZSI6IjI0NjgzNTBiLTE4OTctNDBmMy04NzMyLTY1ZjA0MDEyMGQyYyIsInNlc3Npb25fc3RhdGUiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJhY3IiOiIxIiwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSBncm91cHMiLCJzaWQiOiJlMjRiYjIzYS1hNDVmLTRiZjItOTcwYS0yOGJhMDczYzRjODQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibW9iaWxlIjoiKzkxODEyMjM0OTQzOSIsIm5hbWUiOiJKb3NodWEgRGFuIiwiZ3JvdXBzIjpbImNsb3Vkb3BzLWdyb3VwIiwiaXJhaS15b2dhLXYxLWdyb3VwIiwic2VuaW9yLWdyb3VwIl0sInByZWZlcnJlZF91c2VybmFtZSI6Impvc2h1YS5kQHRlcnYucHJvIiwiZ2l2ZW5fbmFtZSI6Ikpvc2h1YSIsImZhbWlseV9uYW1lIjoiRGFuIiwiZW1haWwiOiJqb3NodWEuZEB0ZXJ2LnBybyJ9.W4JfdRY7BsUSF7pGqosxxIqr6iqTorEuUev8eGbX3xqiJtElCX4SMpzZj69oh34PG0A5QUT9HdxS4_uLTkAqsNl4TmuP790gYZ8jolaasb3uMG33zIyCcXWx4ViH9Ee5XtKL80jVQ-Bal1jzQII25BMUIYYjmCtrjELwvewVn30UM8mxFcGsgzPaIQ_qHwxi3koeETbFluXJkjXaepIKY4_LTLttMQXv0CrN6OFqD8hTbK4mFj9xLeW7ZqV34kcj5pxWdVh-jghq--H9ePm7zU5RUKVpi_mDD1IGh2Z9on578dLbkBTHacUtLnnEtJgj_KgBhanXV--TJMd6dL0WKw");
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
        when(jwt.getClaimAsString("sub")).thenReturn("userSub");
        when(jwt.getClaimAsString("email")).thenReturn("user@email.com");
        when(userService.createUserFromKeycloak(any())).thenReturn(userEntity);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);

        verify(jwtDecoder, times(1)).decode(anyString());
    }


    @Test
    void testDoFilterInternal_LocalToken() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhODY3MmE3MS0wNTE2LTQzYjMtYjExNS00ZjVlMTBiMTI4OTgiLCJmaXJzdE5hbWUiOiJQcmVtaml0aCIsImxhc3ROYW1lIjoiQmFsYWppIiwib3JnYW5pemF0aW9uSWQiOiJmMzQzNWVkNi1hODFmLTRhZjUtYmIwOS05ZWMyY2I2MjRjNjEiLCJsZXZlbCI6IlNZU1RFTSIsInJvbGUiOiJLRVlDTE9BS19VU0VSIiwiaWF0IjoxNzU5NTU2MTI0LCJleHAiOjE3NTk2NDI1MjR9.khQuNpsO9FD7u4jnVBmMPm9tIePzXSOwL-e9N_ugYb4TxYjbib3R9wnP3mvqUTIrvEhJ0uvA8TxITjKrUXHwzkzQmXjmeL8GBQCroLWjfLgiTLueEaErrWV1Ojf5vNRAcrbQZBjcWbdjGxkkykn8NQH5isBN1RMS3qux1qBoYclH8CA8AD9KokaQ4hkXku2Rrw-CmEUOuteMjZpmB0g3hP6Xv0r8c2exNbCt0sMKsE3C2r7XrBbvWz4pqbzffVaBfHykYFmzHtKF9jQym0oP_F8G7D5lRtrKdrZXW7yHQh0UA_zWwNFxhrY2Oq4LGlVCHd8hfcjOqwiHgY9RSJqQqA");
        when(jwtService.extractUsername(anyString())).thenReturn("localUser");
        when(userService.loadUserByUsername(anyString())).thenReturn(userPrincipalEntity);
        when(jwtService.validateToken(anyString(), any())).thenReturn(true);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
    }

    @Test
    void testDoFilterInternal_LocalToken_fail() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhODY3MmE3MS0wNTE2LTQzYjMtYjExNS00ZjVlMTBiMTI4OTgiLCJmaXJzdE5hbWUiOiJQcmVtaml0aCIsImxhc3ROYW1lIjoiQmFsYWppIiwib3JnYW5pemF0aW9uSWQiOiJmMzQzNWVkNi1hODFmLTRhZjUtYmIwOS05ZWMyY2I2MjRjNjEiLCJsZXZlbCI6IlNZU1RFTSIsInJvbGUiOiJLRVlDTE9BS19VU0VSIiwiaWF0IjoxNzU5NTU2MTI0LCJleHAiOjE3NTk2NDI1MjR9.khQuNpsO9FD7u4jnVBmMPm9tIePzXSOwL-e9N_ugYb4TxYjbib3R9wnP3mvqUTIrvEhJ0uvA8TxITjKrUXHwzkzQmXjmeL8GBQCroLWjfLgiTLueEaErrWV1Ojf5vNRAcrbQZBjcWbdjGxkkykn8NQH5isBN1RMS3qux1qBoYclH8CA8AD9KokaQ4hkXku2Rrw-CmEUOuteMjZpmB0g3hP6Xv0r8c2exNbCt0sMKsE3C2r7XrBbvWz4pqbzffVaBfHykYFmzHtKF9jQym0oP_F8G7D5lRtrKdrZXW7yHQh0UA_zWwNFxhrY2Oq4LGlVCHd8hfcjOqwiHgY9RSJqQqA");
        when(jwtService.extractUsername(anyString())).thenReturn("localUser");
        when(userService.loadUserByUsername(anyString())).thenReturn(userPrincipalEntity);
        when(jwtService.validateToken(anyString(), any())).thenReturn(false);
        when(userService.loadUserByUsername(anyString())).thenReturn(userDetails);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
    }

    @Test
    void testDoFilterInternal_LocalToken_WithoutUserName() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhODY3MmE3MS0wNTE2LTQzYjMtYjExNS00ZjVlMTBiMTI4OTgiLCJmaXJzdE5hbWUiOiJQcmVtaml0aCIsImxhc3ROYW1lIjoiQmFsYWppIiwib3JnYW5pemF0aW9uSWQiOiJmMzQzNWVkNi1hODFmLTRhZjUtYmIwOS05ZWMyY2I2MjRjNjEiLCJsZXZlbCI6IlNZU1RFTSIsInJvbGUiOiJLRVlDTE9BS19VU0VSIiwiaWF0IjoxNzU5NTU2MTI0LCJleHAiOjE3NTk2NDI1MjR9.khQuNpsO9FD7u4jnVBmMPm9tIePzXSOwL-e9N_ugYb4TxYjbib3R9wnP3mvqUTIrvEhJ0uvA8TxITjKrUXHwzkzQmXjmeL8GBQCroLWjfLgiTLueEaErrWV1Ojf5vNRAcrbQZBjcWbdjGxkkykn8NQH5isBN1RMS3qux1qBoYclH8CA8AD9KokaQ4hkXku2Rrw-CmEUOuteMjZpmB0g3hP6Xv0r8c2exNbCt0sMKsE3C2r7XrBbvWz4pqbzffVaBfHykYFmzHtKF9jQym0oP_F8G7D5lRtrKdrZXW7yHQh0UA_zWwNFxhrY2Oq4LGlVCHd8hfcjOqwiHgY9RSJqQqA");
        when(jwtService.extractUsername(anyString())).thenReturn(null);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
        verify(jwtService, times(1)).extractUsername(anyString());
    }

    @Test
    void testDoFilterInternal_NoAuthHeader() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn(null);
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoAuthHeader_WithoutBearer() throws ServletException, IOException {
        when(request.getHeader(anyString())).thenReturn("withoutBearer");
        jwtFilterConfig.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }
}