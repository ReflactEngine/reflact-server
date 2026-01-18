package net.reflact.server.gui;

import net.minestom.server.MinecraftServer;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.io.PrintStream;

public class ServerConsole extends JFrame {
    private final JTextArea textArea;
    private final JTextField inputField;

    public ServerConsole() {
        super("Reflact Server Console");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Log Area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.LIGHT_GRAY);
        
        // Auto-scroll
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        // Input Field
        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputField.setBackground(new Color(30, 30, 30));
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        
        inputField.addActionListener((ActionEvent e) -> {
            String command = inputField.getText();
            if (command != null && !command.trim().isEmpty()) {
                // Echo command
                appendLog("> " + command + "\n");
                
                // Execute command
                try {
                    MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().getConsoleSender(), command);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                
                inputField.setText("");
            }
        });
        
        add(inputField, BorderLayout.SOUTH);
    }

    public void start() {
        // Redirect System.out and System.err
        redirectSystemStreams();
        
        // Show GUI
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            inputField.requestFocusInWindow();
        });
    }

    private void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(text);
            // Limit buffer size to prevent memory issues
            if (textArea.getLineCount() > 2000) {
                try {
                    textArea.replaceRange("", 0, textArea.getLineEndOffset(textArea.getLineCount() - 1500));
                } catch (Exception ignored) {}
            }
        });
    }

    private void redirectSystemStreams() {
        OutputStream outStream = new OutputStream() {
            @Override
            public void write(int b) {
                appendLog(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                appendLog(new String(b, off, len));
            }
        };

        OutputStream errStream = new OutputStream() {
            @Override
            public void write(int b) {
                appendLog(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                appendLog(new String(b, off, len));
            }
        };

        // Combine original stream with our stream
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;

        System.setOut(new PrintStream(new DualOutputStream(origOut, outStream)));
        System.setErr(new PrintStream(new DualOutputStream(origErr, errStream)));
    }

    private static class DualOutputStream extends OutputStream {
        private final OutputStream stream1;
        private final OutputStream stream2;

        public DualOutputStream(OutputStream stream1, OutputStream stream2) {
            this.stream1 = stream1;
            this.stream2 = stream2;
        }

        @Override
        public void write(int b) throws java.io.IOException {
            stream1.write(b);
            stream2.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws java.io.IOException {
            stream1.write(b, off, len);
            stream2.write(b, off, len);
        }
    }
}
