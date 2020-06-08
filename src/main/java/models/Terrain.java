package models;

import org.lwjgl.opengl.GL11;

import modelsLibrary.RegularFlatTerrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;

//TODO try to use more composition
public class Terrain extends RegularFlatTerrain3D {

	public Terrain(MasterRenderer masterRenderer, String alias,int size, int definition, int x, int z, float elevation) {
		super(masterRenderer.getLoader(), masterRenderer.get3DRenderer(), alias, size, definition, x, z, elevation);
		setupTerrain();
	}


	private void setupTerrain() {
		RenderingParameters terrainParameters = this.getRenderingParameters();
		// TODO hide from this interface.
		//terrainParameters.disableRenderOptions();
		terrainParameters.doNotUseEntities();
		terrainParameters.setRenderMode(GL11.GL_TRIANGLES);
		//this.invertNormals();
	}

}
