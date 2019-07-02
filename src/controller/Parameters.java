package controller;

import com.jogamp.opengl.math.VectorUtil;

public abstract class Parameters {

	// miscellaneous parameters
	public static final float EPSILON = 0.0001f;

	// terrain parameters
	public static float default_radius;

	public static int grid_size;
	public static float[] axis;
	public String seed;
	public static int iterations;
	public static double water_ratio;

	// climate parameters
	public static int seasons;
	public static float axial_tilt;
	public static float error_tolerance;

	static {
		// default_radius = 40000000;
		// default radius is that of earth
		default_radius = 6371000; // meters

		// terrain
		grid_size = 6;
		axis = new float[] { 0, 0, 1 };
		iterations = 1000;
		water_ratio = 0.65;

		// climate
		seasons = 6;
		axial_tilt = 0.4f;
		error_tolerance = 0.01f;
	}

	/*
	 * 
	 */
	public void setDefault() {
		// terrain
		grid_size = 6;
		axis = new float[] { 0, 0, 1 };
		iterations = 1000;
		water_ratio = 0.65;

		// climate
		seasons = 6;
		axial_tilt = 0.4f;
		error_tolerance = 0.01f;
	}

	public void correctValues() {
		// Ensure grid_size is between 0-8
		grid_size = Math.max(0, grid_size);
		grid_size = Math.min(grid_size, 8);

		if (VectorUtil.isVec3Zero(axis, 0)) {
			axis = new float[] { 0, 0, 1 };
		} else {
			axis = VectorUtil.normalizeVec3(axis);
		}

		iterations = Math.max(0, grid_size);

		water_ratio = Math.max(0.0, water_ratio);
		water_ratio = Math.min(1.0, water_ratio);

		//
		seasons = Math.max(1, seasons);

		axial_tilt = (float) Math.max(0.0, axial_tilt);
		axial_tilt = (float) Math.min(Math.PI / 2, axial_tilt);

		error_tolerance = Math.max(0.001f, error_tolerance);
		error_tolerance = Math.min(1.0f, error_tolerance);
	}
}
