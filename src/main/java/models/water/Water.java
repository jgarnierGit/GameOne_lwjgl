package models.water;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import camera.CameraEntity;
import entities.GeomContainer;
import entities.Light;
import entities.SimpleEntity;
import models.GeomEditor;
import models.EditableGeom;
import models.RenderableGeom;
import models.SimpleGeom3D;
import models.SimpleGeom3DBuilder;
import renderEngine.MasterRenderer;

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
	public static Water create(MasterRenderer masterRenderer, WaterFrameBuffer waterFrameBuffer, CameraEntity cameraEntity, Light sun, String vertexFile, String fragmentFile) throws IOException {
		Water water = new Water();
		WaterShader waterShader = WaterShader.create(vertexFile, fragmentFile);
		int dudv = masterRenderer.getLoader().loadTexture("waterDUDV.png");
		int normalMap = masterRenderer.getLoader().loadTexture("normalMap.png");
		water.renderer = WaterRenderer.create(waterFrameBuffer, waterShader, cameraEntity, dudv, normalMap, sun);
		SimpleEntity entity = new SimpleEntity(new Vector3f(-50,-20,-50), 0, 0, 0, 100);
		
		water.waterGeom = SimpleGeom3DBuilder.create(masterRenderer,  water.renderer, "water").withShader(waterShader).withEntity(entity).build();
		/**SimpleMaterialLibrary materials = SimpleMaterialLibrary.create("waterDUDV.png");
		water.waterGeom.getObjContent().setMaterials(materials); //we can add texture without any uv... i can propose a simple uv but only for square.. and i don't want to reimplement a uv mapper;
		//try to not use uv...
		water.waterGeom.getVAOGeom().loadTextures();**/
		water.initWater();
		return water;
	}

	public void initWater() {
		if (!this.waterGeom.getVertices().isEmpty()) {
			return;
		}
		Vector3f leftNear = new Vector3f(0, 0, 0);
		Vector3f rightNear = new Vector3f(1, 0, 0);
		Vector3f rightFar = new Vector3f(1, 0, 1);
		Vector3f leftFar = new Vector3f(0, 0, 1);
		//TODO addPoint must be unique, and add setFaces which will use indices vertices.
		this.waterGeom.addPoint(leftNear);
		this.waterGeom.addPoint(rightFar);
		this.waterGeom.addPoint(rightNear);
		

		this.waterGeom.addPoint(leftNear);
		this.waterGeom.addPoint(leftFar);
		this.waterGeom.addPoint(rightFar);
		
		this.waterGeom.getRenderingParameters().setRenderMode(GL11.GL_TRIANGLES);
		renderer.reloadGeomToVAO(this.waterGeom);
	}

	@Override
	public EditableGeom getEditableGeom() {
		return waterGeom;
	}

	@Override
	public RenderableGeom getRenderableGeom() {
		return waterGeom;
	}

	/**
	 * TODO hide this...
	 */
	@Override
	public GeomEditor getGeomEditor() {
		return waterGeom.getGeomEditor();
	}
}
