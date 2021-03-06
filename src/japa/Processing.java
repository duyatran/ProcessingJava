package japa;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.Timer;
import java.util.Queue;
import java.util.Random;

/**
 * Summer 2015 - Processing-inspired Java Graphics Library Processing.java
 * Purpose: Provides public methods for the client code.
 *
 * @author Duy Tran
 * @version 1.0 6/10/2015
 */

public class Processing {
	// general, public variables
	public static int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static int width;
	public static int height;

	// private variables
	private static boolean canvasCreated = false; // to deal with multiple calls to size()
	private static ColorFactory colorFactory = new ColorFactory();
	private static ProcessingCanvas canvas;
	private static Random rand = new Random();

	// Animation variables
	private static Class<?> callingClass;
	private static String method = "draw"; // default name for the looped method
	private static Method drawMethod;
	private static Timer timer;
	private static int frameRate = 60;
	private static boolean loop = true;
	private static boolean saveFrame;
	private static boolean redraw;
	
	// Event-handling variables
	public static char key;
	public static double mouseX;
	public static double mouseY;
	public static double pmouseX;
	public static double pmouseY;
	public static boolean mousePressed;
	public static boolean keyPressed;
	public static int mouseButton;
	private static Method eventMethod;

	/******************************************************
	 *********** CANVAS AND SHAPE ATTRIBUTES **************
	 ******************************************************/
	
	/**
	 * This method sets up the canvas and defines the dimension of the display
	 * windows (in pixels) with default parameters (800 x 600). It must be the
	 * first method called to start drawing.
	 */
	public static void size() {
		size(Consts.DEFAULT_WIDTH, Consts.DEFAULT_HEIGHT);
	}

	/**
	 * @param width - Desired width of the display window.
	 * @param height - Desired height of the display window.
	 */
	public static void size(int w, int h) {
		if (!canvasCreated)
			canvasCreated = true;
		else
			canvas.dispose(); // dispose of the old canvas, probably should not
								// do this in animation

		canvas = new ProcessingCanvas(w, h);
		width = w;
		height = h;
	}

	/**
	 * Sets the mode with which color inputs are interpreted.
	 * 
	 * @param mode - Either Consts.RGB or Consts.HSB.
	 */
	public static void colorMode(int mode) {
		colorFactory.setColorMode(mode);
	}

	/**
	 * Sets the mode and the maximum value of the range of all color components.
	 * The default is 255 for all components.
	 * 
	 * @param mode - Either Consts.RGB or Consts.HSB
	 * @param max - Maximum value of the range of all color components
	 */
	public static void colorMode(int mode, double max) {
		colorFactory.setColorMode(mode, (float) max);
	}

	/**
	 * @param mode - Either Consts.RGB or Consts.HSB
	 * @param max1 - Maximum value of the range of the red or hue component
	 * @param max2 - Maximum value of the range of the green or saturation component
	 * @param max3 - Maximum value of the range of the blue or brightness component
	 */
	public static void colorMode(int mode, double max1, double max2, double max3) {
		colorFactory.setColorMode(mode, (float) max1, (float) max2,	(float) max3);
	}

	/**
	 * @param mode - Either Consts.RGB or Consts.HSB
	 * @param max1 - Maximum value of the range of the red or hue component
	 * @param max2 - Maximum value of the range of the green or saturation component
	 * @param max3 - Maximum value of the range of the blue or brightness component
	 * @param maxA - Maximum value of the range of the alpha value
	 */
	public static void colorMode(int mode, double max1, double max2,
			double max3, double maxA) {
		colorFactory.setColorMode(mode, (float) max1, (float) max2,
				(float) max3, (float) maxA);
	}

	/**
	 * Sets the gray-scale color used for the background of the display canvas.
	 * The default background is light gray.
	 * 
	 * @param gray - the gray-scale color
	 */
	public static void background(double gray) {
		canvas.background(colorFactory.produce((float) gray));
	}

	/**
	 * @param gray - the gray-scale color
	 * @param alpha - opacity of the background
	 */
	public static void background(double gray, double alpha) {
		canvas.background(colorFactory.produce((float) gray, (float) alpha));
	}

	/**
	 * Sets the color used for the background of the display canvas. The default
	 * background is light gray.
	 * 
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 */
	public static void background(double v1, double v2, double v3) {
		canvas.background(colorFactory.produce((float) v1, (float) v2, (float) v3));
	}

	/**
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 * @param alpha - opacity of the background
	 */
	public static void background(double v1, double v2, double v3, double alpha) {
		canvas.background(colorFactory.produce((float) v1, (float) v2,
				(float) v3, (float) alpha));
	}

	/**
	 * Sets the background of the display window to an image.
	 * 
	 * @param image - path to image file
	 */
	public static void loadImage(String image) {
			canvas.loadImage(image);
	}

	/**
	 * Sets the gray-scale color used for tinting background image.
	 * The default background is light gray.
	 * 
	 * @param gray - the gray-scale color
	 */
	public static void tint(double gray) {
		canvas.tint(colorFactory.produce((float) gray));
	}

	/**
	 * @param gray - the gray-scale tint color
	 * @param alpha - alpha value of the tint color
	 */
	public static void tint(double gray, double alpha) {
		canvas.tint(colorFactory.produce((float) gray, (float) alpha));
	}

	/**
	 * Sets the color used for tinting background image.
	 * The default background is light gray.
	 * 
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 */
	public static void tint(double v1, double v2, double v3) {
		canvas.tint(colorFactory.produce((float) v1, (float) v2, (float) v3));
	}

	/**
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 * @param alpha - alpha value of the tint color
	 */
	public static void tint(double v1, double v2, double v3, double alpha) {
		canvas.tint(colorFactory.produce((float) v1, (float) v2, (float) v3,
				(float) alpha));
	}

	/**
	 * Sets the gray-scale color used to fill shapes. The default fill color is
	 * white.
	 * 
	 * @param gray - the gray-scale color
	 */
	public static void fill(double gray) {
		canvas.fill(colorFactory.produce((float) gray));
	}

	/**
	 * @param gray - the gray-scale color
	 * @param alpha - opacity of the fill color
	 */
	public static void fill(double gray, double alpha) {
		canvas.fill(colorFactory.produce((float) gray, (float) alpha));
	}

	/**
	 * Sets the color used to fill shapes. The default fill color is white.
	 * 
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 */
	public static void fill(double v1, double v2, double v3) {
		canvas.fill(colorFactory.produce((float) v1, (float) v2, (float) v3));
	}

	/**
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 * @param alpha - opacity of the fill color
	 */
	public static void fill(double v1, double v2, double v3, double alpha) {
		canvas.fill(colorFactory.produce((float) v1, (float) v2, (float) v3,
				(float) alpha));
	}

	/**
	 * Sets the gray-scale color used to draw lines and borders around shapes.
	 * The default stroke color is black.
	 * 
	 * @param gray - the gray-scale color
	 */
	public static void stroke(double gray) {
		canvas.stroke(colorFactory.produce((float) gray));
	}

	/**
	 * @param gray - the gray-scale color
	 * @param alpha - opacity of the stroke
	 */
	public static void stroke(double gray, double alpha) {
		canvas.stroke(colorFactory.produce((float) gray, (float) alpha));
	}

	/**
	 * Sets the color used to draw lines and borders around shapes. The default
	 * stroke color is black.
	 * 
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 */
	public static void stroke(double v1, double v2, double v3) {
		canvas.stroke(colorFactory.produce((float) v1, (float) v2, (float) v3));
	}

	/**
	 * @param v1 - red or hue component
	 * @param v2 - green or saturation component
	 * @param v3 - blue or brightness component
	 * @param alpha - opacity of the fill
	 */
	public static void stroke(double v1, double v2, double v3, double alpha) {
		canvas.stroke(colorFactory.produce((float) v1, (float) v2, (float) v3,
				(float) alpha));
	}

	/**
	 * Sets the style for rendering stroke endings. Default stroke cap is ROUND.
	 * 
	 * @param cap - either SQUARE, ROUND or PROJECT
	 */
	public static void strokeCap(int cap) {
		canvas.setStrokeCap(cap);
	}

	/**
	 * Sets the style of the joints between strokes. Default stroke join is MITER.
	 * 
	 * @param join - either MITER, BEVEL, and ROUND
	 */
	public static void strokeJoin(int join) {
		canvas.setStrokeJoin(join);
	}

	/**
	 * Sets the width (or weight) of the stroke used for lines, points, and the
	 * border around shapes. Default stroke weight is 1.
	 */
	public static void strokeWeight(double w) {
		canvas.setStrokeWeight((float) w);
	}

	/**
	 * Disables drawing the stroke (shape outline).
	 */
	public static void noStroke() {
		canvas.noStroke();
	}

	/**
	 * Disables filling shapes.
	 */
	public static void noFill() {
		canvas.noFill();
	}
	
	/**
	 * Turn on anti-aliasing, which is on by default.
	 */
	public static void smooth() {
		canvas.smooth();
	}

	/**
	 * Turn off anti-aliasing. smooth() is active by default, so it is necessary
	 * to call this method to disable anti-aliasing.
	 */
	public static void noSmooth() {
		canvas.noSmooth();
	}
	
	/**
	 * Modifies the location at which ellipses are drawn by changing the way
	 * in which parameters given to ellipse() are interpreted.
	 * 
	 * The default mode is rectMode(CENTER), which interprets the first two 
	 * parameters of ellipse() as the shape's center point, while the third and 
	 * fourth parameters are its width and height.
	 * 
	 * rectMode(CORNERS) interprets the first two parameters of ellipse() as the
	 * location of one corner, and the third and fourth parameters as the
	 * location of the opposite corner.
	 * 
	 * rectMode(CORNER) interprets the first two parameters of ellipse() as the 
	 * upper-left corner of the shape, while the third and fourth parameters
	 * are its width and height.
	 * 
	 * rectMode(RADIUS) also uses the first two parameters of ellipse() as the
	 * shape's center point, but uses the third and fourth parameters to specify
	 * half of the shapes's width and height.
	 */
	public static void ellipseMode(int mode) {
		canvas.ellipseMode(mode);
	}
	
	/**
	 * Modifies the location at which rectangles are drawn by changing the way
	 * in which parameters given to rect() are interpreted.
	 * 
	 * The default mode is rectMode(CORNER), which interprets the first two
	 * parameters of rect() as the upper-left corner of the shape, while the
	 * third and fourth parameters are its width and height.
	 * 
	 * rectMode(CORNERS) interprets the first two parameters of rect() as the
	 * location of one corner, and the third and fourth parameters as the
	 * location of the opposite corner.
	 * 
	 * rectMode(CENTER) interprets the first two parameters of rect() as the
	 * shape's center point, while the third and fourth parameters are its width
	 * and height.
	 * 
	 * rectMode(RADIUS) also uses the first two parameters of rect() as the
	 * shape's center point, but uses the third and fourth parameters to specify
	 * half of the shapes's width and height.
	 */
	public static void rectMode(int mode) {
		canvas.rectMode(mode);
	}


	/**
	 * Modifies the quality of forms created with curve().
	 * 
	 * @param t - the tension value of the curve
	 */
	public static void curveTightness(double t) {
		canvas.curveTightness(t);
	}
	
	/******************************************************
	 ***************** SHAPE CREATION *********************
	 ******************************************************/
	
	/**
	 * 
	 */
	public static void vertex(double x, double y) {
		canvas.vertex(x, y);
	}

	/**
	 * 
	 */
	public static void curveVertex(double x, double y) {
		canvas.curveVertex(x, y);
	}

	/**
	 * 
	 */
	public static void quadraticVertex(double x1, double y1, double x2,
			double y2) {
		canvas.quadraticVertex(x1, y1, x2, y2);
	}

	/**
	 * 
	 */
	public static void bezierVertex(double x1, double y1, double x2, double y2,
			double x3, double y3) {
		canvas.bezierVertex(x1, y1, x2, y2, x3, y3);
	}

	/**
	 * 
	 */
	public static void beginShape() {
		canvas.beginShape(Consts.POLYGON);
	}

	/**
	 * 
	 */
	public static void beginShape(int kind) {
		// TO-DO
		canvas.beginShape(kind);
	}

	/**
	 * 
	 */
	public static void endShape() {
		canvas.endShape(Consts.PATH_OPEN);
	}

	/**
	 * 
	 */
	public static void endShape(int mode) {
		canvas.endShape(Consts.CLOSE);
	}

	/**
	 * Draws an arc to the screen. The default mode is PIE fill and OPEN stroke.
	 * 
	 * @param x - x-coordinate of the arc's ellipse
	 * @param y - y-coordinate of the arc' ellipse
	 * @param w - width of the arc's ellipse
	 * @param h - height of the arc's ellipse
	 * @param start - angle to start the arc (in degrees)
	 * @param stop - angle to stop the arc (in degrees)
	 * @param mode - the closure type for the arc, either OPEN, CHORD, or PIE.
	 */
	public static void arc(double x, double y, double w, double h,
			double start, double stop, int mode) {
		canvas.arc(x, y, w, h, start, stop, mode);
	}

	/**
	 * The default mode is PIE fill and OPEN stroke.
	 * 
	 * @param x - x-coordinate of the center of the arc's ellipse
	 * @param y - y-coordinate of the center of the arc's ellipse
	 * @param w - width of the arc's ellipse
	 * @param h - height of the arc's ellipse
	 * @param start - angle to start the arc (in degrees)
	 * @param stop - angle to stop the arc (in degrees)
	 */
	public static void arc(double x, double y, double w, double h,
			double start, double stop) {
		canvas.arc(x, y, w, h, start, stop, Consts.DEFAULT_ARC);
	}

	/**
	 * Draws a Bezier curve on the screen.
	 * 
	 * @param x1 - x-coordinate of the first anchor point
	 * @param y1 - y-coordinate of the first anchor point
	 * @param x2 - x-coordinate of the first control point
	 * @param y2 - y-coordinate of the first control point
	 * @param x3 - x-coordinate of the second control point
	 * @param y3 - y-coordinate of the second control point
	 * @param x4 - x-coordinate of the second anchor point
	 * @param y4 - y-coordinate of the second anchor point
	 */
	public static void bezier(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		canvas.curve(x1, y1, x2, y2, x3, y3, x4, y4, Consts.BEZIER);
	}

	/**
	 * Draws a Catmull-Rom curve on the screen.
	 * 
	 * @param x1 - x-coordinate of the first control point
	 * @param y1 - y-coordinate of the first control point
	 * @param x2 - x-coordinate of the first anchor point
	 * @param y2 - y-coordinate of the first anchor point
	 * @param x3 - x-coordinate of the second anchor point
	 * @param y3 - y-coordinate of the second anchor point
	 * @param x4 - x-coordinate of the second control point
	 * @param y4 - y-coordinate of the second control point
	 */
	public static void curve(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		canvas.curve(x1, y1, x2, y2, x3, y3, x4, y4, Consts.CATMULL_ROM);
	}

	/**
	 * Draws an ellipse to the screen.
	 * 
	 * @param v1 - by default, x-coordinate of the ellipse
	 * @param v2 - by default, y-coordinate of the ellipse
	 * @param v3 - by default, width of the ellipse
	 * @param v4 - by default, height of the ellipse
	 */
	public static void ellipse(double v1, double v2, double v3, double v4) {
		canvas.ellipse(v1, v2, v3, v4);
	}

	/**
	 * Draws a line to the screen.
	 * 
	 * @param x1 - x-coordinate of the first point
	 * @param y1 - y-coordinate of the first point
	 * @param x2 - x-coordinate of the second point
	 * @param y2 - y-coordinate of the second point
	 */
	public static void line(double x1, double y1, double x2, double y2) {
		canvas.line(x1, y1, x2, y2);
	}

	/**
	 * Draws a point to the screen.
	 * 
	 * @param x1 - x-coordinate of the point
	 * @param y1 - y-coordinate of the point
	 */
	public static void point(double x, double y) {
		canvas.point(x, y);
	}

	/**
	 * Draws a four-sided polygon to the screen.
	 * 
	 * @param x1 - x-coordinate of the first corner
	 * @param y1 - y-coordinate of the first corner
	 * @param x2 - x-coordinate of the second corner
	 * @param y2 - y-coordinate of the second corner
	 * @param x3 - x-coordinate of the third corner
	 * @param y3 - y-coordinate of the third corner
	 * @param x4 - x-coordinate of the fourth corner
	 * @param y4 - y-coordinate of the fourth corner
	 */
	public static void quad(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		double[] x = new double[] { x1, x2, x3, x4 };
		double[] y = new double[] { y1, y2, y3, y4 };
		canvas.polygon(x, y);
	}

	/**
	 * Draws a rectangle to the screen.
	 * 
	 * @param v1 - by default, x-coordinate of the top left corner of the rectangle
	 * @param v2 - by default, y-coordinate of the top left corner of the rectangle
	 * @param v3 - by default, width of the rectangle
	 * @param v4 - by default, height of the rectangle
	 */
	public static void rect(double v1, double v2, double v3, double v4) {
		canvas.rect(v1, v2, v3, v4);
	}

	/**
	 * Draws a rounded rectangle to the screen, with r being the radius of all
	 * four corners.
	 * 
	 * @param v1 - by default, x-coordinate of the top left corner of the rectangle
	 * @param v2 - by default, y-coordinate of the top left corner of the rectangle
	 * @param v3 - by default, width of the rectangle
	 * @param v4 - by default, height of the rectangle
	 * @param r - radius of all four corners
	 */
	public static void rect(double v1, double v2, double v3, double v4, double r) {
		canvas.rect(v1, v2, v3, v4, r, r, r, r);
	}

	/**
	 * Draws a rounded rectangle to the screen, with tl, tr, bl and br being the
	 * radii of, respectively, top left, top right, bottom left and bottom right
	 * corners.
	 * 
	 * @param v1 - by default, x-coordinate of the top left corner of the rectangle
	 * @param v2 - by default, y-coordinate of the top left corner of the rectangle
	 * @param v3 - by default, width of the rectangle
	 * @param v4 - by default, height of the rectangle
	 * @param tl - radius for top-left corner
	 * @param tr - radius for top-right corner
	 * @param bl - radius for bottom-left corner
	 * @param br - radius for bottom-right corner
	 */
	public static void rect(double v1, double v2, double v3, double v4,
			double tl, double tr, double bl, double br) {
		canvas.rect(v1, v2, v3, v4, tl, tr, bl, br);
	}

	/**
	 * Draws a triangle to the screen.
	 * 
	 * @param x1 - x-coordinate of the first vertex
	 * @param y1 - y-coordinate of the first vertex
	 * @param x2 - x-coordinate of the second vertex
	 * @param y2 - y-coordinate of the second vertex
	 * @param x3 - x-coordinate of the third vertex
	 * @param y3 - y-coordinate of the third vertex
	 */
	public static void triangle(double x1, double y1, double x2, double y2,
			double x3, double y3) {
		double[] x = new double[] { x1, x2, x3 };
		double[] y = new double[] { y1, y2, y3 };
		canvas.polygon(x, y);
	}

	/**
	 * Save canvas as an image file supports png, jpeg, bmp, gif;
	 * default is png if no explicit extension is given
	 * 
	 * @param filename - desired image file's name
	 */
	public static void save(String fileName) {
		canvas.save(fileName);
	}

	/******************************************************
	 ************* ANIMATION & EVENT-HANDLING *************
	 ******************************************************/
	
	/**
	 * Save a numbered sequence of frames (i.e. each time the draw() function
	 * is run). It is recommended to call this function at the end of draw() 
	 * or within mouse and key events such as mousePressed() and keyPressed(). 
	 * The default (and recommended) file format is png.
	 */
	public static void saveFrame() {
		saveFrame = true;
	}

	/**
	 * Set the number of frames to be displayed every second. It is recommended
	 * to call this function in the setup method. The default frame rate is 60 fps.
	 * @param rate
	 */
	public static void frameRate(int rate) {
		frameRate = rate;
	}

	/**
	 * If called inside the setup method, the code in draw() will only be run once. 
	 * Recommended use is with loop() (which will continue looping over the code in 
	 * draw()), or redraw() in response to mouse and key events.
	 */
	public static void noLoop() {
		loop = false;
	}

	/**
	 * Resume looping over draw() code, in case it had been stopped by noLoop().
	 */
	public static void loop() {
		loop = true;
	}

	/**
	 * Executes the code within draw() one time. Redraw() should only be used within events
	 * methods like mousePressed() after noLoop() was called. It should not be called in
	 * draw().
	 */
	public static void redraw() {
		redraw = true;
	}

	/**
	 * Start animation by looping over the "methodName" method in the "className" class.
	 * The default methodName is "draw".
	 * @param className
	 * @param methodName
	 */
	public static void animate(String className, String methodName) {
		method = methodName;
		animate(className);
	}

	/**
	 * @param className
	 */
	public static void animate(String className) {
		canvas.isAnimation();
		try {
			//className = "demo." + className;
			callingClass = Class.forName(className);
		} catch (ClassNotFoundException cnfe) {
			System.out.println("Cannot find the class " + className);
		}
		try {
			drawMethod = callingClass.getMethod(method, (Class<?>[]) null);
			startTimer();
		} catch (NoSuchMethodException nsme) {
			System.out.println("No animation"); // for debug only
		}
	}

	/**
	 * Create a timer to repeatedly execute actionPerformed() at the interval
	 * defined by frameRate. Mouse and key events are handled here.
	 */
	private static void startTimer() {
		int delay = 1000 / frameRate; // in milliseconds

		ActionListener taskPerformer = new ActionListener() {
			int frameCount = 1;
			
			// Do the drawing in drawMethod and then handle events from the canvas' eventQ
			public void actionPerformed(ActionEvent evt) {
				// Must include the two catch blocks to stop exceptions
				try {
					if (loop || redraw) {
						drawMethod.invoke(null, (Object[]) null);
						redraw = false; // redraw only runs drawMethod once
					}
					
					Queue<InputEvent> events = canvas.getEventQ();
					while (!events.isEmpty()) {
						InputEvent e = events.remove();
						// Update the position of the mouse if the mouse
						// was pressed, dragged or moved.
						if (e.getID() == MouseEvent.MOUSE_PRESSED
								|| e.getID() == MouseEvent.MOUSE_DRAGGED
								|| e.getID() == MouseEvent.MOUSE_MOVED) {
							pmouseX = mouseX;
							pmouseY = mouseY;
							MouseEvent mouseEvent = (MouseEvent) e;
							mouseX = mouseEvent.getX();
							mouseY = mouseEvent.getY();
						}
						
						// Update the latest key pressed.
						if (e.getID() == KeyEvent.KEY_PRESSED) {
							KeyEvent keyEvent = (KeyEvent) e;
							key = keyEvent.getKeyChar();
						}
						
						// pass the appropriate event ID to handleEvent()
						switch (e.getID()) {
						case (MouseEvent.MOUSE_CLICKED):
							handleEvent(MouseEvent.MOUSE_CLICKED);
							break;
						case (MouseEvent.MOUSE_PRESSED):
							mousePressed = true;
							MouseEvent mouseEvent = (MouseEvent) e;
							mouseButton = mouseEvent.getButton();
							handleEvent(MouseEvent.MOUSE_PRESSED);
							break;
						case (MouseEvent.MOUSE_RELEASED):
							mousePressed = false;
							handleEvent(MouseEvent.MOUSE_RELEASED);
							break;
						case (MouseEvent.MOUSE_DRAGGED):
							handleEvent(MouseEvent.MOUSE_DRAGGED);
							break;
						case (MouseEvent.MOUSE_MOVED):
							handleEvent(MouseEvent.MOUSE_MOVED);
							break;
						case (MouseEvent.MOUSE_WHEEL):
							handleEvent(MouseEvent.MOUSE_WHEEL);
							break;
						case (KeyEvent.KEY_PRESSED):
							keyPressed = true;
							handleEvent(KeyEvent.KEY_PRESSED);
							break;
						case (KeyEvent.KEY_RELEASED):
							keyPressed = false;
							handleEvent(KeyEvent.KEY_RELEASED);
							break;
						}
					}
				} catch (InvocationTargetException ite) {
					ite.printStackTrace();
				} catch (IllegalAccessException iae) {
					iae.printStackTrace();
				}
				// endDraw will repaint and save frame if saveFrame is true
				canvas.endDraw(saveFrame, frameCount);
				frameCount++;
			}
		};
		timer = new Timer(delay, taskPerformer);
		timer.start();
	}

	/**
	 * Find any event-handling method in client code and execute it if that
	 * event has occurred. 
	 * @param eventID
	 */
	private static void handleEvent(int eventID) {
		String eventName = "";
		switch (eventID) {
		case (MouseEvent.MOUSE_CLICKED):
			eventName = "mouseClicked";
			break;
		case (MouseEvent.MOUSE_PRESSED):
			eventName = "mousePressed";
			break;
		case (MouseEvent.MOUSE_RELEASED):
			eventName = "mouseReleased";
			break;
		case (MouseEvent.MOUSE_DRAGGED):
			eventName = "mouseDragged";
			break;
		case (MouseEvent.MOUSE_MOVED):
			eventName = "mouseMoved";
			break;
		case (MouseEvent.MOUSE_WHEEL):
			eventName = "mouseWheel";
			break;
		case (KeyEvent.KEY_PRESSED):
			eventName = "keyPressed";
			break;
		case (KeyEvent.KEY_RELEASED):
			eventName = "keyReleased";
			break;
		default:
			System.out.println("No such event.");
		}

		try {
			eventMethod = callingClass.getMethod(eventName, (Class<?>[]) null);
		}
		catch (NoSuchMethodException nsme) {
		}

		if (eventMethod != null) {
			try {
				eventMethod.invoke(null, (Object[]) null);
				canvas.repaint();
				eventMethod = null; // reset it to wait for new events
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			} catch (IllegalAccessException iae) {
				iae.printStackTrace();
			}
		}
	}

	/******************************************************
	 *************** RANDOM NUMBER GENERATOR **************
	 ******************************************************/
	
	// Same implementation as Processing's to generate
	// testable results

	/**
	 * Generate a pseudo-random number from 0.0 (inclusive) to high (exclusive)
	 * @param high
	 * @return
	 */
	public static float random(int high) {
		return rand.nextFloat() * high;

	}

	/** 
	 * Generate a pseudo-random number from low (inclusive) to high (exclusive)
	 * @param low
	 * @param high
	 * @return
	 */
	public static float random(int low, int high) {
		if (low >= high)
			return low;
		int diff = high - low;
		return random(diff) + low;
	}

	/**
	 * Sets the seed for the random number generator
	 * @param seed
	 */
	public static void randomSeed(long seed) {
		rand.setSeed(seed);
	}
}
