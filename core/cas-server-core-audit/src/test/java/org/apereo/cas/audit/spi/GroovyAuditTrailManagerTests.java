package org.apereo.cas.audit.spi;

import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This is {@link GroovyAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuditConfiguration.class
},
    properties = {
        "cas.audit.slf4j.enabled=false",
        "cas.audit.groovy.template.location=classpath:/GroovyAudit.groovy"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyAuditTrailManagerTests {

    @Autowired
    @Qualifier("filterAndDelegateAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Test
    void verifyOperation() {
        val ctx = new AuditActionContext("casuser",
            "TEST", "TEST", "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London"));
        auditTrailManager.record(ctx);
    }
}
