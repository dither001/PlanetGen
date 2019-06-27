package graphics;

public enum Color {
	// GRAYSCALE
	JET_BLACK(0, 0, 0), DARK_GRAY(0.2f, 0.2f, 0.2f), GRAY(0.4f, 0.4f, 0.4f), //
	LIGHT_GRAY(0.6f, 0.6f, 0.6f), OFF_WHITE(0.8f, 0.8f, 0.8f), WHITE(1, 1, 1), //

	// REDS
	BLOOD_RED(0.2f, 0.1f, 0.1f), DARK_RED(0.4f, 0.1f, 0.1f), //
	RED(0.6f, 0.1f, 0.1f), LIGHT_RED(0.8f, 0.1f, 0.1f), //

	// testing
//	TESTA(0.4f, 0.4f, 0.1f), TESTB(0.6f, 0.6f, 0.1f), TESTC(0.8f, 0.8f, 0.1f), //

	// BROWNS
	RUSSET(0.4f, 0.3f, 0.1f), OCHRE(0.6f, 0.5f, 0.1f), BRONZE(0.8f, 0.6f, 0.1f), //

	// GREENS
	FOREST_GREEN(0.1f, 0.2f, 0.1f), DARK_GREEN(0.1f, 0.4f, 0.1f), //
	GREEN(0.1f, 0.6f, 0.1f), LIGHT_GREEN(0.1f, 0.8f, 0.1f), //

	// GREENISH-BLUES
	SEA_GREEN(0.1f, 0.4f, 0.3f), MINT(0.1f, 0.6f, 0.5f), JUNGLE(0.1f, 0.8f, 0.6f), //

	// "FADED JEANS" BLUES
	AQUA(0.1f, 0.3f, 0.4f), CYAN(0.1f, 0.5f, 0.6f), TEAL(0.1f, 0.6f, 0.8f), //

	// BLUES
	NAVY_BLUE(0.1f, 0.1f, 0.2f), DARK_BLUE(0.1f, 0.1f, 0.4f), //
	BLUE(0.1f, 0.1f, 0.6f), LIGHT_BLUE(0.1f, 0.1f, 0.8f), //

	// PURPLES
	PURPLE(0.4f, 0.1f, 0.3f), ORCHID(0.6f, 0.1f, 0.5f), MAGENTA(0.8f, 0.1f, 0.6f), //

	;

	/*
	 * FIELDS
	 */
	float r, g, b;

	/*
	 * CONSTRUCTORS
	 */
	Color(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/*
	 * INSTANCE METHODS
	 */
	public float[] rgb() {
		return new float[] { r, g, b };
	}

	/*
	 * STATIC METHODS
	 */
	public static float[] mix(Color a, Color b, double ratio) {
		return new float[] { //
				(float) (a.r * (1.0 - ratio) + b.r * ratio), //
				(float) (a.g * (1.0 - ratio) + b.g * ratio), //
				(float) (a.b * (1.0 - ratio) + b.b * ratio) //
		};
	}

}
