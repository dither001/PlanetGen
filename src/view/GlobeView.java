package view;

import java.util.function.BiConsumer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import controller.PlanetViewController;
import graphics.Color;
import graphics.JO;
import graphics.PlanetColor;
import model.Corner;
import model.Edge;
import model.Grid;
import model.Planet;
import model.Tile;

public class GlobeView implements GLEventListener {

	private float rquad;
	private int scale;
	public int view_height;
	public int view_width;

	// private Planet planet;
	private Grid grid;

	/*
	 * CONSTRUCTORS
	 */
	public GlobeView(Planet planet) {
		// this.planet = planet;
		this.grid = planet.getGrid();

		//
		this.rquad = 0.0f;
		this.scale = 1;
		this.view_height = 640;
		this.view_width = 640;
	}

	public float getRotation() {
		return rquad;
	}

	/*
	 * PRIVATE METHODS
	 */
	private void setMatrix(GL2 gl) {
		// added Matrix Mode
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		// Desired values are view > frame
		double x = view_width / PlanetViewController.FRAME_WIDTH / scale;
		double y = view_height / PlanetViewController.FRAME_HEIGHT / scale;

		/*
		 * XXX - The most important facet of glOrtho in its use here, appears to be
		 * setting up the clipping plane, so the "front" of the sphere is displayed, and
		 * not the "back." (The results of seeing both at once are dizzying.)
		 */
		gl.glOrtho(-x, x, -y, y, 1.0, 0);
	}

	private void rotateGlobe(GL2 gl) {
		if (PlanetViewController.TEST_ROTATE_WORLD)
			gl.glRotatef(90, 1, 0, 0); // rotate up-down

		if (PlanetViewController.TEST_TILT_WORLD)
			gl.glRotatef(-15, 0, 1, 0); // tilt (left-right)

		if (PlanetViewController.TEST_SPIN_WORLD) {
			gl.glRotatef(rquad, 0, 0, 1); // spin

			rquad -= 0.3f;
			rquad %= 360;
			// System.out.println(rquad);
		}
	}

	/*
	 * DISPLAY METHODS
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();

		setMatrix(gl);
		rotateGlobe(gl);

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
		switch (PlanetViewController.getViewType()) {
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
			// if (planet.edgeIsCoast(i) || planet.edgeIsLand(i)) {
			gl.glBegin(GL.GL_LINES);
			JO.glVertex3f(gl, edges[i].corners[0].v);
			JO.glVertex3f(gl, edges[i].corners[1].v);
			gl.glEnd();
			// }
		}

		/*
		 * DRAW SELECTED TILE
		 */
		BiConsumer<Tile, Float> selectedTile = (t, line_weight) -> {
			JO.glColor3f(gl, Color.JET_BLACK);
			gl.glLineWidth(line_weight);
			for (Edge el : t.edges) {
				gl.glBegin(GL2.GL_LINE_LOOP);
				JO.glVertex3f(gl, el.corners[0].v);
				JO.glVertex3f(gl, el.corners[1].v);
				gl.glEnd();
			}
		};

		/*
		 * 
		 */
		for (Integer el : PlanetViewController.selection)
			selectedTile.accept(tiles[el], 1.5f);

		selectedTile.accept(tiles[PlanetViewController.getSelectedTile()], 3.0f);

		gl.glFlush();
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
	public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}

}
