package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Home extends JFrame {


    public Home() {
        setTitle("DocSync - Editor");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        topBar.add(leftButtons, BorderLayout.WEST);
        topBar.add(rightButtons, BorderLayout.EAST);

        // File Explorer Panel
        JPanel fileExplorerPanel = new JPanel(new BorderLayout());
        fileExplorerPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
//        fileExplorerPanel.setBackground(); // Example background
//        fileExplorerPanel.setPreferredSize(new Dimension(200, 0)); // Set initial width

        // Text editor panel (Markdown Editor)
        JTextArea editorArea = new JTextArea();
        editorArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane editorScrollPane = new JScrollPane(editorArea);

        // Markdown Preview Panel (Initially empty)
        JTextPane previewPane = new JTextPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        JScrollPane previewScrollPane = new JScrollPane(previewPane);

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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ThemeManager.applyTheme();
            new Home().setVisible(true);
        });
    }
}