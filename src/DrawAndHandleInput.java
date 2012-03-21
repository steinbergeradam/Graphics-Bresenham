/**

  DrawAndHandleInput.java
  
  This class handles all the drawing and the input.
  
  =========================================================
    
  Michael Eckmann
  Skidmore College
  revised Spring 2010
  CS325 Computer Graphics

  =========================================================
  
  This is an example program that shows how to 
    - draw a grid to be used for program 02
    - handle mouse input
    - handle keyboard input

  =========================================================

  Credit where credit is due:

  This program is a modified version of a program 
  used in G. Drew Kessler's Computer Graphics course at 
  Lehigh University that I took in 1999.

  =========================================================

  @author	
  	Michael Eckmann meckmann@skidmore.edu
  
 */

// imports the Listener classes
// imports the Event classes

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;

// imports the openGL stuff
import javax.media.opengl.GL;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

/*

 We implement the KeyListener and MouseListener to get access to 
 those input events.

 We also implement the GLEventListener which has us provide code
 for the following: 
 display
 displayChange
 init
 reshape

 */
public class DrawAndHandleInput implements GLEventListener, KeyListener,
		MouseListener {

	/* this object will give us access to the gl functions */
	private static GL gl;
	/* this object will give us access to the glu functions */
	private static GLU glu;

	/* define the world coordinate limits */
	public static final int minX = 0;
	public static final int minY = 0;
	public static final int maxX = 250;
	public static final int maxY = 200;

	// height and width of the bigpixel area in world coordinate measurements
	public static final int bigAreaHeight = 200;
	public static final int bigAreaWidth = 200;

	// number of rows and columns of big pixels to appear in the grid
	// eventually these will be set by user input or via a command line
	// parameter. That is functionality you need to add for program 02.
	public static int bigPixelRows = 60;
	public static int bigPixelCols = 60;

	public static double smallLeft = bigAreaWidth;
	public static double smallBottom = bigAreaHeight - 5.0 * (maxY - minY)
			/ 8.0;
	public static double smallRight = bigAreaWidth + 0.2 * (maxX - minX);
	public static double smallTop = bigAreaHeight - 3.0 * (maxY - minY) / 8.0;

	public static double smallWidth = 0.2 * (maxX - minX);
	public static double smallHeight = 3.0 * (maxY - minY) / 8.0;

	// globals that hold the coordinates of the big pixel that is to be
	// "turned on"
	// you may want to change the way this is done.
	public static double bigPixelX = 0;
	public static double bigPixelY = 0;
	public static double antiPixelX = 0;
	public static double antiPixelY = 0;

	private static float colorR = 1.0f;
	private static float colorG = 0.0f;
	private static float colorB = 0.0f;
	private static GLCanvas canvas;

	private static double startX = 0;
	private static double startY = 0;
	private static double endX = 1;
	private static double endY = 1;
	private static boolean lineChanged = true;
	private static boolean isAntialias = false;
	private static boolean isCircle = false;

	public DrawAndHandleInput(GLCanvas c) {
		canvas = c;
	} // end constructor

	// ====================================================================================
	//
	// Start of the methods in GLEventListener
	//

	/**
	 * ============================================================= This method
	 * is called by the drawable to do initialization.
	 * =============================================================
	 * 
	 * @param drawable
	 *            The GLCanvas that will be drawn to
	 */
	public void init(GLAutoDrawable drawable) {

		bigPixelRows = Integer.parseInt(JOptionPane
				.showInputDialog("Enter number of rows:"));
		bigPixelCols = Integer.parseInt(JOptionPane
				.showInputDialog("Enter number of cols:"));

		gl = drawable.getGL();
		glu = new GLU(); // from demo for new version

		/* Set the clear color to black */
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		/* sets up the projection matrix from world to window coordinates */
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		/* show the whole world within the window */
		glu.gluOrtho2D(minX, maxX, minY, maxY);

		// wraps the GL to provide error checking and so
		// it will throw a GLException at the point of failure
		drawable.setGL(new DebugGL(drawable.getGL()));

	} // end init()

	/**
	 * ============================================================= This method
	 * is called when the screen needs to be drawn.
	 * =============================================================
	 * 
	 * @param drawable
	 *            The GLCanvas that will be drawn to
	 */
	public void display(GLAutoDrawable drawable) {

		/* clear the color buffer */
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		/*
		 * sets up the current color for drawing for polygons, it's the
		 * "fill color"
		 */
		gl.glColor3f(1, 1, 1);

		// draw the "big pixel area"
		gl.glBegin(GL.GL_POLYGON);
		// These are the vertices of the polygon using world coordinates
		gl.glVertex2i(0, 0); // vertex 1
		gl.glVertex2i(bigAreaWidth, 0);
		gl.glVertex2i(bigAreaWidth, bigAreaHeight);
		gl.glVertex2i(0, bigAreaHeight);
		gl.glEnd();

		// draw the "small pixel area" to the right of the "big pixel area"
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex2d(smallLeft, smallBottom);
		gl.glVertex2d(smallRight, smallBottom);
		gl.glVertex2d(smallRight, smallTop);
		gl.glVertex2d(smallLeft, smallTop);
		gl.glEnd();

		// change color to black for grid lines
		gl.glColor3f(0, 0, 0);

		// draw the vertical lines for the big grid
		for (int col = 0; col <= bigPixelCols; col++) {
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2d((double) col * bigAreaWidth / bigPixelCols, 0);
			gl.glVertex2d((double) col * bigAreaWidth / bigPixelCols,
					bigAreaHeight);
			gl.glEnd();
		} // end for

		// draw the horizontal lines for the big grid
		for (int row = 0; row <= bigPixelRows; row++) {
			gl.glBegin(GL.GL_LINES);
			gl.glVertex2d(0, (double) row * bigAreaHeight / bigPixelRows);
			gl.glVertex2d(bigAreaWidth, (double) row * bigAreaHeight
					/ bigPixelRows);
			gl.glEnd();
		} // end for

		if (isAntialias) {

			// uses the global double variables that were set when user
			// clicked as the coordinates of the bigpixel to be drawn.
			drawAntiLine((int) startX, (int) startY, (int) endX, (int) endY);

		} else if (isCircle) {

			// uses the global double variables that were set when user
			// clicked as the coordinates of the bigpixel to be drawn.
			drawCircle((int) startX, (int) startY, (int) endX, (int) endY);

		} else {

			// uses the global double variables that were set when user
			// clicked as the coordinates of the bigpixel to be drawn.
			drawBigLine((int) startX, (int) startY, (int) endX, (int) endY);

		} // end if

		/* force any buffered calls to actually be executed */
		gl.glFlush();

		// this will swap the buffers (when double buffering)
		// canvas.swapBuffers() should do the same thing
		drawable.swapBuffers();

	} // end display()

	public static void drawAntiLine(int x0, int y0, int xEnd, int yEnd) {

		ArrayList<Point> bigLine = new ArrayList<Point>();
		int[][] numAntiPix = new int[bigPixelRows][bigPixelCols];

		int dx = Math.abs(xEnd - x0);
		int dy = Math.abs(yEnd - y0);

		if (dy <= dx) {

			int p = 2 * dy - dx;
			int twoDy = 2 * dy;
			int twoDyMinusDx = 2 * (dy - dx);
			int x, y;

			if (x0 > xEnd) {
				x = xEnd;
				y = yEnd;
				xEnd = x0;
			} else {
				x = x0;
				y = y0;
			} // end if

			Point bigPixel = new Point(x / 3, y / 3);
			numAntiPix[x / 3][y / 3]++;
			if (!bigLine.contains(bigPixel))
				bigLine.add(bigPixel);

			while (x < xEnd) {
				x++;
				if (p < 0) {
					p += twoDy;
				} else if (((x0 < xEnd) && (y0 > yEnd))
						|| ((x0 == xEnd) && (y0 < yEnd))) {
					y--;
					p += twoDyMinusDx;
				} else {
					y++;
					p += twoDyMinusDx;
				} // end if

				bigPixel = new Point(x / 3, y / 3);
				numAntiPix[x / 3][y / 3]++;
				if (!bigLine.contains(bigPixel))
					bigLine.add(bigPixel);

			} // end while

		} else {

			int p = 2 * dx - dy;
			int twoDx = 2 * dx;
			int twoDxMinusDy = 2 * (dx - dy);
			int x, y;

			if (y0 > yEnd) {
				x = xEnd;
				y = yEnd;
				yEnd = y0;
			} else {
				x = x0;
				y = y0;
			} // end if

			Point bigPixel = new Point(x / 3, y / 3);
			numAntiPix[x / 3][y / 3]++;
			if (!bigLine.contains(bigPixel))
				bigLine.add(bigPixel);

			while (y < yEnd) {
				y++;
				if (p < 0) {
					p += twoDx;
				} else if (((y0 < yEnd) && (x0 > xEnd))
						|| ((y0 == yEnd) && (x0 < xEnd))) {
					x--;
					p += twoDxMinusDy;
				} else {
					x++;
					p += twoDxMinusDy;
				} // end if

				bigPixel = new Point(x / 3, y / 3);
				numAntiPix[x / 3][y / 3]++;
				if (!bigLine.contains(bigPixel))
					bigLine.add(bigPixel);

			} // end while

		} // end if

		for (int i = 0; i < bigLine.size(); i++) {
			Point p = bigLine.get(i);
			int intensity = numAntiPix[p.x][p.y];
			switch (intensity) {
			case 0:
			case 1:
				drawBigPixel(p.x, p.y, 0.67f, 0.67f, 0.67f);
				drawSmallPixel(p.x, p.y, 0.67f, 0.67f, 0.67f);
				break;
			case 2:
				drawBigPixel(p.x, p.y, 0.33f, 0.33f, 0.33f);
				drawSmallPixel(p.x, p.y, 0.33f, 0.33f, 0.33f);
				break;
			case 3:
				drawBigPixel(p.x, p.y, 0, 0, 0);
				drawSmallPixel(p.x, p.y, 0, 0, 0);
				break;
			} // end switch
		} // end for

	} // end drawAntiLine()

	public void drawBigLine(int x0, int y0, int xEnd, int yEnd) {

		int dx = Math.abs(xEnd - x0);
		int dy = Math.abs(yEnd - y0);

		if (dy <= dx) {

			int p = 2 * dy - dx;
			int twoDy = 2 * dy;
			int twoDyMinusDx = 2 * (dy - dx);
			int x, y;

			if (x0 > xEnd) {
				x = xEnd;
				y = yEnd;
				xEnd = x0;
			} else {
				x = x0;
				y = y0;
			} // end if

			drawBigPixel(x, y, 0, 0, 0);
			drawSmallPixel(x, y, 0, 0, 0);

			while (x < xEnd) {
				x++;
				if (p < 0) {
					p += twoDy;
				} else if (((x0 < xEnd) && (y0 > yEnd))
						|| ((x0 == xEnd) && (y0 < yEnd))) {
					y--;
					p += twoDyMinusDx;
				} else {
					y++;
					p += twoDyMinusDx;
				} // end if
				drawBigPixel(x, y, 0, 0, 0);
				drawSmallPixel(x, y, 0, 0, 0);
			} // end while

		} else {

			int p = 2 * dx - dy;
			int twoDx = 2 * dx;
			int twoDxMinusDy = 2 * (dx - dy);
			int x, y;

			if (y0 > yEnd) {
				x = xEnd;
				y = yEnd;
				yEnd = y0;
			} else {
				x = x0;
				y = y0;
			} // end if

			drawBigPixel(x, y, 0, 0, 0);
			drawSmallPixel(x, y, 0, 0, 0);

			while (y < yEnd) {
				y++;
				if (p < 0) {
					p += twoDx;
				} else if (((y0 < yEnd) && (x0 > xEnd))
						|| ((y0 == yEnd) && (x0 < xEnd))) {
					x--;
					p += twoDxMinusDy;
				} else {
					x++;
					p += twoDxMinusDy;
				} // end if
				drawBigPixel(x, y, 0, 0, 0);
				drawSmallPixel(x, y, 0, 0, 0);
			} // end while

		} // end if

	} // end drawBigLine()

	public void drawCircle(int centerX, int centerY, int radiusX, int radiusY) {

		int dx = Math.abs(radiusX - centerX);
		int dy = Math.abs(radiusY - centerY);

		// get radius of circle
		double xSquared = Math.pow(dx, 2);
		double ySquared = Math.pow(dy, 2);
		double r = Math.sqrt(xSquared + ySquared);

		double x_k = 0f;
		double y_k = r;
		double p_k = (5 / 4) - r;
		double x_kPlus1 = x_k + 1;
		double y_kPlus1 = y_k - 1;

		this.circlePlot(centerX, centerY, (int) x_k, (int) y_k);

		while (x_k < y_k) {

			x_kPlus1 = x_k + 1.0;
			y_kPlus1 = y_k - 1.0;

			if (p_k < 0) {
				// (x_k, y_k) inside circle boundary
				p_k += 2.0 * x_kPlus1 + 1.0;
				this.circlePlot(centerX, centerY, (int) x_kPlus1, (int) y_k);
			} else {
				// (x_k, y_k) on or outside circle boundary
				p_k += (2.0 * x_kPlus1) + 1.0 - (2.0 * y_kPlus1);
				this.circlePlot(centerX, centerY, (int) (x_k + 1),
						(int) (y_k - 1));
				y_k = y_kPlus1;
			} // end if

			x_k = x_kPlus1;

		} // end while

	} // end drawCircle()

	private void circlePlot(int centerX, int centerY, int radiusX, int radiusY) {

		if ((centerX + radiusX <= bigAreaWidth)
				&& (centerY + radiusY <= bigAreaHeight)) {
			drawBigPixel(centerX + radiusX, centerY + radiusY, 0, 0, 0);
			drawSmallPixel(centerX + radiusX, centerY + radiusY, 0, 0, 0);
		} // end if

		if ((centerX - radiusX >= 0) && (centerY + radiusY <= bigAreaHeight)) {
			drawBigPixel(centerX - radiusX, centerY + radiusY, 0, 0, 0);
			drawSmallPixel(centerX - radiusX, centerY + radiusY, 0, 0, 0);
		} // end if

		if ((centerX + radiusX <= bigAreaWidth) && (centerY - radiusY >= 0)) {
			drawBigPixel(centerX + radiusX, centerY - radiusY, 0, 0, 0);
			drawSmallPixel(centerX + radiusX, centerY - radiusY, 0, 0, 0);
		} // end if

		if ((centerX - radiusX >= 0) && (centerY - radiusY >= 0)) {
			drawBigPixel(centerX - radiusX, centerY - radiusY, 0, 0, 0);
			drawSmallPixel(centerX - radiusX, centerY - radiusY, 0, 0, 0);
		} // end if

		if ((centerX + radiusY <= bigAreaWidth)
				&& (centerY + radiusX <= bigAreaHeight)) {
			drawBigPixel(centerX + radiusY, centerY + radiusX, 0, 0, 0);
			drawSmallPixel(centerX + radiusY, centerY + radiusX, 0, 0, 0);
		} // end if

		if ((centerX - radiusY >= 0) && (centerY + radiusX <= bigAreaHeight)) {
			drawBigPixel(centerX - radiusY, centerY + radiusX, 0, 0, 0);
			drawSmallPixel(centerX - radiusY, centerY + radiusX, 0, 0, 0);
		} // end if

		if ((centerX + radiusY <= bigAreaWidth) && (centerY - radiusX >= 0)) {
			drawBigPixel(centerX + radiusY, centerY - radiusX, 0, 0, 0);
			drawSmallPixel(centerX + radiusY, centerY - radiusX, 0, 0, 0);
		} // end if

		if ((centerX - radiusY >= 0) && (centerY - radiusX >= 0)) {
			drawBigPixel(centerX - radiusY, centerY - radiusX, 0, 0, 0);
			drawSmallPixel(centerX - radiusY, centerY - radiusX, 0, 0, 0);
		} // end if

	}// end circlePlot()

	/*
	 * 
	 * method name: drawBigPixel
	 * 
	 * takes in "big pixel" coordinates and displays the "big pixel" (polygon)
	 * associated with those coordinates. Note: the "big pixel" area is situated
	 * so that 0, 0 is the highest-leftmost big pixel and BIXPIXEL_COLS - 1,
	 * BIGPIXELROWS - 1 is the lowest-rightmost big pixel
	 * 
	 * parameters: x - the x coordinate of the big pixel y - the y coordinate of
	 * the big pixel
	 */
	public static void drawBigPixel(int x, int y, double r, double g, double b) {
		// because the y screen coordinates increase as we go down
		// and the y world coordinates increase as we go up
		// we need to compute flip_y which will be the y coordinate
		// of the big pixel if the big pixel coordinates' y values
		// increased as we go up
		int flip_y = Math.abs((bigPixelRows - 1) - y);
		gl.glColor3d(r + colorR, g + colorG, b + colorB);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex2d((double) x * bigAreaHeight / bigPixelRows,
				(double) flip_y * bigAreaWidth / bigPixelCols);
		gl.glVertex2d((double) (x + 1) * bigAreaHeight / bigPixelRows,
				(double) flip_y * bigAreaWidth / bigPixelCols);
		gl.glVertex2d((double) (x + 1) * bigAreaHeight / bigPixelRows,
				(double) (flip_y + 1) * bigAreaWidth / bigPixelCols);
		gl.glVertex2d((double) x * bigAreaHeight / bigPixelRows,
				(double) (flip_y + 1) * bigAreaWidth / bigPixelCols);
		gl.glEnd();
	} // end drawBigPixel()

	/*
	 * 
	 * method name: drawSmallPixel
	 * 
	 * takes in "small pixel" coordinates and displays the "small pixel"
	 * (polygon) associated with those coordinates. Note: the "small pixel" area
	 * is situated so that 0, 0 is the highest-leftmost small pixel and
	 * BIXPIXEL_COLS - 1, BIGPIXELROWS - 1 is the lowest-rightmost small pixel
	 * 
	 * parameters: x - the x coordinate of the small pixel y - the y coordinate
	 * of the small pixel
	 */
	public static void drawSmallPixel(int x, int y, double r, double g, double b) {
		// because the y screen coordinates increase as we go down
		// and the y world coordinates increase as we go up
		// we need to compute flip_y which will be the y coordinate
		// of the small pixel if the small pixel coordinates' y values
		// increased as we go up
		int flip_y = Math.abs((bigPixelRows - 1) - y);
		gl.glColor3d(r + colorR, g + colorG, b + colorB);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex2d(smallLeft + (double) x * smallHeight / bigPixelRows,
				smallBottom + (double) flip_y * smallWidth / bigPixelCols);
		gl.glVertex2d(
				smallLeft + (double) (x + 1) * smallHeight / bigPixelRows,
				smallBottom + (double) flip_y * smallWidth / bigPixelCols);
		gl.glVertex2d(
				smallLeft + (double) (x + 1) * smallHeight / bigPixelRows,
				smallBottom + (double) (flip_y + 1) * smallWidth / bigPixelCols);
		gl.glVertex2d(smallLeft + (double) x * smallHeight / bigPixelRows,
				smallBottom + (double) (flip_y + 1) * smallWidth / bigPixelCols);
		gl.glEnd();
	} // end drawSmallPixel()

	/**
	 * ============================================================= This method
	 * is called when the window is resized.
	 * =============================================================
	 * 
	 * @param drawable
	 *            The GLCanvas that will be drawn to
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
	} // end reshape()

	/**
	 * ============================================================= Called by
	 * the drawable when the display mode or the display device associated with
	 * the GLDrawable has changed.
	 * =============================================================
	 * 
	 * @param drawable
	 *            The GLCanvas that will be drawn to
	 * @param modeChanged
	 *            not implemented
	 * @param deviceChanged
	 *            not implemented
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	} // end displayChanged()

	//
	// End of methods in GLEventListener
	//
	// ====================================================================================

	// ====================================================================================
	// Deal with the keyboard events
	// KeyListener require the following methods to be
	// specified:
	//
	// keyReleased
	// keyPressed
	// keyTyped

	public void keyReleased(KeyEvent ke) {
	} // end keyReleased()

	public void keyPressed(KeyEvent ke) {
	} // end keyPressed()

	public void keyTyped(KeyEvent ke) {
		char ch = ke.getKeyChar();
		switch (ch) {
		case 'A':
		case 'a':
			if (isAntialias) {
				isAntialias = false;
				startX = 0;
				startY = 0;
				endX = 1;
				endY = 1;
			} else {
				isAntialias = true;
				startX = 0;
				startY = 0;
				endX = 1;
				endY = 1;
			} // end if
			isCircle = false;
			break;
		case 'C':
		case 'c':
			if (isCircle) {
				isCircle = false;
				startX = 0;
				startY = 0;
				endX = 1;
				endY = 1;
			} else {
				isCircle = true;
				startX = bigPixelRows / 2;
				startY = bigPixelCols / 2;
				endX = bigPixelRows / 3;
				endY = bigPixelCols / 3;
			} // end if
			isAntialias = false;

			break;
		case 'Q':
		case 'q':
			System.exit(0);
			break;
		case 'R':
		case 'r':
			colorR = 1.0f;
			colorG = 0.0f;
			colorB = 0.0f;
			break;
		case 'G':
		case 'g':
			colorR = 0.0f;
			colorG = 1.0f;
			colorB = 0.0f;
			break;
		case 'B':
		case 'b':
			colorR = 0.0f;
			colorG = 0.0f;
			colorB = 1.0f;
			break;
		} // end switch
	} // end keyTyped

	//
	// End of dealing with Keyboard Events
	//
	// ====================================================================================

	// ====================================================================================
	//
	// Deal with the mouse events
	// MouseListener require the following methods to be
	// specified:
	//
	// mouseReleased
	// mouseEntered
	// mouseExited
	// mouseClicked
	// mousePressed

	public void mouseReleased(MouseEvent me) {
	} // end mouseReleased()

	public void mouseEntered(MouseEvent me) {
	} // end mouseEntered()

	public void mouseExited(MouseEvent me) {
	} // end mouseExited()

	public void mouseClicked(MouseEvent me) {

		lineChanged = true;

		// to get the coordinates of the event
		int x, y;
		// to store which button was clicked, left, right or middle
		int button;

		x = me.getX();
		y = me.getY();

		System.out.println("x = " + x + " y = " + y);
		button = me.getButton();

		// example code for how to check which button was clicked
		if (button == MouseEvent.BUTTON1) {
			System.out.println("LEFT click");
		} else if (button == MouseEvent.BUTTON2) {
			System.out.println("MIDDLE click");
		} else if (button == MouseEvent.BUTTON3) {
			System.out.println("RIGHT click");
		} // end if

		/*
		 * getButton() - returns an int which should be compared against
		 * BUTTON1, BUTTON2, or BUTTON3 the left button is BUTTON1 the middle
		 * button is BUTTON2 the right button is BUTTON3
		 * 
		 * these are the correct designations for linux, they should also be
		 * correct for windows, but I recall some kind of difference for mac os.
		 * try it and see
		 */

		// get the current size of the canvas
		Dimension d = canvas.getSize();

		// System.out.println("height of canvas = " + d.height +
		// " and width of canvas = " + d.width);
		// System.out.println("x = " + x + " and y = " + y);

		// using the x,y screen coordinates of the point that was clicked and
		// using the current size of the canvas and the number of
		// bigpixel rows and columns and the min and max of the
		// world coordinates, compute the coordinates of the bigpixel
		// where bigpixelx increases from left to right starting at 0 and going
		// to
		// BIGPIXEL_COLS - 1
		// and
		// bigpixely increases from top to bottom starting at 0 and going to
		// BIGPIXEL_ROWS - 1
		//
		if (!isAntialias) {

			bigPixelY = y / ((double) d.height / bigPixelRows);
			bigPixelX = x
					/ (((double) d.width / ((double) (maxX - minX) / (maxY - minY))) / bigPixelCols);

			if (button == MouseEvent.BUTTON1) {
				startX = bigPixelX;
				startY = bigPixelY;
			} // end if

			if (button == MouseEvent.BUTTON3) {
				endX = bigPixelX;
				endY = bigPixelY;
			} // end if

			//
			// if either the x or y coordinate of the big pixel is
			// "out of bounds"
			// place it at the nearest big pixel "in bounds"
			//
			if (bigPixelX >= bigPixelCols)
				bigPixelX = bigPixelCols - 1;
			if (bigPixelY >= bigPixelRows)
				bigPixelY = bigPixelRows - 1;
			if (bigPixelX < 0)
				bigPixelX = 0;
			if (bigPixelY < 0)
				bigPixelY = 0;

		} else if (isCircle) {

			bigPixelY = y / ((double) d.height / bigPixelRows);
			bigPixelX = x
					/ (((double) d.width / ((double) (maxX - minX) / (maxY - minY))) / bigPixelCols);

			if (button == MouseEvent.BUTTON1) {
				startX = bigPixelX;
				startY = bigPixelY;
			} // end if

			if (button == MouseEvent.BUTTON3) {
				endX = bigPixelX;
				endY = bigPixelY;
			} // end if

		} else {

			antiPixelY = y / ((double) d.height / (bigPixelRows * 3));
			antiPixelX = x
					/ (((double) d.width / ((double) (maxX - minX) / (maxY - minY))) / (bigPixelCols * 3));

			if (button == MouseEvent.BUTTON1) {
				startX = antiPixelX;
				startY = antiPixelY;
			} // end if

			if (button == MouseEvent.BUTTON3) {
				endX = antiPixelX;
				endY = antiPixelY;
			} // end if

			//
			// if either the x or y coordinate of the big pixel is
			// "out of bounds"
			// place it at the nearest big pixel "in bounds"
			//
			if (antiPixelX >= 3 * bigPixelCols)
				antiPixelX = 3 * bigPixelCols - 1;
			if (antiPixelY >= 3 * bigPixelRows)
				antiPixelY = 3 * bigPixelRows - 1;
			if (antiPixelX < 0)
				antiPixelX = 0;
			if (antiPixelY < 0)
				antiPixelY = 0;

		} // end if

		System.out.println("startX=" + Double.toString(startX) + ", startY="
				+ Double.toString(startY));
		System.out.println("endX=" + Double.toString(endX) + ", endY="
				+ Double.toString(endY));

		double dx = Math.abs(endX - startX);
		double dy = Math.abs(endY - startY);
		System.out.println("dx=" + Double.toString(dx) + ", dy="
				+ Double.toString(dy));

		// System.out.println("bigpixel x, y  = " + bigpixelx + ", " +
		// bigpixely);

	} // end mouseClicked()

	public void mousePressed(MouseEvent me) {
	} // end mousePressed()

	public static boolean isLineChanged() {
		return lineChanged;
	} // end isLineChanged()

	public static void setLineChanged(boolean lineChanged) {
		DrawAndHandleInput.lineChanged = lineChanged;
	} // end setLineChanged()

	public static boolean isAntialias() {
		return isAntialias;
	} // end isAntialias()

	public static void setAntialias(boolean isAntialias) {
		DrawAndHandleInput.isAntialias = isAntialias;
	} // end setAntialias()

	//
	// End of dealing with Mouse Events
	//
	// ====================================================================================

} // end class

