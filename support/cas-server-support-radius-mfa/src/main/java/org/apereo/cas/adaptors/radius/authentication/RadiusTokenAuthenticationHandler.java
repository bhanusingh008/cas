package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.adaptors.radius.RadiusUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.jradius.dictionary.Attr_State;
import net.jradius.packet.attribute.value.AttributeValue;
import org.springframework.beans.factory.ObjectProvider;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link RadiusTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class RadiusTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private final List<RadiusServer> servers;

    private final boolean failoverOnException;

    private final boolean failoverOnAuthenticationFailure;

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;


    public RadiusTokenAuthenticationHandler(final String name,
                                            final ServicesManager servicesManager,
                                            final PrincipalFactory principalFactory,
                                            final List<RadiusServer> servers,
                                            final boolean failoverOnException,
                                            final boolean failoverOnAuthenticationFailure,
                                            final Integer order,
                                            final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(name, servicesManager, principalFactory, order);
        this.servers = servers;
        this.failoverOnException = failoverOnException;
        this.failoverOnAuthenticationFailure = failoverOnAuthenticationFailure;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;

        LOGGER.debug("Using [{}]", getClass().getSimpleName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return RadiusTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return RadiusTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws GeneralSecurityException {
        try {
            val radiusCredential = (RadiusTokenCredential) credential;
            val password = radiusCredential.getToken();

            val authentication = Objects.requireNonNull(WebUtils.getInProgressAuthentication(),
                "CAS has no reference to an authentication event to locate a principal");
            val principal = authentication.getPrincipal();
            val username = principal.getId();

            var state = Optional.empty();
            val attributes = principal.getAttributes();
            if (attributes.containsKey(Attr_State.NAME)) {
                LOGGER.debug("Found state attribute in principal attributes for multifactor authentication");
                val stateValue = CollectionUtils.firstElement(attributes.get(Attr_State.NAME));
                if (stateValue.isPresent()) {
                    val stateAttr = AttributeValue.class.cast(stateValue.get());
                    state = Optional.of(stateAttr.getValueObject());
                }
            }

            val result = RadiusUtils.authenticate(username, password, this.servers,
                this.failoverOnAuthenticationFailure, this.failoverOnException, state);
            if (result.getKey()) {
                val radiusAttributes = CollectionUtils.toMultiValuedMap(result.getValue().get());
                val finalPrincipal = this.principalFactory.createPrincipal(username, radiusAttributes);
                return createHandlerResult(credential, finalPrincipal, new ArrayList<>(0));
            }
            throw new FailedLoginException("Radius authentication failed for user " + username);
        } catch (final Exception e) {
            throw new FailedLoginException("Radius authentication failed " + e.getMessage());
        }
    }
}
