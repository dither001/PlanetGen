package controller;

import javax.swing.JFrame;

import com.jogamp.newt.awt.NewtCanvasAWT;

public class PlanetViewController {
	
	/*
	 * STATIC METHODS
	 */
	private static JFrame frame;
	private static NewtCanvasAWT canvas;

	/*
	 * MAIN METHOD
	 */
	public static void main(String[] args) {
		frame = new JFrame("Random Planet");
		canvas = new NewtCanvasAWT();
		
	}
}
