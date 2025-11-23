package ru.rsreu;

import java.math.BigInteger;

/**
 * Проверяет корректность параметров шифрования.
 */
public final class KeyValidator {

    private KeyValidator() {
    }

    public static KeySet validate(BigInteger eVal, BigInteger dVal, BigInteger nVal) {
        if (eVal.compareTo(BigInteger.ONE) <= 0 || dVal.compareTo(BigInteger.ONE) <= 0) {
            throw new IllegalArgumentException("Параметры e и d должны быть больше 1.");
        }
        if (nVal.compareTo(BigInteger.TWO) <= 0) {
            throw new IllegalArgumentException("Модуль n должен быть больше 2.");
        }
        if (!nVal.isProbablePrime(20)) {
            throw new IllegalArgumentException("n должно быть простым числом.");
        }

        BigInteger phi = nVal.subtract(BigInteger.ONE);
        if (eVal.compareTo(phi) >= 0) {
            throw new IllegalArgumentException("e должно быть меньше n-1.");
        }
        if (dVal.compareTo(phi) >= 0) {
            throw new IllegalArgumentException("d должно быть меньше n-1.");
        }

        if (!eVal.gcd(phi).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException("e и n-1 должны быть взаимно простыми.");
        }

        if (!eVal.multiply(dVal).mod(phi).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException("e*d по модулю (n-1) должно быть равно 1.");
        }

        return new KeySet(eVal, dVal, nVal, phi);
    }
}
