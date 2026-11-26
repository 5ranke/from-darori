package com.daroring.fromdarori.domain.newsletter.mail;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration(proxyBeanMethods = false)
@Profile("newsletter-ses")
class SesNewsletterConfiguration {

    @Bean
    InstanceProfileCredentialsProvider sesCredentials() {
        return InstanceProfileCredentialsProvider.create();
    }

    @Bean
    SesV2Client sesClient(@Value("${aws.region}") String region, InstanceProfileCredentialsProvider credentials) {
        return SesV2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .overrideConfiguration(config -> config.apiCallTimeout(Duration.ofSeconds(30))
                        .retryStrategy(retries -> retries.maxAttempts(1)))
                .build();
    }

    @Bean
    NewsletterMailSender sesNewsletterMailSender(SesV2Client client,
                                                  @Value("${darori.newsletter.from}") String from,
                                                  @Value("${darori.newsletter.ses.test-recipient:}") String recipient,
                                                  @Value("${darori.newsletter.ses.production:false}") boolean production) {
        if (!production) {
            Assert.hasText(recipient, "SES sandbox requires a verified test recipient");
        }
        return new SesNewsletterMailSender(client, from, production ? null : recipient.strip());
    }
}
