package models;

import org.lwjgl.opengl.GL11;

import modelsLibrary.RegularFlatTerrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;

public class Terrain extends RegularFlatTerrain3D {

	public Terrain(MasterRenderer masterRenderer, String alias) {
		super(masterRenderer.getLoader(), masterRenderer.get3DRenderer(), alias, 10, 2, 0, 0, 0);
		setupTerrain();
	}


	private void setupTerrain() {
		RenderingParameters terrainParameters = this.getRenderingParameters();
		// TODO hide from this interface.
		//terrainParameters.disableRenderOptions();
		terrainParameters.doNotUseEntities();
		terrainParameters.setRenderMode(GL11.GL_POINTS);
		//this.invertNormals();
	}

}
