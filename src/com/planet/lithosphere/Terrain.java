package com.planet.lithosphere;

import java.util.List;

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
	 * STATIC METHODS
	 */
	public static Terrain build(Planet planet) {
		Terrain terrain = new Terrain();

		terrain.setPlanet(planet);
		terrain.setGrid(planet.getGrid());

		//
		terrain.setupElevation();

		return terrain;
	}

}
