package ru.rsreu;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилита для разборки и сборки последовательностей чисел.
 */
public final class BlockParser {

    private BlockParser() {
    }

    /**
     * Разбор строки с числами в список {@link BigInteger}.
     * Поддерживаются два варианта:
     * <ul>
     *     <li>"1 5 7" – блоки [1, 5, 7];</li>
     *     <li>"794341" – блоки [7, 9, 4, 3, 4, 1] (каждая цифра – отдельный блок).</li>
     * </ul>
     */
    public static List<BigInteger> parseBlocks(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Введите сообщение (числа P или C).");
        }

        String normalized = trimmed
                .replace(',', ' ')
                .replace(';', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim()
                .replaceAll("\\s+", " ");

        String[] tokens = normalized.split(" ");
        List<BigInteger> blocks = new ArrayList<>();

        if (tokens.length == 1 && tokens[0].matches("\\d+")) {
            String s = tokens[0];
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                blocks.add(new BigInteger(String.valueOf(ch)));
            }
        } else {
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    blocks.add(new BigInteger(token));
                }
            }
        }

        return blocks;
    }

    public static String joinBlocks(List<BigInteger> blocks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(blocks.get(i));
        }
        return sb.toString();
    }

    public static void validateBlock(BigInteger block, BigInteger n) {
        if (block.signum() < 0) {
            throw new IllegalArgumentException("Блоки сообщения должны быть неотрицательными числами.");
        }
        if (block.compareTo(n) >= 0) {
            throw new IllegalArgumentException(
                    "Каждый блок Pi / Ci должен быть меньше модуля n.\n" +
                            "Найден блок: " + block + ", модуль n: " + n
            );
        }
    }
}
