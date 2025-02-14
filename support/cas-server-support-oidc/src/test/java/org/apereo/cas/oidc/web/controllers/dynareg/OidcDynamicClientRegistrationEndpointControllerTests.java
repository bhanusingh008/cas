package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationResponse;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDynamicClientRegistrationEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.registration.client-secret-expiration=P14D")
class OidcDynamicClientRegistrationEndpointControllerTests extends AbstractOidcTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("oidcDynamicClientRegistrationEndpointController")
    protected OidcDynamicClientRegistrationEndpointController controller;

    @Test
    void verifyBadEndpointRequest() throws Exception {
        val request = getHttpRequestForEndpoint("unknown/issuer");
        request.setRequestURI("unknown/issuer");
        val response = new MockHttpServletResponse();
        val mv = controller.handleRequestInternal(StringUtils.EMPTY, request, response);
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, mv.getStatusCode());
    }

    @Test
    void verifyBadInput() throws Exception {
        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();
        assertEquals(HttpStatus.SC_BAD_REQUEST, controller.handleRequestInternal("bad-input", request, response).getStatusCode().value());
    }

    @Test
    void verifyBadRedirect() throws Exception {
        val registrationReq = '{'
                              + "   \"redirect_uris\":"
                              + "     [\"https://client.example.org/callback#something\","
                              + "      \"https://client.example.org/callback2\"],"
                              + "   \"request_uris\":"
                              + "     [\"https://client.example.org/rf.txt#qpXaRLh_n93TTR9F252ValdatUQvQiJi5BDub2BeznA\"]"
                              + "  }";

        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();
        assertEquals(HttpStatus.SC_BAD_REQUEST, controller.handleRequestInternal(registrationReq, request, response).getStatusCode().value());
    }

    @Test
    void verifyOperation() throws Exception {
        val registrationReq = '{'
                              + "   \"application_type\": \"web\","
                              + "   \"default_acr_values\":"
                              + "     [\"mfa-duo\",\"mfa-gauth\"],"
                              + "   \"redirect_uris\":"
                              + "     [\"https://client.example.org/callback\","
                              + "      \"https://client.example.org/callback2\"],"
                              + "   \"client_name\": \"My Example\","
                              + "   \"client_name#ja-Japan-JP\":"
                              + "     \"Japanese\","
                              + "   \"logo_uri\": \"https://client.example.org/logo.png\","
                              + "   \"policy_uri\": \"https://client.example.org/policy\","
                              + "   \"tos_uri\": \"https://client.example.org/tos\","
                              + "   \"subject_type\": \"pairwise\","
                              + "   \"sector_identifier_uri\":"
                              + "     \"http://localhost:7711\","
                              + "   \"token_endpoint_auth_method\": \"client_secret_basic\","
                              + "   \"jwks_uri\": \"https://client.example.org/my_public_keys.jwks\","
                              + "   \"id_token_signed_response_alg\": \"RS256\","
                              + "   \"id_token_encrypted_response_alg\": \"RSA1_5\","
                              + "   \"id_token_encrypted_response_enc\": \"A128CBC-HS256\","
                              + "   \"userinfo_encrypted_response_alg\": \"RSA1_5\","
                              + "   \"userinfo_encrypted_response_enc\": \"A128CBC-HS256\","
                              + "   \"contacts\": [\"ve7jtb@example.org\", \"mary@example.org\"]"
                              + "  }";

        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();

        val entity = MAPPER.writeValueAsString(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
        try (val webServer = new MockWebServer(7711,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), org.springframework.http.HttpStatus.OK)) {
            webServer.start();
            val responseEntity = (ResponseEntity<OidcClientRegistrationResponse>) controller.handleRequestInternal(registrationReq, request, response);
            assertEquals(HttpStatus.SC_CREATED, responseEntity.getStatusCode().value());
            assertTrue(responseEntity.getBody().getClientIdIssuedAt() > 0);
        }
    }

    @Test
    void verifyNoClientNameOperation() throws Exception {
        val registrationReq = '{'
                              + "   \"application_type\": \"web\","
                              + "   \"default_acr_values\":"
                              + "     [\"mfa-duo\",\"mfa-gauth\"],"
                              + "   \"redirect_uris\":"
                              + "     [\"https://client.example.org/callback\","
                              + "      \"https://client.example.org/callback2\"],"
                              + "   \"client_name#ja-Japan-JP\":"
                              + "     \"Japanese\","
                              + "   \"logo_uri\": \"https://client.example.org/logo.png\","
                              + "   \"policy_uri\": \"https://client.example.org/policy\","
                              + "   \"tos_uri\": \"https://client.example.org/tos\","
                              + "   \"subject_type\": \"pairwise\","
                              + "   \"sector_identifier_uri\":"
                              + "     \"http://localhost:7711\","
                              + "   \"token_endpoint_auth_method\": \"client_secret_basic\","
                              + "   \"jwks\": {\"keys\":[{}]},"
                              + "   \"id_token_signed_response_alg\": \"RS256\","
                              + "   \"id_token_encrypted_response_alg\": \"RSA1_5\","
                              + "   \"id_token_encrypted_response_enc\": \"A128CBC-HS256\","
                              + "   \"userinfo_encrypted_response_alg\": \"RSA1_5\","
                              + "   \"contacts\": [\"ve7jtb@example.org\", \"mary@example.org\"]"
                              + "  }";

        val request = getHttpRequestForEndpoint(OidcConstants.REGISTRATION_URL);
        val response = new MockHttpServletResponse();

        val entity = MAPPER.writeValueAsString(List.of("https://client.example.org/callback", "https://client.example.org/callback2"));
        try (val webServer = new MockWebServer(7711,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), org.springframework.http.HttpStatus.OK)) {
            webServer.start();
            assertEquals(HttpStatus.SC_CREATED,
                controller.handleRequestInternal(registrationReq, request, response).getStatusCode().value());
        }
    }
}
