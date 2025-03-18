package com.haonguyen.logService;

import com.haonguyen.logService.authentication.Authenticator;
import com.haonguyen.logService.exception.InvalidFilePathException;
import com.haonguyen.logService.exception.InvalidParameterException;
import com.haonguyen.logService.json.LogResponse;
import com.haonguyen.logService.logReader.LogReader;
import com.haonguyen.logService.logReader.rule.LogRule;
import com.haonguyen.logService.logReader.rule.LogTextContainRule;
import com.haonguyen.logService.logReader.rule.LogTextIsNotEmptyRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class LogController {
    private static final int MIN_TAKE = 0;
    private static final int MAX_TAKE = 1000000;
    @Autowired
    private Authenticator authenticator;

    @Autowired
    private LogReader logReader;

    @RequestMapping("/logs/{*filePathStr}")
    public LogResponse getLogs(
            @RequestHeader("Authorization") String authToken,
            @PathVariable String filePathStr,
            @RequestParam(name = "take", required = false) Integer takeI,
            @RequestParam(name = "ptr", required = false) Long ptrI,
            @RequestParam(name = "contains", required = false) String contains
    ) {
        if (!authenticator.isAuthenticated(authToken))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        try {
            Path filePath = InputValidator.validateFilePath(String.format("/var/log%s", filePathStr));
            Optional<Integer> take = InputValidator.validateOptionalRangeParam(takeI, MIN_TAKE, MAX_TAKE);
            Optional<Long> ptr = InputValidator.validateOptionalRangeParam(ptrI, 0L, null);

            List<LogRule> rules = new ArrayList<>();
            rules.add(new LogTextIsNotEmptyRule());
            if (contains != null && !contains.isEmpty()) rules.add(new LogTextContainRule(contains));

            return logReader.readLogs(filePath, take.isPresent() ? take.get() : MAX_TAKE, rules);
        } catch (InvalidFilePathException | InvalidParameterException e) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
