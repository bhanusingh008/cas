package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.CasBanner;

import lombok.NoArgsConstructor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasWebApplication} that houses the main method.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(proxyBeanMethods = false, exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@NoArgsConstructor
public class CasWebApplication {

    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        CasEmbeddedContainerUtils.getApplicationEntrypointInitializers().forEach(init -> init.initialize(args));
        new SpringApplicationBuilder(CasWebApplication.class)
            .banner(CasBanner.getInstance())
            .web(WebApplicationType.SERVLET)
            .logStartupInfo(true)
            .applicationStartup(CasEmbeddedContainerUtils.getApplicationStartup())
            .run(args);
    }

}
