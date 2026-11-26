package com.daroring.fromdarori.domain.story.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration(proxyBeanMethods = false)
@Profile("story-images-s3")
class S3StoryImageConfiguration {

    @Bean
    S3Client storyImageS3Client(@Value("${darori.story-images.s3.region}") String region) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
                .overrideConfiguration(config -> config.apiCallTimeout(Duration.ofSeconds(30)))
                .build();
    }
}
