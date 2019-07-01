package com.planet.atmosphere;

import controller.Parameters;
import model.Planet;

public class Climate {

	Planet planet;
	Season[] seasons;

	/*
	 * CONSTRUCTORS
	 */
	private Climate() {
		seasons = new Season[0];
	}

	/*
	 * 
	 */
	public Season getSeason(int id) {
		return seasons[id];
	}

	/*
	 * PRIVATE METHODS
	 */
	private void generateSeasons() {
		//
		for (int i = 0; i < seasons.length; ++i) {
			float t = i / seasons.length;
			seasons[i] = Season.build(i, t, planet);
		}
	}

	/*
	 * STATIC METHODS
	 */
	public static Climate build(Planet planet) {
		Climate climate = new Climate();
		climate.planet = planet;

		// clear_climate(planet);
		// m_terrain(planet).var.axial_tilt = par.axial_tilt;

		// m_climate(planet).var.season_count = par.seasons;
		// generate_season(planet, par, (float)i/par.seasons);
		climate.seasons = new Season[Parameters.seasons];
		climate.generateSeasons();

		return climate;
	}

	public static float freezingPoint() {
		return 273.15f;
	}

	public static float temperatureLapseRate() {
		return 9.8e-3f;
	}

	// LAPSE OF ELEVATION
	public static float temperatureLapse(float d) {
		return d * temperatureLapseRate();
	}

	// SATURATION AT TEMPERATURE
	public static float saturationHumidity(double temperature) {
		double c = 4.6e-9;
		double k = 0.05174;

		return (float) (c * Math.pow(k, temperature));
	}

	// ARIDITY AT TEMPERATURE
	public static float aridity(float potential_evapotranspiration) {
		float index_base_temperature = 10 + freezingPoint();

		return potential_evapotranspiration / saturationHumidity(index_base_temperature);
	}

}
