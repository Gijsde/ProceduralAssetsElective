import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;


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
    private int CENTER;
    private int SPURCOUNT;

    // Example parameters
    private JTextField spurCounTextField;
    private JTextField spurFunction;
    private JTextField heightFunctiontTextField;
    private JTextField noiseRandomnessTextField;
    private JTextField noiseStrengthTextField;

    public ImageToolGUI() {
        setTitle("Image Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // ---- LEFT PANEL (controls) ----
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(220, 0));
        
        JButton importButton = new JButton("Import PNG");
        JButton cleanupImage = new JButton("Clean up image");
        JButton calculateSpurButton = new JButton("Calculate Spurs, Expensive!!!");
        JButton fillHeightButton = new JButton("Fill in the rest of the heightmap");

        JButton exportButton = new JButton("Export PNG");
        
        spurCounTextField = new JTextField();
        spurFunction = new JTextField();
        heightFunctiontTextField = new JTextField();
        noiseRandomnessTextField = new JTextField();
        noiseStrengthTextField = new JTextField();
        
        // Sizing fixes
        spurCounTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        spurFunction.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        heightFunctiontTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        noiseRandomnessTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        noiseStrengthTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        // Alignment fixes
        importButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        cleanupImage.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        spurCounTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        spurFunction.setAlignmentX(Component.LEFT_ALIGNMENT);
        heightFunctiontTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiseRandomnessTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiseStrengthTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        fillHeightButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        controlPanel.add(importButton);
        controlPanel.add(Box.createVerticalStrut(15));

        controlPanel.add(cleanupImage);
        controlPanel.add(Box.createVerticalStrut(15));
        
        controlPanel.add(new JLabel("spurcount:"));
        controlPanel.add(spurCounTextField);
        controlPanel.add(Box.createVerticalStrut(10));

        controlPanel.add(new JLabel("Spur function:"));
        controlPanel.add(spurFunction);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(calculateSpurButton);
        controlPanel.add(Box.createVerticalStrut(15));

        controlPanel.add(new JLabel("heightmap function:"));
        controlPanel.add(heightFunctiontTextField);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(new JLabel("noise randomness function:"));
        controlPanel.add(noiseRandomnessTextField);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(new JLabel("noise strength function:"));
        controlPanel.add(noiseStrengthTextField);
        controlPanel.add(Box.createVerticalStrut(20));

        controlPanel.add(fillHeightButton);
        controlPanel.add(Box.createVerticalStrut(15));
        
        controlPanel.add(exportButton);
        
        // ---- IMAGE DISPLAY ----
        imageLabel = new JLabel("", JLabel.CENTER);
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);

        // ---- BUTTON ACTIONS ----
        importButton.addActionListener(e -> importImage());
        cleanupImage.addActionListener(e -> cleanupImage());
        calculateSpurButton.addActionListener((var e) -> {
            try {
                createSpurs();
            } catch (InterruptedException | ExecutionException e1) {
                System.out.println("calculating did not work");
            }
            
        });
        fillHeightButton.addActionListener(e -> fillHeightmap());
        exportButton.addActionListener(e -> exportImage());


        // ---- ADD TO FRAME ----
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

    private void cleanupImage() {
        mask = ImageRW.LoadImageToMask(currentImage, HEIGHT, WIDTH);
        outline = Outline.cleanupMask(mask, HEIGHT, WIDTH, Precompute.precomputeBlacklist());
        CENTER = Helper.findCenter(outline, WIDTH);

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

    private void fillHeightmap() {
        heightmap = Heightmap.makeHeightmap(factorArray, Integer.parseInt(heightFunctiontTextField.getText()));

        int[] noise = Noise.generatePerlin(Integer.parseInt(noiseRandomnessTextField.getText()), HEIGHT, WIDTH);

        heightmap = Noise.applyNoise(heightmap, noise, Double.parseDouble(noiseStrengthTextField.getText()));

        currentImage = Helper.intArrayToImage(heightmap, HEIGHT, WIDTH);
        imageLabel.setIcon(new ImageIcon(currentImage));
    }


    private void exportImage() {
        if (currentImage == null) {
            showError("No image to export");
            return;
        }

        // BufferedImage resultImage = processImage(currentImage, paramA, paramB);

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("output.png"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(currentImage, "png", chooser.getSelectedFile());
            } catch (IOException ex) {
                showError("Failed to export image");
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new ImageToolGUI().setVisible(true)
        );
    }
}
