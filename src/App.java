import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private BufferedImage inputImage;
    private BufferedImage resultImage;
    private JFrame frame;
    private int squareSize = 10;
    private boolean isMultiThreaded = false; // Default is single-threaded

    private JLabel imageLabel;

    public App() {
        frame = new JFrame("Image Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createGUI();
    }

    private void openImage(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                inputImage = ImageIO.read(file);
                resultImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics g = resultImage.getGraphics();
                g.drawImage(inputImage, 0, 0, null);
                g.dispose();
                displayImage(inputImage); // Display the input image
            } else {
                JOptionPane.showMessageDialog(null, "File not found or cannot be read.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveResultImage() {
        try {
            if (resultImage != null) {
                String[] supportedFormats = ImageIO.getWriterFormatNames();
                boolean saved = false;

                for (String format : supportedFormats) {
                    if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg") ||
                            format.equalsIgnoreCase("png") || format.equalsIgnoreCase("jfif")) {

                        File outputfile = new File("result." + format.toLowerCase());
                        ImageIO.write(resultImage, format, outputfile);

                        saved = true;
                        JOptionPane.showMessageDialog(
                                null, "Result image saved as result." + format.toLowerCase(), "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        break; // Break after the first successful save
                    }
                }

                if (!saved) {
                    JOptionPane.showMessageDialog(null, "No suitable format found to save the result image", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "No processed image to save", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving the result image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayImage(BufferedImage image) {
        if (imageLabel != null) {
            frame.remove(imageLabel);
        }
        imageLabel = new JLabel(new ImageIcon(image));
        frame.add(imageLabel);
        frame.pack();
        frame.setVisible(true);
    }

    private int[] getAverageColor(int x, int y) {
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int totalPixels = 0;

        for (int i = x; i < x + squareSize && i < inputImage.getWidth(); i++) {
            for (int j = y; j < y + squareSize && j < inputImage.getHeight(); j++) {
                int rgb = inputImage.getRGB(i, j);
                totalRed += (rgb >> 16) & 0xFF;
                totalGreen += (rgb >> 8) & 0xFF;
                totalBlue += rgb & 0xFF;
                totalPixels++;
            }
        }

        int avgRed = totalRed / totalPixels;
        int avgGreen = totalGreen / totalPixels;
        int avgBlue = totalBlue / totalPixels;

        return new int[] { avgRed, avgGreen, avgBlue };
    }

    private void createGUI() {
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new FlowLayout());
        JLabel sizeLabel = new JLabel("Square Size:");
        JTextField sizeField = new JTextField(5);
        JButton openButton = new JButton("Open Image");
        JButton processButton = new JButton("Process Image");

        JRadioButton singleThread = new JRadioButton("Single Thread");
        JRadioButton multiThread = new JRadioButton("Multi Thread");
        JButton saveButton = new JButton("Save Image");

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveResultImage(); // Call the method to save the processed image
            }
        });
        ButtonGroup group = new ButtonGroup();
        group.add(singleThread);
        group.add(multiThread);

        singleThread.setSelected(true); // Default selection

        singleThread.addActionListener(e -> isMultiThreaded = false);
        multiThread.addActionListener(e -> isMultiThreaded = true);

        openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    openImage(selectedFile.getAbsolutePath());
                }
            }
        });

        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                squareSize = Integer.parseInt(sizeField.getText());
                if (isMultiThreaded) {
                    processImageMultiThreaded();
                } else {
                    processImageSingleThreaded();
                }
            }
        });

        controlPanel.add(sizeLabel);
        controlPanel.add(sizeField);
        controlPanel.add(openButton);
        controlPanel.add(singleThread);
        controlPanel.add(multiThread);
        controlPanel.add(processButton);
        controlPanel.add(saveButton);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
    }

    private void processImageSingleThreaded() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        int delay = 10;
        for (int y = 0; y < inputImage.getHeight(); y += squareSize) {
            for (int x = 0; x < inputImage.getWidth(); x += squareSize) {
                final int startX = x;
                final int startY = y;

                executor.execute(() -> {
                    int endX = Math.min(startX + squareSize, inputImage.getWidth());
                    int endY = Math.min(startY + squareSize, inputImage.getHeight());

                    int[] mostFrequentColor = getAverageColor(startX, startY);

                    for (int i = startX; i < endX; i++) {
                        for (int j = startY; j < endY; j++) {
                            resultImage.setRGB(i, j,
                                    new Color(mostFrequentColor[0], mostFrequentColor[1], mostFrequentColor[2])
                                            .getRGB());
                        }
                    }
                    SwingUtilities.invokeLater(() -> displayImage(resultImage));
                    try {
                        Thread.sleep(delay); // delay for visualization
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        executor.shutdown();
    }

    private void processImageMultiThreaded() {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        // resultImage = new BufferedImage(inputImage.getWidth(),
        // inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        int quarterWidth = inputImage.getWidth() / 2;
        int quarterHeight = inputImage.getHeight() / 2;

        int delay = 10; // Milliseconds delay for visualization

        // Top-left quadrant
        executor.execute(() -> {
            for (int y = 0; y < quarterHeight; y += squareSize) {
                for (int x = 0; x < quarterWidth; x += squareSize) {
                    int[] avgColor = getAverageColor(x, y);
                    for (int iX = x; iX < Math.min(x + squareSize, quarterWidth); iX++) {
                        for (int iY = y; iY < Math.min(y + squareSize, quarterHeight); iY++) {
                            resultImage.setRGB(iX, iY, new Color(avgColor[0], avgColor[1], avgColor[2]).getRGB());
                        }
                    }
                    SwingUtilities.invokeLater(() -> displayImage(resultImage));
                    try {
                        Thread.sleep(delay); // Introduce delay for visualization
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Top-right quadrant
        executor.execute(() -> {
            for (int y = 0; y < quarterHeight; y += squareSize) {
                for (int x = quarterWidth; x < inputImage.getWidth(); x += squareSize) {
                    int[] avgColor = getAverageColor(x, y);
                    for (int iX = x; iX < Math.min(x + squareSize, inputImage.getWidth()); iX++) {
                        for (int iY = y; iY < Math.min(y + squareSize, quarterHeight); iY++) {
                            resultImage.setRGB(iX, iY, new Color(avgColor[0], avgColor[1], avgColor[2]).getRGB());
                        }
                    }
                    SwingUtilities.invokeLater(() -> displayImage(resultImage));
                    try {
                        Thread.sleep(delay); // Introduce delay for visualization
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Bottom-left quadrant
        executor.execute(() -> {
            for (int y = quarterHeight; y < inputImage.getHeight(); y += squareSize) {
                for (int x = 0; x < quarterWidth; x += squareSize) {
                    int[] avgColor = getAverageColor(x, y);
                    for (int iX = x; iX < Math.min(x + squareSize, quarterWidth); iX++) {
                        for (int iY = y; iY < Math.min(y + squareSize, inputImage.getHeight()); iY++) {
                            resultImage.setRGB(iX, iY, new Color(avgColor[0], avgColor[1], avgColor[2]).getRGB());
                        }
                    }
                    SwingUtilities.invokeLater(() -> displayImage(resultImage));
                    try {
                        Thread.sleep(delay); // Introduce delay for visualization
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Bottom-right quadrant
        executor.execute(() -> {
            for (int y = quarterHeight; y < inputImage.getHeight(); y += squareSize) {
                for (int x = quarterWidth; x < inputImage.getWidth(); x += squareSize) {
                    int[] avgColor = getAverageColor(x, y);
                    for (int iX = x; iX < Math.min(x + squareSize, inputImage.getWidth()); iX++) {
                        for (int iY = y; iY < Math.min(y + squareSize, inputImage.getHeight()); iY++) {
                            resultImage.setRGB(iX, iY, new Color(avgColor[0], avgColor[1], avgColor[2]).getRGB());
                        }
                    }
                    SwingUtilities.invokeLater(() -> displayImage(resultImage));
                    try {
                        Thread.sleep(delay); // Introduce delay for visualization
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        executor.shutdown();

    }

    public static void main(String[] args) {
        new App();
    }
}
