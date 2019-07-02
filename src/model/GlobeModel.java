package model;

import java.awt.DisplayMode;
import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.awt.TextRenderer;

import controller.Matrix3;
import controller.PlanetUtil;
import controller.GlobeViewController;
import graphics.Color;
import graphics.JO;
import graphics.PlanetColor;
import view.GlobeView;

@SuppressWarnings("serial")
public class GlobeModel extends GLCanvas implements GLEventListener {

	int counter = 0;

	private Planet planet;
	private Quaternion q;

	//
	double scale;
	int width, height;

	//
	private double latitude;
	private double longitude;
	private boolean showRivers;

	/*
	 * -OLD- FIELDS
	 */
	public static DisplayMode dm, dm_old;
	private GLU glu = new GLU();
	private float rquad = 0.0f;

	/*
	 * CONSTRUCTORS
	 */
	public GlobeModel(Planet planet, Quaternion q) {
		this.planet = planet;
		this.q = q;

		resetRotation();
		//
		this.showRivers = false;

		//
		this.scale = 1;
		//
		this.width = 600;
		this.height = 600;
	}

	/*
	 * PRIVATE METHODS
	 */
	private void setMatrix(GL2 gl) {
		// added Matrix Mode
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		double x = width / GlobeView.DEFAULT_SIZE / scale;
		double y = height / GlobeView.DEFAULT_SIZE / scale;
		gl.glOrtho(-x, x, -y, y, -2.0, 0.0);
	}

	public void setRotation(Quaternion q) {
		this.q = q;
	}

	private void resetRotation() {
		this.latitude = 0;
		this.longitude = 0;
	}

	/*
	 * XXX - So, I have to say something about this. When I first booted up the
	 * globe (and it actually loaded the sphere), the object "flickered." Removing
	 * "rotationAxis()" from the equation seemed to fix it.
	 */
	private Quaternion rotation() {
		// return latitudeRotation().mult(longitudeRotation()).mult(axisRotation());
		// return latitudeRotation().mult(longitudeRotation());
		// return axisRotation();
		// return latitudeRotation();
		return longitudeRotation();
	}

	/*
	 * FIXME - Trying to debug the axisRotation() function
	 */
	private Quaternion axisRotation() {
		// Quaternion q = PlanetUtil.fromTwoVec3(planet.defaultAxis(), new float[] { 0,
		// 1, 0 });
		// return q.mult(PlanetUtil.fromTwoVec3(new float[] { 1, 0, 0 }, new float[] {
		// 0, 0, 1 }));
		return PlanetUtil.fromTwoVec3(new float[] { 1, 0, 0 }, new float[] { 0, 0, 1 });
	}

	private Quaternion latitudeRotation() {
		return PlanetUtil.fromAngle((float) -latitude, new float[] { 1, 0, 0 });
	}

	private Quaternion longitudeRotation() {
		return PlanetUtil.fromAngle((float) -longitude, new float[] { 0, 1, 0 });
	}

	/*
	 * GL METHODS
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		setMatrix(gl);

		gl.glRotatef(-90.0f, 0.95f, 0.1f, -0.1f); // rotate up
		gl.glRotatef(rquad, 0, 0, 1.0f); // spin
		Matrix3 m = new Matrix3(q.mult(rotation()));

		/*
		 * XXX - I wrote this anonymous ("lambda?") expression as a way to avoid passing
		 * a bunch of parameters from one method to another.
		 */
		BiConsumer<Tile, float[]> drawTile = (t, color) -> {
			gl.glBegin(GL2.GL_TRIANGLE_FAN);

			JO.glColor3f(gl, color);
			JO.glVertex3f(gl, m.multVec3(t.v));
			for (Corner el : t.corners)
				JO.glVertex3f(gl, m.multVec3(el.v));

			JO.glVertex3f(gl, m.multVec3(t.corners[0].v));

			gl.glEnd();
		};

		/*
		 * FIXME - "draw_river ()" (globe_renderer.cpp)
		 */
		BiConsumer<Tile, float[]> drawRiver = (t, color) -> {
			gl.glBegin(GL2.GL_TRIANGLE_FAN);

			JO.glColor3f(gl, color);
			// glVertex3f(m*(vector(nth_corner(t, edge)) + (vector(nth_corner(t, edge-1)) -
			// vector(nth_corner(t, edge)))*0.1));

			// glVertex3f(m*vector(nth_corner(t, edge)));
			// glVertex3f(m*vector(nth_corner(t, edge+1)));
			gl.glEnd();

			gl.glBegin(GL2.GL_TRIANGLES);
			// glVertex3f(m*vector(nth_corner(t, edge+1)));
			// glVertex3f(m*(vector(nth_corner(t, edge+1)) + (vector(nth_corner(t, edge+2))
			// - vector(nth_corner(t, edge+1)))*0.1));

			// glVertex3f(m*(vector(nth_corner(t, edge)) + (vector(nth_corner(t, edge-1)) -
			// vector(nth_corner(t, edge)))*0.1));
			gl.glEnd();
		};

		Consumer<Tile> selectedTile = (t) -> {
			JO.glColor3f(gl, Color.JET_BLACK);
			gl.glLineWidth(3.0f);
			for (Edge el : t.edges) {
				gl.glBegin(GL2.GL_LINE_LOOP);
				JO.glVertex3f(gl, el.corners[0].v);
				JO.glVertex3f(gl, el.corners[1].v);
				gl.glEnd();
			}
		};

		BiConsumer<Integer, Integer> castRay = (mouseX, mouseY) -> {
			// step 1
			float x = (2.0f * mouseX) / width - 1.0f;
			float y = 1.0f - (2.0f * mouseY) / height;
			float z = 1.0f;

			float[] ray_nds = new float[] { x, y, z };

			// step 2?
			Quaternion ray_clip = new Quaternion(x, y, -1.0f, 1.0f);

			// vec4 ray_eye = inverse(projection_matrix) * ray_clip;
			FloatBuffer proj = null;
			// gl.glGetMatrixf(GLMatrixFunc.GL_PROJECTION, proj);
			Quaternion ray_eye = ray_clip;

		};

		// CHOOSE VIEW COLORS
		float[][] colors = null;
		switch (GlobeViewController.getViewType()) {
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
		Tile[] gTiles = planet.getGrid().tiles;
		int length = gTiles.length;
		for (int i = 0; i < length; ++i) {
			drawTile.accept(gTiles[i], colors[i]);
		}
		gl.glFlush();

		int selected = GlobeViewController.getSelectedTile();
		if (selected != -1 && selected < planet.tileSize())
			selectedTile.accept(gTiles[selected]);

		gl.glFlush();

		// System.out.println("This ran.");
		rquad -= 0.3f;


		/*
		 * 
		 */
		int x = 50, y = 50;

		// timer label
		TextRenderer tr = new TextRenderer(new Font("Verdana", Font.BOLD, 12));
		tr.beginRendering(GlobeView.DEFAULT_WIDTH, GlobeView.DEFAULT_HEIGHT);
		tr.setColor(java.awt.Color.YELLOW);
		tr.setSmoothing(true);

		tr.draw("Empty", x, y);
		tr.endRendering();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
	}

	/*
	 * XXX - I tried commenting out each of these methods, line by line, and none of
	 * them appeared to do anything.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();

		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();

		if (height <= 0)
			height = 1;
		final float ratio = 1.0f * width / height;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0, ratio, 1.0, 20.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	/*
	 * 
	 */
	public void rayCasting(int mouseX, int mouseY) {

		// step 1
		float x = (2.0f * mouseX) / width - 1.0f;
		float y = 1.0f - (2.0f * mouseY) / height;
		float z = 1.0f;

		float[] ray_nds = new float[] { x, y, z };

		// step 2?
		Quaternion ray_clip = new Quaternion(x, y, -1.0f, 1.0f);

		// vec4 ray_eye = inverse(projection_matrix) * ray_clip;
		float[] proj = new float[16];
		Quaternion ray_eye = ray_clip;

	}

	private static void drawTimer() {
		int x = 50, y = 50;

		// timer label
		TextRenderer tr = new TextRenderer(new Font("Verdana", Font.BOLD, 12));
		tr.beginRendering(GlobeView.DEFAULT_WIDTH, GlobeView.DEFAULT_HEIGHT);
		tr.setColor(java.awt.Color.YELLOW);
		tr.setSmoothing(true);

		tr.draw("Empty", x, y);
		tr.endRendering();

	}

	// public void setScale(float[] vec2, double delta) {
	// double min_scale = 0.6 * Math.min(width, height) / GlobeView.DEFAULT_SIZE;
	// double max_scale = 20;
	// double new_scale = scale * delta;
	//
	// new_scale = new_scale < scale ? scale < min_scale ? scale :
	// Math.max(new_scale, min_scale) : new_scale;
	//
	// new_scale = Math.min(new_scale, max_scale);
	//
	// this.scale = new_scale;
	// }

	// public void mouseDragged(float[] vec2) {
	// longitude -= 0.0035 * vec2[0] / scale;
	//
	// while (longitude > Math.PI)
	// longitude -= 2 * Math.PI;
	// while (longitude <= -Math.PI)
	// longitude += 2 * Math.PI;
	//
	// latitude -= 0.0035 * vec2[1] / scale;
	//
	// latitude = Math.min(Math.PI / 2, latitude);
	// latitude = Math.max(-Math.PI / 2, latitude);
	// }

	// public float[] toCoordinates(float[] vec2) {
	// float[] v = new float[] { width, height };
	// v = VectorUtil.scaleVec2(v, v, 0.5f);
	// v = VectorUtil.subVec2(v, vec2, v);
	// v = VectorUtil.scaleVec2(v, v, (float) (2.0 / scale /
	// GlobeView.DEFAULT_SIZE));
	//
	// double length = VectorUtil.normSquareVec2(v);
	//
	// float[] vec3 = new float[] { 0, 0, 0 };
	// if (length > 1.0)
	// return vec3;
	//
	// vec3 = new float[] { v[0], v[1], (float) Math.sqrt(1.0 - length) };
	// return PlanetUtil.rotateVec3(vec3, rotation().conjugate());
	// }

}
