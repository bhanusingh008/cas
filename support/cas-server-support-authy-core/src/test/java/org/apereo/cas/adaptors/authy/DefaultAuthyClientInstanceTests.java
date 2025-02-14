package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.model.support.mfa.AuthyMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;

import com.authy.AuthyApiClient;
import com.authy.api.Error;
import com.authy.api.User;
import com.authy.api.Users;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthyClientInstanceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("MFAProvider")
class DefaultAuthyClientInstanceTests {
    @Test
    void verifyAction() throws Exception {
        val properties = new AuthyMultifactorAuthenticationProperties()
            .setCountryCode("1")
            .setApiKey("nfg734dbdv10fn$#")
            .setApiUrl(AuthyApiClient.DEFAULT_API_URI)
            .setMailAttribute("mail")
            .setPhoneAttribute("phone");

        val authyUser = mock(User.class);
        when(authyUser.getId()).thenReturn(1);
        when(authyUser.getStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());

        val authyUsers = mock(Users.class);
        when(authyUsers.createUser(anyString(), anyString(), anyString())).thenReturn(authyUser);

        val authy = mock(AuthyApiClient.class);
        when(authy.getUsers()).thenReturn(authyUsers);

        val client = new DefaultAuthyClientInstance(authy, properties);
        val user = client.getOrCreateUser(CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("mail", List.of("casuser@example.org"), "phone", List.of("123-456-6789"))));
        assertNotNull(user);
        assertTrue(user.getId() > 0);
        assertTrue(HttpStatus.valueOf(user.getStatus()).isError());

        val error = new Error();
        error.setCountryCode("1");
        error.setMessage("Error");
        error.setUrl("http://app.example.org");
        val msg = AuthyClientInstance.getErrorMessage(error);
        assertNotNull(msg);
    }
}
