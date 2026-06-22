package com.example.learningApp.utils;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.function.Supplier;

public final class RepositoryUtil {

    private RepositoryUtil() {
        // ngăn new
    }

    /**
     * Generic findById or throw exception
     */
    public static <T, ID> T findOrThrow(
            JpaRepository<T, ID> repository,
            ID id,
            Supplier<? extends RuntimeException> exceptionSupplier
    ) {
        return repository.findById(id)
                .orElseThrow(exceptionSupplier);
    }

    /**
     * Overload: dùng message đơn giản
     */
    public static <T, ID> T findOrThrow(
            JpaRepository<T, ID> repository,
            ID id,
            String errorMessage
    ) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }
}

