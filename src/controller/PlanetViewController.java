package controller;

import javax.swing.JFrame;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import model.Planet;
import view.GlobeView;
import view.PlanetColor;

public class PlanetViewController {

	private static final GLProfile PROFILE;
	private static final GLCapabilities CAPABILITIES;

	static {
		PROFILE = GLProfile.get(GLProfile.GL2);
		CAPABILITIES = new GLCapabilities(PROFILE);
	}

	public static void main(String... args) {
		// the canvas
		final GLCanvas glcanvas = new GLCanvas(CAPABILITIES);
		glcanvas.addGLEventListener(createGlobeView());
		glcanvas.setSize(600, 600);

		// the frame
		final JFrame frame = new JFrame("Random Planet");
		frame.getContentPane().add(glcanvas);
		frame.setSize(frame.getContentPane().getPreferredSize());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// start animation
		final FPSAnimator animator = new FPSAnimator(glcanvas, 300, true);
		animator.start();
	}

	private static GlobeView createGlobeView() {
		Planet p;
		GlobeView view = null;

		try {
			p = Planet.build(6);
			PlanetColor.colorTopography(p);
			PlanetColor.colorVegetation(p.getClimate().getSeason(), p);
			PlanetColor.colorTemperature(p.getClimate().getSeason(), p);
			PlanetColor.colorAridity(p.getClimate().getSeason(), p);
			PlanetColor.colorHumidity(p.getClimate().getSeason(), p);
			PlanetColor.colorPrecipitation(p.getClimate().getSeason(), p);

			view = new GlobeView(p, p.rotationToDefault());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;
	}

}
