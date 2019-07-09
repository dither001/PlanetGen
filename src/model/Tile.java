package model;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;

import controller.Parameters;
import controller.Plane;
import controller.PlanetViewController;

public class Tile {
	public int id;
	int edgeCount;

	//
	public int region;

	public Tile[] tiles;
	public Corner[] corners;
	public Edge[] edges;

	public float[] v;
	private BoundingSphere bounds;

	// terrain fields
	public float latitude;

	/*
	 * CONSTRUCTORS
	 */
	public Tile(int id, int edgeCount) {
		this.id = id;
		this.edgeCount = edgeCount;

		v = new float[] { 0, 0, 0 };
		bounds = null;

		tiles = new Tile[edgeCount];
		corners = new Corner[edgeCount];
		edges = new Edge[edgeCount];

		// terrain
		this.latitude = 0;
	}

	/*
	 * Method adapted from original JavaScript code written for
	 * "Procedural Planet Generation" by Andy Gainey
	 * 
	 * https://experilous.com/1/blog/post/procedural-planet-generation
	 */
	public boolean intersects(Ray r, Grid g) {
		if (true != g.intersectSphere(r))
			return false;

		// System.out.println("A");
		++PlanetViewController.A_COUNT;
		if (true != bounds.hit(r, -2, 2))
			return false;

		// System.out.println("B");
//		++PlanetViewController.B_COUNT;
//		Plane surface = Plane.setFromNormalAndCoplanarPoint(VectorUtil.normalizeVec3(new float[3], v), v);
//		if (surface.distanceToPoint(r.orig) < 0)
//			return false;

//		float denom = VectorUtil.dotVec3(surface.normal, r.dir);
//		if (FloatUtil.isZero(denom, Parameters.EPSILON))
//			return false;

		// System.out.println("C");
//		++PlanetViewController.C_COUNT;
//		float t = -(VectorUtil.dotVec3(r.orig, surface.normal) + surface.k) / denom;
//		float[] point = VectorUtil.copyVec3(new float[3], 0, r.dir, 0);
//		point = VectorUtil.scaleVec3(point, point, t);
//		point = VectorUtil.addVec3(point, point, r.orig);
//
//		float[] origin = new float[3];
//		for (int i = 0; i < corners.length; ++i) {
//			int j = (i + 1) % corners.length;
//			Plane side = Plane.setFromCoplanarPoints(corners[j].v, corners[i].v, origin);
//
//			if (side.distanceToPoint(point) < 0)
//				return false;
//		}

//		++PlanetViewController.D_COUNT;
//		System.out.println("D");
		return true;
	}

	public double north(Planet p) {
		return p.north(this);
	}

	public void setupBoundingSphere() {
		float maxDistanceToCorner = 0;

		for (int i = 0; i < corners.length; ++i) {
			maxDistanceToCorner = Math.max(maxDistanceToCorner, VectorUtil.distVec3(v, corners[i].v));
		}

		this.bounds = new BoundingSphere(v, maxDistanceToCorner * 0.6f);
	}

	/*
	 * PRIVATE BOUNDING SPHERE CLASS
	 */
	private class BoundingSphere {
		float[] center;
		float radius;

		BoundingSphere(float[] center, float radius) {
			this.center = center;
			this.radius = radius;
		}

		boolean hit(Ray r, float t_min, float t_max) {
			float[] oc = VectorUtil.subVec2(new float[3], r.orig, center);
			float[] d = r.dir;

			float a = VectorUtil.dotVec3(d, d);
			float b = VectorUtil.dotVec3(oc, d);
			float c = VectorUtil.dotVec3(oc, oc) - radius * radius;
			float discriminant = b * b - a * c;

			if (discriminant > 0 || FloatUtil.isZero(discriminant, Parameters.EPSILON)) {
				float temp = (float) (-1 * b - Math.sqrt(discriminant) / a);
				if (temp < t_max && temp > t_min)
					return true;

				temp = (float) (-1 * b + Math.sqrt(discriminant) / a);
				if (temp < t_max && temp > t_min)
					return true;
			}

			return false;
		}

	}
}
