package view;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

public class GlobePanel extends GLCanvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GlobePanel(GLCapabilities capabilities) {
		super(capabilities);
		// GLCanvas glcanvas = new GLCanvas(capabilities);
		// glcanvas.addGLEventListener(createGlobeView());
		this.setSize(400, 400);

	}

}
