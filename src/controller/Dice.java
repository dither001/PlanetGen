package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Dice {
	/*
	 * STATIC FIELDS
	 */
	private static final Random RAND = new Random();

	/*
	 * STATIC METHODS
	 */
	public static int nextInt(int start, int end) {
		return RAND.nextInt(end - start) + start;
	}

	public static int roll(int faces) {
		return roll(1, faces);
	}

	public static int roll(int dice, int faces) {
		int result = 0;

		dice = (dice < 1) ? 1 : dice;
		faces = (faces < 1) ? 1 : faces;

		for (int i = 0; i < dice; ++i) {
			result += RAND.nextInt(faces) + 1;
		}

		return result;
	}

	public static int[] roll3d6InOrder() {
		int[] array = new int[] { 0, 0, 0, 0, 0, 0 };
		for (int i = 0; i < array.length; ++i) {
			array[i] += roll(3, 6);
		}

		return array;
	}

	/*
	 * 
	 */
	public static List<float[][]> elevationVectors(int n) {
		List<float[][]> list = new ArrayList<float[][]>(n);

		for (int i = 0; i < n; ++i) {
			list.add(new float[][] { //
					uniformPoint(), //
					uniformPoint(), //
					uniformPoint() //
			});
		}

		return list;
	}

	/*
	 * Returns random point on a unit sphere (uniform distribution).
	 */
	private static float[] uniformPoint() {
		double theta = 2 * Math.PI * RAND.nextDouble();
		double phi = Math.acos(1 - 2 * RAND.nextDouble());
		float x = (float) (Math.sin(phi) * Math.cos(theta));
		float y = (float) (Math.sin(phi) * Math.sin(theta));
		float z = (float) (Math.cos(phi));

		return new float[] { x, y, z };
	}

	public static String printVector3(float[] array) {
		return String.format("[%s, %s, %s]", array[0], array[1], array[2]);
	}

}
