package controller;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;

import model.Tile;

public abstract class PlanetUtil {

	/*
	 * M - MATRIX
	 */
	public static float[][] multMat2ByVec2Array(double[][] m, float[][] v) {
		float[][] a = new float[v.length][];

		for (int i = 0; i < v.length; ++i) {
			a[i] = multMat2ByVec2(m, v[i]);

		}

		return a;
	}

	public static float[] multMat2ByVec2(double[][] m, float[] v) {
		float[] a = new float[] { //
				(float) (v[0] * m[0][0] + v[1] * m[0][1]), //
				(float) (v[0] * m[1][0] + v[1] * m[1][1]) //
		};

		return a;
	}

	public static double[][] rotationMatrix(double a) {
		double[][] m = new double[2][2];

		m[0][0] = Math.cos(a);
		m[0][1] = -(Math.sin(a));
		m[1][0] = Math.sin(a);
		m[1][1] = Math.cos(a);

		return m;
	}

	/*
	 * P - POLYGON
	 */
	public static float[][] polygon(Quaternion q, Tile t) {
		float[][] a = new float[t.edges.length][];
		Quaternion r = referenceRotation(t.v, q);

		for (int i = 0; i < a.length; ++i) {
			float[] c = rotateVec3(t.corners[i].v, r);
			a[i] = new float[] { c[0], c[1] };
		}

		return a;
	}

	/*
	 * Q - QUATERNION
	 */
	public static Quaternion fromAngle(float angle, float[] v) {
		return new Quaternion().setFromAngleNormalAxis(angle, v);
	}

	public static Quaternion fromTwoVec3(float[] a, float[] b) {
		/*
		 * XXX - This method is supposed to create a Quaternion representing the
		 * rotation between two vector-3s (i.e. a pair of float arrays of size/length
		 * 3). I have no way of verifying or guaranteeing that it does what it claims,
		 * as I barely understand the math behind the operations.
		 */
		float[] u = new float[] { 0, 0, 0 }, v = new float[] { 0, 0, 0 };
		/*
		 * The two float arrays here are apparently used for operations performed within
		 * the function "setFromVectors()". My assumption is this is because such
		 * operations are performed en masse, and the conservative approach is to reuse
		 * the space. Why the method isn't overloaded is beyond my understanding.
		 */

		return new Quaternion().setFromVectors(a, b, u, v);
	}

	public static Quaternion referenceRotation(float[] t, Quaternion d) {
		/*
		 * XXX - I hate this method so much. I barely understand the math behind vectors
		 * and quaternions. I'm pretty sure vraid uses multiplication of quaternions BY
		 * vectors to rotate said vectors. I've included fragments of the original code
		 * for posterity (and bug-fixing down the line).
		 */

		// Vector3 v = d * vector(t);
		float[] v = rotateVec3(new float[] { t[0], t[1], t[2] }, d);

		// I added these booleans to improve readability.
		boolean isXZero = FloatUtil.isZero(v[0], Parameters.EPSILON);
		boolean isYZero = FloatUtil.isZero(v[1], Parameters.EPSILON);

		Quaternion h = new Quaternion();
		if (!(isXZero) || !(isYZero)) {

			if (!(isYZero)) {
				float[] tmp = VectorUtil.normalizeVec3(new float[] { v[0], v[1], 0 });
				h = fromTwoVec3(tmp, new float[] { -1, 0, 0 });

			} else if (v[0] > 0) {
				h = fromAngle((float) Math.PI, new float[] { 0, 0, 1 });

			}
		}

		Quaternion q = new Quaternion();
		if (isXZero && isYZero) {
			if (v[2] < 0) {
				// q = Quaternion(Vector3(1, 0, 0), pi);
				q = fromAngle((float) Math.PI, new float[] { 1, 0, 0 });
			}

		} else {
			// q = Quaternion(h * v, Vector3(0, 0, 1));
			q = PlanetUtil.fromTwoVec3(rotateVec3(v, h), new float[] { 0, 0, 1 });

		}

		// return q * h * d;
		return q.mult(h).mult(d);
	}

	/*
	 * V - VECTOR
	 */
	public static boolean isVec3Parallel(float[] a, float[] b) {
		float[] array = VectorUtil.crossVec3(new float[] { 0, 0, 0 }, a, b);

		return VectorUtil.isVec3Zero(array, 0);
	}

	public static float[] rotateVec3(float[] v, Quaternion q) {
		return q.rotateVector(new float[3], 0, v, 0);
	}

	public static float[] subVec2(float[] a, float[] b) {
		return VectorUtil.subVec2(new float[2], a, b);
	}

	public static float[] subVec3(float[] a, float[] b) {
		return VectorUtil.subVec3(new float[3], a, b);
	}

}
