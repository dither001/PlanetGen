package model;

import com.jogamp.opengl.math.Ray;

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
	 * 
	 */
	public boolean intersects(Ray ray) {
//		if (!intersectRayWithSphere(ray, this.boundingSphere)) 
//			return false;

//		var surface = new THREE.Plane().setFromNormalAndCoplanarPoint(this.normal, this.averagePosition);
//		if (surface.distanceToPoint(ray.origin) <= 0) return false;

//		var denominator = surface.normal.dot(ray.direction);
//		if (denominator === 0) return false;

//		var t = -(ray.origin.dot(surface.normal) + surface.constant) / denominator;
//		var point = ray.direction.clone().multiplyScalar(t).add(ray.origin);
		
//		var origin = new Vector3(0, 0, 0);
//		for (var i = 0; i < this.corners.length; ++i) {
//			var j = (i + 1) % this.corners.length;
//			var side = new THREE.Plane().setFromCoplanarPoints(this.corners[j].position, this.corners[i].position, origin);

//			if (side.distanceToPoint(point) < 0)
//				return false;
//		}
		
//		return true;
		
		
		return false;
	}
	
	public double north(Planet p) {
		return p.north(this);
	}

}
