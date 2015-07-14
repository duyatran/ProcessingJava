import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Summer 2015 - Processing-inspired Java Graphics Library MyCanvas.java
 * Purpose: Create MyCanvas class that holds the graphical containers
 *
 * @author Duy Tran
 * @version 1.0 6/10/2015
 */

@SuppressWarnings("serial")
public class ProcessingCanvas extends JFrame implements MouseListener,
		MouseMotionListener, MouseWheelListener, KeyListener {
	
	// Canvas and shape variables
	private boolean isAnimation;
	private static int canvasWidth = Consts.DEFAULT_WIDTH;
	private static int canvasHeight = Consts.DEFAULT_HEIGHT;
	private Color backgroundColor = Color.LIGHT_GRAY;
	private ArrayList<Shape> displayList = new ArrayList<Shape>();
	private ShapeAttributes att = new ShapeAttributes();
	private ProcessingShape currentShape;
	private DrawCanvas drawCanvas;
	
	// Save() variables
	private BufferedImage outputImage;
	private Graphics2D outputGraphics;
	private SwingWorker<Void, Void> saveWorker;
	private String outputFileName = "image.png";
	private String outputFileType = "png";
	private boolean save;
	// Buffer and background
	private boolean useImageAsBackground;
	private BufferedImage bufferImage;
	private Graphics2D bufferGraphics;
	private BufferedImage backgroundImage;
	private Graphics2D bgGraphics;
	private BufferedImage compatibleImage;
	private Graphics2D compatibleGraphics;
	private Color tintColor;

	// Event-handling
	private static Queue<InputEvent> eventQ = new LinkedList<InputEvent>();

	/**
	 * 
	 */
	public ProcessingCanvas() {
		this(canvasWidth, canvasHeight);
	}

	/**
	 * @param w
	 * @param h
	 */
	public ProcessingCanvas(int w, int h) {
		// make bufferImage available as soon as possible for drawing
		// putting it on the EDT seems to be slower and still produces flicker
		// for student14.
		canvasWidth = w;
		canvasHeight = h;
		
		// buffer for animation or image background
		bufferImage = new BufferedImage(canvasWidth, canvasHeight,
				BufferedImage.TYPE_INT_ARGB);
		bufferGraphics = bufferImage.createGraphics();
		//clearGraphics(bufferGraphics);

		// the actual background image that compatible image(s) draw to
		backgroundImage = new BufferedImage(canvasWidth, canvasHeight,
				BufferedImage.TYPE_INT_ARGB);
		bgGraphics = backgroundImage.createGraphics();

		// compatibleImage keeps temporary image loaded from loadImage()
		compatibleImage = new BufferedImage(canvasWidth, canvasHeight,
				BufferedImage.TYPE_INT_ARGB);
		compatibleGraphics = compatibleImage.createGraphics();

		// Create the JFrame and JPanel on the EDT
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(canvasWidth, canvasHeight);
			}
		});
	}

	/**
	 * @param w
	 * @param h
	 */
	private void createAndShowGUI(int w, int h) {

		drawCanvas = new DrawCanvas();
		drawCanvas.setPreferredSize(new Dimension(w, h));

		// to listen for key events right away
		drawCanvas.setFocusable(true);
		drawCanvas.requestFocusInWindow();

		// add the event listeners
		drawCanvas.addMouseListener(this);
		drawCanvas.addMouseMotionListener(this);
		drawCanvas.addKeyListener(this);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false); // must be set before this.pack() so Swing
									// does not resize JPanel and JFrame
		this.setTitle("My Canvas");

		if (w >= Consts.MIN_CANVAS_SIZE && h >= Consts.MIN_CANVAS_SIZE) {
			this.add(drawCanvas);
			this.pack();
		} else {
			// If canvas is smaller than 200x200, GridBagLayout with default
			// GridBagConstraints will center the canvas, so no need to do 
			// setLayout(null) or worry about insets
			this.setLayout(new GridBagLayout());
			this.add(drawCanvas, new GridBagConstraints());
			// set minimum size for the window frame
			this.setMinimumSize(new Dimension(Consts.MIN_CANVAS_SIZE + 50,
					Consts.MIN_CANVAS_SIZE + 50));
		}
		this.setLocationRelativeTo(null); // center the window on the screen
		this.setVisible(true);
	}

	/**
	 * @author Duy
	 *
	 */
	private class DrawCanvas extends JPanel {
		// why make a new class and not just create a JPanel?
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// If animation, draw to the JPanel graphics from the buffer
			if (isAnimation) {
				g.drawImage(bufferImage, 0, 0, this);
			}
			// If static images, let Swing do the drawing (and repaint)
			else {
				Graphics2D g2 = (Graphics2D) g;
				clearGraphics(g2);
				clearGraphics(bufferGraphics);
				for (Shape s : displayList) {
					s.paintShape(g2);
					s.paintShape(bufferGraphics);
				}
				if (save) {
					createSaveWorker();
					saveWorker.execute();
				}
			}
		}
	}

	/******************************************************
	 ***************** HELPER METHODS *********************
	 ******************************************************/
	
	/**
	 * @param g
	 */
	private void clearGraphics(Graphics g) {
		if (!useImageAsBackground) {
			// setColor and fillRect are used because setBackground
			// DO NOT set background color. Read API for more info.
			g.setColor(backgroundColor);
			g.fillRect(0, 0, canvasWidth, canvasHeight);
		} else {
			g.drawImage(backgroundImage, 0, 0, null);
		}
	}

	/**
	 * 
	 */
	private void createSaveWorker() {
		saveWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				int outputFormat = (outputFileType.equals("png") || outputFileType
						.equals("gif")) ? BufferedImage.TYPE_INT_ARGB
						: BufferedImage.TYPE_INT_RGB;
				outputImage = new BufferedImage(canvasWidth, canvasHeight,
						outputFormat);
				outputGraphics = outputImage.createGraphics();
				
				if (!isAnimation && useImageAsBackground) { // not animation, paint background and shapes on the output
//					clearGraphics(outputGraphics);
//					for (Shape s: displayList) {
//						s.paintShape(outputGraphics);
//					}
					outputGraphics.drawImage(bufferImage, 0, 0, null);
				}
				else if (!isAnimation && !useImageAsBackground) {
					System.out.println("static, draw from buffer");
					outputGraphics.drawImage(bufferImage, 0, 0, null);
				}
				else { // if animation, just draw the buffer
					outputGraphics.drawImage(bufferImage, 0, 0, null);
				}
				
				outputGraphics.dispose();

				try {
					ImageIO.write(outputImage, outputFileType, new File(
							outputFileName));
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
				}
				return null;
			}
		};
	}
	
	/**
	 * @param saveFrame
	 * @param frameCount
	 */
	public void endDraw(boolean saveFrame, int frameCount) {
		repaint();
		if (saveFrame) {
			outputFileName = "screen-" + frameCount + ".png";
			createSaveWorker();
			saveWorker.execute();
		}
	}
	
	/**
	 * 
	 */
	public void isAnimation() {
		isAnimation = true;
	}

	/**
	 * @param s
	 */
	private void paintImage(Shape s) {
		if (!isAnimation && useImageAsBackground) { // static w/ image background
			displayList.add(s);
		}
		else if (!isAnimation && !useImageAsBackground) {
			displayList.add(s);
			//s.paintShape(bufferGraphics);
		}
		else { // if animation, paint the shape on the buffer right away
			s.paintShape(bufferGraphics);
		}
	}
	
	/******************************************************
	 ***************** SAVING IMAGES *********************
	 ******************************************************/

	public void save(String fileName) {
		if (fileName.indexOf(".") == -1) {
			fileName = fileName.concat(".png");
		} else {
			outputFileType = fileName.substring(fileName.indexOf(".") + 1)
					.toLowerCase();
		}
		this.outputFileName = fileName;
		save = true;
		// Putting saveImage() on the EDT to let the drawing by Swing finishes first
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				createSaveWorker();
//				saveWorker.execute();
//			}
//		});
	}

	/******************************************************
	 *********** CANVAS AND SHAPE ATTRIBUTES **************
	 ******************************************************/

	public void background(Color c) {
		backgroundColor = c;
		clearGraphics(bufferGraphics);
	}

	public void loadImage(final String image) { // works but looks ugly, also
												// ImageIO.read is TOO SLOW
		useImageAsBackground = true;
		if (tintColor != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						BufferedImage temp = ImageIO.read(new File(image));
						compatibleGraphics.drawImage(temp, 0, 0, null);
						doTint(tintColor);
						bgGraphics.drawImage(compatibleImage, 0, 0, null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						BufferedImage temp = ImageIO.read(new File(image));
						compatibleGraphics.drawImage(temp, 0, 0, null);
						bgGraphics.drawImage(compatibleImage, 0, 0, null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void tint(Color c) {
		tintColor = c;
	}

	private void doTint(Color c) {
		int width = compatibleImage.getWidth();
		int height = compatibleImage.getHeight();

		int[] pixels = new int[width * height];

		Raster raster = compatibleImage.getRaster();
		pixels = (int[]) raster.getDataElements(0, 0, width, height, pixels);

		int tintARGB = c.getRGB();
		int tintA = (tintARGB >> 24) & 0xff;
		int tintR = (tintARGB >> 16) & 0xff;
		int tintG = (tintARGB >> 8) & 0xff;
		int tintB = (tintARGB) & 0xff;

		for (int i = 0; i < pixels.length; i++) {
			int argb = pixels[i];
			int a = (argb >> 24) & 0xff;
			int r = (argb >> 16) & 0xff;
			int g = (argb >> 8) & 0xff;
			int b = (argb) & 0xff;

			pixels[i] = (((a * tintA) & 0xff00) << 16)
					| (((r * tintR) & 0xff00) << 8) | ((g * tintG) & 0xff00)
					| (((b * tintB) & 0xff00) >> 8);
		}

		WritableRaster wr = compatibleImage.getRaster();
		wr.setDataElements(0, 0, width, height, pixels);
	}

	/**
	 * Modifies the quality of forms created with curve().
	 * 
	 * @param t
	 *            the tension value of the cardinal curve
	 */
	public void curveTightness(double t) {
		att.setCurveTightness(t);
	}

	/**
	 * 
	 */
	public void ellipseMode(int mode) {
		att.setEllipseMode(mode);
	}

	public void fill(Color c) {
		att.setFill(true);
		att.setFillColor(c);
	}

	public void stroke(Color c) {
		att.setStroke(true);
		att.setStrokeColor(c);
	}

	/**
	 * Sets the style for rendering stroke endings.
	 * 
	 * @param cap
	 *            - either SQUARE, ROUND or PROJECT
	 */
	public void setStrokeCap(int cap) {
		att.setStrokeCap(cap);
	}

	/**
	 * Sets the style of the joints between strokes.
	 * 
	 * @param join
	 *            - either MITER, BEVEL, and ROUND
	 * @throws IllegalArgumentException
	 */
	public void setStrokeJoin(int join) {
		att.setStrokeJoin(join);
	}

	/**
	 * Sets the width (in pixels) of the stroke used for lines, points, and the
	 * border around shapes.
	 * 
	 * @throws IllegalArgumentException
	 */
	public void setStrokeWeight(float w) {
		att.setStrokeWeight(w);
	}

	/**
	 * Disables drawing the stroke(outline).
	 */
	public void noStroke() {
		att.setStroke(false);
	}

	/**
	 * Disables filling geometry.
	 */
	public void noFill() {
		att.setFill(false);
	}

	/**
	 *
	 */
	public void rectMode(int mode) {
		att.setRectMode(mode);
	}

	/**
	 * Turn on anti-aliasing, which is on by default.
	 */
	public void smooth() {
		att.setSmooth(true);
	}

	/**
	 * Turn off anti-aliasing.
	 */
	public void noSmooth() {
		att.setSmooth(false);
	}

	/******************************************************
	 ***************** SHAPE CREATION *********************
	 ******************************************************/

	/**
	 * Adds an arc to the displayList.
	 * 
	 * @param x
	 *            by default, x-coordinate of the ellipse
	 * @param y
	 *            by default, y-coordinate of the ellipse
	 * @param w
	 *            by default, width of the rectangle
	 * @param h
	 *            by default, height of the rectangle
	 * @param start
	 *            - The starting angle of the arc in degrees.
	 * @param stop
	 *            - The angular extent of the arc in degrees.
	 * @param mode
	 *            - The closure type for the arc: Arc2D.OPEN, Arc2D.CHORD, or
	 *            Arc2D.PIE.
	 */
	public void arc(double x, double y, double w, double h, double start,
			double stop, int mode) {
		// Arguments modified to produce Processing-like arc with Java2D Arc2D
		// constructor,
		// which draws arc counter-clockwise and use 'extent' instead of 'stop'
		// angle.
		paintImage(new ProcessingArc(x, y, w, h, Consts.TWO_PI - stop, stop
				- start, mode, att));
	}

	/**
	 * Adds a curve to the displayList.
	 * 
	 * @param x1
	 *            x-coordinate of the first anchor point
	 * @param y1
	 *            y-coordinate of the first anchor point
	 * @param x2
	 *            x-coordinate of the first control point
	 * @param y2
	 *            y-coordinate of the first control point
	 * @param x3
	 *            x-coordinate of the second control point
	 * @param y3
	 *            y-coordinate of the second control point
	 * @param x4
	 *            x-coordinate of the second anchor point
	 * @param y4
	 *            y-coordinate of the second anchor point
	 */
	public void curve(double x1, double y1, double x2, double y2, double x3,
			double y3, double x4, double y4, int type) {
		double[] x = new double[] { x1, x2, x3, x4 };
		double[] y = new double[] { y1, y2, y3, y4 };
		paintImage(new ProcessingCurve(x, y, type, att));
	}

	/**
	 * Adds an ellipse to the displayList.
	 * 
	 * @param x
	 *            by default, x-coordinate of the rectangle containing the
	 *            ellipse
	 * @param y
	 *            by default, y-coordinate of the rectangle containing the
	 *            ellipse
	 * @param w
	 *            by default, width of the ellipse
	 * @param h
	 *            by default, height of the ellipse
	 */
	public void ellipse(double x, double y, double w, double h) {
		paintImage(new ProcessingEllipse(x, y, w, h, att));
	}

	/**
	 * Adds a line to the displayList.
	 * 
	 * @param x1
	 *            x-coordinate of the first point
	 * @param y1
	 *            y-coordinate of the first point
	 * @param x2
	 *            x-coordinate of the second point
	 * @param y2
	 *            y-coordinate of the second point
	 */
	public void line(double x1, double y1, double x2, double y2) {
		paintImage(new ProcessingLine(x1, y1, x2, y2, att));
	}

	/**
	 * Adds a point to the displayList.
	 * 
	 * @param x1
	 *            x-coordinate of the point
	 * @param y1
	 *            y-coordinate of the point
	 */
	public void point(double x, double y) {
		paintImage(new ProcessingLine(x, y, x + Consts.EPSILON, y
				+ Consts.EPSILON, att));
	}

	/**
	 * Adds a polygon (triangle and quad) to the screen
	 * 
	 * @param x1
	 *            x-coordinate of the first corner
	 * @param y1
	 *            y-coordinate of the first corner
	 * @param x2
	 *            x-coordinate of the second corner
	 * @param y2
	 *            y-coordinate of the second corner
	 * @param x3
	 *            x-coordinate of the third corner
	 * @param y3
	 *            y-coordinate of the third corner
	 * @param x4
	 *            x-coordinate of the fourth corner
	 * @param y4
	 *            y-coordinate of the fourth corner
	 */
	public void polygon(double[] x, double[] y) {
		paintImage(new ProcessingPolygon(x, y, att));
	}

	/**
	 * Adds a rectangle to the displayList.
	 * 
	 * @param x
	 *            by default, x-coordinate of the rectangle
	 * @param y
	 *            by default, y-coordinate of the rectangle
	 * @param w
	 *            by default, width of the rectangle
	 * @param h
	 *            by default, height of the rectangle
	 */
	public void rect(double x, double y, double w, double h) {
		paintImage(new ProcessingRect(x, y, w, h, att));
	}

	public void rect(double v1, double v2, double v3, double v4, double tl,
			double tr, double br, double bl) {
		beginShape(Consts.POLYGON);

		double[] temp = currentShape.setCoordinates(att.getRectMode(), v1, v2,
				v3, v4);
		double x = temp[0];
		double y = temp[1];
		double w = temp[2];
		double h = temp[3];

		// top left corner
		vertex(x + tl, y);
		quadraticVertex(x, y, x, y + tl);

		// bottom left corner
		vertex(x, y + h - bl);
		quadraticVertex(x, y + h, x + bl, y + h);

		// bottom right corner
		vertex(x + w - br, y + h);
		quadraticVertex(x + w, y + h, x + w, y + h - br);

		// top right corner
		vertex(x + w, y + tr);
		quadraticVertex(x + w, y, x + w - tl, y);

		endShape(Consts.CLOSE);
	}

	/**
	 * 
	 */
	public void vertex(double x, double y) {
		currentShape.addVertex(x, y);
	}

	/**
	 * 
	 */
	public void curveVertex(double x, double y) {
		currentShape.addCurveVertex(x, y);
	}

	/**
	 * 
	 */
	public void quadraticVertex(double x1, double y1, double x2, double y2) {
		currentShape.add(x1, y1, x2, y2);
	}

	/**
	 * 
	 */
	public void bezierVertex(double x1, double y1, double x2, double y2,
			double x3, double y3) {
		currentShape.add(x1, y1, x2, y2, x3, y3);
	}

	/**
	 * 
	 */
	public void beginShape(int kind) {
		currentShape = new ProcessingShape(att, kind);
	}

	/**
	 * 
	 */
	public void endShape(int mode) {
		currentShape.closePath(mode);
		for (Shape s : currentShape.getShapeList()) {
			paintImage(s);
		}
	}

	/******************************************************
	 ***************** EVENT-HANDLING *********************
	 ******************************************************/

	@Override
	public void mouseClicked(MouseEvent e) {
		eventQ.add(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		eventQ.add(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		eventQ.add(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		eventQ.add(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		eventQ.add(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		eventQ.add(e);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		eventQ.add(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		eventQ.add(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	public Queue<InputEvent> getEventQ() {
		return eventQ;
	}
}