package club.ttg.dnd5.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

@Configuration
@RequiredArgsConstructor
public class S3HealthConfig
{
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.aws.s3.region}")
    private String region;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Bean
    public ApplicationRunner s3BucketHealthCheck()
    {
        return args ->
        {
            System.out.println("=== S3 HEALTH CHECK ===");
            System.out.println("endpoint = " + endpoint);
            System.out.println("region   = " + region);
            System.out.println("bucket   = " + bucket);

            try
            {
                var buckets = s3Client.listBuckets().buckets();
                System.out.println("Buckets visible to credentials: "
                        + buckets.stream().map(Bucket::name).toList());
            }
            catch (Exception ex)
            {
                System.out.println("listBuckets failed: "
                        + ex.getClass().getSimpleName() + " : " + ex.getMessage());
            }

            try
            {
                s3Client.headBucket(b -> b.bucket(bucket));
                System.out.println("headBucket OK");
            }
            catch (Exception ex)
            {
                System.out.println("headBucket failed: "
                        + ex.getClass().getSimpleName() + " : " + ex.getMessage());
            }

            System.out.println("=======================");
        };
    }
}
