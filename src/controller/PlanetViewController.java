package controller;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;
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

@SuppressWarnings("serial")
public class PlanetViewController extends GLCanvas implements GLEventListener, KeyListener, MouseListener {

	public static int A_COUNT;
	public static int B_COUNT;
	public static int C_COUNT;
	public static int D_COUNT;

	/*
	 * STATIC METHODS
	 */
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
		FRAME_HEIGHT = 480;
		FRAME_WIDTH = 480;

		//
		GRID_SIZE = 4;
		MOUSE_THRESHOLD = 1.5f;

		planet = null;
		defaultRotation = null;
		rquad = 0.0f;

		//
		scale = 1;
		view_height = 640;
		view_width = 640;

		//
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
		// gl.glMatrixMode(GL2.GL_PROJECTION);
		// gl.glLoadIdentity();
		//
		// glu.gluPerspective(45.0, aspect_ratio, 1.0, 100.0);
		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glLoadIdentity();
	}

	private void drawGlobe(GL2 gl, Grid grid) {
		/*
		 * IN-LINE DRAW TILE
		 */
		BiConsumer<Tile, float[]> drawTile = (t, color) -> {
			gl.glBegin(GL2.GL_TRIANGLE_FAN);

			JO.glColor3f(gl, color);

			JO.glVertex3f(gl, t.v);
			for (Corner el : t.corners)
				JO.glVertex3f(gl, el.v);

			JO.glVertex3f(gl, t.corners[0].v);

			gl.glEnd();
		};

		// CHOOSE VIEW COLORS
		float[][] colors = null;
		switch (ZGlobeViewController.getViewType()) {
		case ARIDITY:
			colors = PlanetColor.aridColors;
			break;
		case ELEVATION:
			colors = PlanetColor.topoColors;
			break;
		case HUMIDITY:
			colors = PlanetColor.humidColors;
			break;
		case LATITUDE:
			colors = PlanetColor.latColors;
			break;
		case PRECIPITATION:
			colors = PlanetColor.rainColors;
			break;
		case REGION:
			colors = PlanetColor.regionColors;
			break;
		case TEMPERATURE:
			colors = PlanetColor.tempColors;
			break;
		case VEGETATION:
			colors = PlanetColor.vegeColors;
			break;
		default:
			break;
		}

		//
		Tile[] tiles = grid.tiles;
		int length = tiles.length;
		for (int i = 0; i < length; ++i) {
			drawTile.accept(tiles[i], colors[i]);
		}

		Edge[] edges = grid.edges;
		length = edges.length;
		JO.glColor3f(gl, Color.JET_BLACK);
		gl.glLineWidth(1.0f);
		for (int i = 0; i < length; ++i) {
			if (true != planet.edgeIsWater(i)) {
				gl.glBegin(GL.GL_LINES);
				JO.glVertex3f(gl, edges[i].corners[0].v);
				JO.glVertex3f(gl, edges[i].corners[1].v);
				gl.glEnd();
			}
		}

	}

	/*
	 * GLEVENT LISTENER
	 * 
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		Tile[] grid = planet.getGrid().tiles;

		//
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		setMatrix(gl);

		gl.glRotatef(-90.0f, 0.95f, 0.1f, -0.1f); // rotate up
		gl.glRotatef(rquad, 0, 0, 1.0f); // spin

		/*
		 * RAY PICKING
		 */
		Consumer<Boolean> pickRay = (flag) -> {
			if (flag) {
				PlanetViewController.picking = false;

				// step 1
				int[] viewport = new int[4];
				gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

				// float x = (2.0f * mouseX) / view_width - 1.0f;
				// float y = 1.0f - (2.0f * mouseY) / view_height;
				float x = (2.0f * mouseX) / viewport[2] - 1.0f;
				float y = 1.0f - (2.0f * mouseY) / viewport[3];
				float z = 1.0f;

				float[] ray_nds = new float[] { x, y, z };

				// step 2
				float[] ray_clip = new float[] { x, y, -1.0f, 1.0f };

				// step 3
				float[] p_matrix = new float[16];
				gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, p_matrix, 0);

				float[] inverse = FloatUtil.invertMatrix(p_matrix, new float[16]);
				float[] ray_eye = FloatUtil.multMatrixVec(inverse, ray_clip, new float[4]);
				ray_eye = new float[] { ray_eye[0], ray_eye[1], -1.0f, 0.0f };

				// step 4
				float[] mv_matrix = new float[16];
				gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mv_matrix, 0);
				inverse = FloatUtil.invertMatrix(mv_matrix, inverse);

				float[] ray_wor = FloatUtil.multMatrixVec(inverse, ray_eye, new float[4]);
				ray_wor = new float[] { ray_wor[0], ray_wor[1], ray_wor[2] };
				VectorUtil.normalizeVec3(ray_wor);

				// System.out.printf("ray_wor: (%f, %f, %f)%n", ray_wor[0], ray_wor[1],
				// ray_wor[2]);

				/*
				 * 
				 */

				// System.out.printf("[%d, %d, %d, %d]%n", viewport[0], viewport[1],
				// viewport[2], viewport[3]);

				// float realy = 0;
				// float[] wcoord = new float[4];

				// realy = viewport[3] - mouseY - 1;

				// glu.gluUnProject(mouseX, mouseY, 0, mv_matrix, 0, p_matrix, 0, viewport, 0,
				// wcoord, 0);
				// System.out.printf("wcoord: (%.2f, %.2f, %.2f) %n", wcoord[0], wcoord[1],
				// wcoord[2]);

				// float[] mousePoint = FloatUtil.multMatrixVec(new float[4], mv_matrix,
				// new float[] { mouseX, mouseY, 1, 1 });
				// mousePoint = FloatUtil.multMatrixVec(mousePoint, p_matrix, mousePoint);

				//
				ray = new Ray();
				//
				// System.out.printf("(%f, %f)%n", x, y);
				// FloatUtil.mapWinToRay(mousePoint[0], mousePoint[1], 0, 1, mv_matrix, 0,
				// p_matrix, 0, viewport, 0, ray, new float[16],
				// new float[16], new float[4]);

				// XXX - The last version to "work"
				FloatUtil.mapWinToRay(mouseX, mouseY, 0, 1, mv_matrix, 0, p_matrix, 0, viewport, 0, ray, new float[16],
						new float[16], new float[4]);

				PlanetViewController.A_COUNT = 0;
				PlanetViewController.B_COUNT = 0;
				PlanetViewController.C_COUNT = 0;
				PlanetViewController.D_COUNT = 0;

				selection.clear();
				for (Tile el : grid) {
					if (el.intersects(ray, planet.getGrid())) {
						selection.add(el.id);
					}
				}

				// System.out.println(selection.size());

				// for (Object el : selection.toArray())
				// System.out.print(el.toString() + ", ");

				System.out.println();
				// System.out.printf("A: %d | B: %d | C: %d | D: %d %n", A_COUNT, B_COUNT,
				// C_COUNT, D_COUNT);

			}
		};

		/*
		 * DRAW GLOBE
		 */
		drawGlobe(gl, planet.getGrid());

		/*
		 * DRAW SELECTED TILE
		 */
		Consumer<Tile> selectedTile = (t) -> {
			JO.glColor3f(gl, Color.JET_BLACK);
			gl.glLineWidth(1.5f);
			for (Edge el : t.edges) {
				gl.glBegin(GL2.GL_LINE_LOOP);
				JO.glVertex3f(gl, el.corners[0].v);
				JO.glVertex3f(gl, el.corners[1].v);
				gl.glEnd();
			}
		};

		//
		pickRay.accept(picking);
		// int selected = getSelectedTile();
		// if (-1 != selected && selected < planet.tileSize())
		// selectedTile.accept(grid[selected]);

		if (selection.size() > 0) {
			// selectedTile.accept(grid[selection.get(0)]);
			for (Integer el : selection.toArray(new Integer[selection.size()]))
				selectedTile.accept(grid[el]);
		}

		// if (null != ray) {
		// gl.glColor3f(0.3f, 0.5f, 1f);
		// GLUquadric dot = glu.gluNewQuadric();
		// glu.gluQuadricDrawStyle(dot, GLU.GLU_FILL);
		// glu.gluQuadricNormals(dot, GLU.GLU_FLAT);
		// glu.gluQuadricOrientation(dot, GLU.GLU_OUTSIDE);
		//
		// glu.gluSphere(dot, 0.1f, 16, 16);
		// glu.gluDeleteQuadric(dot);
		// }

		gl.glFlush();

		rquad -= 0.3f;
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
