package model;

import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;

import controller.Parameters;
import controller.Plane;

public class Tile {
	public int id;
	int edgeCount;

	//
	public int region;

	public Tile[] tiles;
	public Corner[] corners;
	public Edge[] edges;

	public float[] v;

	// terrain fields
	public float latitude;

	/*
	 * CONSTRUCTORS
	 */
	public Tile(int id, int edgeCount) {
		this.id = id;
		this.edgeCount = edgeCount;

		v = new float[] { 0, 0, 0 };
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

		Plane surface = Plane.setFromNormalAndCoplanarPoint(VectorUtil.normalizeVec3(new float[3], v), v);
		if (surface.distanceToPoint(r.orig) < 0)
			return false;

		float denom = VectorUtil.dotVec3(surface.normal, r.dir);
		if (FloatUtil.isZero(denom, Parameters.EPSILON))
			return false;

		float t = -(VectorUtil.dotVec3(r.orig, surface.normal) + surface.k) / denom;
		float[] point = VectorUtil.copyVec3(new float[3], 0, r.dir, 0);
		point = VectorUtil.scaleVec3(point, point, t);
		point = VectorUtil.addVec3(point, point, r.orig);

		float[] origin = new float[] { 0, 0, 0 };
		for (int i = 0; i < corners.length; ++i) {
			int j = (i + 1) % corners.length;
			Plane side = Plane.setFromCoplanarPoints(corners[j].v, corners[i].v, origin);

			if (side.distanceToPoint(point) < 0)
				return false;
		}

		return true;
	}

	public double north(Planet p) {
		return p.north(this);
	}

}
