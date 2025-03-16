package com.haonguyen.logService;

import com.haonguyen.logService.exception.InvalidFilePathException;
import com.haonguyen.logService.exception.InvalidParameterException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class InputValidator {
    private static final Set<Path> INVALID_PATH_ELEMS = new HashSet<>(Arrays.asList(
            Paths.get(".."),
            Paths.get(".")
    ));

    public static Path validateFilePath(String filePathStr) throws InvalidFilePathException {
        if (isPathEmpty(filePathStr)) throw new InvalidFilePathException();

        Path filePath = Paths.get(filePathStr);
        for (Path elem : filePath) {
            if (INVALID_PATH_ELEMS.contains(elem)) {
               throw new InvalidFilePathException();
            }
        }

        return filePath;
    }

    public static <T extends Comparable<T>> Optional<T> validateOptionalRangeParam(T value, T min, T max) {
        if (value == null) return Optional.empty();

        boolean isValid = true;
        if (min != null) isValid &= (value.compareTo(min) > 0);
        if (max != null) isValid &= (value.compareTo(max) < 0);

        if (!isValid) throw new InvalidParameterException();
        return Optional.of(value);
    }

    private static boolean isPathEmpty(String filePathStr) {
        return filePathStr.trim().isEmpty();
    }
}
