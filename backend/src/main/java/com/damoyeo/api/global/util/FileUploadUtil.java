package com.damoyeo.api.global.util;

import com.damoyeo.api.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 파일 업로드 유틸리티
 *
 * 파일 업로드 관련 공통 로직을 처리합니다.
 * - 파일 저장
 * - 파일 삭제
 * - 파일 유효성 검사 (확장자, 크기)
 *
 * [저장 경로]
 * application.properties의 com.damoyeo.upload.path 설정 사용
 */
@Component
@Slf4j
public class FileUploadUtil {

    @Value("${com.damoyeo.upload.path}")
    private String uploadPath;

    /** 허용되는 이미지 확장자 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /** 최대 파일 크기 (10MB) */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 프로필 이미지 업로드
     *
     * @param file 업로드할 파일
     * @return 저장된 파일의 URL 경로
     */
    public String uploadProfileImage(MultipartFile file) {
        validateImageFile(file);

        String savedFileName = saveFile(file, "profiles");
        return "/uploads/profiles/" + savedFileName;
    }

    /**
     * 모임 커버 이미지 업로드
     */
    public String uploadGroupImage(MultipartFile file) {
        validateImageFile(file);

        String savedFileName = saveFile(file, "groups");
        return "/uploads/groups/" + savedFileName;
    }

    /**
     * 갤러리 이미지 업로드
     *
     * [저장 경로]
     * /uploads/gallery/{groupId}/{UUID}.{확장자}
     *
     * @param file 업로드할 파일
     * @param groupId 모임 ID (폴더 구분용)
     * @return 저장된 파일의 URL 경로
     */
    public String uploadGalleryImage(MultipartFile file, Long groupId) {
        validateImageFile(file);

        String subDir = "gallery/" + groupId;
        String savedFileName = saveFile(file, subDir);
        return "/uploads/" + subDir + "/" + savedFileName;
    }

    /**
     * 게시판 이미지 업로드
     *
     * <p>저장 경로: /uploads/board/{groupId}/{UUID}.{확장자}</p>
     *
     * @param file    업로드할 파일
     * @param groupId 모임 ID (폴더 구분용)
     * @return 저장된 파일의 URL 경로
     */
    public String uploadBoardImage(MultipartFile file, Long groupId) {
        validateImageFile(file);

        String subDir = "board/" + groupId;
        String savedFileName = saveFile(file, subDir);
        return "/uploads/" + subDir + "/" + savedFileName;
    }

    /**
     * 파일 저장
     *
     * @param file 업로드할 파일
     * @param subDir 하위 디렉토리 (profiles, groups 등)
     * @return 저장된 파일명 (UUID + 확장자)
     */
    private String saveFile(MultipartFile file, String subDir) {
        try {
            // 저장 디렉토리 생성
            Path uploadDir = Paths.get(uploadPath, subDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 고유 파일명 생성 (UUID + 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String savedFileName = UUID.randomUUID().toString() + "." + extension;

            // 파일 저장
            Path filePath = uploadDir.resolve(savedFileName);
            Files.copy(file.getInputStream(), filePath);

            log.info("File uploaded: {}", filePath);
            return savedFileName;

        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new CustomException("파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 파일 삭제
     *
     * @param fileUrl 파일 URL (예: /uploads/profiles/xxx.jpg)
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // URL에서 파일 경로 추출
            String relativePath = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadPath, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("File deletion failed: {}", fileUrl, e);
        }
    }

    /**
     * 이미지 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("파일이 비어있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException("파일 크기는 10MB 이하여야 합니다.", HttpStatus.BAD_REQUEST);
        }

        // 확장자 확인
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(
                    "허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
