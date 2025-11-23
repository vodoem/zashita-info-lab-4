package ru.rsreu;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Выполняет операции шифрования и дешифрования для схемы Полига–Хеллмана.
 */
public final class PolyHellmanCipher {

    private PolyHellmanCipher() {
    }

    public static List<BigInteger> encrypt(List<BigInteger> blocks, KeySet keySet) {
        List<BigInteger> result = new ArrayList<>();
        for (BigInteger block : blocks) {
            BlockParser.validateBlock(block, keySet.n());
            result.add(block.modPow(keySet.e(), keySet.n()));
        }
        return result;
    }

    public static List<BigInteger> decrypt(List<BigInteger> blocks, KeySet keySet) {
        List<BigInteger> result = new ArrayList<>();
        for (BigInteger block : blocks) {
            BlockParser.validateBlock(block, keySet.n());
            result.add(block.modPow(keySet.d(), keySet.n()));
        }
        return result;
    }
}
