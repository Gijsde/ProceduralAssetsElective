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

/*
note: the GUI was made heavily with AI. I personally don't enjoy creating the user interfaces,
I much prefer making the underlying product. For this reason I decided to heavily vibe code this file.
*/
public class GUI extends JFrame {

    private final JLabel imageLabel;

    // values needed for generation
    private BufferedImage currentImage;
    private boolean[] mask;
    private List<Integer> outline;
    private List<Integer> spurs;
    private int[] heightmap;
    private float[][] factorArray;
    private int imageWidth;
    private int imageHeight;

    // flags
    private boolean isImported = false;
    private boolean isCleaned = false;
    private boolean isSpursDone = false;
    private boolean isHeightmapDone = false;

    // controls
    private JButton importButton;
    private JButton cleanupImageButton;
    private JButton calculateSpurButton;
    private JButton fillHeightButton;
    private JButton exportPNGButton;
    private JButton exportOBJButton;
    private JProgressBar progressBar;

    // fields / spinners
    private JSpinner spurCountSpinner;
    private JSpinner spurFunctionSpinner;
    private JSpinner heightFunctionSpinner;
    private JSpinner noiseRandomnessSpinner;
    private JSpinner noiseStrengthSpinner;
    private JTextField outnameTextField;
    private JSpinner heightFactorSpinner;
    private JSpinner maxSizeSpinner;

    public GUI() {
        super("Image Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        layoutControls();
        initListeners();

        imageLabel = new JLabel("", JLabel.CENTER);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        add(imageScrollPane, BorderLayout.CENTER);
    }

    // Initialize UI components (spinners, buttons, progress bar)
    private void initComponents() {
        importButton = new JButton("Import PNG");
        cleanupImageButton = new JButton("Clean up image");
        calculateSpurButton = new JButton("Calculate Spurs (Expensive)");
        fillHeightButton = new JButton("Fill Heightmap");
        exportPNGButton = new JButton("Export PNG");
        exportOBJButton = new JButton("Export OBJ");

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // Numeric spinners with sensible defaults and ranges
        spurCountSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 10, 1));
        spurFunctionSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        heightFunctionSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        noiseRandomnessSpinner = new JSpinner(new SpinnerNumberModel(20, 0, 100, 1));
        noiseStrengthSpinner = new JSpinner(new SpinnerNumberModel(0.1d, 0.0d, 10.0d, 0.1d));
        heightFactorSpinner = new JSpinner(new SpinnerNumberModel(1.0d, 0.0d, 10.0d, 0.1d));
        maxSizeSpinner = new JSpinner(new SpinnerNumberModel(1000.0d, 1.0d, 100_000.0d, 1.0d));
        outnameTextField = new JTextField();

        // initial enabled states
        cleanupImageButton.setEnabled(false);
        calculateSpurButton.setEnabled(false);
        fillHeightButton.setEnabled(false);
        exportPNGButton.setEnabled(false);
        exportOBJButton.setEnabled(false);

        // tooltips
        spurCountSpinner.setToolTipText("Number of spurs to generate (integer).");
        spurFunctionSpinner.setToolTipText("Function selector for spur strength.");
        heightFunctionSpinner.setToolTipText("Function selector for base height.");
        noiseRandomnessSpinner.setToolTipText("Seed/scale for noise generation.");
        noiseStrengthSpinner.setToolTipText("Noise strength as multiplier.");
        heightFactorSpinner.setToolTipText("Scale factor for heights when exporting OBJ.");
        maxSizeSpinner.setToolTipText("Max size in meters (for OBJ scaling).");
        outnameTextField.setToolTipText("Name used for exported files (PNG/OBJ).");
    }

    private void layoutControls() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(300, 0));

        // helper to add labeled component
        java.util.function.BiConsumer<String, JComponent> addLabeled = (label, comp) -> {
            controlPanel.add(new JLabel(label));
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            controlPanel.add(comp);
            controlPanel.add(Box.createVerticalStrut(8));
        };

        controlPanel.add(importButton);
        controlPanel.add(Box.createVerticalStrut(8));
        controlPanel.add(cleanupImageButton);
        controlPanel.add(Box.createVerticalStrut(12));

        addLabeled.accept("Spur Count:", spurCountSpinner);
        addLabeled.accept("Spur Function:", spurFunctionSpinner);
        controlPanel.add(calculateSpurButton);
        controlPanel.add(Box.createVerticalStrut(12));

        addLabeled.accept("Heightmap Function:", heightFunctionSpinner);
        addLabeled.accept("Noise randomness:", noiseRandomnessSpinner);
        addLabeled.accept("Noise strength:", noiseStrengthSpinner);
        controlPanel.add(fillHeightButton);
        controlPanel.add(Box.createVerticalStrut(12));

        addLabeled.accept("Output name:", outnameTextField);
        controlPanel.add(new JLabel("Progress:"));
        controlPanel.add(progressBar);
        controlPanel.add(Box.createVerticalStrut(10));

        controlPanel.add(exportPNGButton);
        controlPanel.add(Box.createVerticalStrut(8));
        addLabeled.accept("Height factor:", heightFactorSpinner);
        addLabeled.accept("Max size (m):", maxSizeSpinner);
        controlPanel.add(exportOBJButton);

        add(controlPanel, BorderLayout.WEST);
    }

    private void initListeners() {
        importButton.addActionListener(e -> {
            importImage();
            isImported = currentImage != null;
            isCleaned = false;
            isSpursDone = false;
            isHeightmapDone = false;
            updateStates();
        });

        cleanupImageButton.addActionListener(e -> {
            cleanupImageAction();
            isCleaned = true;
            isSpursDone = false;
            isHeightmapDone = false;
            updateStates();
        });

        calculateSpurButton.addActionListener(e -> runSpurTask());
        fillHeightButton.addActionListener(e -> runHeightmapTask());
        exportPNGButton.addActionListener(e -> runExportPNGTask());
        exportOBJButton.addActionListener(e -> runExportOBJTask());

        // enable/disable controls based on text input (only outname is text)
        outnameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateStates(); }
            @Override public void removeUpdate(DocumentEvent e) { updateStates(); }
            @Override public void changedUpdate(DocumentEvent e) { updateStates(); }
        });
    }

    private void importImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentImage = ImageIO.read(chooser.getSelectedFile());
                if (currentImage != null) {
                    imageWidth = currentImage.getWidth();
                    imageHeight = currentImage.getHeight();
                    imageLabel.setIcon(new ImageIcon(currentImage));
                } else {
                    showError("Selected file is not a valid image.");
                }
            } catch (IOException ex) {
                showError("Failed to load image: " + ex.getMessage());
            }
        }
    }

    private void cleanupImageAction() {
        if (currentImage == null) {
            showError("No image loaded.");
            return;
        }
        mask = ImageRW.LoadImageToMask(currentImage, imageHeight, imageWidth);
        outline = Outline.cleanupMask(mask, imageHeight, imageWidth, Precompute.precomputeBlacklist());
        currentImage = Helper.booleanArrayToImage(mask, imageHeight, imageWidth);
        imageLabel.setIcon(new ImageIcon(currentImage));
    }

    // Creates spurs on background thread, updates UI when done
    private void runSpurTask() {
        if (outline == null) {
            showError("You must clean the image before calculating spurs.");
            return;
        }

        int spurcount = ((Number) spurCountSpinner.getValue()).intValue();
        int func = ((Number) spurFunctionSpinner.getValue()).intValue();

        calculateSpurButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Calculating Spurs...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                spurs = Spurs.createSpurs(outline, imageHeight, imageWidth, spurcount);
                factorArray = Heightmap.createFactorHeightArray(outline, spurs, imageHeight, imageWidth, func);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    // show combined outline+spurs image
                    List<Integer> temp = new ArrayList<>(spurs);
                    temp.addAll(outline);
                    currentImage = Helper.IListToImage(temp, imageHeight, imageWidth);
                    imageLabel.setIcon(new ImageIcon(currentImage));

                    isSpursDone = true;
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Spurs Done!");
                } catch (InterruptedException | ExecutionException ex) {
                    showError("Error creating spurs: " + ex.getMessage());
                } finally {
                    calculateSpurButton.setEnabled(true);
                    updateStates();
                }
            }
        };
        worker.execute();
    }

    // Runs heightmap generation and noise application on a background thread
    private void runHeightmapTask() {
        if (factorArray == null) {
            showError("Spurs/factor array not ready.");
            return;
        }

        int heightFunc = ((Number) heightFunctionSpinner.getValue()).intValue();
        int noiseRandomness = ((Number) noiseRandomnessSpinner.getValue()).intValue();
        double noiseStrength = ((Number) noiseStrengthSpinner.getValue()).doubleValue();

        fillHeightButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Generating heightmap...");

        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() throws Exception {
                int[] hm = Heightmap.makeHeightmap(factorArray, heightFunc);
                int[] noise = Noise.generatePerlin(noiseRandomness, imageHeight, imageWidth);
                hm = Noise.applyNoise(hm, noise, noiseStrength);
                return hm;
            }

            @Override
            protected void done() {
                try {
                    heightmap = get();
                    currentImage = Helper.intArrayToImage(heightmap, imageHeight, imageWidth);
                    imageLabel.setIcon(new ImageIcon(currentImage));
                    isHeightmapDone = true;
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    progressBar.setString("Heightmap Done!");
                } catch (InterruptedException | ExecutionException ex) {
                    showError("Error generating heightmap: " + ex.getMessage());
                } finally {
                    fillHeightButton.setEnabled(true);
                    updateStates();
                }
            }
        };
        worker.execute();
    }

    // Export PNG on a background thread
    private void runExportPNGTask() {
        if (heightmap == null) {
            showError("No heightmap available to export.");
            return;
        }
        String name = outnameTextField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter an output name.");
            return;
        }

        exportPNGButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Exporting PNG...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                File file = new File("images/output/" + name + ".png");
                ImageRW.writeIntMaskToFile(heightmap, file, imageHeight, imageWidth);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    progressBar.setString("PNG Exported");
                } catch (InterruptedException | ExecutionException ex) {
                    showError("Error exporting PNG: " + ex.getMessage());
                } finally {
                    exportPNGButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                }
            }
        };
        worker.execute();
    }

    // Export OBJ on background thread
    private void runExportOBJTask() {
        if (heightmap == null) {
            showError("No heightmap available to export.");
            return;
        }
        String name = outnameTextField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter an output name.");
            return;
        }

        double heightFactor = ((Number) heightFactorSpinner.getValue()).doubleValue();
        double maxSize = ((Number) maxSizeSpinner.getValue()).doubleValue();

        exportOBJButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setString("Exporting OBJ...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String filePath = "obj/output/" + name + ".obj";
                OBJWriter.writeOBJ(heightmap, imageWidth, imageHeight, (float) maxSize, (float) maxSize, (float) heightFactor, filePath);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    progressBar.setString("OBJ Exported");
                } catch (InterruptedException | ExecutionException ex) {
                    showError("Error exporting OBJ: " + ex.getMessage());
                } finally {
                    exportOBJButton.setEnabled(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                }
            }
        };
        worker.execute();
    }

    private void updateStates() {
        boolean spursReady = true; // spinners always contain values
        boolean heightReady = true; // spinners always contain values

        boolean exportOBJ = !outnameTextField.getText().trim().isEmpty();

        cleanupImageButton.setEnabled(isImported);
        calculateSpurButton.setEnabled(isCleaned && spursReady);
        fillHeightButton.setEnabled(isSpursDone && heightReady);
        exportPNGButton.setEnabled(!outnameTextField.getText().trim().isEmpty() && isHeightmapDone);
        exportOBJButton.setEnabled(exportOBJ && isHeightmapDone);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI g = new GUI();
            g.setVisible(true);
        });
    }
}