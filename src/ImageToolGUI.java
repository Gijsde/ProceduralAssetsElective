import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class ImageToolGUI extends JFrame {

    private JLabel imageLabel;

    // values that will be needed for the different steps in the generation of the mountain
    private BufferedImage currentImage;
    private boolean[] mask;
    private List<Integer> outline;
    private List<Integer> spurs;
    private int[] heightmap;
    private float[][] factorArray;
    private int WIDTH;
    private int HEIGHT;

    // Add these as class-level boolean flags
    private boolean isImported = false;
    private boolean isCleaned = false;
    private boolean isSpursDone = false;
    private boolean isHeightmapDone = false;

    private JButton importButton, cleanupImage, calculateSpurButton, fillHeightButton, exportPNGButton, exportOBJButton;
    private JProgressBar progressBar;

    // Example parameters
    private JTextField spurCounTextField;
    private JTextField spurFunction;
    private JTextField heightFunctionTextField;
    private JTextField noiseRandomnessTextField;
    private JTextField noiseStrengthTextField;
    private JTextField outnameTextField;
    private JTextField heightFactorTextField;
    private JTextField maxSizeTextField;

    public ImageToolGUI() {
        setTitle("Image Tool");
        setSize(900, 700); // Slightly larger to accommodate the progress bar
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ---- LEFT PANEL (controls) ----
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(250, 0));

        // Initialize Buttons
        importButton = new JButton("Import PNG");
        cleanupImage = new JButton("Clean up image");
        calculateSpurButton = new JButton("Calculate Spurs (Expensive)");
        fillHeightButton = new JButton("Fill Heightmap");
        exportPNGButton = new JButton("Export PNG");
        exportOBJButton = new JButton("Export OBJ");

        // Initialize Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // Set initial enabled states
        cleanupImage.setEnabled(false);
        calculateSpurButton.setEnabled(false);
        fillHeightButton.setEnabled(false);
        exportPNGButton.setEnabled(false);
        exportOBJButton.setEnabled(false);

        // Setup TextFields (using your existing naming)
        spurCounTextField = new JTextField();
        spurFunction = new JTextField();
        heightFunctionTextField = new JTextField();
        noiseRandomnessTextField = new JTextField();
        noiseStrengthTextField = new JTextField();
        outnameTextField = new JTextField();
        heightFactorTextField = new JTextField();
        maxSizeTextField = new JTextField();

        // Grouping all components for formatting
        JComponent[] components = {
            importButton, cleanupImage, spurCounTextField, spurFunction, 
            calculateSpurButton, heightFunctionTextField, noiseRandomnessTextField, 
            noiseStrengthTextField, fillHeightButton, outnameTextField, exportPNGButton, heightFactorTextField, maxSizeTextField, exportOBJButton, progressBar
        };

        for (JComponent c : components) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (c instanceof JTextField) c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        }

        // Add to panel with labels
        controlPanel.add(importButton);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(cleanupImage);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(new JLabel("Spur Count:"));
        controlPanel.add(spurCounTextField);
        controlPanel.add(new JLabel("Spur Function:"));
        controlPanel.add(spurFunction);
        controlPanel.add(calculateSpurButton);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(new JLabel("Heightmap Function:"));
        controlPanel.add(heightFunctionTextField);
        controlPanel.add(Box.createVerticalStrut(20));
        
        controlPanel.add(new JLabel("noise randomness Function:"));
        controlPanel.add(noiseRandomnessTextField);
        controlPanel.add(Box.createVerticalStrut(20));
        
        controlPanel.add(new JLabel("noise strength Function:"));
        controlPanel.add(noiseStrengthTextField);
        controlPanel.add(fillHeightButton);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(new JLabel("name for file:"));
        controlPanel.add(outnameTextField);
        controlPanel.add(new JLabel("Progress:"));
        controlPanel.add(progressBar);


        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(exportPNGButton);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(new JLabel("factor compared to the max size:"));
        controlPanel.add(heightFactorTextField);
        controlPanel.add(new JLabel("max size in m:"));
        controlPanel.add(maxSizeTextField);
        controlPanel.add(exportOBJButton);

        // ---- DOCUMENT LISTENERS ----
        DocumentListener fieldChecker = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateStates(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateStates(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateStates(); }
        };
        spurCounTextField.getDocument().addDocumentListener(fieldChecker);
        spurFunction.getDocument().addDocumentListener(fieldChecker);
        heightFunctionTextField.getDocument().addDocumentListener(fieldChecker);
        noiseRandomnessTextField.getDocument().addDocumentListener(fieldChecker);
        noiseStrengthTextField.getDocument().addDocumentListener(fieldChecker);
        outnameTextField.getDocument().addDocumentListener(fieldChecker);
        heightFactorTextField.getDocument().addDocumentListener(fieldChecker);
        maxSizeTextField.getDocument().addDocumentListener(fieldChecker);

        // ---- BUTTON ACTIONS ----
        importButton.addActionListener(e -> {
            importImage();
            isImported = true;
            isCleaned = false;
            isSpursDone = false;
            isHeightmapDone = false;
            updateStates();
        });

        cleanupImage.addActionListener(e -> {
            cleanupImageAction(); // rename your existing cleanupImage() to avoid conflict
            isCleaned = true;
            isSpursDone = false;
            isHeightmapDone = false;
            updateStates();
        });

        calculateSpurButton.addActionListener(e ->  {
            runSpurTask();
            isSpursDone = true;
            isHeightmapDone = false;
            updateStates();
        });

        fillHeightButton.addActionListener(e -> {
            runHeightmapTask();
            isHeightmapDone = true;
        });

        exportPNGButton.addActionListener(e -> exportImage());

        exportOBJButton.addActionListener(e -> {
            try {
                exportObject();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        // ---- IMAGE DISPLAY ----
        imageLabel = new JLabel("", JLabel.CENTER);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        add(controlPanel, BorderLayout.WEST);
        add(imageScrollPane, BorderLayout.CENTER);
    }

    private void importImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
            new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png")
        );

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentImage = ImageIO.read(chooser.getSelectedFile());
                WIDTH = currentImage.getWidth();
                HEIGHT = currentImage.getHeight();
                imageLabel.setIcon(new ImageIcon(currentImage));
            } catch (IOException ex) {
                showError("Failed to load image");
            }
        }
    }

    private void cleanupImageAction() {
        mask = ImageRW.LoadImageToMask(currentImage, HEIGHT, WIDTH);
        outline = Outline.cleanupMask(mask, HEIGHT, WIDTH, Precompute.precomputeBlacklist());

        currentImage = Helper.booleanArrayToImage(mask, HEIGHT, WIDTH);
        imageLabel.setIcon(new ImageIcon(currentImage));
    }

    private void createSpurs() throws InterruptedException, ExecutionException {
        int spurcount = Integer.parseInt(spurCounTextField.getText());
        int func = Integer.parseInt(spurFunction.getText()); 

        spurs = Spurs.createSpurs(outline, HEIGHT, WIDTH, spurcount);
        factorArray = Heightmap.createFactorHeightArray(outline, spurs, HEIGHT, WIDTH, func);
        List<Integer> temp = new ArrayList<>(spurs);
        temp.addAll(outline);

        currentImage = Helper.IListToImage(temp, HEIGHT, WIDTH);
        imageLabel.setIcon(new ImageIcon(currentImage));
    }

    private void runHeightmapTask() {
        heightmap = Heightmap.makeHeightmap(factorArray, Integer.parseInt(heightFunctionTextField.getText()));

        int[] noise = Noise.generatePerlin(Integer.parseInt(noiseRandomnessTextField.getText()), HEIGHT, WIDTH);

        heightmap = Noise.applyNoise(heightmap, noise, Double.parseDouble(noiseStrengthTextField.getText()));

        currentImage = Helper.intArrayToImage(heightmap, HEIGHT, WIDTH);
        imageLabel.setIcon(new ImageIcon(currentImage));
        isHeightmapDone = true;
    }


    private void exportImage() {
        File file = new File("images/output/" + outnameTextField.getText() + ".png");

        ImageRW.writeIntMaskToFile(heightmap, file, HEIGHT, WIDTH);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void exportObject() throws IOException {
        float maxSize = Float.parseFloat(maxSizeTextField.getText());
        float heightFactor = Float.parseFloat(heightFactorTextField.getText());
        String filePath = "objects/" + outnameTextField.getText() + ".obj";
        Object.writeOBJ(heightmap, WIDTH, HEIGHT, maxSize, maxSize, heightFactor, filePath);

    }

    private void updateStates() {
        boolean spursReady = !spurCounTextField.getText().trim().isEmpty() && 
                             !spurFunction.getText().trim().isEmpty();
        boolean heightReady = !heightFunctionTextField.getText().trim().isEmpty() &&
                              !noiseRandomnessTextField.getText().trim().isEmpty() &&
                              !noiseStrengthTextField.getText().trim().isEmpty();

        boolean exportOBJ = !outnameTextField.getText().trim().isEmpty() &&
                            !maxSizeTextField.getText().trim().isEmpty() &&
                            !heightFactorTextField.getText().trim().isEmpty();
    
        cleanupImage.setEnabled(isImported);
        calculateSpurButton.setEnabled(isCleaned && spursReady);
        fillHeightButton.setEnabled(isSpursDone && heightReady);
        System.out.println(isHeightmapDone);
        exportPNGButton.setEnabled(!outnameTextField.getText().trim().isEmpty());
        exportOBJButton.setEnabled(exportOBJ);
    }

    private void runSpurTask() {
        // UI Feedback: Lock the button and start the bar
        calculateSpurButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Calculating Spurs...");
    
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                createSpurs(); // Your multi-threaded logic
                return null;
            }
    
            @Override
            protected void done() {
                try {
                    get(); // check for errors
                    isSpursDone = true;
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Spurs Done!");
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(ImageToolGUI.this, "Error: " + ex.getMessage());
                }
                updateStates();
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new ImageToolGUI().setVisible(true)
        );
    }
}
