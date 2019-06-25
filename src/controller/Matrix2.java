package controller;

public class Matrix2 {

	public double[][] m;

	/*
	 * CONSTRUCTORS
	 */
	public Matrix2() {
		m = new double[][] { //
				{ 1.0 , 0.0 }, //
				{ 0.0 , 1.0 } //
		};
		
	}

	public float[] mulByVec2(float[] v) {
		float x = (float) (v[0] * m[0][0] + v[1] * m[0][1]);
		float y = (float) (v[0] * m[1][0] + v[1] * m[1][1]);

		return new float[] { x, y };
	}

	/*
	 * METHODS
	 */
	public static Matrix2 rotationMatrix(double d) {
		Matrix2 m = new Matrix2();

		m.m[0][0] = Math.cos(d);
		m.m[0][1] = -(Math.sin(d));
		m.m[1][0] = Math.sin(d);
		m.m[1][1] = Math.cos(d);

		return m;
	}

}
