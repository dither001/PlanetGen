package api;

public enum ViewType {
	ELEVATION(11), VEGETATION(12), TEMPERATURE(13), ARIDITY(14), HUMIDITY(15), PRECIPITATION(16),
	// new and utility map schemes
	REGION(21), LATITUDE(22) //
	;

	/*
	 * FIELDS
	 */
	private static ViewType[] VIEWS;

	static {
		VIEWS = new ViewType[] { ELEVATION, VEGETATION, TEMPERATURE, ARIDITY, HUMIDITY, PRECIPITATION };
	}

	public int id;

	/*
	 * CONSTRUCTORS
	 */
	ViewType(int id) {
		this.id = id;
	}

	/*
	 * STATIC METHODS
	 */
	public static ViewType get(int id) {
		for (ViewType el : VIEWS) {
			if (el.id == id)
				return el;
		}

		return null;
	}
}
