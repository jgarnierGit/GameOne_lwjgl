package models.backgroundTerrain;

import java.io.IOException;

import org.lwjglx.util.vector.Vector3f;

import camera.CameraEntity;
import entities.Entity;
import entities.GeomContainer;
import entities.SimpleEntity;
import modelsLibrary.GeomEditor;
import modelsLibrary.IEditableGeom;
import modelsLibrary.IRenderableGeom;
import modelsLibrary.SimpleGeom3D;
import modelsLibrary.SimpleGeom3DBuilder;
import modelsLibrary.terrain.RegularElevationTerrain3D;
import renderEngine.MasterRenderer;

public class BackgroundTerrain implements GeomContainer{

	SimpleGeom3D terrainGeom;
	
	private BackgroundTerrain() {
		//hidden
	}
	
	public static BackgroundTerrain create(MasterRenderer masterRenderer,CameraEntity cameraEntity, Entity entity, int size, int amplitude, String heightMap) throws IOException {
		BackgroundTerrain terrain = new BackgroundTerrain();
		BackgroundTerrainRenderer renderer = BackgroundTerrainRenderer.create(cameraEntity);
		terrain.terrainGeom =  SimpleGeom3DBuilder.create(masterRenderer.getLoader(), renderer, "backgroundTerrain").withDefaultShader().withEntity(entity).build();
		RegularElevationTerrain3D.generateRegular(terrain.terrainGeom, entity, size, amplitude, heightMap,1);
		return terrain;
	}
	
	@Override
	public IEditableGeom getEditableGeom() {
		return terrainGeom;
	}

	@Override
	public IRenderableGeom getRenderableGeom() {
		return terrainGeom;
	}

	@Override
	public GeomEditor getGeomEditor() {
		return terrainGeom.getGeomEditor();
	}

}
