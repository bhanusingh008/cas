package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CassandraAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CassandraAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsSerializationConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreConfiguration.class,
    CassandraAuthenticationConfiguration.class
}, properties = {
    "cas.authn.cassandra.table-name=users_table",
    "cas.authn.cassandra.local-dc=datacenter1",
    "cas.authn.cassandra.username-attribute=user_attr",
    "cas.authn.cassandra.password-attribute=pwd_attr",
    "cas.authn.cassandra.keyspace=cas",
    "cas.authn.cassandra.username=casuser",
    "cas.authn.cassandra.password=password",
    "cas.authn.cassandra.ssl-protocols=TLSv1.2",
    "cas.http-client.host-name-verifier=none"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Cassandra")
@EnabledIfListeningOnPort(port = 9042)
class CassandraAuthenticationHandlerTests {
    @Autowired
    @Qualifier("cassandraAuthenticationHandler")
    private AuthenticationHandler cassandraAuthenticationHandler;

    @Test
    void verifyUserNotFound() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("baduser", "Mellon");
        assertThrows(AccountNotFoundException.class, () -> cassandraAuthenticationHandler.authenticate(c, mock(Service.class)));
    }

    @Test
    void verifyUserBadPassword() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "bad");
        assertThrows(FailedLoginException.class, () -> cassandraAuthenticationHandler.authenticate(c, mock(Service.class)));
    }

    @Test
    void verifyUser() throws Exception {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = cassandraAuthenticationHandler.authenticate(c, mock(Service.class));
        assertNotNull(result);
        assertEquals("casuser", result.getPrincipal().getId());
    }
}
