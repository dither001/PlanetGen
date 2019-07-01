package controller;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.jogamp.nativewindow.NativeWindow;
import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.TraceMouseAdapter;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

import api.ViewType;
import graphics.PlanetColor;
import menu.ViewsMenu;
import model.GlobeModel;
import model.Planet;
import view.GlobeView;

public class PlanetViewController {

	private static ViewType viewType;

	private static final int GRID_SIZE;
	private static final GLProfile PROFILE;
	private static final GLCapabilities CAPABILITIES;

	static {
		setViewType(ViewType.ELEVATION);

		GRID_SIZE = 6;
		PROFILE = GLProfile.get(GLProfile.GL2);
		CAPABILITIES = new GLCapabilities(PROFILE);
	}

	/*
	 * MAIN METHOD
	 */
	public static void main(String[] args) {
		// GL Window
		final GLWindow window = GLWindow.create(CAPABILITIES);

		// event listener, adds GlobeView
		window.addGLEventListener(createGlobeView());
		window.setSize(480, 480);

		// starts animator, causing globe to rotate
		final FPSAnimator animator = new FPSAnimator(window, 300, true);
		animator.start();

		// mouse listener
		window.addMouseListener(new TraceMouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.out.println(e.getButton());
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
		});

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
		PlanetViewController.viewType = viewType;
	}

	/*
	 * PRIVATE METHODS
	 */
	private static GlobeModel createGlobeView() {
		Planet p;
		GlobeModel view = null;

		try {
			p = Planet.build(GRID_SIZE);
			//
			PlanetColor.setupColors(p);
			view = new GlobeModel(p, p.rotationToDefault());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return view;
	}

}
