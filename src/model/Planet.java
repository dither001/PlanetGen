package model;

import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.math.VectorUtil;
import com.planet.hydrosphere.Water;

import api.LandType;
import controller.Parameters;
import controller.PlanetUtil;

public class Planet {
	protected Grid grid;
	protected Terrain terrain;
	private Water water;
	protected Climate climate;

	/*
	 * 
	 */
	public void clear() {
		// TODO

	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public int tileSize() {
		return grid.tileSize();
	}

	public Terrain getTerrain() {
		return terrain;
	}

	public void setTerrain(Terrain terrain) {
		this.terrain = terrain;
	}

	public Water getWater() {
		return water;
	}

	public void setWater(Water water) {
		this.water = water;
	}

	public Climate getClimate() {
		return climate;
	}

	public void setClimate(Climate climate) {
		this.climate = climate;
	}

	/*
	 * EXPERIMENTAL
	 */
	public float getElevationOfCorner(int id) {
		return terrain.getElevationOfCorner(id);
	}

	public float getElevationOfTile(int id) {
		return terrain.getElevationOfTile(id);
	}

	public float getDepthAtTile(int id) {
		return water.getDepthAtTile(id);
	}

	public float getSurfaceAtTile(int id) {
		return water.getSurfaceAtTile(id);
	}

	public boolean tileIsLand(int id) {
		return terrain.getTypeOfTile(id).isLand();
	}

	public boolean tileIsWater(int id) {
		return terrain.getTypeOfTile(id).isWater();
	}

	/*
	 * PRIVATE METHODS
	 */
	private void classifyTerrain() {
		terrain.terrainCorners = new LandType[grid.corners.length];
		terrain.terrainEdges = new LandType[grid.edges.length];
		terrain.terrainTiles = new LandType[grid.tiles.length];

		for (Tile el : grid.tiles)
			classifyTileHelper(el);

		for (Corner el : grid.corners)
			classifyCornerHelper(el);

		for (Edge el : grid.edges)
			classifyEdgeHelper(el);
	}

	private void classifyTileHelper(Tile tile) {
		int land = 0, sea = 0;

		for (Tile el : tile.tiles) {
			// (el.water.depth > 0)
			if (water.getDepthAtTile(el.id) > 0)
				++sea;
			else
				++land;
		}

		terrain.terrainTiles[tile.id] = land > 0 && sea > 0 ? LandType.COAST
				: land > 0 ? LandType.LAND : LandType.WATER;
		// terrainTiles[tile.id] = tile.water.depth > 0 ? LandType.WATER :
		// LandType.LAND;
		// if (land > 0 && water > 0)
		// terrainTiles[tile.id] = LandType.COAST;
	}

	private void classifyCornerHelper(Corner corner) {
		int land = 0, sea = 0;

		for (Tile el : corner.tiles) {
			// if (el.water.depth > 0)
			if (water.getDepthAtTile(el.id) > 0)
				++sea;
			else
				++land;
		}

		terrain.terrainCorners[corner.id] = land > 0 && sea > 0 ? LandType.COAST
				: land > 0 ? LandType.LAND : LandType.WATER;
	}

	private void classifyEdgeHelper(Edge edge) {
		int land = 0, sea = 0;

		for (Tile el : edge.tiles) {
			// if (el.water.depth > 0)
			if (water.getDepthAtTile(el.id) > 0)
				++sea;
			else
				++land;
		}

		terrain.terrainEdges[edge.id] = land > 0 && sea > 0 ? LandType.COAST
				: land > 0 ? LandType.LAND : LandType.WATER;
	}

	/*
	 * INSTANCE METHODS
	 */
	public double area(Tile t) {
		double a = 0.0;

		int e = t.edgeCount;
		for (int i = 0; i < e; ++i) {
			float[] u = new float[3];
			float[] v = new float[3];

			VectorUtil.normalizeVec3(VectorUtil.subVec3(u, t.v, t.corners[i].v));
			VectorUtil.normalizeVec2(VectorUtil.subVec3(v, t.v, t.corners[(i + 1) % e].v));

			double angle = Math.acos(VectorUtil.dotVec3(u, v));
			a += 0.5 * Math.sin(angle) * VectorUtil.distVec3(t.v, t.corners[i].v)
					* VectorUtil.distVec3(t.v, t.corners[(i + 1) % e].v);
		}

		return a * Math.pow(terrain.getRadius(), 2.0);
	}

	public double edgeLength(Edge e) {
		return VectorUtil.distVec3(e.corners[0].v, e.corners[1].v) * terrain.getRadius();
	}

	public float[] defaultAxis() {
		return new float[] { 0, 0, 1 };
	}

	public Quaternion rotation() {
		// return Quaternion(default_axis(), axis(p));
		return PlanetUtil.fromTwoVec3(defaultAxis(), Parameters.axis);
	}

	public Quaternion rotationToDefault() {
		/*
		 * XXX - I'm pissed off because this C++ code makes no goddamn sense: what does
		 * C++ do if you give it to returns, one line after another?
		 */

		// return conjugate(rotation(p));
		// return Quaternion(axis(p), default_axis());
		return rotation().conjugate();
	}

	public float getSeaLevel() {
		return water.getSeaLevel();
	}

	/*
	 * XXX - This method presumably returns "north" relative to the parameter Tile.
	 */
	public float north(Tile tile) {
		// Vector3 v = reference_rotation(t, rotation_to_default(p)) *
		// vector(nth_tile(t, 0));
		float x = tile.v[0], y = tile.v[1], z = tile.v[2];
		float[] v = new float[] { x, y, z };
		v = PlanetUtil.referenceRotation(v, rotationToDefault()).rotateVector(v, 0, v, 0);

		// return pi - atan2(v.y, v.x);
		return (float) (Math.PI - Math.atan2(v[1], v[0]));
	}

	/*
	 * STATIC METHODS
	 */
	public static Planet build(int size) throws Exception {
		if (size < 9) {
			Planet planet = new Planet();

			planet.setGrid(Grid.build(size));
			planet.setTerrain(Terrain.build(planet));
			planet.setWater(Water.build(planet));
			planet.classifyTerrain();
			planet.setClimate(Climate.build(planet));

			return planet;
		}

		throw new Exception("Please enter smaller size.");
	}

	/*
	 * XXX - default is set to "24 hours"
	 */
	public static float angularVelocity(Planet p) {
		return (float) (2.0 * Math.PI / (24 * 60 * 60));
	}

	public static float coriolisCoeeficient(Planet p, double latitude) {
		return (float) (2.0 * angularVelocity(p) * Math.sin(latitude));
	}

	// public static float latitude(Planet p, float[] vector3) {
	// /*
	// * FIXME - subtracts (angle between planet's axis and vector3)
	// */
	// return (float) (Math.PI / 2 - VectorUtil.angleVec3(p.defaultAxis(),
	// vector3));
	// }

	/*
	 * Checks if x and y are 0; returns inverse-tangent of y, x
	 */
	public static float longitude(float[] vector3) {
		int x = (int) vector3[0];
		int y = (int) vector3[1];
		if (x == 0 && y == 0)
			return 0;

		return (float) Math.atan2(vector3[1], vector3[0]);
	}

	/*
	 * Returns inverse-sine of z-coordinate
	 */
	public static float latitude(float[] vector3) {
		return (float) Math.asin(vector3[2]);
	}

	public static float[] pressureGradientForce(double tropicalEquator, double latitude) {
		double pressure_derivate;
		double pressure_deviation = 20.0 / 15000;

		double c;
		if (latitude > tropicalEquator)
			c = 3.0 * Math.PI / (Math.PI / 2.0 - tropicalEquator);
		else
			c = 3.0 * Math.PI / (Math.PI / 2.0 + tropicalEquator);

		pressure_derivate = pressure_deviation * Math.sin(c * (latitude - tropicalEquator));

		double a = tropicalEquator + (Math.PI / 2.0 - tropicalEquator) / 3.0;
		double b = tropicalEquator - (Math.PI / 2.0 + tropicalEquator) / 3.0;
		if (latitude < a && latitude > b)
			pressure_derivate = pressure_deviation / 3.0;

		return new float[] { (float) -pressure_derivate, 0.0f };
	}

}
