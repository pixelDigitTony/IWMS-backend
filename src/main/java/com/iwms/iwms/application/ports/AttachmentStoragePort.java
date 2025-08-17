package com.iwms.iwms.application.ports;

import java.net.URL;

public interface AttachmentStoragePort {
    URL createPresignedUploadUrl(String objectKey, String contentType, long ttlSeconds);
}


