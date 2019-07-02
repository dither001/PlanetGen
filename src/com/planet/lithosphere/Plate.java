package com.planet.lithosphere;

public class Plate {

	private int[] tiles;

	/*
	 * CONSTRUCTORS
	 */
	public Plate(Integer[] tileIDS) {
		tiles = new int[tileIDS.length];
		for (int i = 0; i < tileIDS.length; ++i)
			tiles[i] = tileIDS[i];
	}

	/*
	 * INSTANCE METHODS
	 */
	public int[] getTiles() {
		return tiles;
	}

	public void setTiles(int[] tiles) {
		this.tiles = tiles;
	}

}
