package model;

/*
 * So, let's talk a little bit about the Terrain Generation method used in 
 * vraid's "old earthgen" project. It's been a long time in porting his code 
 * from C++ to Java and I have learned a lot about both languages from it.
 * 
 * The first big hurdle for me was of course knowing next to nothing about
 * trigonometry or linear algebra. Vectors stumped me when I first began the
 * port in the early months of 2018 (I was taking Networks & Operating Systems 
 * at the time and was bored out of my mind).
 * 
 * The classes that helped me develop the "minimum required knowledge" to best
 * port this code were: Discrete Math, Linear Algebra, and CS-306 Algorithms. 
 * A few other things I had to figure out on my own, or with some probing 
 * questions aimed at my instructors.
 * 
 *  The last great hurdle was the inlined lambda expression in "Create Sea,"
 *  which required me to not only learn & understand how to create lambda
 *  expressions in Java ('cause it was a pain in the ass to do it another way)
 *  but to learn the half-dozen C++ syntax nuggets just to piece together the
 *  function of the original code.
 *  
 *  Oh and bloody multimaps aren't standard library in Java as far as I could
 *  tell, so I hacked together a Visited/Unvisited solution.
 *  
 *  But I got it done.
 *  
 *  And as of this writing, there's only the "river direction" function left.
 *  
 *  Nick Foster, June 1st, 2019
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import com.jogamp.opengl.math.VectorUtil;

import api.LandType;
import controller.Dice;
import controller.Parameters;

public class Terrain {

	private static final int SCALE = 3000;

	/*
	 * 
	 */
	private float[] axis;
	private float axial_tilt;
	private float radius;
	protected float seaLevel;

	private Grid grid;

	/*
	 * INSTANCE FIELDS
	 */
	private Planet planet;
	private float[] elevationCorners;
	private float[] elevationTiles;

	/*
	 * CONSTRUCTORS
	 */
	private Terrain() {
		// TODO
	}

	/*
	 * INSTANCE METHODS
	 */
	public Planet getPlanet() {
		return planet;
	}

	public void setPlanet(Planet planet) {
		this.planet = planet;
	}

	public Grid getGrid() {
		return grid;
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public float getElevationOfCorner(int id) {
		return elevationCorners[id];
	}

	public float getElevationOfTile(int id) {
		return elevationTiles[id];
	}

	/*
	 * OLDER METHODS
	 */
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	/*
	 * PRIVATE METHODS
	 */
	private void setupElevation() {
		this.elevationCorners = new float[grid.corners.length];
		this.elevationTiles = new float[grid.tiles.length];
		/*
		 * First step appears to create 1,000 Vec3[3] arrays with points uniformly
		 * distributed over a unit sphere.
		 */

		// _elevation_vectors
		List<float[][]> d = Dice.elevationVectors(Parameters.iterations);

		for (Tile el : grid.tiles)
			elevationTiles[el.id] = elevationHelper(el.v, d);

		for (Corner el : grid.corners)
			elevationCorners[el.id] = elevationHelper(el.v, d);

		/*
		 * SCALE ELEVATION - was separate method; just moved it here
		 */
		float lowest = elevationTiles[0], highest = lowest;

		for (Tile el : grid.tiles) {
			lowest = elevationTiles[el.id] < lowest ? elevationTiles[el.id] : highest;
			highest = elevationTiles[el.id] > highest ? elevationTiles[el.id] : highest;
		}

		for (Corner el : grid.corners) {
			lowest = elevationCorners[el.id] < lowest ? elevationCorners[el.id] : lowest;
			highest = elevationCorners[el.id] > highest ? elevationCorners[el.id] : highest;
		}

		highest = Math.max(1, highest - lowest);
		float scalar = SCALE / highest;

		for (Tile el : grid.tiles) {
			elevationTiles[el.id] -= lowest;
			elevationTiles[el.id] *= scalar;
		}

		for (Corner el : grid.corners) {
			elevationCorners[el.id] -= lowest;
			elevationCorners[el.id] *= scalar;
		}
	}

	private float elevationHelper(float[] point, List<float[][]> list) {
		float elevation = 0;

		for (float[][] el : list) {

			if (VectorUtil.distSquareVec3(point, el[0]) < 2.0 //
					&& VectorUtil.distSquareVec3(point, el[1]) < 2.0 //
					&& VectorUtil.distSquareVec3(point, el[2]) < 2.0) {

				++elevation;
			}
		}

		return elevation;
	}

	private void createSea() {
		Tile start = lowestTile();

		float seaLevel = elevationTiles[start.id];
		// float seaLevel = start.elevation;

		int waterTileTarget = (int) (Parameters.water_ratio * grid.tiles.length);

		HashSet<Tile> waterTileSet = new HashSet<Tile>();
		boolean[] coastalTileArray = new boolean[grid.tiles.length];
		// Arrays.fill(coastalTileArray, true);

		HashSet<Tile> unvisited = new HashSet<Tile>();
		HashSet<Tile> visited = new HashSet<Tile>();

		if (waterTileTarget > 0) {
			waterTileSet.add(start);

			// setup
			for (Tile el : start.tiles) {
				coastalTileArray[el.id] = true;
				unvisited.add(el);
			}

			/*
			 * Anonymous function "InsertNextTile" iterates through adjacent tiles and adds
			 * them to unvisited if they aren't already in the WaterTileSet.
			 */
			Supplier<Float> insertNextTile = () -> {
				Tile tile = unvisited.iterator().next();
				unvisited.remove(tile);
				visited.add(tile);

				//
				waterTileSet.add(tile);
				coastalTileArray[tile.id] = false;
				for (Tile el : tile.tiles) {
					if (!(waterTileSet.contains(el)) && !(coastalTileArray[el.id])) {
						unvisited.add(el);
						coastalTileArray[tile.id] = true;
					}
				}

				return elevationTiles[tile.id];
				// return tile.elevation;
			};

			while (waterTileSet.size() < waterTileTarget) {
				seaLevel = (float) insertNextTile.get();

				while (unvisited.size() > 0 && elevationTiles[unvisited.iterator().next().id] < seaLevel)
					insertNextTile.get();

				// while (unvisited.size() > 0 && unvisited.iterator().next().elevation <
				// seaLevel)
				// insertNextTile.get();
			}

			if (waterTileSet.size() > 0)
				seaLevel = (seaLevel + elevationTiles[waterTileSet.iterator().next().id]) / 2;

			// if (waterTileSet.size() > 0)
			// seaLevel = (seaLevel + waterTileSet.iterator().next().elevation) / 2;

			for (Tile el : waterTileSet) {
				el.water.surface = (float) seaLevel;
				el.water.depth = (float) (seaLevel - elevationTiles[el.id]);
				// el.water.depth = (float) (seaLevel - el.elevation);
			}
		}

		this.seaLevel = seaLevel;
	}

	private Tile lowestTile() {
		Tile lowest = grid.tiles[0];
		for (int i = 0; i < grid.tiles.length; ++i) {

			if (elevationTiles[grid.tiles[i].id] < elevationTiles[lowest.id])
				lowest = grid.tiles[i];

			// if (grid.tiles[i].elevation < lowest.elevation)
			// lowest = grid.tiles[i];
		}

		return lowest;
	}

	private void classifyTerrain() {
		for (Tile el : grid.tiles)
			classifyTileHelper(el);

		for (Corner el : grid.corners)
			classifyCornerHelper(el);

		for (Edge el : grid.edges)
			classifyEdgeHelper(el);
	}

	private void classifyTileHelper(Tile tile) {
		int land = 0, water = 0;

		for (Tile el : tile.tiles) {
			if (el.water.depth > 0)
				++water;
			else
				++land;
		}

		/*
		 * XXX - For some reason, tile classification is handled differently from Corner
		 * and Edge classification (which both use ternary expression).
		 */
		tile.type = tile.water.depth > 0 ? LandType.WATER : LandType.LAND;
		if (land > 0 && water > 0)
			tile.type = LandType.COAST;
	}

	private void classifyCornerHelper(Corner corner) {
		int land = 0, water = 0;

		for (Tile el : corner.tiles) {
			if (el.water.depth > 0)
				++water;
			else
				++land;
		}

		corner.type = land > 0 && water > 0 ? LandType.COAST : land > 0 ? LandType.LAND : LandType.WATER;
	}

	private void classifyEdgeHelper(Edge edge) {
		int land = 0, water = 0;

		for (Tile el : edge.tiles) {
			if (el.water.depth > 0)
				++water;
			else
				++land;
		}

		edge.type = land > 0 && water > 0 ? LandType.COAST : land > 0 ? LandType.LAND : LandType.WATER;
	}

	private void setRiverDirections() {
		HashSet<Corner> endpoints = new HashSet<Corner>();

		for (Corner el : grid.corners) {
			if (el.isCoast()) {
				el.distanceToSea = 0;
				endpoints.add(el);
			}
		}

		while (endpoints.size() > 0) {
			Iterator<Corner> it = endpoints.iterator();
			Corner current = it.next();

			for (Corner el : current.corners) {
				if (el.isLand() && el.riverDirection == -1) {
					// XXX - I might need to swap current & el
					el.riverDirection = Grid.position(current, el);
					el.distanceToSea = 1 + current.distanceToSea;
					endpoints.add(el);
				}
			}

			endpoints.remove(current);
		}
	}

	/*
	 * STATIC METHODS
	 */
	public static Terrain build(Planet planet) {
		Terrain terrain = new Terrain();

		terrain.setPlanet(planet);
		terrain.setGrid(planet.getGrid());

		// set_grid_size(planet, parameters.grid_size)

		//
		// terrain.initializeTerrain(grid);

		// parameters
		terrain.axis = Parameters.axis;
		terrain.setRadius(40000000);

		//
		terrain.setupElevation();
		terrain.createSea();
		terrain.classifyTerrain();
		terrain.setRiverDirections();

		return terrain;
	}

}
