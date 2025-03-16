package com.haonguyen.logService;

import com.haonguyen.logService.exception.InvalidFilePathException;
import com.haonguyen.logService.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTests {
    @Test
    public void validateFilePath_notThrowsException_validPath() {
        String filePath = "hello/world/file.log";
        assertEquals(Paths.get(filePath), InputValidator.validateFilePath(filePath));
    }

    @Test
    public void validateFilePath_notThrowsException_containSlashes() {
        String filePath = "hello/world////file.log";
        assertEquals(Paths.get(filePath), InputValidator.validateFilePath(filePath));
    }

    @Test
    public void validateFilePath_notThrowsException_oneElement() {
        String filePath = "file.log";
        assertEquals(Paths.get(filePath), InputValidator.validateFilePath(filePath));
    }

    @Test
    public void validateFilePath_throwsException_containsBackwardDir() {
        assertThrows(InvalidFilePathException.class, () -> {
            String filePath = "hello/../world";
            InputValidator.validateFilePath(filePath);
        });
    }

    @Test
    public void validateFilePath_throwsException_containsCurrentDir() {
        assertThrows(InvalidFilePathException.class, () -> {
            String filePath = "hello/./world";
            InputValidator.validateFilePath(filePath);
        });
    }

    @Test
    public void validateFilePath_throwsException_empty() {
        assertThrows(InvalidFilePathException.class, () -> {
            String filePath = "";
            InputValidator.validateFilePath(filePath);
        });
    }

    @Test
    public void validateFilePath_throwsException_blank() {
        assertThrows(InvalidFilePathException.class, () -> {
            String filePath = "    ";
            InputValidator.validateFilePath(filePath);
        });
    }

    @Test
    public void validateOptionalRange_throwsException_lessThanOrEqualsZero() {
        assertThrows(InvalidParameterException.class, () -> {
            InputValidator.validateOptionalRangeParam(-1, 0, 1000);
        });

        assertThrows(InvalidParameterException.class, () -> {
            InputValidator.validateOptionalRangeParam(0, 0, 1000);
        });
    }

    @Test
    public void validateOptionalRange_throwsException_greaterThan1000() {
        assertThrows(InvalidParameterException.class, () -> {
            InputValidator.validateOptionalRangeParam(2000, 0, 1000);
        });
    }

    @Test
    public void validateOptionalRange_notThrowsException_betweenOneAnd1000() {
        Optional<Integer> take = InputValidator.validateOptionalRangeParam(500, 0, 1000);
        assertEquals(500, take.get());
    }

    @Test
    public void validateOptionalRange_optionalNotPresent_nullValue() {
        Optional<Integer> take = InputValidator.validateOptionalRangeParam(null, 0, 1000);
        assertTrue(take.isEmpty());
    }

    @Test
    public void validateOptionalRange_notThrowsException_noLowerBound() {
        Optional<Integer> take = InputValidator.validateOptionalRangeParam(Integer.MIN_VALUE, null, 1000);
        assertEquals(Integer.MIN_VALUE, take.get());
    }

    @Test
    public void validateOptionalRange_notThrowsException_noUpperBound() {
        Optional<Integer> take = InputValidator.validateOptionalRangeParam(Integer.MAX_VALUE, 0, null);
        assertEquals(Integer.MAX_VALUE, take.get());
    }
}
