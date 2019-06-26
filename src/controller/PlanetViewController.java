package controller;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

import api.ViewType;
import menu.ViewsMenu;
import model.Planet;
import view.GlobeView;
import view.GlobePanel;
import view.PlanetColor;

public class PlanetViewController {

	private static ViewType viewType;

	private static final GLProfile PROFILE;
	private static final GLCapabilities CAPABILITIES;

	static {
		setViewType(ViewType.TOPOGRAPHY);

		PROFILE = GLProfile.get(GLProfile.GL2);
		CAPABILITIES = new GLCapabilities(PROFILE);
	}

	/*
	 * MAIN METHOD
	 */
	public static void main(String... args) {
		// the canvas
		final GlobePanel main = new GlobePanel(CAPABILITIES);
		main.addGLEventListener(createGlobeView());

		// frame
		final JFrame frame = new JFrame("Random Planet");

		// content panel
		frame.getContentPane().add(main);

		// menu bar
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new ViewsMenu());
		frame.setJMenuBar(menuBar);

		// dimension - increases height to account for menu height
		Dimension d = frame.getContentPane().getPreferredSize();
		d.height += 23; // menu height isn't determined until display

		frame.setSize(d);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		// start animation
		final FPSAnimator animator = new FPSAnimator(main, 300, true);
		animator.start();

	}

	/*
	 * STATIC METHODS
	 */
	public static ViewType getViewType() {
		return viewType;
	}

	public static void setViewType(ViewType viewType) {
		PlanetViewController.viewType = viewType;
	}

	/*
	 * PRIVATE METHODS
	 */
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
