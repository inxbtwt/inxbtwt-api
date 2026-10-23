package com.inxbtwt;

import com.inxbtwt.infra.config.s3.S3Properties;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("dev")
class S3StorageIT {

    @Autowired
    S3Client s3;

    @Autowired
    S3Presigner presigner;

    @Autowired
    S3Properties properties;

    @Test
    void uploadsDownloadsAndReadsUsingPresignedUrl() throws Exception {
        String key = "integration-tests/" + UUID.randomUUID() + ".txt";
        byte[] content = "Arquivo de teste S3: olá!".getBytes(StandardCharsets.UTF_8);
        try {
            s3.putObject(request -> request.bucket(properties.bucket()).key(key)
                    .contentType("text/plain; charset=utf-8"), RequestBody.fromBytes(content));
            assertArrayEquals(content, s3.getObjectAsBytes(request ->
                    request.bucket(properties.bucket()).key(key)).asByteArray());

            var signed = presigner.presignGetObject(request -> request
                    .signatureDuration(Duration.ofMinutes(1))
                    .getObjectRequest(object -> object.bucket(properties.bucket()).key(key)));
            try (HttpClient http = HttpClient.newHttpClient()) {
                var response = http.send(HttpRequest.newBuilder(signed.url().toURI()).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                assertEquals(200, response.statusCode());
                assertArrayEquals(content, response.body());
            }
        } finally {
            s3.deleteObject(request -> request.bucket(properties.bucket()).key(key));
        }
    }
}
