package com.example.learningApp.configuration;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.translate.TranslateClient;

@Configuration
public class AwsConfig {

    @Value("${aws.iam.access-key}")
    private String accessKey;

    @Value("${aws.iam.secret-key}")
    private String secretKey;

    @Value("${aws.iam-nam.access-key}")
    private String accessKeyNam;

    @Value("${aws.iam-nam.secret-key}")
    private String secretKeyNam;

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        System.out.println("AccessKeyNam: " + accessKeyNam);
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyNam, secretKeyNam)
                        )
                )
                .build();
    }

    @Bean
    public PollyClient pollyClient() {
        // Thay YOUR_ACCESS_KEY và YOUR_SECRET_KEY bằng key thực tế
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                accessKeyNam,
                secretKeyNam
        );

        return PollyClient.builder()
                .region(Region.US_EAST_1) // chọn region phù hợp
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
    @Bean
    public AmazonS3 amazonS3() {
        System.out.println("AccessKeyVy: " + accessKey);
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))   
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
    }
    @Bean
    public TranscribeClient transcribeClient() {
        return TranscribeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyNam, secretKeyNam)
                        )
                )
                .build();
    }
    @Bean
    public TranslateClient translateClient() {
        return TranslateClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyNam, secretKeyNam)
                        )
                )
                .build();
    }

}

