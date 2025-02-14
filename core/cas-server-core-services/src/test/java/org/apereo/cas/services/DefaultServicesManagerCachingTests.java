package org.apereo.cas.services;

import org.apereo.cas.services.mgmt.DefaultServicesManager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultServicesManagerCachingTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("RegisteredService")
class DefaultServicesManagerCachingTests {

    @Test
    void verifyServicesCache() throws Exception {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val cache = Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .recordStats()
            .build();

        val context = ServicesManagerConfigurationContext.builder()
            .applicationContext(applicationContext)
            .registeredServicesTemplatesManager(mock(RegisteredServicesTemplatesManager.class))
            .serviceRegistry(new InMemoryServiceRegistry(applicationContext, List.of(service1), List.of()))
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .servicesCache((Cache) cache)
            .build();

        val mgr = new DefaultServicesManager(context);
        assertFalse(mgr.getAllServices().isEmpty());

        assertEquals(1, mgr.load().size());
        assertEquals(1, mgr.getAllServices().size());

        Thread.sleep(1500);
        assertFalse(mgr.getAllServices().isEmpty());

        assertEquals(1, mgr.load().size());
        assertEquals(1, mgr.getAllServices().size());
    }
}
