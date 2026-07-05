package com.mutuelle.mobille.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private boolean success;

    private String message;

    private T data;

    private List<String> errors;

    private Integer code;

    //pagination
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponseDto<T> ok(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .code(200)
                .build();
    }

    public static <T> ApiResponseDto<T> ok(T data) {
        return ok(data, "Opération réussie");
    }

    public static <T> ApiResponseDto<T> okPaged(T content, String message,
                                                long totalElements, int totalPages,
                                                int currentPage, int pageSize) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(content)
                .code(200)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .build();
    }

    public static <T> ApiResponseDto<T> created(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message("Ressource créée avec succès")
                .data(data)
                .code(201)
                .build();
    }

    public static <T> ApiResponseDto<T> unauthorized(String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(401)
                .build();
    }

    public static <T> ApiResponseDto<T> forbidden(String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(403)
                .build();
    }

    public static <T> ApiResponseDto<T> notFound(String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(404)
                .build();
    }

    public static <T> ApiResponseDto<T> badRequest(String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(400)
                .build();
    }

    public static <T> ApiResponseDto<T> badRequest(String message, List<String> errors) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .code(400)
                .build();
    }

    public static <T> ApiResponseDto<T> validationError(List<String> errors) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message("Données invalides")
                .errors(errors)
                .code(422)
                .build();
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(500)
                .build();
    }

    public static <T> ApiResponseDto<T> errorCustom(String message, Integer code) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .build();
    }
}