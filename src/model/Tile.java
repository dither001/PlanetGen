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
	// public float elevation;
	// public LandType type;
	public Water water;

	// climate fields
	// public Wind wind;
	// private float latitude;
	// public double temperature;
	// public float humidity;
	// public float precipitation;

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

		// terrain fields
		this.latitude = 0;
		this.region = -1;
		
		// elevation = 0;
		// type = LandType.LAND;
		water = new Water();

		// climate fields
		// latitude = 0;
		// temperature = 0;
		// humidity = 0;
		// precipitation = 0;
	}

	/*
	 * 
	 */
	public double north(Planet p) {
		return p.north(this);
	}

	/*
	 * TERRAIN METHODS
	 */
	public float getWaterDepth() {
		return water.depth;
	}

}
