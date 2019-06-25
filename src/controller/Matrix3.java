package controller;

import com.jogamp.opengl.math.Quaternion;

public class Matrix3 {

	private static final int DIMENSION = 3;

	private float[][] m;

	/*
	 * CONSTRUCTORS
	 */
	public Matrix3(float[][] m) {
		this.set(m);
	}

	public Matrix3(Matrix3 m) {
		this.set(m);
	}

	public Matrix3(Quaternion q) {
		float x = q.getX(), y = q.getY(), z = q.getZ(), w = q.getW();

		m = new float[][] { //
				{ 1 - 2 * y * y - 2 * z * z, 2 * x * y - 2 * z * w, 2 * x * z + 2 * y * w }, //
				{ 2 * x * y + 2 * z * w, 1 - 2 * x * x - 2 * z * z, 2 * y * z - 2 * x * w }, //
				{ 2 * x * z - 2 * y * w, 2 * y * z + 2 * x * w, 1 - 2 * x * x - 2 * y * y } //
		};
	}

	/*
	 * METHODS
	 */
	public void set(float[][] m) {
		for (int i = 0; i < DIMENSION; ++i) {
			for (int j = 0; j < DIMENSION; ++j)
				this.m[i][j] = m[i][j];
		}
	}

	public void set(Matrix3 other) {
		for (int i = 0; i < DIMENSION; ++i) {
			for (int j = 0; j < DIMENSION; ++j)
				this.m[i][j] = other.m[i][j];
		}
	}

	public float[] multVec3(float[] v3) {
		return new float[] { v3[0] * m[0][0] + v3[1] * m[0][1] + v3[2] * m[0][2],
				v3[0] * m[1][0] + v3[1] * m[1][1] + v3[2] * m[1][2],
				v3[0] * m[2][0] + v3[1] * m[2][1] + v3[2] * m[2][2] };
	}

	/*
	 * STATIC METHODS
	 */
	public static Matrix3 identity() {
		float[][] m = new float[3][3];

		for (int i = 0; i < DIMENSION; ++i) {
			for (int j = 0; j < DIMENSION; ++j) {
				if (i == j)
					m[i][j] = 1;
				else
					m[i][j] = 0;
			}
		}

		return new Matrix3(m);
	}
}
