package com.planet.lithosphere;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.jogamp.opengl.math.VectorUtil;

import controller.Dice;
import controller.Parameters;
import model.Corner;
import model.Grid;
import model.Planet;
import model.Tile;

public class Terrain {

	private static final int SCALE = 3000;

	// private float[] axis;
	// private float axial_tilt;
	// private float radius;
	// protected float seaLevel;

	/*
	 * INSTANCE FIELDS
	 */
	private Planet planet;
	private Grid grid;

	float[] elevationCorners;
	float[] elevationTiles;

	private int[] tilePlateIds;
	private Plate[] plates;

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

	public float[] getElevationTiles() {
		return elevationTiles;
	}

	public void setElevationTiles(float[] elevationTiles) {
		this.elevationTiles = elevationTiles;
	}

	public float getElevationOfCorner(int id) {
		return elevationCorners[id];
	}

	public float getElevationOfTile(int id) {
		return getElevationTiles()[id];
	}

	public Plate[] getPlates() {
		return plates;
	}

	public void setPlates(Plate[] plates) {
		this.plates = plates;
	}

	public int[] getTilePlateIds() {
		return tilePlateIds;
	}

	public void setTilePlateIds(int[] tilePlateIds) {
		this.tilePlateIds = tilePlateIds;
	}

	public Tile lowestTile() {
		Tile lowest = grid.tiles[0];
		for (int i = 0; i < grid.tiles.length; ++i) {

			if (getElevationTiles()[grid.tiles[i].id] < getElevationTiles()[lowest.id])
				lowest = grid.tiles[i];
		}

		return lowest;
	}

	/*
	 * OLDER METHODS
	 */
	// public float getRadius() {
	// return radius;
	// }
	//
	// public void setRadius(float radius) {
	// this.radius = radius;
	// }

	/*
	 * PRIVATE METHODS
	 */
	private void setupElevation() {
		this.elevationCorners = new float[grid.corners.length];
		this.setElevationTiles(new float[grid.tiles.length]);
		/*
		 * First step appears to create 1,000 Vec3[3] arrays with points uniformly
		 * distributed over a unit sphere.
		 */

		// _elevation_vectors
		List<float[][]> d = Dice.elevationVectors(Parameters.iterations);

		for (Tile el : grid.tiles)
			getElevationTiles()[el.id] = elevationHelper(el.v, d);

		for (Corner el : grid.corners)
			elevationCorners[el.id] = elevationHelper(el.v, d);

		/*
		 * SCALE ELEVATION - was separate method; just moved it here
		 */
		float lowest = getElevationTiles()[0], highest = lowest;

		for (Tile el : grid.tiles) {
			lowest = getElevationTiles()[el.id] < lowest ? getElevationTiles()[el.id] : highest;
			highest = getElevationTiles()[el.id] > highest ? getElevationTiles()[el.id] : highest;
		}

		for (Corner el : grid.corners) {
			lowest = elevationCorners[el.id] < lowest ? elevationCorners[el.id] : lowest;
			highest = elevationCorners[el.id] > highest ? elevationCorners[el.id] : highest;
		}

		highest = Math.max(1, highest - lowest);
		float scalar = SCALE / highest;

		for (Tile el : grid.tiles) {
			getElevationTiles()[el.id] -= lowest;
			getElevationTiles()[el.id] *= scalar;
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

	/*
	 * Randomly determine starting locations and sizes for tectonic plates.
	 */
	@SuppressWarnings("unchecked")
	private void setupTectonicPlates(int numberOfPlates) {
		Tile[] array = planet.getGrid().tiles;
		this.setPlates(new Plate[numberOfPlates]);

		// FIXME - testing only
//		System.out.println(numberOfPlates);

		HashSet<Integer> visited = new HashSet<Integer>();
		int size = planet.tileSize();
		this.tilePlateIds = new int[size];

		// random plate starting locations
		while (visited.size() < numberOfPlates)
			visited.add(Dice.nextInt(12, size));

		// create Sets for plates
		HashSet<Integer>[] plateSet = (HashSet<Integer>[]) new HashSet[numberOfPlates];
		for (int i = 0; i < plateSet.length; ++i)
			plateSet[i] = new HashSet<Integer>();

		Integer[] start = visited.toArray(new Integer[numberOfPlates]);
		// FIXME - testing only
//		for (Integer el : start)
//			System.out.println(el);

		class Node {
			int tileId;
			int plateId;

			Node(int tileId, int plateId) {
				this.tileId = tileId;
				this.plateId = plateId;
			}
		}
		;

		// setup queue, then clear for algorithm
		ArrayDeque<Node> queue = new ArrayDeque<Node>(visited.size());
		for (int i = 0; i < start.length; ++i) {
			if (start[i] != null)
				queue.add(new Node(start[i], i));
		}

		visited.clear();

		/*
		 * XXX - Function based on flood-fill
		 */
		Consumer<Node> nextTile = (node) -> {
			if (node != null) {
				if (visited.contains(node.tileId))
					return;

				visited.add(node.tileId);
				plateSet[node.plateId].add(node.tileId);

				for (Tile el : array[node.tileId].tiles) {
					queue.add(new Node(el.id, node.plateId));
				}

			}
		};

		// main loop
		while (queue.size() > 0) {
			// iterate through plates
			for (int i = 0; i < numberOfPlates; ++i) {
				Node current = queue.poll();

				if (current != null) {
					if (Dice.roll(6) < 6)
						nextTile.accept(current);
					else
						queue.add(current);					
				}
			}
		}

		// final step, turn the plateSets into new tectonic plates
		for (int i = 0; i < numberOfPlates; ++i) {
			int aSize = plateSet[i].size();
			this.plates[i] = new Plate(plateSet[i].toArray(new Integer[aSize]));

			for (Integer el : plateSet[i])
				tilePlateIds[el] = i;
		}

//		System.out.println("Total visited: " + visited.size());
//		int total = 0;
//		for (int i = 0; i < this.getPlates().length; ++i) {
//			int tmp = this.getPlates()[i].getTiles().length;
//			System.out.printf("Plate %d: %d %n", i, tmp);
//			total += tmp;
//		}
//		System.out.println("Total tiles: " + total);

	}

	/*
	 * STATIC METHODS
	 */
	public static Terrain build(Planet planet) {
		Terrain terrain = new Terrain();

		terrain.setPlanet(planet);
		terrain.setGrid(planet.getGrid());

		//
		terrain.setupElevation();
		terrain.setupTectonicPlates(Dice.roll(3, 4) + 4);

		return terrain;
	}
}
