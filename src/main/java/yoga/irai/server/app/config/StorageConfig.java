package yoga.irai.server.app.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class StorageConfig {

    @Value("${digitalocean.spaces.endpoint}")
    private String endpoint;

    @Value("${digitalocean.spaces.access-key}")
    private String accessKey;

    @Value("${digitalocean.spaces.secret-key}")
    private String secretKey;

    /**
     * Creates an S3Client bean configured for DigitalOcean Spaces. * * @return
     * S3Client instance configured with the specified endpoint and credentials.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder().endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.AP_SOUTH_2)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();
    }
}
