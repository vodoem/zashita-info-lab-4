package ru.rsreu;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;

public class PoligHellmanGUI extends JFrame {

    private final JTextField eField;
    private final JTextField dField;
    private final JTextField nField;

    private final JTextArea plainTextArea;
    private final JTextArea cipherTextArea;

    private final SecureRandom random = new SecureRandom();

    public PoligHellmanGUI() {
        setTitle("Схема шифрования Полига–Хеллмана");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        // Панель с параметрами e, d, n
        JPanel keyPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel eLabel = new JLabel("e:");
        JLabel dLabel = new JLabel("d:");
        JLabel nLabel = new JLabel("n:");

        eField = new JTextField(10);
        dField = new JTextField(10);
        nField = new JTextField(10);

        // по умолчанию можно поставить пример из методички: e=25, d=9, n=29
        eField.setText("25");
        dField.setText("9");
        nField.setText("29");

        gbc.gridx = 0; gbc.gridy = 0;
        keyPanel.add(eLabel, gbc);
        gbc.gridx = 1;
        keyPanel.add(eField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        keyPanel.add(dLabel, gbc);
        gbc.gridx = 1;
        keyPanel.add(dField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        keyPanel.add(nLabel, gbc);
        gbc.gridx = 1;
        keyPanel.add(nField, gbc);

        root.add(keyPanel, BorderLayout.NORTH);

        // Центральная часть: исходное сообщение и криптограмма
        plainTextArea = new JTextArea(6, 25);
        cipherTextArea = new JTextArea(6, 25);

        plainTextArea.setLineWrap(true);
        plainTextArea.setWrapStyleWord(true);
        cipherTextArea.setLineWrap(true);
        cipherTextArea.setWrapStyleWord(true);

        JScrollPane plainScroll = new JScrollPane(plainTextArea);
        JScrollPane cipherScroll = new JScrollPane(cipherTextArea);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(new JLabel("Исходное сообщение P:"), BorderLayout.NORTH);
        leftPanel.add(plainScroll, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(new JLabel("Криптограмма C:"), BorderLayout.NORTH);
        rightPanel.add(cipherScroll, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        root.add(centerPanel, BorderLayout.CENTER);

        // Нижняя панель с кнопками
        JButton encryptButton = new JButton("Зашифровать  P → C");
        JButton decryptButton = new JButton("Расшифровать C → P");
        JButton clearButton   = new JButton("Очистить");
        JButton validateButton = new JButton("Проверить ключи");
        JButton generateButton = new JButton("Сгенерировать e, d, n");

        encryptButton.addActionListener(e -> encryptAction());
        decryptButton.addActionListener(e -> decryptAction());
        clearButton.addActionListener(e -> {
            plainTextArea.setText("");
            cipherTextArea.setText("");
        });
        validateButton.addActionListener(e -> {
            try {
                validateKeys(true);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
        generateButton.addActionListener(e -> generateKeys());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.add(encryptButton);
        buttonsPanel.add(decryptButton);
        buttonsPanel.add(clearButton);
        buttonsPanel.add(validateButton);
        buttonsPanel.add(generateButton);

        root.add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // центр экрана
    }

    private void encryptAction() {
        try {
            KeySet keys = validateKeys(false);
            BigInteger eVal = keys.e();
            BigInteger nVal = keys.n();

            List<BigInteger> blocks = parseBlocks(plainTextArea.getText());
            List<BigInteger> result = new ArrayList<>();

            for (BigInteger block : blocks) {
                checkBlock(block, nVal);
                // Ci = Pi^e (mod n)
                result.add(block.modPow(eVal, nVal));
            }

            cipherTextArea.setText(joinBlocks(result));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void decryptAction() {
        try {
            KeySet keys = validateKeys(false);
            BigInteger dVal = keys.d();
            BigInteger nVal = keys.n();

            List<BigInteger> blocks = parseBlocks(cipherTextArea.getText());
            List<BigInteger> result = new ArrayList<>();

            for (BigInteger block : blocks) {
                checkBlock(block, nVal);
                // Pi = Ci^d (mod n)
                result.add(block.modPow(dVal, nVal));
            }

            plainTextArea.setText(joinBlocks(result));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // Разбор строки с числами в список BigInteger
    // Поддерживает два варианта:
    // 1) "1 5 7"  -> блоки [1, 5, 7]
    // 2) "794341" -> блоки [7, 9, 4, 3, 4, 1] (каждая цифра — отдельный блок)
    private List<BigInteger> parseBlocks(String text) {
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
            // одна "слитная" строка цифр -> шифруем / расшифровываем по одной цифре
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

    // Проверка, что блок корректен для модуля n
    private void checkBlock(BigInteger block, BigInteger n) {
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

    private BigInteger parseBigInteger(String text, String name) {
        String t = text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Поле \"" + name + "\" не заполнено.");
        }
        try {
            return new BigInteger(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Поле \"" + name + "\" содержит некорректное число.");
        }
    }

    private String joinBlocks(List<BigInteger> blocks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(blocks.get(i).toString());
        }
        return sb.toString();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private KeySet validateKeys(boolean showSuccess) {
        BigInteger eVal = parseBigInteger(eField.getText(), "e");
        BigInteger dVal = parseBigInteger(dField.getText(), "d");
        BigInteger nVal = parseBigInteger(nField.getText(), "n");

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

        if (showSuccess) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ключи корректны. Вектор проверки: e*d mod (n-1) = 1 при n = " + nVal + '.',
                    "Проверка ключей",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        return new KeySet(eVal, dVal, nVal, phi);
    }

    private void generateKeys() {
        BigInteger nVal = BigInteger.probablePrime(12, random);
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

        eField.setText(eVal.toString());
        dField.setText(dVal.toString());
        nField.setText(nVal.toString());

        JOptionPane.showMessageDialog(
                this,
                "Набор параметров сгенерирован:\n" +
                        "n (простое) = " + nVal + '\n' +
                        "e = " + eVal + '\n' +
                        "d = " + dVal + '\n' +
                        "Проверка: e*d mod (n-1) = 1",
                "Параметры сгенерированы",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private record KeySet(BigInteger e, BigInteger d, BigInteger n, BigInteger phi) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PoligHellmanGUI gui = new PoligHellmanGUI();
            gui.setVisible(true);
        });
    }
}
