package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private JButton toggleSidebarButton, newNoteButton, searchButton, favoritesButton;
    private JButton profileButton, previewButton;
    private JPanel sidebar, editorPanel, fileExplorerPanel, topPanel;
    private JScrollPane previewPanel;
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
        this.sessionId = sessionId;
        if (sessionId.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please Login first", "Error", JOptionPane.ERROR_MESSAGE);
            new LoginForm().setVisible(true);
            dispose();
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
        JButton search = new JButton("🔍");
        JButton favorites = new JButton("★");

        // Profile & Preview toggle button on the right
        JButton saveButton = new JButton("save");
        JButton previewToggle = new JButton("👁️");
        JButton profile = new JButton("👤");

        leftButtons.add(collapseSidebar);
        leftButtons.add(newNote);
        leftButtons.add(search);
        leftButtons.add(favorites);

        rightButtons.add(saveButton);
        rightButtons.add(previewToggle);
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
        textEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textEditor.addCaretListener(e -> updatePreview());

        editorScrollPane = new RTextScrollPane(textEditor);
        editorPanel.add(editorScrollPane, BorderLayout.CENTER);
        

        // Markdown Preview Panel (Initially empty and empty)
        previewPane = new JTextPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        JScrollPane previewScrollPane = new JScrollPane(previewPane);
        previewPane.setVisible(false);
        previewScrollPane.setVisible(false);

        JSplitPane editorAndPreviewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorScrollPane, previewScrollPane);
        editorAndPreviewSplitPane.setResizeWeight(0.5); // Both get equal space initially

        // Split pane for Editor & Preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileExplorerPanel, editorAndPreviewSplitPane);
        splitPane.setResizeWeight(0.30);

        // Layout Setup
        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
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

        newNote.addActionListener(e->{
            createNewFile();
        });

        saveButton.addActionListener(e->{
            handleSaveFile(e);
        });
//        hideEditor();
    }

    class File{
        private String name;
        private String code;
        private int id;

        public File(String name, int id) {
            this.name = name;
            this.id = id;
        }


        public static boolean checkExists(String filename, List<File> fileList){
            for (File file: fileList){
                if (filename.equals(file.name)){
                    return true;
                }
            }
            return false;
        }
    }

    private void createNewFile(){
        String fileName = JOptionPane.showInputDialog(this, "Enter File name: ", "New file", JOptionPane.PLAIN_MESSAGE);
        if (fileName!=null && !fileName.trim().isEmpty()){
            if (File.checkExists(fileName, fileList)){
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

        for (File file : fileList) {
//            System.out.println(file);

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

//    private void handleCreateFile(ActionEvent e) {
//        String fileName = JOptionPane.showInputDialog(this, "Enter file name:");
//        if (fileName == null || fileName.trim().isEmpty()) {
//            JOptionPane.showMessageDialog(this, "File name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        sendRequest("CREATE_FILE|" + sessionId + "|" + fileName);
//        queryAllFiles();
//    }

//    private void handleDeleteFile(ActionEvent e) {
//        if (currentFileId == null) {
//            JOptionPane.showMessageDialog(this, "Select a file to delete.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//        sendRequest("DELETE_FILE|" + sessionId + "|" + currentFileId);
//        queryAllFiles();
//        fileContentArea.setText("");
//        currentFileId = null;
//    }

    private void handleSaveFile(ActionEvent e) {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Select a file to save.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String content = textEditor.getText();
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));

//        String content = fileContentArea.getText();
        sendRequest("UPDATE_FILE|" + sessionId + "|" + currentFile.id + "|" + encodedContent);
    }

    private void loadFileContent(int fileId) {
        String response = sendRequest("QUERY_FILE|" + sessionId + "|" + fileId);
        if (response.startsWith("SUCCESS|")) {
            textEditor.setText(response.substring(8));
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

        if (response.startsWith("SUCCESS|")) {
            String[] files = response.substring(8).split(",");
            for (String file : files) {
                System.out.println(file);
                if (!file.isEmpty()){
//                    System.out.println(file);
//                System.out.println(file.split(":")[0] + " " + file.split(":")[1]);
                    int id = Integer.parseInt(file.split(":")[0]);
                    String name = file.split(":")[1];
                    fileList.add(new File(name, id));
                }
//                // Format: "id:fileName"
            }
            return;
        }
        JOptionPane.showMessageDialog(this, response, "Error", JOptionPane.ERROR_MESSAGE);
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThemeManager.applyTheme();
            new Home("").setVisible(true);
        });
    }
}