package net.reflact.server.gui

import net.minestom.server.MinecraftServer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.*
import javax.swing.text.DefaultCaret

class ServerConsole : JFrame("Reflact Server Console") {
    private val textArea: JTextArea
    private val inputField: JTextField

    init {
        setSize(800, 600)
        defaultCloseOperation = EXIT_ON_CLOSE
        layout = BorderLayout()

        // Log Area
        textArea = JTextArea()
        textArea.isEditable = false
        textArea.font = Font("Monospaced", Font.PLAIN, 12)
        textArea.background = Color.BLACK
        textArea.foreground = Color.LIGHT_GRAY

        // Auto-scroll
        val caret = textArea.caret as DefaultCaret
        caret.updatePolicy = DefaultCaret.ALWAYS_UPDATE

        val scrollPane = JScrollPane(textArea)
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
        add(scrollPane, BorderLayout.CENTER)

        // Input Field
        inputField = JTextField()
        inputField.font = Font("Monospaced", Font.PLAIN, 14)
        inputField.background = Color(30, 30, 30)
        inputField.foreground = Color.WHITE
        inputField.caretColor = Color.WHITE

        inputField.addActionListener {
            val command = inputField.text
            if (!command.isNullOrBlank()) {
                // Echo command
                appendLog("> $command\n")

                // Execute command
                try {
                    MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().consoleSender, command)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                inputField.text = ""
            }
        }

        add(inputField, BorderLayout.SOUTH)
    }

    fun start() {
        // Redirect System.out and System.err
        redirectSystemStreams()

        // Show GUI
        SwingUtilities.invokeLater {
            isVisible = true
            inputField.requestFocusInWindow()
        }
    }

    private fun appendLog(text: String) {
        SwingUtilities.invokeLater {
            textArea.append(text)
            // Limit buffer size to prevent memory issues
            if (textArea.lineCount > 2000) {
                try {
                    textArea.replaceRange("", 0, textArea.getLineEndOffset(textArea.lineCount - 1500))
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun redirectSystemStreams() {
        val outStream = object : OutputStream() {
            override fun write(b: Int) {
                appendLog(b.toChar().toString())
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                appendLog(String(b, off, len))
            }
        }

        val errStream = object : OutputStream() {
            override fun write(b: Int) {
                appendLog(b.toChar().toString())
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                appendLog(String(b, off, len))
            }
        }

        // Combine original stream with our stream
        val origOut = System.out
        val origErr = System.err

        System.setOut(PrintStream(DualOutputStream(origOut, outStream)))
        System.setErr(PrintStream(DualOutputStream(origErr, errStream)))
    }

    private class DualOutputStream(private val stream1: OutputStream, private val stream2: OutputStream) :
        OutputStream() {
        override fun write(b: Int) {
            stream1.write(b)
            stream2.write(b)
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            stream1.write(b, off, len)
            stream2.write(b, off, len)
        }
    }
}
