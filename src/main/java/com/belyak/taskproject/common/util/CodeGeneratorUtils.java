package com.belyak.taskproject.common.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.Random;

@UtilityClass
public class CodeGeneratorUtils {

    private static final Random RANDOM = new SecureRandom();

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Generates a random alphanumeric string of the specified length.
     * <p>
     * The generated string contains only uppercase letters (A-Z) and digits (0-9).
     *
     * @param length the desired length of the string
     * @return a random alphanumeric string
     */
    public static String generateJoinCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }
}
