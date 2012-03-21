/**

  Example.java

  This class has the main method and sets up the window and
  does some initialization.  The bulk of the work is done in
  DrawAndHandleInput.java
  
  =========================================================

  Michael Eckmann
  Skidmore College
  revised Spring 2010
  CS325 Computer Graphics

  =========================================================

  This is a program that can be used as a start for 
  Programming Assignment 02

  =========================================================

  Credit where credit is due:

  This program is a modified version of a C program 
  used in G. Drew Kessler's Computer Graphics course at 
  Lehigh University.

  =========================================================

  Credit where credit is due:
  
  I based some of this code on the code found in
  JOGL: A Beginner's Guide and Tutorial
  By Kevin Conroy
  http://www.cs.umd.edu/~meesh/kmconroy/JOGLTutorial/

  =========================================================

  @author	
  	Michael Eckmann meckmann@skidmore.edu

*/

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.sun.opengl.util.Animator;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;

public class Example {

	private static DrawAndHandleInput dahi;
	public static Frame testFrame;
	
	public static void main(String[] args) 
  	{
	    /* Create the Frame */
        	testFrame = new Frame("TestFrame");
 
        
	        /* set the coordinates on the screen of the
	       upper left corner of the window 

	       So the window will start off at 10,10 
	       (near the upper left corner of the whole screen)
	       */
        	testFrame.setLocation(10, 10);

	    /* set the window to be 400x500 pixels 
               higher b/c of borders
            */
	        testFrame.setSize( 510, 428 );


		// This allows us to define some attributes
		// about the capabilities of GL for this program
		// such as color depth, and whether double buffering is
		// used.
		GLCapabilities glCapabilities = new GLCapabilities();
		glCapabilities.setRedBits(8);
		glCapabilities.setGreenBits(8);
		glCapabilities.setBlueBits(8);
		glCapabilities.setAlphaBits(8);

		/*
		 * this will turn on double buffering
		 * ignore for now
		 * glCapabilities.setDoubleBuffered(true);
		 */
		glCapabilities.setDoubleBuffered(true);
		// create the GLCanvas that is to be added to our Frame
		GLCanvas canvas = new GLCanvas(glCapabilities);
		testFrame.add( canvas );

		// create the Animator and attach the GLCanvas to it
		Animator a = new Animator(canvas);
		
		// create an instance of the Class that listens to all events
		// (GLEvents, Keyboard, and Mouse events)
		// add this object as all these listeners to the canvas 
		dahi = new DrawAndHandleInput(canvas);
		canvas.addGLEventListener(dahi);
		canvas.addKeyListener(dahi);
		canvas.addMouseListener(dahi);

		// this will swap the buffers (when double buffering)
		// ignore for now
		// canvas.swapBuffers();
		
		// if user closes the window by clicking on the X in 
		// upper right corner
		testFrame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		      System.exit(0);
		    }
		  });
		
		testFrame.setVisible(true);
		a.start(); // start the Animator, which periodically calls display() on the GLCanvas

	}

}

