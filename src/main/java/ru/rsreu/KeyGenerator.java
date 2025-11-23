package ru.rsreu;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Генератор корректных параметров e, d, n.
 */
public final class KeyGenerator {

    private static final int DEFAULT_BITS = 12;

    private KeyGenerator() {
    }

    public static KeySet generate(SecureRandom random) {
        BigInteger nVal = BigInteger.probablePrime(DEFAULT_BITS, random);
        BigInteger phi = nVal.subtract(BigInteger.ONE);

        BigInteger eVal;
        do {
            eVal = new BigInteger(phi.bitLength(), random);
            if (eVal.compareTo(BigInteger.TWO) < 0) {
                eVal = eVal.add(BigInteger.TWO);
            }
            eVal = eVal.mod(phi);
            if (eVal.compareTo(BigInteger.TWO) < 0) {
                eVal = eVal.add(BigInteger.TWO);
            }
        } while (!eVal.gcd(phi).equals(BigInteger.ONE));

        BigInteger dVal = eVal.modInverse(phi);
        return new KeySet(eVal, dVal, nVal, phi);
    }
}
