package ru.rsreu;

import java.math.BigInteger;

/**
 * Набор параметров e, d, n и значение φ(n) = n - 1.
 */
public record KeySet(BigInteger e, BigInteger d, BigInteger n, BigInteger phi) {
}
