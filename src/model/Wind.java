package model;

import com.jogamp.opengl.math.VectorUtil;

import controller.Matrix2;

public class Wind {
	float direction;
	float speed;

	/*
	 * CONSTRUCTORS
	 */
	private Wind() {
		direction = 0;
		speed = 0;
	}

	private Wind(float direction, float speed) {
		this.direction = direction;
		this.speed = speed;
	}

	/*
	 * INSTANCE METHODS
	 */
	@Override
	public boolean equals(Object o) {
		boolean equals = false;

		if (o.equals(this))
			return true;

		if (o.getClass().equals(Wind.class)) {
			Wind w = (Wind) o;

			int d1 = (int) (this.direction), d2 = (int) (w.direction);
			int s1 = (int) (this.speed), s2 = (int) (w.speed);

			if (d1 == d2 && s1 == s2)
				equals = true;
		}

		return equals;
	}

	/*
	 * STATIC METHODS
	 */

	/*
	 * Prevailing Wind
	 * 
	 * @param pressure gradient force, Coriolis coefficient, friction coefficient
	 */
	public static Wind prevailingWind(float[] pgf, double cc, double fc) {
		// calculate wind direction
		double angleOffset = Math.atan2(cc, fc);
		Matrix2 m = Matrix2.rotationMatrix(Math.atan2(pgf[1], pgf[0]) - angleOffset);
		float[] v = m.mulByVec2(new float[] { 1.0f, 0.0f });
		float direction = (float) Math.atan2(v[1], v[0]);

		// calculate wind speed
		float[] divisor = new float[] { (float) cc, (float) fc };
		float speed = VectorUtil.normVec2(pgf) / VectorUtil.normVec2(divisor);

		return new Wind(direction, speed);
	}
}
