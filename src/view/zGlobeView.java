package view;

//import com.jogamp.newt.Window;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseEvent;
//import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.TraceMouseAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;
//import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.FPSAnimator;

import graphics.PlanetColor;
import model.ZGlobeModel;
import model.Planet;

@SuppressWarnings("serial")
public class zGlobeView extends NewtCanvasAWT {
	public static final double DEFAULT_SIZE = 480;

	//
	public static final int DEFAULT_HEIGHT;
	public static final int DEFAULT_WIDTH;

	static {
		DEFAULT_HEIGHT = 480;
		DEFAULT_WIDTH = 480;
	}

	GLWindow window;
	public ZGlobeModel canvas;

	/*
	 * CONSTRUCTORS
	 */
	public zGlobeView(GLCapabilities capabilities) {
		this.window = GLWindow.create(capabilities);
		
		// event listener, adds GlobeView
		this.canvas = createGlobeModel();
		window.addGLEventListener(canvas);
		window.setSize(480, 480);

		// starts animator, causing globe to rotate
//		final FPSAnimator animator = new FPSAnimator(window, 300, true);
//		animator.start();

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


		System.out.println("This ran.");
	}

	/*
	 * PRIVATE METHODS
	 */
	private static ZGlobeModel createGlobeModel() {
		// default array is UWP for earth [8,6,7,A,6,9]
		int[] array = new int[] { 8, 6, 7, 10, 6, 9 };

		return createGlobeModel(array, 6);
	}

	private static ZGlobeModel createGlobeModel(int[] uwp, int divisions) {
		Planet p;
		ZGlobeModel view = null;

		try {
			p = Planet.build(divisions);
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
//			Quaternion ray_eye = new PMVMatrix();
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
