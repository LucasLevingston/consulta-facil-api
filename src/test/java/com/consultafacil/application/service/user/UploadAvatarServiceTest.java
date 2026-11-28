package com.consultafacil.application.service.user;

import com.consultafacil.api.dto.user.UserResponseDTO;
import com.consultafacil.core.exception.ResourceNotFoundException;
import com.consultafacil.domain.entity.User;
import com.consultafacil.domain.enums.UserRole;
import com.consultafacil.domain.port.out.StoragePort;
import com.consultafacil.domain.port.out.user.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UploadAvatarServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock StoragePort storagePort;
    @Mock MultipartFile file;

    UploadAvatarService service;

    private static final String S3_URL =
            "https://my-bucket.s3.amazonaws.com/avatars/avatar-u-1.jpg";

    @BeforeEach
    void setUp() throws IOException {
        service = new UploadAvatarService(userRepository, storagePort, new UserMapper());

        when(file.getBytes()).thenReturn("content".getBytes());
        when(file.getOriginalFilename()).thenReturn("avatar-u-1.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(storagePort.upload(any(), eq("avatar-u-1.jpg"), eq("image/jpeg"), eq("avatars")))
                .thenReturn(S3_URL);
    }

    private User buildUserWithoutImage(String id) {
        return User.builder()
                .id(id)
                .name("Gabriela Torres")
                .email("gabi@email.com")
                .password("hashed")
                .role(UserRole.PATIENT)
                .build();
    }

    private User buildUserWithImage(String id) {
        User user = buildUserWithoutImage(id);
        user.updateAvatar("https://old-url.amazonaws.com/avatars/old.jpg", "avatars/old.jpg");
        return user;
    }

    @Test
    void execute_newAvatar_updatesImageUrlOnUser() throws IOException {
        User user = buildUserWithoutImage("u-1");
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute("u-1", file);

        assertThat(user.getImageUrl()).isEqualTo(S3_URL);
    }

    @Test
    void execute_newAvatar_extractsAndSetsImageId() throws IOException {
        User user = buildUserWithoutImage("u-1");
        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute("u-1", file);

        assertThat(user.getImageId()).isEqualTo("avatars/avatar-u-1.jpg");
    }

    @Test
    void execute_existingImage_deletesOldBeforeUpload() throws IOException {
        User user = buildUserWithImage("u-2");
        when(userRepository.findById("u-2")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute("u-2", file);

        verify(storagePort).delete("avatars/old.jpg");
    }

    @Test
    void execute_noExistingImage_doesNotCallDelete() throws IOException {
        User user = buildUserWithoutImage("u-3");
        when(userRepository.findById("u-3")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute("u-3", file);

        verify(storagePort, never()).delete(any());
    }

    @Test
    void execute_savesUserAfterUpload() throws IOException {
        User user = buildUserWithoutImage("u-4");
        when(userRepository.findById("u-4")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute("u-4", file);

        verify(userRepository).save(user);
    }

    @Test
    void execute_returnsMappedDTO() throws IOException {
        User user = buildUserWithoutImage("u-5");
        when(userRepository.findById("u-5")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserResponseDTO result = service.execute("u-5", file);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("u-5");
    }

    @Test
    void execute_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute("missing", file))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_storageThrowsIOException_wrapsAsRuntimeException() throws IOException {
        User user = buildUserWithoutImage("u-6");
        when(userRepository.findById("u-6")).thenReturn(Optional.of(user));
        when(file.getBytes()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> service.execute("u-6", file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload avatar");
    }
}
