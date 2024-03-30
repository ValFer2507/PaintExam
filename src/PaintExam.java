//package paintexam;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.*;


public class PaintExam extends JFrame {
    private ArrayList<Shape> shapes = new ArrayList<>();
    private Shape selectedShape;
    private Point startPoint;
    private boolean shapeSelected;

    private enum ToolOption {
        LINE, RECTANGLE, ELLIPSE, SELECT
    }

    private ToolOption currentTool = ToolOption.LINE;

    public PaintExam() {
        setTitle("Simple Paint");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                for (Shape shape : shapes) {
                    if (shape == selectedShape) {
                        g2d.setColor(Color.RED);
                    } else {
                        g2d.setColor(Color.BLACK);
                    }
                    g2d.draw(shape);
                }
            }
        };
        
        canvas.setBackground(Color.WHITE);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                shapeSelected = false;
                if (selectedShape != null && selectedShape.contains(startPoint)) {
                    shapeSelected = true;
                } else {
                    switch (currentTool) {
                        case LINE:
                            shapes.add(new Line2D.Double(startPoint, startPoint));
                            break;
                        case RECTANGLE:
                            shapes.add(new Rectangle(startPoint));
                            break;
                        case ELLIPSE:
                            shapes.add(new Ellipse2D.Double(startPoint.getX(), startPoint.getY(), 0, 0));
                            break;
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!shapeSelected) {
                    for (Shape shape : shapes) {
                        if (shape.contains(e.getPoint())) {
                            selectedShape = shape;
                            break;
                        }
                    }
                }
            }
        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (shapeSelected && selectedShape != null) {
                    double deltaX = e.getX() - startPoint.getX();
                    double deltaY = e.getY() - startPoint.getY();
                    AffineTransform transform = AffineTransform.getTranslateInstance(deltaX, deltaY);
                    selectedShape = transform.createTransformedShape(selectedShape);
                    startPoint = e.getPoint();
                    canvas.repaint();
                } else {
                    if (!shapes.isEmpty()) {
                        Shape currentShape = shapes.get(shapes.size() - 1);
                        switch (PaintExam.this.currentTool) {
                            case LINE:
                                if (currentShape instanceof Line2D line2D) {
                                    line2D.setLine(startPoint, e.getPoint());
                                }
                                break;

                            case RECTANGLE:
                                if (currentShape instanceof Rectangle rect) {
                                    int width = e.getX() - startPoint.x;
                                    int height = e.getY() - startPoint.y;
                                    rect.setRect(startPoint.x, startPoint.y, width, height);
                                }
                                break;

                            case ELLIPSE:
                                if (currentShape instanceof Ellipse2D ellipse) {
                                    double width = e.getX() - startPoint.x;
                                    double height = e.getY() - startPoint.y;
                                    ellipse.setFrame(startPoint.getX(), startPoint.getY(), width, height);
                                }
                                break;
                            case SELECT:
                                selectedShape = null;
                                for (Shape shape : shapes) {
                                    if (shape.contains(startPoint)) {
                                        selectedShape = shape;
                                        break;
                                    }
                                }
                                break;

                        }
                        canvas.repaint();
                    }
                }
            }
        });

        
        JComboBox<String> toolComboBox = new JComboBox<>();
        toolComboBox.addItem("Linea");
        toolComboBox.addItem("Rectangulo");
        toolComboBox.addItem("Elipse");
        toolComboBox.addActionListener(e -> {
            String selectedTool = (String) toolComboBox.getSelectedItem();
            switch (selectedTool) {
                case "Linea":
                    currentTool = ToolOption.LINE;
                    break;
                case "Rectangulo":
                    currentTool = ToolOption.RECTANGLE;
                    break;
                case "Elipse":
                    currentTool = ToolOption.ELLIPSE;
                    break;
            }
        });

        
        JToolBar toolBar = new JToolBar();
        
        toolBar.add(toolComboBox);
        toolBar.add(Box.createHorizontalGlue());
        /*JButton lineButton = new JButton("Line");
        lineButton.addActionListener(e -> currentTool = ToolOption.LINE);
        JButton rectangleButton = new JButton("Rectangle");
        rectangleButton.addActionListener(e -> currentTool = ToolOption.RECTANGLE);
        JButton ellipseButton = new JButton("Ellipse");
        ellipseButton.addActionListener(e -> currentTool = ToolOption.ELLIPSE);*/
        JButton selectButton = new JButton("Select");
        selectButton.addActionListener(e -> currentTool = ToolOption.SELECT);
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> guardar(canvas));
        JButton openButton = new JButton("Open");
        openButton.addActionListener(e -> openImage(canvas));
        

        
        toolBar.add(selectButton);
        toolBar.add(saveButton);
        toolBar.add(openButton);
        toolBar.add(Box.createHorizontalGlue());

        add(toolBar, BorderLayout.NORTH);
        add(canvas);
    }
    
    private void guardar(Component component) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como JPG");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
                component.paint(image.getGraphics());
                File file = new File(fileToSave.getAbsolutePath() + ".jpg");
                ImageIO.write(image, "jpg", file);
                JOptionPane.showMessageDialog(this, "Dibujo guardado como " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar el dibujo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void openImage(Component component) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Abrir dibujo");
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(fileToOpen);
                Graphics2D g2d = (Graphics2D) component.getGraphics();
                g2d.drawImage(image, 0, 0, null);
                JOptionPane.showMessageDialog(this, "Dibujo abierto desde " + fileToOpen.getName());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al abrir el dibujo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PaintExam paint = new PaintExam();
            paint.setVisible(true);
        });
    }
}