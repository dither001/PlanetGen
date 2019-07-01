package view;

/*
 * GlobePanel is ultimately redundant with PlanetView from vraid's earthgen. 
 * I'll need to consolidate the objects when I have a chance.
 * 
 * Nick Foster
 * 6/26/2019
 */

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.awt.GLCanvas;

public class GlobeView extends GLCanvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final double DEFAULT_SIZE = 600;

	public GlobeView(GLCapabilities capabilities) {
		super(capabilities);
		// GLCanvas glcanvas = new GLCanvas(capabilities);
		// glcanvas.addGLEventListener(createGlobeView());
		this.setSize(480, 480);

	}

}
