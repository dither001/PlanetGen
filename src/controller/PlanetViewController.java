package controller;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.util.FPSAnimator;
import com.planet.atmosphere.Season;

import api.ViewType;
import graphics.PlanetColor;
import menu.ViewsMenu;
import model.Planet;
import model.Tile;
import view.ArrowView;
import view.GlobeView;

@SuppressWarnings("serial")
public class PlanetViewController extends GLCanvas implements GLEventListener, KeyListener, MouseListener {

	/*
	 * TESTING
	 */
	public static boolean TEST_ROTATE_WORLD = true;
	public static boolean TEST_SPIN_WORLD = true;
	public static boolean TEST_TILT_WORLD = false;

	public static boolean TEST_INVERT_X = false;
	// invert Y gets the ray on the correct "hemisphere" when -NOT- rotated
	public static boolean TEST_INVERT_Y = false;
	// invert Z gets the ray on the correct "hemisphere" when rotated
	public static boolean TEST_INVERT_Z = true;

	/*
	 * STATIC FIELDS
	 */
	public static final int FRAME_HEIGHT;
	public static final int FRAME_WIDTH;
	private static JFrame frame;

	//
	private static JMenuBar menuBar;
	private static final int MENUBAR_OFFSET;

	//
	private static NewtCanvasAWT newt;
	private static PlanetViewController canvas;
	private static GLWindow window;
	private static FPSAnimator animator;

	// PlanetController fields
	private static final int GRID_SIZE;
	private static Planet planet;

	// Ray-Picking and Tile Selection
	public static final float MOUSE_THRESHOLD;
	private static ViewType viewType;
	private static boolean picking;
	private static int mouseX, mouseY;
	private static int selectedTile;

	public static int selection_index;
	public static ArrayList<Integer> selection;

	private static Ray ray;

	// PlanetView fields
	private static GlobeView globe;
	private static int view_height;
	private static int view_width;
	
	//
	private static ArrayList<ArrowView> wind_vectors;

	/*
	 * INITIALIZATION
	 */
	static {
		//
		FRAME_HEIGHT = 480;
		FRAME_WIDTH = 480;
		MENUBAR_OFFSET = 23;

		//
		GRID_SIZE = 4;
		MOUSE_THRESHOLD = 1;

		planet = null;

		//
		view_height = 480;
		view_width = 480;

		//
		viewType = ViewType.ELEVATION;
		selection = new ArrayList<Integer>();
		//
		wind_vectors = new ArrayList<ArrowView>();

	}

	/*
	 * CONSTRUCTORS
	 */
	private PlanetViewController() {
		try {
			planet = Planet.build(GRID_SIZE);
			PlanetColor.setupColors(planet);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * MAIN METHOD
	 */
	public static void main(String[] args) {
		canvas = new PlanetViewController();

		// setup GLWindow
		GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities caps = new GLCapabilities(profile);
		window = GLWindow.create(caps);
		window.addGLEventListener(canvas);

		globe = new GlobeView(planet);
		window.addGLEventListener(globe);

		window.addKeyListener(canvas);
		window.addMouseListener(canvas);
		window.setSize(view_width, view_height);

		// setup animator
		animator = new FPSAnimator(window, 300, true);
		animator.start();

		// setup NEWT
		newt = new NewtCanvasAWT(window);

		// setup frame
		frame = new JFrame("Random Planet");
		frame.getContentPane().add(newt);

		// setup menu bar
		menuBar = new JMenuBar();
		menuBar.add(new ViewsMenu());
		frame.setJMenuBar(menuBar);

		// final steps
		frame.setSize(setupFrame());
		// System.out.println(frame.getSize());
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

	public static int getSelectedTile() {
		return selectedTile;
	}

	public static void setSelectedTile(int selectedTile) {
		int max = planet.tileSize();
		PlanetViewController.selectedTile = selectedTile < 0 ? max - 1 : selectedTile >= max ? 0 : selectedTile;
	}

	private static Dimension setupFrame() {
		Dimension d = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);

		// XXX - height of menu bar
		d.height += MENUBAR_OFFSET;

		return d;
	}
	
	/*
	 * HELPER METHODS
	 * 
	 */
	private void setupPlanetVectors() {
		Season s = planet.getClimate().getSeason(0);
		
		
		
	}

	/*
	 * DISPLAY METHOD
	 * 
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		Tile[] grid = planet.getGrid().tiles;

		//
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		/*
		 * RAY PICKING
		 */
		Supplier<Integer> pickRay = () -> {
			/*
			 * JOGL/OpenGL ray-picking
			 */
			int[] viewport = new int[4];
			gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

			float[] p_matrix = new float[16];
			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, p_matrix, 0);

			float[] mv_matrix = new float[16];
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mv_matrix, 0);

			ray = new Ray();
			FloatUtil.mapWinToRay(mouseX, mouseY, 0, 1, mv_matrix, 0, p_matrix, 0, viewport, 0, ray, new float[16],
					new float[16], new float[4]);

			// System.out.printf("%-14s (%.2f, %.2f, %.2f)%n", "Ray origin:", ray.orig[0],
			// ray.orig[1], ray.orig[2]);
			// System.out.printf("%-14s (%.2f, %.2f, %.2f)%n", "Ray direction:",
			// ray.orig[0], ray.orig[1], ray.orig[2]);

			int counter = 0;
			selection.clear();
			for (Tile el : grid) {
				if (el.intersects(ray.orig, ray.dir, planet.getGrid())) {
					// if (el.intersects(ray_ndc, ray_dir, planet.getGrid())) {
					selection.add(el.id);
					++counter;
				}
			}
			// System.out.println(counter);

			if (counter > 0) {
				PlanetViewController.selection_index = 0;
				return selection.get(PlanetViewController.selection_index);
			}

			return -1;
		};

		if (picking) {
			PlanetViewController.picking = false;

			/*
			 * GL_SCALE inverts the y-axis for picking, then resets it before doing any
			 * other work.
			 */
			if (TEST_INVERT_X)
				gl.glScalef(-1.0f, 1.0f, 1.0f);
			if (TEST_INVERT_Y)
				gl.glScalef(1.0f, -1.0f, 1.0f);
			if (TEST_INVERT_Z)
				gl.glScalef(1.0f, 1.0f, -1.0f);

			int pickTile = pickRay.get();

			if (TEST_INVERT_X)
				gl.glScalef(-1.0f, 1.0f, 1.0f);
			if (TEST_INVERT_Y)
				gl.glScalef(1.0f, -1.0f, 1.0f);
			if (TEST_INVERT_Z)
				gl.glScalef(1.0f, 1.0f, -1.0f);

			if (pickTile != -1 && pickTile != getSelectedTile()) {
				setSelectedTile(pickTile);
				System.out.println("Selected tile " + pickTile);
			}
		}

	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		// GL2 gl = drawable.getGL().getGL2();

		// gl.glViewport(0, 0, w, h);
		// gl.glMatrixMode(GL2.GL_PROJECTION);
		// gl.glLoadIdentity();
		// glu.gluPerspective(45.0, (float) w / (float) h, 1.0, 100.0);
		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glLoadIdentity();

	}

	/*
	 * MOUSE LISTENER
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			mouseX = e.getX();
			mouseY = e.getY();
			PlanetViewController.picking = true;
			canvas.display();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * KEY EVENT METHODS
	 * 
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP && selection.size() > 0) {
			selection_index = selection_index > 0 ? selection_index - 1 : selection.size() - 1;
			PlanetViewController.setSelectedTile(selection.get(selection_index));
			//
			// PlanetViewController.setSelectedTile(PlanetViewController.getSelectedTile() -
			// 1);
		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN && selection.size() > 0) {
			selection_index = selection_index + 1 < selection.size() ? selection_index + 1 : 0;
			PlanetViewController.setSelectedTile(selection.get(selection_index));
			//
			// PlanetViewController.setSelectedTile(PlanetViewController.getSelectedTile() +
			// 1);
		}

		int id = PlanetViewController.getSelectedTile();
		Tile t = planet.getGrid().getTile(id);
		System.out.printf("Tile id: %d (%f, %f, %f) %n", id, t.v[0], t.v[1], t.v[2]);

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
