package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import com.vladsch.flexmark.util.ast.Node;

import java.net.*;
import java.io.*;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class Home extends JFrame {
    private JPanel  editorPanel, fileExplorerPanel, topPanel;
    private JTextPane previewPane;
    private JLabel openedFileLabel;
    private RSyntaxTextArea textEditor;
    private RTextScrollPane editorScrollPane;
    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;

    private List<File> fileList;
    private File currentFile;

    private String sessionId;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public Home(String sessionId) {

//      If user is not logged in, move to the login page.
        this.sessionId = sessionId;
        if (sessionId.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please Login first", "Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm().setVisible(true);
            dispose();
            return;
        }
        fileList = new ArrayList<>();
        queryAllFiles();

        setTitle("SyncNotes - Editor");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        currentFile = null;

        markdownParser = Parser.builder().build();
        htmlRenderer = HtmlRenderer.builder().build();

        // Top bar with buttons
        JPanel topBar = new JPanel(new BorderLayout());
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton collapseSidebar = new JButton("☰");
        JButton newNote = new JButton("+");
//        JButton search = new JButton("🔍");
//        JButton favorites = new JButton("★");

        // Profile & Preview toggle button on the right
        JButton saveButton = new JButton("save");
        JButton previewToggle = new JButton("👁️");
        JButton profile = new JButton("👤");
        JButton deleteButton = new JButton("\uE872");

        leftButtons.add(collapseSidebar);
        leftButtons.add(newNote);
//        leftButtons.add(search);
//        leftButtons.add(favorites);

        rightButtons.add(saveButton);
        rightButtons.add(previewToggle);
        rightButtons.add(deleteButton);
        rightButtons.add(profile);

        openedFileLabel = new JLabel("No file opened.");
        openedFileLabel.setHorizontalAlignment(SwingConstants.CENTER);

        topBar.add(leftButtons, BorderLayout.WEST);
        topBar.add(openedFileLabel, BorderLayout.CENTER);
        topBar.add(rightButtons, BorderLayout.EAST);

        // File Explorer Panel
        fileExplorerPanel = new JPanel(new BorderLayout());
        fileExplorerPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        fileExplorerPanel.setLayout(new BoxLayout(fileExplorerPanel, BoxLayout.Y_AXIS));
        updateFileExplorer();


        // Text editor panel (Markdown Editor)
        editorPanel = new JPanel(new BorderLayout());
        textEditor = new RSyntaxTextArea();
        textEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        textEditor.setCodeFoldingEnabled(true);
        textEditor.setFont(new Font("Monospaced", Font.PLAIN, 18));

//      Caret listener(listens to every text change in editor) updates preview on every change
        textEditor.addCaretListener(e -> updatePreview());

        editorScrollPane = new RTextScrollPane(textEditor);
        editorPanel.add(editorScrollPane, BorderLayout.CENTER);
        

        // Markdown Preview Panel (Initially empty and hidden)
        previewPane = new JTextPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JScrollPane previewScrollPane = new JScrollPane(previewPane);
        previewPane.setVisible(false);
        previewScrollPane.setVisible(false);

//      Inner split panel for text editor and preview panel
        JSplitPane editorAndPreviewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorScrollPane, previewScrollPane);
        editorAndPreviewSplitPane.setResizeWeight(0.5); // Both get equal space initially

//      Outer Split panel for FileExplorer and inner split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileExplorerPanel, editorAndPreviewSplitPane);
        splitPane.setResizeWeight(0.30);

        // Layout Setup
        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

//      Special event listener to recalculate inner split panel and file explorer width when
//      toggling file explorer panel or preview panel
        previewToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (previewPane.isVisible()) {
                    editorAndPreviewSplitPane.setDividerLocation(1.0);
                    previewPane.setVisible(false);
                    previewScrollPane.setVisible(false);
                } else {
                    previewPane.setVisible(true);
                    previewScrollPane.setVisible(true);
                    editorAndPreviewSplitPane.resetToPreferredSizes();
                    editorAndPreviewSplitPane.setDividerLocation(0.5);
                }
                splitPane.resetToPreferredSizes(); // Important: reset main split pane too
            }
        });

        collapseSidebar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileExplorerPanel.isVisible()) {
                    splitPane.setDividerLocation(0); // Move divider to left edge
                    fileExplorerPanel.setVisible(false);
                } else {
                    fileExplorerPanel.setVisible(true);
                    splitPane.resetToPreferredSizes();
                    splitPane.setDividerLocation(splitPane.getLastDividerLocation()); // Restore last divider location
                }
            }
        });

        newNote.addActionListener(this::createNewFile);
        saveButton.addActionListener(this::handleSaveFile);
        deleteButton.addActionListener(this::handleDeleteFile);

    }

//  A custom class to hold information about notes or files.
    static class File{
        private final String name;
        private final int id;

        public File(String name, int id) {
            this.name = name;
            this.id = id;
        }


        public static boolean checkExistsWithName(String filename, List<File> fileList){
            for (File file: fileList){
                if (filename.equals(file.name)){
                    return true;
                }
            }
            return false;
        }
    }

    private void createNewFile(ActionEvent e){
        String fileName = JOptionPane.showInputDialog(this, "Enter File name: ", "New file", JOptionPane.PLAIN_MESSAGE);
        if (fileName!=null && !fileName.trim().isEmpty()){
            if (File.checkExistsWithName(fileName, fileList)){
                JOptionPane.showMessageDialog(this, "Filename already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sendRequest("CREATE_FILE|" + sessionId + "|" + fileName);
            queryAllFiles();
            updateFileExplorer();
        }
    }

    private void openFile(File file){
        currentFile = file;
        openedFileLabel.setText(currentFile.name);
        loadFileContent(currentFile.id);
        updateFileExplorer();
    }

    private void updateFileExplorer() {
        fileExplorerPanel.removeAll();

//      create buttons for every note or files and add event listeners to them for opening it.
        for (File file : fileList) {
            JButton fileButton = new JButton(file.name);
            fileButton.setHorizontalAlignment(SwingConstants.LEFT);
            fileButton.setFocusPainted(false);
            fileButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding

            if (currentFile!=null){
                if (!file.name.equals(currentFile.name)) {
                    fileButton.setBackground(UIManager.getColor("Button.focusedBackground"));
                }
            }


            fileButton.addActionListener(e -> openFile(file));

            // Wrapper panel to ensure full width
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, fileButton.getPreferredSize().height));
            wrapper.add(fileButton, BorderLayout.CENTER);

            fileExplorerPanel.add(wrapper);
        }

        fileExplorerPanel.revalidate();
        fileExplorerPanel.repaint();
    }


    private void updatePreview() {
        String markdownText = textEditor.getText();
        Node document = markdownParser.parse(markdownText);
        String html = htmlRenderer.render(document);
        previewPane.setText("<html><body>" + html + "</body></html>");
    }


    private void handleDeleteFile(ActionEvent e) {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Select a file to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        sendRequest("DELETE_FILE|" + sessionId + "|" + currentFile.id);
        queryAllFiles();
        textEditor.setText("");
        openedFileLabel.setText("");
        currentFile = null;
        updateFileExplorer();
    }

//  Uses base64 encoding to convert multiple lines into single lines to better handle it on server.
    private void handleSaveFile(ActionEvent e) {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Select a file to save.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String content = textEditor.getText();
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

        sendRequest("UPDATE_FILE|" + sessionId + "|" + currentFile.id + "|" + encodedContent);
    }

//  loads content of file into base64 format and then decodes it.
//  Response Format: (SUCCESS|%s, content in base64 format) or ERROR|Failed to retrieve file
    private void loadFileContent(int fileId) {
        String response = sendRequest("QUERY_FILE|" + sessionId + "|" + fileId);
        if (response.startsWith("SUCCESS|")) {
            String content = new String(Base64.getDecoder().decode(response.substring(8)), StandardCharsets.UTF_8);

            textEditor.setText(content);
        } else {
            JOptionPane.showMessageDialog(this, "Error loading file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void queryAllFiles() {
        String response = sendRequest("QUERY_FILES|" + sessionId);
        if (fileList!=null){
            fileList.clear();
        }else {
            fileList = new ArrayList<>();
        }

//      Format: SUCCESS|"id:fileName","id:fileName","id:fileName"...
        if (response.startsWith("SUCCESS|")) {
            String[] files = response.substring(8).split(",");

            for (String file : files) {
                if (!file.isEmpty()){
                    int id = Integer.parseInt(file.split(":")[0]);
                    String name = file.split(":")[1];
                    fileList.add(new File(name, id));
                }
            }

            return;
        }
        JOptionPane.showMessageDialog(this, response, "Error", JOptionPane.ERROR_MESSAGE);
    }

//  A wrapper method to send requests to the server
    private String sendRequest(String request) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println(request+"\n");
            return reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|Connection failed";
        }
    }

//  If home page started directly the pass sessionId as empty string.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThemeManager.applyTheme();
            new Home("").setVisible(true);
        });
    }
}