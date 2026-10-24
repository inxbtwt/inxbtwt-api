package com.inxbtwt.domain.service.user.userBlob;

import com.inxbtwt.domain.dto.bucket.DownloadFileResponse;
import com.inxbtwt.domain.dto.bucket.UploadFileResponse;
import com.inxbtwt.domain.dto.user.http.UserPhotoUrlsResponse;
import com.inxbtwt.domain.enums.UserPhotoType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserBlobService {

    UserPhotoUrlsResponse getPhotoUrls(UUID userId);
    DownloadFileResponse downloadPhoto(UUID userId, String url);
    UploadFileResponse uploadPhoto(UUID userId, MultipartFile photo, UserPhotoType type);
}
