import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;


@SuppressWarnings("ALL")
public class HedyGUI implements GUICallback {
    private boolean assistantRunning = false;
    private JTextArea chatArea;
    private JTextField userInputField;
    private JButton sendButton;
    private HedyAssistant assistant;
    private InteractionHistory interactionHistory;
    private Properties appPreferences;
    private JComboBox appearanceModeDropdown;
    private JLabel logoLabel;
    private JButton startAssistantButton; // 1. Declarar el botón como variable de clase
    String configFilePath = "Virtual Assistant/utils/config.json";
    String historyFilePath = "Virtual Assistant/utils/history.log";
    String visualPath = "Virtual Assistant/utils/visuals";

    public HedyGUI() {
        loadPreferences();
        String themePreference = appPreferences.getProperty("theme", "dark");
        if ("dark".equalsIgnoreCase(themePreference)) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        interactionHistory = new InteractionHistory(historyFilePath);

        JFrame frame = new JFrame("Hedy Assistant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int x = Integer.parseInt(appPreferences.getProperty("windowX", "100"));
        int y = Integer.parseInt(appPreferences.getProperty("windowY", "100"));
        int width = Integer.parseInt(appPreferences.getProperty("windowWidth", "1100"));
        int height = Integer.parseInt(appPreferences.getProperty("windowHeight", "800"));
        frame.setBounds(x, y, width, height);
        frame.setMinimumSize(new Dimension(1100, 800));
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                saveWindowPreferences(frame);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                saveWindowPreferences(frame);
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.setContentPane(mainPanel);

        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(new Color(30, 30, 30));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Logo
        logoLabel = new JLabel();
        updateLogoBasedOnTheme(themePreference);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sidebarPanel.add(logoLabel, BorderLayout.NORTH);

        // Título
        JLabel titleLabel = new JLabel("Hedy Lamarr", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        sidebarPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel de botones corregido
        JPanel buttonPanel = new JPanel(); // 2. Inicialización correcta
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(30, 30, 30));
        buttonPanel.add(Box.createVerticalGlue());

        // 3. Asignar el primer botón a la variable de clase
        startAssistantButton = createStyledButton("Iniciar Asistente");
        addButton(buttonPanel, startAssistantButton);

        // Voice Control Button
        JButton voiceControlButton = createStyledButton("Control por Voz");
        voiceControlButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    String configFilePath = "Virtual Assistant/utils/config.json";
                    ConfigManager configManager = new ConfigManager(configFilePath);
                    configManager.loadConfig();
                    try {
                        String accessKey = ConfigManager.getConfig("picovoice_api_key");
                        String modelPath = ConfigManager.getConfig("picovoice_model_path");
                        String[] keywordPaths = {ConfigManager.getConfig("picovoice_keywords_path")};
                        float[] sensitivities = {0.5f};
                        int audioDeviceIndex = -1;
                        // Pasar la referencia de HedyGUI como GUICallback
                        WakeWordDetector detector = new WakeWordDetector(accessKey, modelPath, keywordPaths, sensitivities, audioDeviceIndex, HedyGUI.this);
                        appendMessage("Hedy", "Control por voz iniciado. Escuchando...");
                        detector.startListening();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error starting voice control: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
            }.execute();
        });
        addButton(buttonPanel, voiceControlButton);

        // Edit Configuration Button
        JButton editConfigButton = createStyledButton("Editar Configuración");
        editConfigButton.addActionListener(e -> {
            try {
                File configFile = new File(configFilePath);
                if (!configFile.exists()) {
                    configFile.createNewFile(); // Create the file if it doesn't exist
                }
                // Specify the path to the editor executable
                String editorPath = "notepad.exe"; // Replace with the path to your preferred editor
                ProcessBuilder processBuilder = new ProcessBuilder(editorPath, configFile.getAbsolutePath());
                processBuilder.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error opening configuration file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        addButton(buttonPanel, editConfigButton);

        // Settings Button for Theme Selection
        JButton settingsButton = createStyledButton("⚙️ Settings");
        settingsButton.addActionListener(e -> {
            JDialog settingsDialog = new JDialog(frame, "Settings", true);
            settingsDialog.setLayout(new GridLayout(2, 1));
            JLabel themeLabel = new JLabel("Select Theme:");
            JComboBox<String> themeDropdown = new JComboBox<>(new String[]{"Dark", "Light"});
            themeDropdown.setSelectedItem(appPreferences.getProperty("theme", "dark").equalsIgnoreCase("dark") ? "Dark" : "Light");
            themeDropdown.addActionListener(e1 -> {
                String selectedTheme = (String) themeDropdown.getSelectedItem();
                setTheme(selectedTheme, frame);
                settingsDialog.dispose();
            });
            settingsDialog.add(themeLabel);
            settingsDialog.add(themeDropdown);
            settingsDialog.setSize(200, 100);
            settingsDialog.setLocationRelativeTo(frame);
            settingsDialog.setVisible(true);
        });
        addButton(buttonPanel, settingsButton);

        buttonPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 22));
        chatArea.setBackground(new Color(40, 40, 40));
        chatArea.setForeground(Color.WHITE);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        userInputField = new JTextField();
        userInputField.putClientProperty("JTextField.placeholderText", "Escribe tu mensaje aquí...");
        userInputField.setEnabled(false);
        userInputField.addActionListener(e -> sendMessage()); // Send message on Enter key press
        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false);
        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        startAssistantButton.addActionListener(e -> toggleAssistant());
        sendButton.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    // 5. Modificar metodo addButton para aceptar JButton
    private void addButton(JPanel panel, JButton button) {
        panel.add(button);
        panel.add(Box.createVerticalStrut(15));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.arc", 15);
        button.setBackground(new Color(0, 122, 255));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 45));
        return button;
    }

    private void addCenteredButton(JPanel panel, JButton button, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1; // Adjust weight as needed
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel.add(button, gbc);
    }

    private void toggleAssistant() {
        assistantRunning = !assistantRunning;
        userInputField.setEnabled(assistantRunning);
        sendButton.setEnabled(assistantRunning);

        if (assistantRunning) {
            if (assistant == null) {
                assistant = new HedyAssistant(configFilePath, historyFilePath);
                assistant.clearHistory(); // Clear the history when starting a new session
            }
            appendMessage("Hedy", "Asistente iniciado.");
        } else {
            appendMessage("Hedy", "Asistente detenido.");
        }
    }

    private void sendMessage() {
        String input = userInputField.getText().trim();
        if (!input.isEmpty()) {
            appendMessage("Usuario", input);
            userInputField.setText("");

            new Thread(() -> {
                try {
                    String response = assistant.processInput(input);
                    SwingUtilities.invokeLater(() -> appendMessage("Hedy", response));
                    interactionHistory.addInteraction(input, response); // Add interaction to history
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> appendMessage("Hedy", "Lo siento, ha ocurrido un error al procesar tu solicitud."));
                }
            }).start();
        }
    }

    @Override
    public void appendMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    @Override
    public void updateStatus(String status) {
        System.out.println("Estado actualizado: " + status);
    }

    private void setTheme(String theme, JFrame frame) {
        if ("Dark".equalsIgnoreCase(theme)) {
            FlatDarkLaf.setup();
            appPreferences.setProperty("theme", "dark");
        } else {
            FlatLightLaf.setup();
            appPreferences.setProperty("theme", "light");
        }
        updateLogoBasedOnTheme(theme);
        SwingUtilities.updateComponentTreeUI(frame);
        savePreferences();
    }

    private void updateLogoBasedOnTheme(String theme) {
        String logoPath = visualPath + "/elder_" + theme.toLowerCase() + ".png";
        ImageIcon logoIcon = new ImageIcon(logoPath);

        if (logoIcon != null) {
            int width = logoIcon.getIconWidth();
            int height = logoIcon.getIconHeight();
            logoLabel.setPreferredSize(new Dimension(width, height));
        }

        logoLabel.setIcon(logoIcon);
    }

    private void loadPreferences() {
        appPreferences = new Properties();
        try (InputStream input = new FileInputStream("Virtual Assistant/utils/preferences.properties")) {
            appPreferences.load(input);
        } catch (IOException ex) {
            appPreferences.setProperty("theme", "dark");
            appPreferences.setProperty("windowX", "100");
            appPreferences.setProperty("windowY", "100");
            appPreferences.setProperty("windowWidth", "1100");
            appPreferences.setProperty("windowHeight", "800");
        }
    }

    private void saveWindowPreferences(JFrame frame) {
        appPreferences.setProperty("windowX", String.valueOf(frame.getX()));
        appPreferences.setProperty("windowY", String.valueOf(frame.getY()));
        appPreferences.setProperty("windowWidth", String.valueOf(frame.getWidth()));
        appPreferences.setProperty("windowHeight", String.valueOf(frame.getHeight()));
        savePreferences();
    }

    private void savePreferences() {
        try (OutputStream output = new FileOutputStream("Virtual Assistant/utils/preferences.properties")) {
            appPreferences.store(output, "Hedy Preferences");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HedyGUI::new);
    }
}