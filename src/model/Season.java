package model;

/*
 * So, I was working on the "_iterate_humidity" function when I realized the 
 * "error" of what I was doing: by placing the seasonal effects in the tiles
 * themselves, I made it more difficult to change seasons without touching the
 * tiles every single time.
 * 
 * I could totally fix this in a later refactor. For now, I don't really care.
 * I think I had a totally legitimate reason to not use the same method for
 * getting access to all the face/edge/corner data as vraid did. For starters,
 * I didn't understand it and couldn't work with it effectively.
 * 
 *  That's a big one. I've only been able to make it this far into porting his
 *  program to Java because I've made compromises along the way.
 *  
 *  If I get a function world generator out of this, complete with elevation,
 *  water, wind, seasons, and whatnot -- it will all have been worth it. Even
 *  if it ultimately fails to work, I have learned a lot along the way.
 *  
 *  But of course I really hope it works. Duh.
 *  
 *  Nick Foster, 6/13/2019
 */

import com.jogamp.opengl.math.VectorUtil;

import controller.Parameters;
import controller.PlanetUtil;

public class Season {

	/*
	 * original list from Tile
	 * 
	 * latitude, temperature, humidity, precipitation
	 */

	float[][] climateTiles;
	float[][] windTiles;

	//
	Planet planet;
	int id;

	double solar_equator;
	float time_of_year;

	//
	double tropical_equator;

	/*
	 * CONSTRUCTORS
	 */
	private Season() {
		// TODO
	}

	/*
	 * INSTANCE METHODS
	 */
	public void setTemperature(int id, float temperature) {
		climateTiles[id][0] = temperature;
	}

	public float getTemperature(int id) {
		return climateTiles[id][0];
	}

	public void setHumidity(int id, float humidity) {
		climateTiles[id][1] = humidity;
	}

	public float getHumidity(int id) {
		return climateTiles[id][1];
	}

	public void setPrecipitation(int id, float precipitation) {
		climateTiles[id][2] = precipitation;
	}

	public float getPrecipitation(int id) {
		return climateTiles[id][2];
	}

	public float potentialEvapotranspiration(int id) {
		return Climate.saturationHumidity(getTemperature(id)) - getHumidity(id);
	}

	public float aridity(int id) {
		return (float) Climate.aridity(potentialEvapotranspiration(id));
	}

	public void setWind(int id, Wind wind) {
		windTiles[id][0] = wind.direction;
		windTiles[id][0] = wind.speed;
	}

	public void setWindDirection(int id, float direction) {
		windTiles[id][0] = direction;
	}

	public float getWindDirection(int id) {
		return windTiles[id][0];
	}

	public void setWindSpeed(int id, float speed) {
		windTiles[id][1] = speed;
	}

	public float getWindSpeed(int id) {
		return windTiles[id][1];
	}

	/*
	 * PRIVATE METHODS
	 */
	private void setupTiles() {
		Tile[] gTiles = planet.getGrid().tiles;
		for (int i = 0; i < gTiles.length; ++i) {
			climateTiles[i] = new float[] { 0, 0, 0, 0 };
			windTiles[i] = new float[] { 0, 0 };
		}

	}

	private void setupTemperature() {
		Tile[] gTiles = planet.getGrid().tiles;

		for (int i = 0; i < gTiles.length; ++i) {
			float temperature = (float) temperatureAtLatitude(tropical_equator - Planet.latitude(gTiles[i].v));

			if (planet.tileIsLand(i) && planet.getElevationOfTile(i) > planet.getSeaLevel()) {
				// if (gTiles[i].isLand() && planet.getElevationOfTile(i) >
				// planet.getSeaLevel()) {
				temperature -= Climate.temperatureLapse(planet.getElevationOfTile(i) - planet.getSeaLevel());
			} else {
				temperature = (float) (0.3 * temperature + 0.7 * temperatureAtLatitude(Planet.latitude(gTiles[i].v)));
			}

			// if (gTiles[i].isLand() && gTiles[i].elevation > planet.getSeaLevel()) {
			// temperature -= Climate.temperatureLapse(gTiles[i].elevation -
			// planet.getSeaLevel());
			// } else {
			// temperature = (float) (0.3 * temperature + 0.7 *
			// temperatureAtLatitude(Planet.latitude(gTiles[i].v)));
			// }

			setTemperature(i, temperature);
		}
	}

	private void setupWind() {
		for (Tile el : planet.grid.tiles) {
			defaultWind(el);

			setWindDirection(el.id, getWindDirection(el.id) + planet.north(el));
		}

		for (Tile el : planet.grid.tiles) {
			double[][] m = PlanetUtil.rotationMatrix(el.north(planet) - getWindDirection(el.id));
			float[][] corners = PlanetUtil.polygon(planet.rotationToDefault(), el);

			corners = PlanetUtil.multMat2ByVec2Array(m, corners);

			int n = el.edges.length;
			for (int i = 0; i < n; ++i) {
				int direction = el.edges[i].sign(el);
				if (corners[i][0] + corners[(i + 1) % n][0] < 0) {
					direction *= -1;
				}

				planet.grid.edges[el.edges[i].id].wind_velocity -= 0.5 * direction * getWindSpeed(el.id)
						* Math.abs(corners[i][1] - corners[(i + 1) % n][1])
						/ VectorUtil.normVec2(PlanetUtil.subVec2(corners[i], corners[(i + 1) % n]));
			}
		}
	}

	private void setupHumidity() {
		for (Tile el : planet.grid.tiles) {
			float humidity = 0.0f;

			if (planet.tileIsWater(el.id)) {
				// if (el.isWater()) {
				humidity = Climate.saturationHumidity(getTemperature(el.id));
			}

			setHumidity(el.id, humidity);
		}

		/*
		 * XXX - I decided to embed the "_iterate_humidity" function within the
		 * setupHumidity() function because there wasn't a good reason NOT TO.
		 */

		int n = planet.grid.tiles.length;
		float[] humidity = new float[n];
		float[] precipitation = new float[n];

		float delta = 1.0f;
		while (delta > Parameters.error_tolerance) {

			Tile current = null;
			for (int i = 0; i < n; ++i) {
				current = planet.grid.tiles[i];

				precipitation[i] = 0.0f;
				if (planet.tileIsLand(i)) {
					// if (current.isLand()) {
					humidity[i] = 0.0f;
					float incomingWind = incomingWind(current);
					float outgoingWind = outgoingWind(current);

					// System.out.printf("In: %.2f || Out: %.2f %n", incomingWind, outgoingWind);
					// System.out.printf("Out - In: %.2f %n", outgoingWind - incomingWind );

					if (incomingWind > 0) {
						float convection = outgoingWind - incomingWind;
						float incomingHumidity = incomingHumidity(current);

						// Humidity decreases if outgoing wind > incoming wind
						float density = convection > 0 ? incomingHumidity / (incomingWind + convection)
								: incomingHumidity / incomingWind;

						float saturation = Climate.saturationHumidity(getTemperature(current.id));
						// if (saturation > 0.0001)
						// System.out.println(current.temperature);

						// Limit to local saturation humidity
						humidity[i] = Math.min(saturation, density);
						if (saturation < density) {
							precipitation[i] += (density - saturation) * incomingWind;
						}

						// Humidity increases when outgoing wind < incoming wind
						if (convection < 0) {
							float convective = humidity[i] * (-convection / incomingWind);
							if (humidity[i] + convective > saturation) {
								precipitation[i] += (humidity[i] + convective - saturation) * (-convection);
							}

							humidity[i] = Math.min(saturation, humidity[i] + convective);
						}

						// Scale precipitation by constant (3.0) and area
						precipitation[i] *= 3.0 / planet.area(current);

					} else {
						// humidity[i] = current.humidity;
						setHumidity(current.id, getHumidity(current.id));

					}
				}
			}

			float largestChange = 0.0f;
			for (int i = 0; i < humidity.length; ++i) {
				float localHumidity = getHumidity(i);
				largestChange = Math.max(largestChange, humidityChange(humidity[i], localHumidity));

				// System.out.println(localHumidity);
				// System.out.println(largestChange);
			}

			delta = largestChange;
			for (int i = 0; i < humidity.length; ++i) {
				setHumidity(i, humidity[i]);
				setPrecipitation(i, precipitation[i]);

				// System.out.printf("Humidity: %.2f || Rainfall: %.2f %n", humidity[i],
				// precipitation[i]);
			}
		}
	}

	private float incomingWind(Tile t) {
		float sum = 0.0f;

		for (Edge el : t.edges) {
			if (el.sign(t) * el.wind_velocity > 0) {
				sum += Math.abs(el.wind_velocity * planet.edgeLength(el));
			}
		}

		return sum;
	}

	private float outgoingWind(Tile t) {
		float sum = 0.0f;

		for (Edge el : t.edges) {
			if (el.sign(t) * el.wind_velocity < 0) {
				sum += Math.abs(el.wind_velocity) * planet.edgeLength(el);
			}
		}

		return sum;
	}

	private float incomingHumidity(Tile t) {
		float sum = 0.0f;

		for (Edge el : t.edges) {
			if (el.sign(t) * el.wind_velocity > 0) {
				sum += getHumidity(t.id) * Math.abs(el.wind_velocity) * planet.edgeLength(el);
			}
		}

		return sum;
	}

	private float humidityChange(float a, float b) {
		float nearZero = 1.0e-15f;

		if (a < nearZero) {
			if (b > nearZero)
				return 1.0f;
			else
				return 0.0f;
		}

		return 1.0f - a / b;
	}

	/*
	 * XXX - Friction appears to be the same on land as elsewhere. Error?
	 */
	private void defaultWind(Tile tile) {
		// "_default_wind" method
		double latitude = Planet.latitude(tile.v);

		float[] pgf = Planet.pressureGradientForce(tropical_equator, latitude);
		double coriolis = Planet.coriolisCoeeficient(planet, latitude);
		double friction = planet.tileIsLand(tile.id) ? 0.000045 : 0.000045;
		// double friction = tile.isLand() ? 0.000045 : 0.000045;

		setWind(tile.id, Wind.prevailingWind(pgf, coriolis, friction));
	}

	private double temperatureAtLatitude(double latitude) {
		return Climate.freezingPoint() - 25 + 50 * Math.cos(latitude);
	}

	/*
	 * STATIC METHODS
	 */
	public static Season build(int id, float timeOfYear, Planet planet) {
		Season season = new Season();

		//
		season.id = id;
		season.time_of_year = timeOfYear;
		season.planet = planet;

		season.climateTiles = new float[planet.grid.tileSize()][];
		season.windTiles = new float[planet.grid.tileSize()][];

		season.solar_equator = Parameters.axial_tilt * Math.sin(2 * Math.PI * timeOfYear);
		season.tropical_equator = 0.67 * season.solar_equator;

		//
		season.setupTiles();
		season.setupTemperature();
		season.setupWind();
		season.setupHumidity();

		return season;
	}
}
