import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@SuppressWarnings("ALL")
public class HedyGUI {
    private boolean assistantRunning = false;
    private JTextArea chatArea;
    private JTextField userInputField;
    private JButton sendButton;
    private HedyAssistant assistant;
    private Properties appPreferences;
    private JComboBox<String> appearanceModeDropdown;

    public HedyGUI() {
        loadPreferences();

        String themePreference = appPreferences.getProperty("theme", "dark");
        if ("dark".equalsIgnoreCase(themePreference)) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

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

        JPanel sidebarPanel = new JPanel(new GridLayout(12, 1, 0, 10));
        sidebarPanel.setBackground(new Color(30, 30, 30));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel logoLabel = new JLabel(new ImageIcon("utils/visuals/elder_dark.png"));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sidebarPanel.add(logoLabel);

        JLabel titleLabel = new JLabel("Hedy Lamarr", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        sidebarPanel.add(titleLabel);

        JButton startAssistantButton = createStyledButton("Iniciar Asistente");
        sidebarPanel.add(startAssistantButton);

        appearanceModeDropdown = new JComboBox<>(new String[]{"Dark", "Light"});
        appearanceModeDropdown.addActionListener(e -> {
            String selectedTheme = (String) appearanceModeDropdown.getSelectedItem();
            setTheme(selectedTheme, frame);
        });
        sidebarPanel.add(appearanceModeDropdown);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        chatArea.setBackground(new Color(40, 40, 40));
        chatArea.setForeground(Color.WHITE);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        userInputField = new JTextField();
        userInputField.putClientProperty("JTextField.placeholderText", "Escribe tu mensaje aquí...");
        userInputField.setEnabled(false);
        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false);
        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        startAssistantButton.addActionListener(e -> toggleAssistant());
        sendButton.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 122, 255));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    private void toggleAssistant() {
        assistantRunning = !assistantRunning;
        userInputField.setEnabled(assistantRunning);
        sendButton.setEnabled(assistantRunning);
        if (assistantRunning) {
            if (assistant == null) {
                assistant = new HedyAssistant("config.json", "history.log");
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
            appendMessage("Hedy", assistant.processInput(input));
        }
    }

    private void appendMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void setTheme(String theme, JFrame frame) {
        if ("Dark".equalsIgnoreCase(theme)) {
            FlatDarkLaf.setup();
            appPreferences.setProperty("theme", "dark");
        } else {
            FlatLightLaf.setup();
            appPreferences.setProperty("theme", "light");
        }
        SwingUtilities.updateComponentTreeUI(frame);
        savePreferences();
    }

    private void loadPreferences() {
        appPreferences = new Properties();
        try (InputStream input = new FileInputStream("preferences.properties")) {
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
