import java.awt.*;
import java.io.File;
import javax.swing.*;

public class GUI extends JFrame {

    private final JLabel imageLabel;

    public GUI() {
        setTitle("PNG Image Viewer");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Button
        JButton importButton = new JButton("Import PNG");

        // Image display label
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        // Button action
        importButton.addActionListener(e -> openImage());

        // Layout
        setLayout(new BorderLayout());
        add(importButton, BorderLayout.WEST);
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GUI().setVisible(true);
        });
    }
}
