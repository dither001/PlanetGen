package controller;

import com.jogamp.opengl.math.VectorUtil;

/*
 * Class adapted from original JavaScript "Three.js" API
 * 
 * https://threejs.org/
 */

public class Plane {

	public float[] normal;
	public float k;

	/*
	 * CONSTRUCTORS
	 */
	public Plane(float[] normal, float k) {
		this.set(normal, k);

	}

	/*
	 * INSTANCE METHODS
	 */
	public void set(float[] normal, float k) {
		this.normal = new float[] { normal[0], normal[1], normal[2] };
		this.k = k;

	}

	// public void setComponents(float x, float y, float z, float w) {
	// this.normal = new float[] { x, y, z };
	// this.constant = w;
	// }

	public float distanceToPoint(float[] vec3) {
		return VectorUtil.dotVec3(normal, vec3) + k;
	}

	/*
	 * STATIC METHODS
	 */
	public static Plane setFromCoplanarPoints(float[] a, float[] b, float[] c) {
		float[] v1 = VectorUtil.subVec3(new float[3], c, b);
		float[] v2 = VectorUtil.subVec3(new float[3], a, b);

		float[] normal = VectorUtil.normalizeVec3(VectorUtil.crossVec3(new float[3], v1, v2));

		return setFromNormalAndCoplanarPoint(normal, a);
	}

	public static Plane setFromNormalAndCoplanarPoint(float[] normal, float[] point) {
		return new Plane(normal, -VectorUtil.dotVec3(point, normal));
	}

}
