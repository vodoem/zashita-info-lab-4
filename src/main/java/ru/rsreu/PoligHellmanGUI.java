package ru.rsreu;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

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
        validateButton.addActionListener(e -> runKeyValidation(true));
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
            KeySet keys = runKeyValidation(false);
            List<BigInteger> blocks = BlockParser.parseBlocks(plainTextArea.getText());
            List<BigInteger> result = PolyHellmanCipher.encrypt(blocks, keys);
            cipherTextArea.setText(BlockParser.joinBlocks(result));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void decryptAction() {
        try {
            KeySet keys = runKeyValidation(false);
            List<BigInteger> blocks = BlockParser.parseBlocks(cipherTextArea.getText());
            List<BigInteger> result = PolyHellmanCipher.decrypt(blocks, keys);
            plainTextArea.setText(BlockParser.joinBlocks(result));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private KeySet runKeyValidation(boolean showSuccess) {
        KeySet keySet = KeyValidator.validate(
                parseBigInteger(eField.getText(), "e"),
                parseBigInteger(dField.getText(), "d"),
                parseBigInteger(nField.getText(), "n")
        );

        if (showSuccess) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ключи корректны. Вектор проверки: e*d mod (n-1) = 1 при n = " + keySet.n() + '.',
                    "Проверка ключей",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        return keySet;
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void generateKeys() {
        KeySet keySet = KeyGenerator.generate(random);
        eField.setText(keySet.e().toString());
        dField.setText(keySet.d().toString());
        nField.setText(keySet.n().toString());

        JOptionPane.showMessageDialog(
                this,
                "Набор параметров сгенерирован:\n" +
                        "n (простое) = " + keySet.n() + '\n' +
                        "e = " + keySet.e() + '\n' +
                        "d = " + keySet.d() + '\n' +
                        "Проверка: e*d mod (n-1) = 1",
                "Параметры сгенерированы",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PoligHellmanGUI gui = new PoligHellmanGUI();
            gui.setVisible(true);
        });
    }
}
