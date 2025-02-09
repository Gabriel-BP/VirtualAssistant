import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class HedyGUI {
    private boolean assistantRunning = false;
    private JTextArea chatArea;
    private JTextField userInputField;
    private JButton sendButton;
    private HedyAssistant assistant;
    private Properties appPreferences;

    public HedyGUI() {
        // Cargar preferencias
        loadPreferences();

        // Configurar tema según preferencias
        String themePreference = appPreferences.getProperty("theme", "light");
        if ("dark".equals(themePreference)) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        // Crear ventana principal
        JFrame frame = new JFrame("Hedy Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Restaurar posición y tamaño de la ventana desde preferencias
        int x = Integer.parseInt(appPreferences.getProperty("windowX", "100"));
        int y = Integer.parseInt(appPreferences.getProperty("windowY", "100"));
        int width = Integer.parseInt(appPreferences.getProperty("windowWidth", "800"));
        int height = Integer.parseInt(appPreferences.getProperty("windowHeight", "450"));
        frame.setBounds(x, y, width, height);
        frame.setMinimumSize(new Dimension(800, 450)); // Relación 16:9
        frame.setLayout(new BorderLayout());

        // Configurar eventos para guardar posición y tamaño al cerrar
        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
                saveWindowPreferences(frame);
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                saveWindowPreferences(frame);
            }
        });

        // Configurar tamaño de fuente inicial desde preferencias
        int fontSize = Integer.parseInt(appPreferences.getProperty("fontSize", "12"));

        // Área de texto para mostrar interacciones
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize)); // Usar tamaño guardado en preferencias
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior para entrada de usuario
        JPanel inputPanel = new JPanel(new BorderLayout());
        userInputField = new JTextField();
        userInputField.setEnabled(false); // Deshabilitar cuando el asistente no está activo
        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false);

        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Panel superior para botones
        JPanel topPanel = new JPanel(new BorderLayout());

        // Panel izquierdo (Iniciar/Terminar Asistente)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton toggleAssistantButton = new JButton("Iniciar Asistente");
        leftPanel.add(toggleAssistantButton);

        // Panel derecho (Ver historial, Configurar, Tamaño de fuente)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton openHistoryButton = new JButton("Ver Historial");
        JButton configureButton = new JButton("Configurar");

        // Lista desplegable para seleccionar tamaño de fuente
        String[] fontSizes = {"10", "12", "14", "16", "18", "20", "24", "28", "32", "36"};
        JComboBox<String> fontSizeComboBox = new JComboBox<>(fontSizes);
        fontSizeComboBox.setSelectedItem(String.valueOf(fontSize)); // Seleccionar tamaño desde preferencias
        fontSizeComboBox.addActionListener(e -> {
            String selectedSize = (String) fontSizeComboBox.getSelectedItem();
            int newFontSize = Integer.parseInt(selectedSize);
            chatArea.setFont(chatArea.getFont().deriveFont((float) newFontSize)); // Cambiar solo el tamaño
            appPreferences.setProperty("fontSize", selectedSize); // Guardar preferencia
            savePreferences();
        });

        rightPanel.add(openHistoryButton);
        rightPanel.add(configureButton);
        rightPanel.add(new JLabel("Tamaño de Fuente:"));
        rightPanel.add(fontSizeComboBox);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);

        // Acción del botón "Iniciar/Terminar Asistente"
        toggleAssistantButton.addActionListener(e -> {
            assistantRunning = !assistantRunning;
            toggleAssistantButton.setText(assistantRunning ? "Terminar Asistente" : "Iniciar Asistente");

            if (assistantRunning) {
                if (assistant == null) {
                    assistant = new HedyAssistant("config.json", "history.log");
                }
                chatArea.append("Hedy: Asistente iniciado.\n");
                userInputField.setEnabled(true);
                sendButton.setEnabled(true);
            } else {
                chatArea.append("Hedy: Asistente detenido.\n");
                userInputField.setEnabled(false);
                sendButton.setEnabled(false);
            }
        });

        // Acción del botón "Enviar"
        sendButton.addActionListener(e -> sendMessage());
        userInputField.addActionListener(e -> sendMessage()); // También enviar con Enter

        // Acción del botón "Ver Historial"
        openHistoryButton.addActionListener(e -> openHistory(frame));

        // Acción del botón "Configurar"
        configureButton.addActionListener(e -> configureSettings(frame));

        // Menú para cambiar tema
        JMenuBar menuBar = new JMenuBar();
        JMenu themeMenu = new JMenu("Temas");
        JMenuItem lightTheme = new JMenuItem("Claro");
        lightTheme.addActionListener(e -> setTheme(new FlatLightLaf(), frame, "light"));
        JMenuItem darkTheme = new JMenuItem("Oscuro");
        darkTheme.addActionListener(e -> setTheme(new FlatDarkLaf(), frame, "dark"));
        themeMenu.add(lightTheme);
        themeMenu.add(darkTheme);
        menuBar.add(themeMenu);
        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
    }

    private void setTheme(LookAndFeel theme, JFrame frame, String themeName) {
        try {
            UIManager.setLookAndFeel(theme);
            SwingUtilities.updateComponentTreeUI(frame);
            appPreferences.setProperty("theme", themeName);
            savePreferences();
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
    }

    private void saveWindowPreferences(JFrame frame) {
        appPreferences.setProperty("windowX", String.valueOf(frame.getX()));
        appPreferences.setProperty("windowY", String.valueOf(frame.getY()));
        appPreferences.setProperty("windowWidth", String.valueOf(frame.getWidth()));
        appPreferences.setProperty("windowHeight", String.valueOf(frame.getHeight()));
        savePreferences();
    }

    private void sendMessage() {
        String input = userInputField.getText();
        if (input.isBlank() || !assistantRunning) {
            return;
        }
        chatArea.append("Usuario: " + input + "\n");
        String response = assistant.processInput(input);
        chatArea.append("Hedy: " + response + "\n");
        userInputField.setText(""); // Borrar campo de entrada
    }

    private void openHistory(JFrame parent) {
        try {
            String history = new String(Files.readAllBytes(Paths.get("history.log")));
            JTextArea historyArea = new JTextArea(history);
            historyArea.setEditable(false);
            JScrollPane historyScrollPane = new JScrollPane(historyArea);

            JFrame historyFrame = new JFrame("Historial de Interacciones");
            historyFrame.setSize(500, 400);
            historyFrame.add(historyScrollPane);
            historyFrame.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo abrir el archivo history.log", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configureSettings(JFrame parent) {
        try {
            Desktop.getDesktop().edit(new File("config.json"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "No se pudo abrir el archivo config.json", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPreferences() {
        appPreferences = new Properties();
        try (InputStream input = new FileInputStream("preferences.properties")) {
            appPreferences.load(input);
        } catch (IOException ex) {
            appPreferences.setProperty("theme", "light");
            appPreferences.setProperty("fontSize", "12");
            appPreferences.setProperty("windowX", "100");
            appPreferences.setProperty("windowY", "100");
            appPreferences.setProperty("windowWidth", "800");
            appPreferences.setProperty("windowHeight", "450");
        }
    }

    private void savePreferences() {
        try (OutputStream output = new FileOutputStream("preferences.properties")) {
            appPreferences.store(output, "Hedy Preferences");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(HedyGUI::new);
    }
}
