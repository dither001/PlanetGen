package controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.TraceMouseAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.awt.TextRenderer;

import api.ViewType;
import graphics.PlanetColor;
import menu.ViewsMenu;
import model.ZGlobeModel;
import model.Planet;
import model.Tile;
import view.zGlobeView;

public class ZGlobeViewController {

	// CONSTANT
	private static final int GRID_SIZE;
	private static final GLProfile PROFILE;
	private static final GLCapabilities CAPABILITIES;

	private static ViewType viewType;
	private static int selectedTile;

	static {
		/*
		 * GRID_SIZE 6 = 7,292 tiles (Civ6 largest map = 6,996 tiles)
		 */
		GRID_SIZE = 6;
		PROFILE = GLProfile.get(GLProfile.GL2);
		CAPABILITIES = new GLCapabilities(PROFILE);

		setViewType(ViewType.ELEVATION);
		setSelectedTile(16);
	}

	/*
	 * MAIN METHOD
	 */
	private static void main1(String[] args) {
		zGlobeView view = new zGlobeView(CAPABILITIES);

		// frame
		final JFrame frame = new JFrame("Random Planet");

		// adds NewtCanvas to frame
		frame.getContentPane().add(view);

		/*
		 * It seems the menu's "height" isn't determined until display time, so I have
		 * to get the preferred dimensions of the content pane (in this case, the
		 * GlobeView inside a GLCanvas), then increase the height to allow for the menu.
		 */
		// Dimension d = frame.getContentPane().getPreferredSize();
		Dimension d = new Dimension(480, 480 + 23);
		// d.height += 23;

		// menu bar
		JMenuBar menuBar = new JMenuBar();

		// add View menu to menu bar
		menuBar.add(new ViewsMenu());
		frame.setJMenuBar(menuBar);

		frame.setSize(d);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// GL Window
		final GLWindow window = GLWindow.create(CAPABILITIES);
		// final GlobeView window = new GlobeView(CAPABILITIES);

		// event listener, adds GlobeView
		window.addGLEventListener(createGlobeModel());
		window.setSize(480, 480);

		// starts animator, causing globe to rotate
		final FPSAnimator animator = new FPSAnimator(window, 300, true);
		animator.start();

		// mouse listener
		window.addMouseListener(new GlobeMouse());

		// XXX - window listener (not sure if I need this... ?)
		// window.addWindowListener(new WindowAdapter() {
		//
		// public void windowClosing(WindowEvent e) {
		// animator.stop();
		// window.destroy();
		// // frame.dispose();
		// System.exit(0);
		// }
		// });

		// NewtCanvasAWT enables interoperability between JOGL and Swing
		NewtCanvasAWT canvas = new NewtCanvasAWT(window);

		// frame
		final JFrame frame = new JFrame("Random Planet");

		// adds NewtCanvas to frame
		frame.getContentPane().add(canvas);

		/*
		 * It seems the menu's "height" isn't determined until display time, so I have
		 * to get the preferred dimensions of the content pane (in this case, the
		 * GlobeView inside a GLCanvas), then increase the height to allow for the menu.
		 */
		Dimension d = frame.getContentPane().getPreferredSize();
		d.height += 23;

		// menu bar
		JMenuBar menuBar = new JMenuBar();

		// add View menu to menu bar
		menuBar.add(new ViewsMenu());
		frame.setJMenuBar(menuBar);

		frame.setSize(d);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	/*
	 * STATIC METHODS
	 */
	public static ViewType getViewType() {
		return viewType;
	}

	public static void setViewType(ViewType viewType) {
		ZGlobeViewController.viewType = viewType;
	}

	public static int getSelectedTile() {
		return selectedTile;
	}

	public static void setSelectedTile(int selectedTile) {
		ZGlobeViewController.selectedTile = selectedTile;
	}

	/*
	 * PRIVATE METHODS
	 */
	private static ZGlobeModel createGlobeModel() {
		// default array is UWP for earth [8,6,7,A,6,9]
		int[] array = new int[] { 8, 6, 7, 10, 6, 9 };

		return createGlobeModel(array);
	}

	private static ZGlobeModel createGlobeModel(int[] uwp) {
		Planet p = null;
		ZGlobeModel view = null;

		try {
			p = Planet.build(GRID_SIZE);
			//
			PlanetColor.setupColors(p);
			view = new ZGlobeModel(p, p.rotationToDefault());

			// System.out.println(p.tileSize());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;
	}

	/*
	 * INNER CLASS
	 */
	private static class GlobeMouse extends TraceMouseAdapter {
		public void mouseClicked(MouseEvent e) {
			System.out.printf("Button: %d (%d, %d) %n", e.getButton(), e.getX(), e.getY());

			// step 1
			float x = (2.0f * e.getX()) / zGlobeView.DEFAULT_WIDTH - 1.0f;
			float y = 1.0f - (2.0f * e.getY()) / zGlobeView.DEFAULT_HEIGHT;
			float z = 1.0f;

			float[] ray_nds = new float[] { x, y, z };

			// step 2?
			Quaternion ray_clip = new Quaternion(x, y, -1.0f, 1.0f);

			// step 3?
			// Quaternion ray_eye = new PMVMatrix();
		}

		public void mouseDragged(MouseEvent e) {
			// TODO
		}

		public void mouseEntered(MouseEvent e) {
			// TODO
		}

		public void mouseExited(MouseEvent e) {
			// TODO
		}

		public void mouseMoved(MouseEvent e) {
			// TODO
		}

		public void mousePressed(MouseEvent e) {
			// TODO
		}

		public void mouseReleased(MouseEvent e) {
			// TODO
		}
	}

}
