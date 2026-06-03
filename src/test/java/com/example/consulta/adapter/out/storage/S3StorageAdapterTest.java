package com.example.consulta.adapter.out.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    S3Client s3Client;

    S3StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new S3StorageAdapter(s3Client);
        ReflectionTestUtils.setField(adapter, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(adapter, "region", "us-east-1");
    }

    @Test
    void upload_shouldReturnPublicUrl() {
        var url = adapter.upload("content".getBytes(), "photo.jpg", "image/jpeg", "avatars");

        assertThat(url).startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/avatars/");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    void upload_shouldCallS3WithCorrectBucket() {
        adapter.upload("content".getBytes(), "photo.jpg", "image/jpeg", "avatars");

        var captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
    }

    @Test
    void upload_shouldCallS3WithCorrectContentType() {
        adapter.upload("content".getBytes(), "doc.pdf", "application/pdf", "docs");

        var captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().contentType()).isEqualTo("application/pdf");
    }

    @Test
    void upload_fileWithNoExtension_shouldNotAppendDot() {
        var url = adapter.upload("content".getBytes(), "noextension", "text/plain", "files");

        assertThat(url).doesNotContain(".noextension");
    }

    @Test
    void upload_nullFilename_shouldNotAppendExtension() {
        var url = adapter.upload("content".getBytes(), null, "text/plain", "files");

        assertThat(url).startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/files/");
    }

    @Test
    void delete_shouldCallS3WithCorrectKey() {
        adapter.delete("avatars/some-uuid.jpg");

        var captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo("avatars/some-uuid.jpg");
        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
    }

    @Test
    void delete_s3Throws_shouldNotPropagateException() {
        doThrow(S3Exception.builder().message("S3 error").build())
                .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        adapter.delete("avatars/some-uuid.jpg");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}
