package models.water;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import modelsLibrary.SimpleGeom3D;
import modelsLibrary.SimpleGeom3DBuilder;
import renderEngine.MasterRenderer;
import shaderManager.ShaderProgram;

public class Water { 
	SimpleGeom3D waterGeom;
	MasterRenderer masterRenderer;

	private Water(){
	}

	/**
	 * FIXME optimizable if needed, only x & z needed, y is calculated in
	 * frameBuffer
	 * 
	 * @param masterRenderer
	 * @return
	 * @throws IOException 
	 */
	public static Water create(MasterRenderer masterRenderer, String vertexFile, String fragmentFile) throws IOException {
		Water water = new Water();
		water.waterGeom = SimpleGeom3DBuilder.create(masterRenderer.getLoader(),  masterRenderer.get3DRenderer(), "water").withShader(Water.class::getResourceAsStream,vertexFile, fragmentFile).build();
		water.masterRenderer = masterRenderer;
		return water;
	}

	public void initWater() {
		if (!this.waterGeom.buildVerticesList().isEmpty()) {
			return;
		}
		Vector3f leftNear = new Vector3f(-50, -20, -50);
		Vector3f rightNear = new Vector3f(50, -20, -50);
		Vector3f rightFar = new Vector3f(50, -20, 50);
		Vector3f leftFar = new Vector3f(-50, -20, 50);
		//TODO addPoint must be unique, and add setFaces which will use indices vertices.
		this.waterGeom.addPoint(leftNear);
		this.waterGeom.addPoint(rightFar);
		this.waterGeom.addPoint(rightNear);
		

		this.waterGeom.addPoint(leftNear);
		this.waterGeom.addPoint(leftFar);
		this.waterGeom.addPoint(rightFar);
		
		this.waterGeom.getRenderingParameters().setRenderMode(GL11.GL_TRIANGLES);
		prepareForRender();
	}

	public void prepareForRender() { // TODO try to automate this part.
		masterRenderer.reloadAndprocess(this.waterGeom);
		masterRenderer.sendForRendering();
	}
}
