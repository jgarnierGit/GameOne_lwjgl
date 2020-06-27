package models.water;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import camera.CameraEntity;
import entities.GeomContainer;
import modelsLibrary.ISimpleGeom;
import modelsLibrary.SimpleGeom3D;
import modelsLibrary.SimpleGeom3DBuilder;
import renderEngine.MasterRenderer;
import shaderManager.Draw3DShader;

public class Water implements GeomContainer{ 
	SimpleGeom3D waterGeom;
	WaterRenderer renderer;

	private Water(){
	}

	/**
	 * FIXME optimizable if needed, only x & z needed, y is calculated in
	 * frameBuffer
	 * 
	 * @param masterRenderer
	 * @param waterFrameBuffer 
	 * @param cameraEntity 
	 * @return
	 * @throws IOException 
	 */
	public static Water create(MasterRenderer masterRenderer, WaterFrameBuffer waterFrameBuffer, CameraEntity cameraEntity, String vertexFile, String fragmentFile) throws IOException {
		Water water = new Water();
		WaterShader waterShader = WaterShader.create(vertexFile, fragmentFile);
		water.renderer = WaterRenderer.create(waterFrameBuffer, waterShader, cameraEntity);
		masterRenderer.addRenderer(water.renderer);
		water.waterGeom = SimpleGeom3DBuilder.create(masterRenderer.getLoader(),  water.renderer, "water").withShader(waterShader).build();
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
		
		//TODO hide this part?
		renderer.reloadAndprocess(this.waterGeom);
		renderer.sendForRendering();
	}

	@Override
	public List<ISimpleGeom> getGeoms() {
		return Arrays.asList(waterGeom);
	}
}
