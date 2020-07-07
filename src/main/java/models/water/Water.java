package models.water;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import camera.CameraEntity;
import entities.GeomContainer;
import entities.SimpleEntity;
import models.GeomEditor;
import models.IEditableGeom;
import models.IRenderableGeom;
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
	public static Water create(MasterRenderer masterRenderer, WaterFrameBuffer waterFrameBuffer, CameraEntity cameraEntity, String vertexFile, String fragmentFile) throws IOException {
		Water water = new Water();
		WaterShader waterShader = WaterShader.create(vertexFile, fragmentFile);
		water.renderer = WaterRenderer.create(waterFrameBuffer, waterShader, cameraEntity);
		masterRenderer.addRenderer(water.renderer);
		SimpleEntity entity = new SimpleEntity(new Vector3f(0,-20,0), 0, 0, 0, 1);
		
		water.waterGeom = SimpleGeom3DBuilder.create(masterRenderer.getLoader(),  water.renderer, "water").withShader(waterShader).withEntity(entity).build();
		water.initWater();
		return water;
	}

	public void initWater() {
		if (!this.waterGeom.getVertices().isEmpty()) {
			return;
		}
		Vector3f leftNear = new Vector3f(-50, 0, -50);
		Vector3f rightNear = new Vector3f(50, 0, -50);
		Vector3f rightFar = new Vector3f(50, 0, 50);
		Vector3f leftFar = new Vector3f(-50, 0, 50);
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
	public IEditableGeom getEditableGeom() {
		return waterGeom;
	}

	@Override
	public IRenderableGeom getRenderableGeom() {
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
