package com.inxbtwt.domain.service.bucket;

import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.bucket.UploadFileRequest;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;

public interface BucketService {
    UploadFileResponse upload(UploadFileRequest request);

    /** Busca pela chave ou pela URL completa do bucket configurado. */
    DownloadFileResponse download(String keyOrUrl);
}
