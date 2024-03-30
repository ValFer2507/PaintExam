package paintexam;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;


public class PaintExam extends JFrame {
    private ArrayList<Shape> shapes = new ArrayList<>();
    private Shape selectedShape;
    private Point startPoint;
    private boolean shapeSelected;

    private enum ShapeType {
        LINE, RECTANGLE, ELLIPSE
    }

    private ShapeType currentShape = ShapeType.LINE;

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
                    switch (currentShape) {
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
                        switch (PaintExam.this.currentShape) {
                            case LINE:
                                if (currentShape instanceof Line2D) {
                                    ((Line2D) currentShape).setLine(startPoint, e.getPoint());
                                }
                                break;
                            case RECTANGLE:
                                if (currentShape instanceof Rectangle) {
                                    Rectangle rect = (Rectangle) currentShape;
                                    int width = e.getX() - startPoint.x;
                                    int height = e.getY() - startPoint.y;
                                    rect.setRect(startPoint.x, startPoint.y, width, height);
                                }
                                break;
                            case ELLIPSE:
                                if (currentShape instanceof Ellipse2D) {
                                    Ellipse2D ellipse = (Ellipse2D) currentShape;
                                    double width = e.getX() - startPoint.x;
                                    double height = e.getY() - startPoint.y;
                                    ellipse.setFrame(startPoint.getX(), startPoint.getY(), width, height);
                                }
                                break;
                        }
                        canvas.repaint();
                    }
                }
            }
        });

        JToolBar toolBar = new JToolBar();
        JButton lineButton = new JButton("Line");
        lineButton.addActionListener(e -> currentShape = ShapeType.LINE);
        JButton rectangleButton = new JButton("Rectangle");
        rectangleButton.addActionListener(e -> currentShape = ShapeType.RECTANGLE);
        JButton ellipseButton = new JButton("Ellipse");
        ellipseButton.addActionListener(e -> currentShape = ShapeType.ELLIPSE);

        toolBar.add(lineButton);
        toolBar.add(rectangleButton);
        toolBar.add(ellipseButton);

        add(toolBar, BorderLayout.NORTH);
        add(canvas);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PaintExam paint = new PaintExam();
            paint.setVisible(true);
        });
    }
}