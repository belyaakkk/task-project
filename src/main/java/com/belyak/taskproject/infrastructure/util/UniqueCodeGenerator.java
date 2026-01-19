package com.belyak.taskproject.infrastructure.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Component
@Slf4j
public class UniqueCodeGenerator {

    private static final int MAX_RETRIES = 5;

    /**
     * A universal method for generating a unique code
     *
     * @param codeGenerator    String generation function (e.g. () -> Utils.gen(6))
     * @param existenceChecker Function to check existence (e.g. repo::existsByCode)
     * @return Unique code
     */
    public String generate(Supplier<String> codeGenerator, Predicate<String> existenceChecker) {
        String code;
        int attempts = 0;
        do {
            code = codeGenerator.get();
            attempts++;

            if (attempts > MAX_RETRIES) {
                throw new IllegalStateException("Failed to generate unique code after " + MAX_RETRIES + " attempts");
            }
        } while (existenceChecker.test(code));

        return code;
    }
}
