package com.gana.security;

import com.gana.dto.SongRequest;
import com.gana.entity.Role;
import com.gana.entity.User;
import com.gana.repository.UserRepository;
import com.gana.service.SongService;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.DeniedController.class)
class SecurityIntegrationTest {

    private static final String PASSWORD = "Password123";
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    /** A GET under /api/songs/** is public, so a denial here comes from method security. */
    @RestController
    static class DeniedController {

        private final SongService songService;

        DeniedController(SongService songService) {
            this.songService = songService;
        }

        @GetMapping("/api/songs/method-security-probe")
        void probe() {
            songService.addSong(new SongRequest());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SongService songService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // --- URL rules ---

    @Test
    void publicEndpointsDoNotNeedAToken() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/api/songs")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/playlists")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/stream/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void endpointsWithoutARuleAreDeniedEvenWithAToken() throws Exception {
        String token = registerUser();

        mockMvc.perform(get("/api/unmapped").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminsCanAddSongs() throws Exception {
        String userToken = registerUser();
        String adminToken = jwtFor(createAccount(Role.ADMIN));
        String song = """
                {"title":"T","artist":"A","filePath":"track.mp3"}""";

        mockMvc.perform(post("/api/songs").contentType(MediaType.APPLICATION_JSON).content(song))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/songs").contentType(MediaType.APPLICATION_JSON).content(song)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/songs").contentType(MediaType.APPLICATION_JSON).content(song)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());
    }

    // --- Method security ---

    @Test
    @WithMockUser(roles = "USER")
    void addSongIsDeniedAtTheServiceLayerForNonAdmins() {
        assertThrows(AccessDeniedException.class, () -> songService.addSong(new SongRequest()));
    }

    @Test
    void methodSecurityDenialsAreNotReportedAsServerErrors() throws Exception {
        mockMvc.perform(get("/api/songs/method-security-probe"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/songs/method-security-probe")
                        .header(HttpHeaders.AUTHORIZATION, bearer(registerUser())))
                .andExpect(status().isForbidden());
    }

    // --- Tokens ---

    @Test
    void validTokenIsAccepted() throws Exception {
        mockMvc.perform(get("/api/playlists").header(HttpHeaders.AUTHORIZATION, bearer(registerUser())))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRevokesTheToken() throws Exception {
        String email = uniqueEmail();
        String token = register(email);

        mockMvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/playlists").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());

        String newToken = login(email);
        mockMvc.perform(get("/api/playlists").header(HttpHeaders.AUTHORIZATION, bearer(newToken)))
                .andExpect(status().isOk());
    }

    @Test
    void tokenWithoutAVersionIsRejected() throws Exception {
        User account = createAccount(Role.USER);
        String token = signedToken(account.getEmail(), jwtSecret, null, 60_000);

        assertUnauthorized(token);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        User account = createAccount(Role.USER);
        String forged = signedToken(account.getEmail(), "another-secret-another-secret-another-secret", 0, 60_000);

        assertUnauthorized(forged);
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        User account = createAccount(Role.USER);
        String expired = signedToken(account.getEmail(), jwtSecret, 0, -60_000);

        assertUnauthorized(expired);
    }

    @Test
    void unsignedTokenIsRejected() throws Exception {
        User account = createAccount(Role.USER);
        String unsigned = Jwts.builder().subject(account.getEmail()).issuer("gana-app").claim("ver", 0)
                .expiration(new Date(System.currentTimeMillis() + 60_000)).compact();

        assertUnauthorized(unsigned);
    }

    @Test
    void tokenOfADeletedAccountIsRejected() throws Exception {
        String email = uniqueEmail();
        String token = register(email);

        userRepository.delete(userRepository.findByEmail(email).orElseThrow());

        assertUnauthorized(token);
    }

    // --- Credential endpoints ---

    @Test
    void credentialEndpointsAreRateLimitedPerIp() throws Exception {
        String ip = uniqueIp();

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(loginRequest(uniqueEmail(), PASSWORD, ip))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(loginRequest(uniqueEmail(), PASSWORD, ip))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"));
        mockMvc.perform(registerRequest(uniqueEmail(), PASSWORD, ip))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(loginRequest(uniqueEmail(), PASSWORD, uniqueIp()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedFailuresLockOutThatIpAndAccountPair() throws Exception {
        String email = uniqueEmail();
        String ip = uniqueIp();
        register(email);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(loginRequest(email, "WrongPassword1", ip))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(loginRequest(email, PASSWORD, ip))
                .andExpect(status().isTooManyRequests());
        mockMvc.perform(loginRequest(email, PASSWORD, uniqueIp()))
                .andExpect(status().isOk());
    }

    @Test
    void loginDoesNotRevealWhetherAnAccountExists() throws Exception {
        String email = uniqueEmail();
        register(email);

        String wrongPassword = mockMvc.perform(loginRequest(email, "WrongPassword1", uniqueIp()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        String unknownAccount = mockMvc.perform(loginRequest(uniqueEmail(), PASSWORD, uniqueIp()))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertEquals(JsonPath.<String>read(wrongPassword, "$.message"), JsonPath.read(unknownAccount, "$.message"));
    }

    @Test
    void passwordsAreStoredAsBcryptHashes() throws Exception {
        String email = uniqueEmail();
        register(email);

        String stored = userRepository.findByEmail(email).orElseThrow().getPassword();

        assertTrue(stored.startsWith("$2"));
    }

    @Test
    void passwordOverBcryptsByteLimitIsRejected() throws Exception {
        // Passes the letter-and-digit rule, so only the length check can reject it.
        String seventyTwoCharsOver72Bytes = "a1" + "é".repeat(70);

        mockMvc.perform(registerRequest(uniqueEmail(), seventyTwoCharsOver72Bytes, uniqueIp()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void longMultibytePasswordAtLoginIsAPlainFailure() throws Exception {
        String email = uniqueEmail();
        register(email);

        mockMvc.perform(loginRequest(email, "é".repeat(72), uniqueIp()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientCannotChooseItsOwnRole() throws Exception {
        String email = uniqueEmail();
        String body = """
                {"fullName":"Test","email":"%s","password":"%s","role":"ADMIN"}""".formatted(email, PASSWORD);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(request -> {
                            request.setRemoteAddr(uniqueIp());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    // --- Playlists ---

    @Test
    void usersCannotReadEachOthersPlaylists() throws Exception {
        String ownerToken = registerUser();
        String otherToken = registerUser();

        String created = mockMvc.perform(post("/api/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mine\"}")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int playlistId = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/api/playlists/" + playlistId + "/songs")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/playlists/" + playlistId + "/songs")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    // --- Browser-facing behaviour ---

    @Test
    void corsOnlyAllowsTheConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/playlists")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        mockMvc.perform(options("/api/playlists")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void securityHeadersAreSent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", startsWith("default-src 'none'")));
    }

    // --- Helpers ---

    private void assertUnauthorized(String token) throws Exception {
        mockMvc.perform(get("/api/playlists").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    private String registerUser() throws Exception {
        return register(uniqueEmail());
    }

    private String register(String email) throws Exception {
        String body = mockMvc.perform(registerRequest(email, PASSWORD, uniqueIp()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(loginRequest(email, PASSWORD, uniqueIp()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String jwtFor(User account) throws Exception {
        return login(account.getEmail());
    }

    private User createAccount(Role role) {
        return userRepository.save(User.builder()
                .fullName("Test")
                .email(uniqueEmail())
                .password(passwordEncoder.encode(PASSWORD))
                .role(role)
                .build());
    }

    private String signedToken(String subject, String secret, Integer version, long expiresInMs) {
        var builder = Jwts.builder()
                .subject(subject)
                .issuer("gana-app")
                .expiration(new Date(System.currentTimeMillis() + expiresInMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)));
        if (version != null) {
            builder.claim("ver", version);
        }
        return builder.compact();
    }

    private static MockHttpServletRequestBuilder loginRequest(String email, String password, String ip) {
        return credentialRequest("/api/auth/login", """
                {"email":"%s","password":"%s"}""".formatted(email, password), ip);
    }

    private static MockHttpServletRequestBuilder registerRequest(String email, String password, String ip) {
        return credentialRequest("/api/auth/register", """
                {"fullName":"Test","email":"%s","password":"%s"}""".formatted(email, password), ip);
    }

    private static MockHttpServletRequestBuilder credentialRequest(String path, String body, String ip) {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                });
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String uniqueEmail() {
        return "user" + SEQUENCE.incrementAndGet() + "@example.com";
    }

    // Tests share one limiter, so each caller gets its own address.
    private static String uniqueIp() {
        int n = SEQUENCE.incrementAndGet();
        return "10.0." + (n >> 8) + "." + (n & 0xFF);
    }
}
