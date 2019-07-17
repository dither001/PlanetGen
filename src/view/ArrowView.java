package view;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.VectorUtil;

import graphics.Color;
import graphics.JO;

public class ArrowView implements GLEventListener {

	float[][] vertices;

	// buildArrow(geometry, position, direction, normal, baseWidth, color)
	// {
	// if (direction.lengthSq() === 0) return;
	// var sideOffset = direction.clone().cross(normal).setLength(baseWidth / 2);
	// var baseIndex = geometry.vertices.length;
	// geometry.vertices.push(position.clone().add(sideOffset),
	// position.clone().add(direction), position.clone().sub(sideOffset));
	// geometry.faces.push(new THREE.Face3(baseIndex, baseIndex + 2, baseIndex + 1,
	// normal, [ color, color, color ]));
	// }

	public ArrowView(float[] position, float[] direction, float[] normal, float baseWidth) {
		// XXX - WTF
		if (VectorUtil.isVec3Zero(direction, 0, FloatUtil.EPSILON))
			return;

		float[] sideOffset = VectorUtil.crossVec3(new float[3], direction, normal);
		VectorUtil.normalizeVec3(sideOffset);
		VectorUtil.scaleVec3(sideOffset, sideOffset, baseWidth / 2);

		vertices = new float[3][];
		// position.clone().add(sideOffset)
		vertices[0] = VectorUtil.addVec3(new float[3], position, sideOffset);
		// position.clone().add(direction)
		vertices[1] = VectorUtil.addVec3(new float[3], position, direction);
		// position.clone().sub(sideOffset)
		vertices[2] = VectorUtil.subVec3(new float[3], position, sideOffset);

	}

	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();

		gl.glBegin(GL2.GL_TRIANGLE_FAN);
		JO.glColor3f(gl, Color.OFF_WHITE);

		JO.glVertex3f(gl, vertices[0]);
		JO.glVertex3f(gl, vertices[1]);
		JO.glVertex3f(gl, vertices[2]);
		// finish triangle at starting point
		JO.glVertex3f(gl, vertices[0]);

		gl.glEnd();
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}

}
