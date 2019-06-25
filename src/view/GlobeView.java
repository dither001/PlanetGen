package view;

import java.awt.DisplayMode;
import java.util.function.BiConsumer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.Quaternion;

import controller.Matrix3;
import controller.PlanetUtil;
import model.Corner;
import model.Planet;
import model.Tile;

public class GlobeView extends PlanetView implements GLEventListener {

	int counter = 0;

	private Planet planet;
	private Quaternion q;

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
	public GlobeView(Planet planet, Quaternion q) {
		this.planet = planet;
		this.q = q;

		resetRotation();
		//
		this.showRivers = false;
	}

	/*
	 * PRIVATE METHODS
	 */
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
	 * METHODS
	 */
	private void drawTileOld(Tile t, Matrix3 m, float[] color, GL2 gl) {
		gl.glBegin(GL2.GL_TRIANGLE_FAN);

		JO.glColor3f(gl, color);
		JO.glVertex3f(gl, m.multVec3(t.v));
		for (Corner el : t.corners)
			JO.glVertex3f(gl, m.multVec3(el.v));

		JO.glVertex3f(gl, m.multVec3(t.corners[0].v));

		// System.out.println("Drew tile");
		gl.glEnd();
	}

	private void setMatrix(GL2 gl) {
		// added Matrix Mode
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		double x = width / PlanetView.DEFAULT_SIZE / scale;
		double y = height / PlanetView.DEFAULT_SIZE / scale;
		gl.glOrtho(-x, x, -y, y, -2.0, 0.0);
	}

	/*
	 * "Draw()" method from vraid's "Globe_Renderer" object.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
//		System.out.println(GLContext.getCurrent().getGLVersion());
		
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		setMatrix(gl);

		// gl.glTranslatef(0f, 0f, -15.0f);
//		gl.glRotatef(rquad, 1.0f, 1.0f, 1.0f);
		gl.glRotatef(rquad, 0, 1.0f, 0);

		// gl.glFrontFace(GL.GL_CCW);
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

		Tile[] gTiles = planet.getGrid().tiles;
		int length = gTiles.length;

		for (int i = 0; i < length; ++i) {
			// TOPOGRAPHY
			drawTile.accept(gTiles[i], PlanetColor.topoColors[i]);
			// VEGETATION
//			 drawTile.accept(gTiles[i], PlanetColor.vegeColors[i]);
			// TEMPERATURE
//			 drawTile.accept(gTiles[i], PlanetColor.tempColors[i]);
			// ARIDITY
//			 drawTile.accept(gTiles[i], PlanetColor.aridColors[i]);
			// HUMIDITY
//			 drawTile.accept(gTiles[i], PlanetColor.humidColors[i]);
			// PRECIPITATION
//			 drawTile.accept(gTiles[i], PlanetColor.rainColors[i]);
		}

		gl.glFlush();

		// System.out.println("This ran.");
		rquad -= 0.3f;
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
		// final GL2 gl = drawable.getGL().getGL2();

		// gl.glShadeModel(GL2.GL_SMOOTH);
		// gl.glClearColor(0f, 0f, 0f, 0f);
		// gl.glClearDepth(1.0f);
		// gl.glEnable(GL2.GL_DEPTH_TEST);
		// gl.glDepthFunc(GL2.GL_LEQUAL);
		// gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();

		if (height <= 0)
			height = 1;
		final float h = (float) width / (float) height;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, h, 1.0, 20.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

}
