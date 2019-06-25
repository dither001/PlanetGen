package view;

public class PlanetView {

	public static final double DEFAULT_SIZE = 600;

	/*
	 * 
	 */
	double scale;
	int width, height;

	/*
	 * CONSTRUCTORS
	 */
	public PlanetView() {
		this.scale = 1;
		//
		this.width = 600;
		this.height = 600;
	}

	/*
	 * 
	 */
	public void setViewportSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setScale(float[] vec2, double delta) {
		this.scale *= delta;
	}

	public void mouseDragged(float[] vec2) {
		// TODO
	}

	public float[] toCoordinates(float[] vec2) {
		return new float[] { 0, 0, 0 };
	}
}
