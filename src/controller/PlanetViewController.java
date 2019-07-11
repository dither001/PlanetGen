package controller;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.util.FPSAnimator;

import api.ViewType;
import graphics.Color;
import graphics.JO;
import graphics.PlanetColor;
import menu.ViewsMenu;
import model.Corner;
import model.Edge;
import model.Grid;
import model.Planet;
import model.Tile;
import view.GlobeView;

@SuppressWarnings("serial")
public class PlanetViewController extends GLCanvas implements GLEventListener, KeyListener, MouseListener {

	public static int A_COUNT;
	public static int B_COUNT;
	public static int C_COUNT;
	public static int D_COUNT;

	/*
	 * STATIC FIELDS
	 */
	private static ExecutorService executor;

	private static GLU glu;
	private static JFrame frame;
	private static JMenuBar menuBar;

	//
	private static NewtCanvasAWT newt;
	private static PlanetViewController canvas;
	private static GLWindow window;
	private static FPSAnimator animator;

	//
	public static final int FRAME_HEIGHT;
	public static final int FRAME_WIDTH;
	public static final float MOUSE_THRESHOLD;

	//
	private static final int GRID_SIZE;

	private static Planet planet;
	private static Quaternion defaultRotation;

	private static float rquad;
	private static int scale;
	private static int view_height;
	private static int view_width;

	//
	private static ViewType viewType;
	private static boolean picking;
	private static float mouseX, mouseY;
	private static int selectedTile;
	public static ArrayList<Integer> selection;

	private static Ray ray;

	static {
		executor = Executors.newCachedThreadPool();

		//
		FRAME_HEIGHT = 480;
		FRAME_WIDTH = 480;

		//
		GRID_SIZE = 4;
		MOUSE_THRESHOLD = 1.5f;

		planet = null;
		defaultRotation = null;

		//
		rquad = 0.0f;
		scale = 1;
		view_height = 640;
		view_width = 640;

		//
		viewType = ViewType.TEMPERATURE;
		selection = new ArrayList<Integer>();
	}

	/*
	 * CONSTRUCTORS
	 */
	private PlanetViewController() {
		try {
			planet = Planet.build(GRID_SIZE);
			defaultRotation = planet.rotationToDefault();

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
		
		GlobeView globe = new GlobeView(planet);
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

	/*
	 * HELPER METHODS
	 * 
	 */
	private static Dimension setupFrame() {
		Dimension d = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);

		// XXX - height of menu bar
		d.height += 23;

		return d;
	}

	private void setMatrix(GL2 gl) {
		// added Matrix Mode
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		double x = view_width / FRAME_WIDTH / scale;
		double y = view_height / FRAME_HEIGHT / scale;
		gl.glOrtho(-x, x, -y, y, -2.0, 0.0);

		// float aspect_ratio = view_width / view_height * scale;
		//
		// gl.glViewport(0, 0, view_width, view_height);
		// gl.glMatrixMode(GL2.GL_PROJECTION);
		// gl.glLoadIdentity();
		//
		// glu.gluPerspective(45.0, aspect_ratio, 1.0, 100.0);
		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glLoadIdentity();
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
			// step 1
			int[] viewport = new int[4];
			gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

			float[] p_matrix = new float[16];
			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, p_matrix, 0);

			float[] mv_matrix = new float[16];
			gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mv_matrix, 0);

			ray = new Ray();
			FloatUtil.mapWinToRay(mouseX, mouseY, 0.0f, 1.0f, mv_matrix, 0, p_matrix, 0, viewport, 0, ray,
					new float[16], new float[16], new float[4]);

			for (Tile el : grid) {
				if (el.intersects(ray, planet.getGrid()))
					return el.id;
			}

			return -1;
		};

		if (picking) {
			PlanetViewController.picking = false;

			/*
			 * GL_SCALE inverts the y-axis for picking, then resets it before doing any
			 * other work.
			 */
//			gl.glRotatef(90, 1, 0, 0); // rotate up-down
//			gl.glRotatef(15, 0, 1, 0); // rotate left-right
			gl.glScalef(1.0f, -1.0f, 1.0f);
			int pickTile = pickRay.get();
			gl.glScalef(1.0f, -1.0f, 1.0f);

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
		final GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		GL2 gl = drawable.getGL().getGL2();

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
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			PlanetViewController.setSelectedTile(PlanetViewController.getSelectedTile() - 1);

		}

		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			PlanetViewController.setSelectedTile(PlanetViewController.getSelectedTile() + 1);
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
