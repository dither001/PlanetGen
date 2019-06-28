package model;

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
	public double north(Planet p) {
		return p.north(this);
	}

}
