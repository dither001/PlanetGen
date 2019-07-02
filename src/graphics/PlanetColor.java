package graphics;

import com.planet.atmosphere.Climate;
import com.planet.atmosphere.Season;
import com.planet.lithosphere.Plate;

import controller.GlobeViewController;
import model.Planet;
import model.Tile;

public abstract class PlanetColor {

	public static float[][] tileColors;

	//
	public static float[][] topoColors;
	public static float[][] vegeColors;
	public static float[][] tempColors;
	public static float[][] aridColors;
	public static float[][] humidColors;
	public static float[][] rainColors;

	public static float[][] regionColors;
	public static float[][] latColors;

	/*
	 * COLOR METHODS
	 */
	public static void setupColors(Planet p) {
		Season s = p.getClimate().getSeason(0);

		//
		colorTopography(p);
		colorVegetation(s, p);
		colorTemperature(s, p);
		colorAridity(s, p);
		colorHumidity(s, p);
		colorPrecipitation(s, p);

		//
		colorRegions(p);
		colorLatitude(p);
	}

	public static void updateColors(int season, Planet p) {
		Season s = p.getClimate().getSeason(season);

		switch (GlobeViewController.getViewType()) {
		case ARIDITY:
			colorAridity(s, p);
			break;
		case HUMIDITY:
			colorHumidity(s, p);
			break;
		case PRECIPITATION:
			colorPrecipitation(s, p);
			break;
		case TEMPERATURE:
			colorTemperature(s, p);
			break;
		case VEGETATION:
			colorVegetation(s, p);
			break;
		default:
			break;
		}
	}

	private static float[] interpolateColor(float[] a, float[] b, double ratio) {
		return new float[] { //
				(float) (a[0] * (1.0 - ratio) + b[0] * ratio), //
				(float) (a[1] * (1.0 - ratio) + b[1] * ratio), //
				(float) (a[2] * (1.0 - ratio) + b[2] * ratio) //
		};
	}

	/*
	 * TOPOGRAPHY
	 */
	private static void colorTopography(Planet p) {
		float[] deepWater = Color.NAVY_BLUE.rgb();
		float[] water = Color.BLUE.rgb();
		float[] shallow = Color.LIGHT_BLUE.rgb();

		// float[][] land = new float[][] { //
		// Color.DARK_GREEN.rgb(), Color.LIGHT_GREEN.rgb(), Color.BRONZE.rgb(), //
		// Color.GREEN.rgb(), Color.RED.rgb(), Color.DARK_GRAY.rgb() //
		// };

		float[][] land = new float[][] { //
				{ 0.0f, 0.4f, 0.0f }, { 0.0f, 0.7f, 0.0f }, { 1.0f, 1.0f, 0.0f }, //
				{ 1.0f, 0.5f, 0.0f }, { 0.7f, 0.0f, 0.0f }, { 0.1f, 0.1f, 0.1f } //
		};
		float[] limit = { -500, 0, 500, 1000, 1500, 2000, 2500 };

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;
		double seaLevel = p.getSeaLevel();

		topoColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			double elevation = p.getElevationOfTile(i) - seaLevel;
			double ratio = 0.0;

			if (p.tileIsWater(i)) {
				// if (gTiles[i].isWater()) {
				// WATER
				if (elevation < -1000) {
					topoColors[i] = deepWater;
				} else if (elevation < -500) {
					ratio = (elevation + 500) / -500;
					topoColors[i] = interpolateColor(water, deepWater, ratio);
				} else {
					ratio = elevation / -500;
					topoColors[i] = interpolateColor(shallow, water, ratio);
				}
			} else {
				// LAND
				topoColors[i] = land[5];
				for (int j = 0; j < 5; ++j) {
					if (elevation <= limit[j + 1]) {
						ratio = Math.max(0.0, Math.min(1.0, elevation - limit[j] / (limit[j + 1] - limit[j])));
						topoColors[i] = interpolateColor(land[j], land[j + 1], ratio);
						break;
					}
				}
			}
		}
	}

	/*
	 * VEGETATION
	 */
	private static void colorVegetation(Season s, Planet p) {
		float[] deepWater = Color.NAVY_BLUE.rgb();
		float[] shallow = Color.LIGHT_BLUE.rgb();
		float[] snow = new float[] { 1.0f, 1.0f, 1.0f };
		float[] lowland = new float[] { 0.95f, 0.81f, 0.53f };
		float[] highland = new float[] { 0.1f, 0.1f, 0.1f };
		float[] vegetation = new float[] { 0.176f, 0.32f, 0.05f };

		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		vegeColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			double ratio = 0.0;

			if (p.tileIsWater(i)) {
				// WATER
				ratio = Math.min(1, p.getDepthAtTile(i) / 400);
				vegeColors[i] = interpolateColor(shallow, deepWater, ratio);

			} else {
				if (s.getTemperature(i) <= Climate.freezingPoint()) {
					vegeColors[i] = snow;

				} else {
					ratio = Math.min(1, (p.getElevationOfTile(i) - p.getSeaLevel()) / 2500);
					float[] ground = interpolateColor(lowland, highland, ratio);
					ratio = Math.min(1.0f, s.aridity(i) / 1.5f);
					vegeColors[i] = interpolateColor(vegetation, ground, ratio);
				}
			}
		}
	}

	/*
	 * TEMPERATURE
	 */
	private static void colorTemperature(Season s, Planet p) {
		float[][] colors = new float[][] { //
				Color.WHITE.rgb(), //
				{ 0.7f, 0f, 0.5f }, //
				{ 0f, 0f, 0.5f }, //
				{ 0f, 0f, 1.0f }, //
				{ 0f, 1.0f, 1 }, //
				{ 1.0f, 1.0f, 0 }, //
				{ 1.0f, 0.1f, 0 }, //
				{ 0.45f, 0f, 0 } //
		};

		float[] limit = new float[] { -50, -35, -20, -10, 0, 10, 20, 30 };

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		tempColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			float temp = s.getTemperature(i) - Climate.freezingPoint();
			if (temp <= limit[0]) {
				tempColors[i] = colors[0];
			} else if (temp >= limit[7]) {
				tempColors[i] = colors[7];
			} else {
				for (int j = 0; j < 7; ++j) {
					if (temp >= limit[j] && temp < limit[j + 1]) {
						double ratio = (temp - limit[j]) / (limit[j + 1] - limit[j]);
						tempColors[i] = interpolateColor(colors[j], colors[j + 1], ratio);
						break;
					}
				}
			}
		}
	}

	/*
	 * ARIDITY
	 */
	private static void colorAridity(Season s, Planet p) {
		float[] water = Color.WHITE.rgb();
		float[][] colors = new float[][] { { 1, 0, 0 }, { 1, 1, 0 }, { 0, 1, 0 }, { 0, 0.5f, 0 } };
		float[] limit = new float[] { 2, 1, 0.5f, 0 };

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		aridColors = new float[length][];
		for (int i = 0; i < length; ++i) {

			if (p.tileIsWater(i)) {
				// if (gTiles[i].isWater()) {
				aridColors[i] = water;
			} else {
				float aridity = s.aridity(i);
				aridColors[i] = colors[3];
				for (int j = 1; j < 4; ++j) {
					if (aridity > limit[j]) {
						float ratio = Math.min(1, (aridity - limit[j]) / (limit[j - 1] - limit[j]));
						aridColors[i] = interpolateColor(colors[j], colors[j - 1], ratio);
						break;
					}
				}
			}
		}
	}

	/*
	 * HUMIDITY
	 */
	private static void colorHumidity(Season s, Planet p) {
		float[] water = Color.WHITE.rgb();
		float[] dryland = new float[] { 1, 1, 0.5f };
		float[] midland = new float[] { 1, 1, 0 };
		float[] humid = new float[] { 0, 0.7f, 0 };

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		humidColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			if (p.tileIsWater(i)) {
				humidColors[i] = water;
			} else {
				double humidity = s.getHumidity(i) / Climate.saturationHumidity(s.getTemperature(i));
				double ratio = 0.0;
				if (humidity < 0.5) {
					ratio = humidity / 0.5;
					humidColors[i] = interpolateColor(dryland, midland, ratio);
				} else {
					ratio = (humidity - 0.5) / 0.5;
					humidColors[i] = interpolateColor(midland, humid, ratio);
				}
			}
		}
	}

	/*
	 * PRECIPITATION
	 */
	private static void colorPrecipitation(Season s, Planet p) {
		float[] water = Color.WHITE.rgb();
		float[] dry = new float[] { 1, 1, 0.5f };
		float[] medium = new float[] { 0, 1, 0 };
		float[] wet = new float[] { 0, 0, 1 };

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		rainColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			double high = 7e-8;
			double low = high / 10;

			if (p.tileIsWater(i)) {
				rainColors[i] = water;
			} else {
				double ratio = 0.0;
				float rain = s.getPrecipitation(i);
				if (rain < low) {
					ratio = rain / low;
					rainColors[i] = interpolateColor(dry, medium, ratio);
				} else {
					ratio = Math.min(1, (rain - low) / (high - low));
					rainColors[i] = interpolateColor(medium, wet, ratio);
				}
			}
		}
	}

	/*
	 * STAINED GLASS
	 */
	private static void stainedGlass(Planet p) {
		float[][] regions = new float[][] { //
				Color.BLOOD_RED.rgb(), Color.DARK_RED.rgb(), Color.RED.rgb(), Color.DARK_RED.rgb(), //
				Color.RUSSET.rgb(), Color.OCHRE.rgb(), Color.BRONZE.rgb(), //
				Color.FOREST_GREEN.rgb(), Color.DARK_GREEN.rgb(), Color.GREEN.rgb(), Color.LIGHT_GREEN.rgb(), //
				Color.SEA_GREEN.rgb(), Color.MINT.rgb(), Color.JUNGLE.rgb(), //
				Color.NAVY_BLUE.rgb(), Color.DARK_BLUE.rgb(), Color.BLUE.rgb(), Color.LIGHT_BLUE.rgb(), //
				Color.PURPLE.rgb(), Color.ORCHID.rgb(), Color.MAGENTA.rgb() //
		};

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		/*
		 * XXX - Doesn't currently save to anything important.
		 */
		float[][] stainedGlass = new float[length][];
		for (int i = 0; i < length; ++i) {
			if (i > 11)
				stainedGlass[i] = regions[i % 20];
			else
				stainedGlass[i] = new float[] { 1, 1, 1 };
		}

	}

	/*
	 * REGION
	 */
	private static void colorRegions(Planet p) {
		int[] plateIds = p.getTerrain().getTilePlateIds();

		float[][] regions = new float[][] { //
				Color.BLOOD_RED.rgb(), Color.DARK_RED.rgb(), Color.RED.rgb(), Color.DARK_RED.rgb(), //
				Color.RUSSET.rgb(), Color.OCHRE.rgb(), Color.BRONZE.rgb(), //
				Color.FOREST_GREEN.rgb(), Color.DARK_GREEN.rgb(), Color.GREEN.rgb(), Color.LIGHT_GREEN.rgb(), //
				Color.SEA_GREEN.rgb(), Color.MINT.rgb(), Color.JUNGLE.rgb(), //
				Color.NAVY_BLUE.rgb(), Color.DARK_BLUE.rgb(), Color.BLUE.rgb(), Color.LIGHT_BLUE.rgb(), //
				Color.PURPLE.rgb(), Color.ORCHID.rgb(), Color.MAGENTA.rgb() //
		};

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		regionColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			Tile current = gTiles[i];

			regionColors[i] = regions[plateIds[i]];
		}

	}

	/*
	 * LATITUDE
	 */
	private static void colorLatitude(Planet p) {
		float[] purple = new float[] { 0.4f, 0.1f, 0.4f };
		float[] yellow = new float[] { 0.4f, 0.4f, 0.1f };
		float[] green = new float[] { 0.1f, 0.4f, 0.1f };
		float[] blue = new float[] { 0.1f, 0.1f, 0.4f };

		//
		Tile[] gTiles = p.getGrid().tiles;
		int length = gTiles.length;

		latColors = new float[length][];
		for (int i = 0; i < length; ++i) {
			float latitude = gTiles[i].latitude;

			if (latitude < -1.0 || latitude > 1.0)
				latColors[i] = purple;
			else if (latitude < -0.5 || latitude > 0.5)
				latColors[i] = yellow;
			else if (latitude > 0)
				latColors[i] = green;
			else
				latColors[i] = blue;
		}
	}
}
