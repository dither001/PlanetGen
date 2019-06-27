package graphics;

import com.jogamp.opengl.GL2;

public final class JO {

	public static void glColor3f(GL2 gl, Color color) {
		gl.glColor3f(color.r, color.g, color.b);
	}

	public static void glColor3f(GL2 gl, float[] f) {
		gl.glColor3f(f[0], f[1], f[2]);
	}

	public static void glVertex3f(GL2 gl, float[] f) {
		gl.glVertex3f(f[0], f[1], f[2]);
	}

}
