package view;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import controller.PlanetViewController;
import controller.ZGlobeViewController;
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
	private int view_height;
	private int view_width;

	private Planet planet;
	private Grid grid;

	/*
	 * CONSTRUCTORS
	 */
	public GlobeView(Planet planet) {
		this.planet = planet;
		this.grid = planet.getGrid();

		//
		this.rquad = 0.0f;
		this.scale = 1;
		this.view_height = 640;
		this.view_width = 640;
	}

	/*
	 * PRIVATE METHODS
	 */
	private void setMatrix(GL2 gl) {
		// added Matrix Mode
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		double x = view_width / PlanetViewController.FRAME_WIDTH / scale;
		double y = view_height / PlanetViewController.FRAME_HEIGHT / scale;
		gl.glOrtho(-x, x, -y, y, -2.0, 0.0);
	}

	private void rotateGlobe(GL2 gl) {
		// gl.glRotatef(-90.0f, 0.95f, 0.1f, -0.1f); // rotate up
		// gl.glRotatef(-90, 0.1f, 0, 0); // rotate up-down

		gl.glRotatef(-90, 1, 0, 0); // rotate up-down
		// gl.glRotatef(-15, 0, 1, 0); // rotate left-right
		gl.glRotatef(rquad, 0, 0, 1.0f); // spin

		rquad -= 0.3f;
		System.out.println("Done");
	}

	/*
	 * DISPLAY METHODS
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		// gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		setMatrix(gl);
//		rotateGlobe(gl);

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

		selectedTile.accept(tiles[PlanetViewController.getSelectedTile()]);

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
