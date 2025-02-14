package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.DefaultServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link MultiTimeUseOrTimeoutExpirationPolicy}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("ExpirationPolicy")
class MultiTimeUseOrTimeoutExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "multiTimeUseOrTimeoutExpirationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final long TIMEOUT_SECONDS = 1;

    private static final int NUMBER_OF_USES = 5;

    private MultiTimeUseOrTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticket;

    private static ServiceTicketSessionTrackingPolicy getTrackingPolicy() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getCore().setOnlyTrackMostRecentSession(true);
        return new DefaultServiceTicketSessionTrackingPolicy(props, new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog()));
    }

    @BeforeEach
    public void initialize() {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT_SECONDS);
        this.ticket = new TicketGrantingTicketImpl("test", CoreAuthenticationTestUtils.getAuthentication(), this.expirationPolicy);
    }

    @Test
    void verifyTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    @Test
    void verifyTicketIsNotExpired() {
        this.expirationPolicy.setClock(Clock.fixed(this.ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT_SECONDS).minusNanos(1), ZoneOffset.UTC));
        assertFalse(this.ticket.isExpired());
        assertEquals(0, this.expirationPolicy.getTimeToIdle());
    }

    @Test
    void verifyTicketIsExpiredByTime() {
        this.expirationPolicy.setClock(Clock.fixed(this.ticket.getCreationTime().toInstant().plusSeconds(TIMEOUT_SECONDS).plusNanos(1), ZoneOffset.UTC));
        assertTrue(this.ticket.isExpired());
    }

    @Test
    void verifyTicketIsExpiredByCount() {
        IntStream.range(0, NUMBER_OF_USES)
            .forEach(i -> this.ticket.grantServiceTicket("test", RegisteredServiceTestUtils.getService(),
                NeverExpiresExpirationPolicy.INSTANCE, false, getTrackingPolicy()));
        assertTrue(this.ticket.isExpired());
    }

    @Test
    void verifySerializeATimeoutExpirationPolicyToJson() throws IOException {
        MAPPER.writeValue(JSON_FILE, expirationPolicy);
        val policyRead = MAPPER.readValue(JSON_FILE, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }

    @Test
    void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, MultiTimeUseOrTimeoutExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
