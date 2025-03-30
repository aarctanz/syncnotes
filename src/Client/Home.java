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

public class Home extends JFrame {
    private JButton toggleSidebarButton, newNoteButton, searchButton, favoritesButton;
    private JButton profileButton, previewButton;
    private JPanel sidebar, editorPanel, fileExplorerPanel, topPanel;
    private JScrollPane previewPanel;
    private JTextPane previewPane;
    private JLabel openedFileLabel;
    private RSyntaxTextArea textEditor;
    private RTextScrollPane editorScrollPane;
    private List<String> fileList;
    private String currentFile;
    private Parser markdownParser;
    private HtmlRenderer htmlRenderer;

    public Home() {
        setTitle("DocSync - Editor");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fileList = new ArrayList<>();

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
        JButton previewToggle = new JButton("👁️");
        JButton profile = new JButton("👤");

        leftButtons.add(collapseSidebar);
        leftButtons.add(newNote);
        leftButtons.add(search);
        leftButtons.add(favorites);

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
    }

    private void createNewFile(){
        String fileName = JOptionPane.showInputDialog(this, "Enter File name: ", "New file", JOptionPane.PLAIN_MESSAGE);
        if (fileName!=null && !fileName.trim().isEmpty()){
            fileList.add(fileName);
//            System.out.println(fileList);
            currentFile = fileName;
            openedFileLabel.setText(currentFile);
            updateFileExplorer();

            // Todo: add new file to the database and refetch all the files.
        }
    }

    private void openFile(String fileName){
        currentFile = fileName;
        openedFileLabel.setText(currentFile);
        updateFileExplorer();
    }

    private void updateFileExplorer() {
        fileExplorerPanel.removeAll();

        for (String file : fileList) {
//            System.out.println(file);

            JButton fileButton = new JButton(file);
            fileButton.setHorizontalAlignment(SwingConstants.LEFT);
            fileButton.setFocusPainted(false);
            fileButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding

            if (!file.equals(currentFile)) {
                fileButton.setBackground(UIManager.getColor("Button.focusedBackground"));
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

    private void hideEditor(){
        editorPanel.setVisible(false);
        editorScrollPane.setVisible(false);
        textEditor.setVisible(false);
    }

    private void showEditor(){
        editorPanel.setVisible(true);
        editorScrollPane.setVisible(true);
        textEditor.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThemeManager.applyTheme();
            new Home().setVisible(true);
        });
    }
}