package com.planet.hydrosphere;

import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Supplier;

import com.planet.lithosphere.Terrain;

import controller.Parameters;
import model.Corner;
import model.Grid;
import model.Planet;
import model.Tile;

public class Water {

	private Planet planet;
	private float seaLevel;

	private float[] depth;
	private float[] surface;

	/*
	 * CONSTRUCTORS
	 */
	private Water() {
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

	public float getSeaLevel() {
		return seaLevel;
	}

	public void setSeaLevel(float seaLevel) {
		this.seaLevel = seaLevel;
	}

	public float getDepthAtTile(int id) {
		return depth[id];
	}

	public void setDepthAtTile(int id, float depth) {
		this.depth[id] = depth;
	}

	public float getSurfaceAtTile(int id) {
		return surface[id];
	}

	public void setSurfaceAtTile(int id, float surface) {
		this.surface[id] = surface;
	}

	/*
	 * PRIVATE METHODS
	 */
	private void createSea() {
		this.depth = new float[planet.tileSize()];
		this.surface = new float[planet.tileSize()];

		Terrain terrain = planet.getTerrain();
		Tile start = terrain.lowestTile();

		float seaLevel = terrain.getElevationTiles()[start.id];
		// float seaLevel = start.elevation;

		int waterTileTarget = (int) (Parameters.water_ratio * planet.tileSize());

		HashSet<Tile> waterTileSet = new HashSet<Tile>();
		boolean[] coastalTileArray = new boolean[planet.tileSize()];
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

				// return elevationTiles[tile.id];
				return terrain.getElevationOfTile(tile.id);
			};

			while (waterTileSet.size() < waterTileTarget) {
				seaLevel = (float) insertNextTile.get();

				while (unvisited.size() > 0 && terrain.getElevationOfTile(unvisited.iterator().next().id) < seaLevel)
					insertNextTile.get();
			}

			if (waterTileSet.size() > 0)
				seaLevel = (seaLevel + terrain.getElevationOfTile(waterTileSet.iterator().next().id)) / 2;

			for (Tile el : waterTileSet) {
				setSurfaceAtTile(el.id, seaLevel);
				setDepthAtTile(el.id, seaLevel - terrain.getElevationOfTile(el.id));
			}
		}

		this.seaLevel = seaLevel;
	}

	private void setRiverDirections() {
		HashSet<Corner> endpoints = new HashSet<Corner>();

		for (Corner el : planet.getGrid().corners) {
			if (planet.getTypeOfCorner(el.id).isCoast()) {
				el.distanceToSea = 0;
				endpoints.add(el);
			}
		}

		while (endpoints.size() > 0) {
			Iterator<Corner> it = endpoints.iterator();
			Corner current = it.next();

			for (Corner el : current.corners) {
				if (planet.getTypeOfCorner(el.id).isLand() && el.riverDirection == -1) {
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
	public static Water build(Planet p) {
		Water w = new Water();

		//
		w.setPlanet(p);
		w.createSea();

		return w;
	}

}
