package api;

public enum LandType {
	LAND, WATER, COAST;

	/*
	 * 
	 */
	public boolean isCoast() {
		return this.equals(COAST);
	}

	public boolean isLand() {
		return this.equals(LAND);
	}

	public boolean isWater() {
		return this.equals(WATER);
	}

	/*
	 * STATIC METHODS
	 */
	public static LandType get(int i) {
		if (i == 1)
			return LAND;
		else if (i == 2)
			return WATER;
		else if (i == 4)
			return COAST;

		return null;
	}
}
