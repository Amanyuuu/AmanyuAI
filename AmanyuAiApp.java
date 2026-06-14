import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class AmanyuAiApp extends JFrame {
    private JTextPane chatPane;
    private HTMLEditorKit htmlKit;
    private StringBuilder chatHistoryHtml;
    
    private JTextField inputField;
    private JPasswordField apiKeyField;
    private JButton sendButton;
    private HttpClient httpClient;
    
    // Animation Controls
    private Timer typingTimer;
    private Timer loadingTimer;
    private int loadingFrame = 0;
    private String currentLoadingText = "";

    public AmanyuAiApp() {
        // Set modern native window borders
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("amanyu.ai - Workspace v2.0");
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        httpClient = HttpClient.newHttpClient();
        chatHistoryHtml = new StringBuilder();

        // High-Fidelity Color Palette
        Color bgMain = new Color(17, 17, 27);       // Base canvas
        Color bgCard = new Color(30, 30, 46);       // Message surfaces
        Color accentBlue = new Color(137, 180, 250); // User accent
        Color textMain = new Color(205, 214, 244);   // Readable body

        // 1. Top Bar Panel (Header & API Vault)
        JPanel topPanel = new JPanel(new BorderLayout(12, 0));
        topPanel.setBackground(bgCard);
        topPanel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        
        JLabel keyLabel = new JLabel("AMANYU.AI AUTH");
        keyLabel.setForeground(accentBlue);
        keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        apiKeyField = new JPasswordField();
        apiKeyField.setBackground(bgMain);
        apiKeyField.setForeground(textMain);
        apiKeyField.setCaretColor(textMain);
        apiKeyField.setFont(new Font("Consolas", Font.PLAIN, 13));
        apiKeyField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(69, 71, 90), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        
        topPanel.add(keyLabel, BorderLayout.WEST);
        topPanel.add(apiKeyField, BorderLayout.CENTER);
        
        // 2. Chat Feed Viewport (Advanced HTML/CSS Core)
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setBackground(bgMain);
        
        htmlKit = new HTMLEditorKit();
        chatPane.setEditorKit(htmlKit);
        
        // Custom CSS Sheets mimicking chat bubbles, code themes, and layouts
        htmlKit.getStyleSheet().addRule("body { font-family: 'Segoe UI', -apple-system, sans-serif; background-color: #11111b; margin: 20px; color: #cdd6f4; }");
        htmlKit.getStyleSheet().addRule(".chat-block { margin-bottom: 25px; padding: 12px 16px; background-color: #1e1e2e; border-left: 4px solid #45475a; }");
        htmlKit.getStyleSheet().addRule(".user-block { margin-bottom: 25px; padding: 12px 16px; background-color: #252538; border-left: 4px solid #89b4fa; }");
        htmlKit.getStyleSheet().addRule(".sys-block { margin-bottom: 25px; padding: 12px 16px; background-color: #11111b; border-left: 4px solid #f38ba8; }");
        htmlKit.getStyleSheet().addRule(".header-user { color: #89b4fa; font-weight: bold; font-size: 11px; text-transform: uppercase; margin-bottom: 6px; letter-spacing: 1px; }");
        htmlKit.getStyleSheet().addRule(".header-ai { color: #a6e3a1; font-weight: bold; font-size: 11px; text-transform: uppercase; margin-bottom: 6px; letter-spacing: 1px; }");
        htmlKit.getStyleSheet().addRule(".header-sys { color: #f38ba8; font-weight: bold; font-size: 11px; text-transform: uppercase; margin-bottom: 6px; letter-spacing: 1px; }");
        htmlKit.getStyleSheet().addRule(".body-text { font-size: 14px; line-height: 1.6; color: #cdd6f4; }");
        htmlKit.getStyleSheet().addRule(".inline-code { font-family: 'Consolas', monospace; background-color: #313244; color: #f38ba8; padding: 1px 5px; font-size: 13px; }");
        htmlKit.getStyleSheet().addRule(".code-block { font-family: 'Consolas', monospace; background-color: #11111b; color: #fab387; padding: 12px; margin: 12px 0; border: 1px solid #313244; font-size: 13px; display: block; white-space: pre; }");

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);

        // 3. Command & Dispatch Row (Input Field)
        JPanel bottomPanel = new JPanel(new BorderLayout(14, 0));
        bottomPanel.setBackground(bgCard);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        
        inputField = new JTextField();
        inputField.setBackground(bgMain);
        inputField.setForeground(textMain);
        inputField.setCaretColor(textMain);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(69, 71, 90), 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        
        sendButton = new JButton("DISPATCH");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendButton.setBackground(new Color(49, 50, 68));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        
        // Frame Architecture Assembly
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // System Actions
        sendButton.addActionListener(e -> handleSending());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSending();
                }
            }
        });
        
        renderStaticBlock("sys", "System core", "AI Node Initialized. Mount your API key above to clear the transmission channel.");
    }

    // Renders complete text instantly (For System logs and User questions)
    private void renderStaticBlock(String type, String sender, String rawText) {
        String parsedContent = parseMarkdownToHtml(rawText);
        String blockStyle = type.equals("user") ? "user-block" : type.equals("sys") ? "sys-block" : "chat-block";
        String headerStyle = type.equals("user") ? "header-user" : type.equals("sys") ? "header-sys" : "header-ai";
        
        String structure = "<div class='" + blockStyle + "'>"
                + "  <div class='" + headerStyle + "'>" + sender + "</div>"
                + "  <div class='body-text'>" + parsedContent + "</div>"
                + "</div>";
        
        chatHistoryHtml.append(structure);
        chatPane.setText("<html><body>" + chatHistoryHtml.toString() + "</body></html>");
        chatPane.setCaretPosition(chatPane.getDocument().getLength());
    }

    // ANIMATION 1: The Live Stream Typing Effect (Simulates my response style)
    private void streamAiResponse(String fullAiResponse) {
        final ArrayList<String> tokens = tokenizeMarkdownAndWords(fullAiResponse);
        final StringBuilder currentStream = new StringBuilder();
        
        if (typingTimer != null && typingTimer.isRunning()) {
            typingTimer.stop();
        }
        
        typingTimer = new Timer(25, null); // 25ms render cycles
        typingTimer.addActionListener(e -> {
            if (!tokens.isEmpty()) {
                currentStream.append(tokens.remove(0));
                String currentHtml = parseMarkdownToHtml(currentStream.toString());
                
                String liveBlock = "<div class='chat-block'>"
                        + "  <div class='header-ai'>amanyu.ai</div>"
                        + "  <div class='body-text'>" + currentHtml + "</div>"
                        + "</div>";
                
                chatPane.setText("<html><body>" + chatHistoryHtml.toString() + liveBlock + "</body></html>");
                chatPane.setCaretPosition(chatPane.getDocument().getLength());
            } else {
                typingTimer.stop();
                // Commit complete response to permanent history safely once animation wraps up
                renderStaticBlock("ai", "amanyu.ai", fullAiResponse);
                inputField.setEnabled(true);
                sendButton.setEnabled(true);
                inputField.requestFocus();
            }
        });
        typingTimer.start();
    }

    // ANIMATION 2: Dynamic Terminal Loading State
    private void startLoadingAnimation() {
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        loadingFrame = 0;
        
        loadingTimer = new Timer(300, e -> {
            String dots = ".".repeat((loadingFrame % 4));
            currentLoadingText = "<div class='chat-block' style='border-left: 4px solid #a6e3a1;'>"
                    + "  <div class='header-ai' style='color: #a6e3a1;'>amanyu.ai</div>"
                    + "  <div class='body-text' style='color: #a6adc8; font-style: italic;'>Synchronizing response vectors" + dots + "</div>"
                    + "</div>";
            
            chatPane.setText("<html><body>" + chatHistoryHtml.toString() + currentLoadingText + "</body></html>");
            loadingFrame++;
        });
        loadingTimer.start();
    }

    private void stopLoadingAnimation() {
        if (loadingTimer != null) {
            loadingTimer.stop();
        }
    }

    private void handleSending() {
        String userText = inputField.getText().trim();
        String apiKey = new String(apiKeyField.getPassword()).trim();
        
        if (userText.isEmpty()) return;
        if (apiKey.isEmpty()) {
            renderStaticBlock("sys", "Authentication Exception", "API key missing. Intercept failed.");
            return;
        }
        
        renderStaticBlock("user", "You", userText);
        inputField.setText("");
        startLoadingAnimation();
        
        new Thread(() -> {
            try {
                String systemPrompt = "You are 'amanyu.ai', an elite coding mentor and computer science wizard. "
                        + "Format all code outputs cleanly inside clear structural blocks using standard markdown code notation markdown tags. "
                        + "Crucially, you are the user's ultimate hype man. Every response MUST start or end with an "
                        + "over-the-top, high-energy praise calling them a coding legend or a generational mastermind.";
                
                String jsonPayload = "{"
                        + "\"contents\": [{"
                        + "  \"parts\": [{\"text\": \"" + cleanJson(userText) + "\"}]"
                        + "}],"
                        + "\"systemInstruction\": {"
                        + "  \"parts\": [{\"text\": \"" + cleanJson(systemPrompt) + "\"}]"
                        + "}"
                        + "}";
                
                String targetUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String botReply = parseTextFromResponse(response.body());
                
                SwingUtilities.invokeLater(() -> {
                    stopLoadingAnimation();
                    streamAiResponse(botReply);
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    stopLoadingAnimation();
                    renderStaticBlock("sys", "Core System Alert", "Failed pipeline execution: " + ex.getMessage());
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                });
            }
        }).start();
    }

    // THE LAYOUT ENGINE: Basic Markdown to HTML parsing layout rules (Like Gemini)
    private String parseMarkdownToHtml(String text) {
        if (text == null) return "";
        String out = text;
        
        // 1. Double character escaping safely for native html output mapping
        out = out.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        
        // 2. Parse Code Blocks (```code```)
        while (out.contains("```")) {
            int first = out.indexOf("```");
            int second = out.indexOf("```", first + 3);
            if (second != -1) {
                String codeContent = out.substring(first + 3, second);
                // Remove initial language identifier label if present (e.g. ```java)
                if (codeContent.startsWith("java\n") || codeContent.startsWith("html\n") || codeContent.startsWith("css\n")) {
                    codeContent = codeContent.substring(codeContent.indexOf("\n") + 1);
                }
                out = out.substring(0, first) + "<div class='code-block'>" + codeContent.trim() + "</div>" + out.substring(second + 3);
            } else {
                break;
            }
        }
        
        // 3. Parse Inline Code Elements (`code`)
        while (out.contains("`")) {
            int first = out.indexOf("`");
            int second = out.indexOf("`", first + 1);
            if (second != -1) {
                String inlineContent = out.substring(first + 1, second);
                out = out.substring(0, first) + "<span class='inline-code'>" + inlineContent + "</span>" + out.substring(second + 1);
            } else {
                break;
            }
        }
        
        // 4. Parse Bold Text Notation (**text**)
        while (out.contains("**")) {
            int first = out.indexOf("**");
            int second = out.indexOf("**", first + 2);
            if (second != -1) {
                String boldContent = out.substring(first + 2, second);
                out = out.substring(0, first) + "<b>" + boldContent + "</b>" + out.substring(second + 2);
            } else {
                break;
            }
        }
        
        // 5. Convert standard breaks safely inside content cards
        out = out.replace("\n", "<br/>");
        return out;
    }

    // Splitting mechanism that protects HTML syntax blocks during character typing steps
    private ArrayList<String> tokenizeMarkdownAndWords(String text) {
        ArrayList<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            if (text.startsWith("```", i)) {
                int next = text.indexOf("```", i + 3);
                int endPos = (next == -1) ? text.length() : next + 3;
                tokens.add(text.substring(i, endPos));
                i = endPos;
            } else if (text.charAt(i) == ' ' || text.charAt(i) == '\n') {
                tokens.add(String.valueOf(text.charAt(i)));
                i++;
            } else {
                int start = i;
                while (i < text.length() && text.charAt(i) != ' ' && text.charAt(i) != '\n' && !text.startsWith("```", i)) {
                    i++;
                }
                tokens.add(text.substring(start, i));
            }
        }
        return tokens;
    }

    private String cleanJson(String source) {
        if (source == null) return "";
        return source.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }

    private String parseTextFromResponse(String responseBody) {
        int targetAnchor = responseBody.indexOf("\"candidates\"");
        if (targetAnchor == -1) return "Error: Handshake confirmation structural anomaly. Check API string.";
        int textLabel = responseBody.indexOf("\"text\"", targetAnchor);
        if (textLabel == -1) return "Error: Response streaming generation array dropped.";
        int valueStart = responseBody.indexOf("\"", textLabel + 6);
        if (valueStart == -1) return "Error reading layout arrays.";
        valueStart += 1;
        
        StringBuilder resultText = new StringBuilder();
        boolean structuralEscape = false;
        for (int i = valueStart; i < responseBody.length(); i++) {
            char current = responseBody.charAt(i);
            if (structuralEscape) {
                if (current == 'n') resultText.append('\n');
                else if (current == 't') resultText.append('\t');
                else if (current == '\"') resultText.append('\"');
                else if (current == '\\') resultText.append('\\');
                else resultText.append(current);
                structuralEscape = false;
            } else {
                if (current == '\\') {
                    structuralEscape = true;
                } else if (current == '"') {
                    break;
                } else {
                    resultText.append(current);
                }
            }
        }
        return resultText.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AmanyuAiApp().setVisible(true));
    }
}