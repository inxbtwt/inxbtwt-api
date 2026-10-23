package com.inxbtwt.infra.service.bucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.infra.config.s3.S3Properties;
import com.inxbtwt.infra.exception.bucket.BucketStorageException;
import com.inxbtwt.infra.exception.domain.DomainException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class S3ServiceTest {
    @Test
    void resolvesConfiguredBucketUrlAndRejectsForeignOrMalformedUrls() {
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(ResponseBytes.fromByteArray(
                GetObjectResponse.builder().contentType("image/png").build(), new byte[]{1}));
        var response = service.download("http://localhost:8333/inxbtwt-files/img/profile/photo/example");
        assertThat(response.key()).isEqualTo("img/profile/photo/example");
        verify(client).getObjectAsBytes(GetObjectRequest.builder().bucket("inxbtwt-files")
                .key("img/profile/photo/example").build());
        org.mockito.Mockito.clearInvocations(client);
        for (String url : java.util.List.of("http://example.com/inxbtwt-files/img/photo",
                "http://localhost:8333/other-bucket/photo", "http://localhost:8333/inxbtwt-files/",
                "http://localhost:8333/inxbtwt-files/photo?x=1", "http://[malformed")) {
            assertThatThrownBy(() -> service.download(url)).isInstanceOf(DomainException.class);
        }
        verifyNoInteractions(client);
    }

    @Test
    void missingObjectHasDedicatedNotFoundException() {
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(
                software.amazon.awssdk.services.s3.model.NoSuchKeyException.builder().statusCode(404).build());
        assertThatThrownBy(() -> service.download("img/missing"))
                .isInstanceOf(com.inxbtwt.infra.exception.bucket.BucketFileNotFoundException.class);
    }

    private final S3Client client = mock(S3Client.class);
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final S3Service service = new S3Service(client,
            new S3Properties(URI.create("http://localhost:8333"), "us-east-1", "test", "test", "inxbtwt-files"),
            factory.getValidator());

    @AfterEach
    void closeValidator() {
        factory.close();
    }

    @Test
    void uploadsWithVersionSevenIdentifierAndReturnsMatchingUrl() throws Exception {
        when(client.utilities()).thenReturn(S3Utilities.builder()
                .region(software.amazon.awssdk.regions.Region.US_EAST_1)
                .s3Configuration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                        .pathStyleAccessEnabled(true).build())
                .endpoint(URI.create("http://localhost:8333")).build());
        byte[] data = {1, 2, 3};
        var file = new MockMultipartFile("photo", "avatar.png", "image/png", data);

        var response = service.upload(new UploadFileRequest(file, "/img/profile/photo"));

        assertThat(response.key()).startsWith("img/profile/photo/");
        UUID id = UUID.fromString(response.key().substring("img/profile/photo/".length()));
        assertThat(id.version()).isEqualTo(7);
        assertThat(id.variant()).isEqualTo(2);
        assertThat(response.url()).isEqualTo("http://localhost:8333/inxbtwt-files/" + response.key());
        assertThat(response.fileName()).isEqualTo("avatar.png");
        assertThat(response.size()).isEqualTo(data.length);
        var put = ArgumentCaptor.forClass(PutObjectRequest.class);
        var body = ArgumentCaptor.forClass(RequestBody.class);
        verify(client).putObject(put.capture(), body.capture());
        assertThat(put.getValue().key()).isEqualTo(response.key());
        assertThat(put.getValue().bucket()).isEqualTo("inxbtwt-files");
        assertThat(put.getValue().contentType()).isEqualTo("image/png");
        try (var input = body.getValue().contentStreamProvider().newStream()) {
            assertThat(input.readAllBytes()).isEqualTo(data);
        }
    }

    @Test
    void downloadsOriginalBytesAndContentTypeByKey() {
        byte[] data = {4, 5, 6};
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(ResponseBytes.fromByteArray(
                GetObjectResponse.builder().contentType("image/png").build(), data));

        var response = service.download("img/profile/photo/test");

        assertThat(response.data()).isEqualTo(data);
        assertThat(response.contentType()).isEqualTo("image/png");
        assertThat(response.size()).isEqualTo(data.length);
        verify(client).getObjectAsBytes(GetObjectRequest.builder()
                .bucket("inxbtwt-files").key("img/profile/photo/test").build());
    }

    @Test
    void rejectsMissingOrEmptyFileAndMissingKeyBeforeCallingStorage() {
        assertThatThrownBy(() -> service.upload(new UploadFileRequest(null, "img/")))
                .isInstanceOf(DomainException.class).hasMessage("O arquivo é obrigatório.");
        assertThatThrownBy(() -> service.upload(new UploadFileRequest(
                new MockMultipartFile("photo", new byte[0]), "img/")))
                .isInstanceOf(DomainException.class).hasMessage("O arquivo não pode estar vazio.");
        assertThatThrownBy(() -> service.download(" ")).isInstanceOf(DomainException.class);
        verifyNoInteractions(client);
    }

    @Test
    void wrapsStorageFailureWithPublicMessageAndPreservesCause() {
        var failure = S3Exception.builder().statusCode(503).message("storage unavailable").build();
        when(client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.download("img/photo"))
                .isInstanceOf(BucketStorageException.class)
                .hasMessage("Não foi possível buscar o arquivo no bucket.").hasCause(failure);
    }
}
