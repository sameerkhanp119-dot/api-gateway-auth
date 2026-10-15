package com.skp.platform.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TokenEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void issuesTokenForValidClientAndScope() throws Exception {
        mvc.perform(post("/oauth2/token")
                        .with(httpBasic("order-portal", "portal-secret"))
                        .param("grant_type", "client_credentials")
                        .param("scope", "orders.read orders.write"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.scope").value("orders.read orders.write"));
    }

    @Test
    void rejectsScopeTheClientIsNotAllowed() throws Exception {
        mvc.perform(post("/oauth2/token")
                        .with(httpBasic("reporting-job", "reporting-secret"))
                        .param("grant_type", "client_credentials")
                        .param("scope", "orders.write"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_scope"));
    }

    @Test
    void rejectsWrongSecret() throws Exception {
        mvc.perform(post("/oauth2/token")
                        .with(httpBasic("order-portal", "wrong"))
                        .param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publishesSigningKeys() throws Exception {
        mvc.perform(get("/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"));
    }
}
