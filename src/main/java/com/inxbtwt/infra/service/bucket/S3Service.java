package com.inxbtwt.infra.service.bucket;

import com.github.f4b6a3.uuid.UuidCreator;
import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.domain.service.bucket.BucketService;
import com.inxbtwt.infra.config.s3.S3Properties;
import com.inxbtwt.infra.exception.bucket.BucketStorageException;
import com.inxbtwt.infra.exception.domain.DomainException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.net.URI;
import com.inxbtwt.infra.exception.bucket.BucketFileNotFoundException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Profile("dev|local")
@RequiredArgsConstructor
public class S3Service implements BucketService {

    private final S3Client s3Client;
    private final S3Properties properties;
    private final Validator validator;

    @Override
    public UploadFileResponse upload(UploadFileRequest request) {
        if (request == null) {
            throw new DomainException("Os dados do envio são obrigatórios.");
        }
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new DomainException(violations.iterator().next().getMessage());
        }
        if (request.file().isEmpty()) {
            throw new DomainException("O arquivo não pode estar vazio.");
        }

        String path = request.path().strip();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isBlank()) {
            throw new DomainException("O caminho de destino no bucket é obrigatório.");
        }
        String key = path + (path.endsWith("/") ? "" : "/") + UuidCreator.getTimeOrderedEpoch();
        String contentType = request.file().getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        try (var stream = request.file().getInputStream()) {
            String url = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(properties.bucket()).key(key).build()).toExternalForm();
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(properties.bucket()).key(key).contentType(contentType).build(),
                    RequestBody.fromInputStream(stream, request.file().getSize()));
            return new UploadFileResponse(properties.bucket(), key, url,
                    request.file().getOriginalFilename(), contentType, request.file().getSize());
        } catch (IOException | SdkException exception) {
            throw new BucketStorageException("Não foi possível enviar o arquivo ao bucket.", exception);
        }
    }

    @Override
    public DownloadFileResponse download(String keyOrUrl) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) {
            throw new DomainException("A chave do arquivo no bucket é obrigatória.");
        }
        String key = resolveKey(keyOrUrl);
        try {
            var object = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(properties.bucket()).key(key).build());
            byte[] data = object.asByteArray();
            String contentType = object.response().contentType();
            return new DownloadFileResponse(key, data,
                    contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType, data.length);
        } catch (NoSuchKeyException exception) {
            throw new BucketFileNotFoundException(exception);
        } catch (SdkException exception) {
            throw new BucketStorageException("Não foi possível buscar o arquivo no bucket.", exception);
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(properties.bucket()).key(resolveKey(key)).build());
        } catch (SdkException exception) {
            throw new BucketStorageException("Não foi possível remover o arquivo do bucket.", exception);
        }
    }

    private String resolveKey(String keyOrUrl) {
        if (!keyOrUrl.contains("://")) {
            return keyOrUrl;
        }
        try {
            URI url = URI.create(keyOrUrl);
            URI endpoint = properties.endpoint();
            String endpointPath = endpoint.getPath() == null ? "" : endpoint.getPath();
            if (endpointPath.endsWith("/")) {
                endpointPath = endpointPath.substring(0, endpointPath.length() - 1);
            }
            String prefix = endpointPath + "/" + properties.bucket() + "/";
            if (!endpoint.getScheme().equalsIgnoreCase(url.getScheme())
                    || !endpoint.getRawAuthority().equalsIgnoreCase(url.getRawAuthority())
                    || url.getRawQuery() != null || url.getRawFragment() != null
                    || url.getPath() == null || !url.getPath().startsWith(prefix)
                    || url.getPath().length() == prefix.length()) {
                throw new IllegalArgumentException();
            }
            return url.getPath().substring(prefix.length());
        } catch (IllegalArgumentException exception) {
            throw new DomainException("A URL da foto deve pertencer ao bucket configurado.");
        }
    }
}
