package model;

import api.LandType;
import api.UsesTerrain;

public class Edge implements UsesTerrain {
	int id;

	public Tile[] tiles;
	Corner[] corners;

	// terrain fields
	public LandType type;

	// climate fields
	float river_flow;
	float wind_velocity;

	/*
	 * CONSTRUCTORS
	 */
	public Edge(int id) {
		this.id = id;

		tiles = new Tile[2];
		corners = new Corner[2];

		// terrain fields
		type = LandType.LAND;

		// climate fields
		river_flow = 0;
		wind_velocity = 0;
	}

	/*
	 * 
	 */
	public String toString() {
		return "(" + tiles[0].id + "," + tiles[1].id + ")";
	}

	void addEdge(Tile[] t) {
		// TODO - untested
		Corner[] c = { //
				t[0].corners[Grid.position(t[0], t[1])], //
				t[0].corners[(Grid.position(t[0], t[1]) + 1) % t[0].edgeCount] //
		};

		for (int i = 0; i < 2; i++) {
			t[i].edges[Grid.position(t[i], t[(i + 1) % 2])] = this;
			tiles[i] = t[i];
			c[i].edges[Grid.position(c[i], c[(i + 1) % 2])] = this;
			corners[i] = c[i];
		}
	}

	@Override
	public LandType getLandType() {
		return type;
	}

	@Override
	public void setLandType(LandType type) {
		this.type = type;
	}

	public int sign(Tile t) {
		if (tiles[0].equals(t))
			return 1;
		else if (tiles[1].equals(t))
			return -1;
		else
			return 0;
	}
	
}
