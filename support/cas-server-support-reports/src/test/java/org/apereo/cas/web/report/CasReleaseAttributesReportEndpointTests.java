package org.apereo.cas.web.report;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasReleaseAttributesReportEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = "management.endpoint.releaseAttributes.enabled=true")
@Tag("ActuatorEndpoint")
class CasReleaseAttributesReportEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("releaseAttributesReportEndpoint")
    private CasReleaseAttributesReportEndpoint endpoint;

    private RegisteredService registeredService;

    @BeforeEach
    public void setup() {
        registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
    }

    @ParameterizedTest
    @ValueSource(strings = "casuser")
    @NullAndEmptySource
    public void verifyOperation(final String password) {
        val response = endpoint.releasePrincipalAttributes("casuser", password, registeredService.getServiceId());
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertNotNull(endpoint.releaseAttributes("casuser", password, registeredService.getServiceId()));
    }
}

