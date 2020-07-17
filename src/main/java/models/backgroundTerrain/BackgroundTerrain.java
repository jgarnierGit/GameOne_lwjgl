package models.backgroundTerrain;

import java.io.IOException;
import java.util.Optional;

import camera.CameraEntity;
import entities.Entity;
import entities.GeomContainer;
import models.GeomEditor;
import models.EditableGeom;
import models.RenderableGeom;
import models.SimpleGeom3D;
import models.SimpleGeom3DBuilder;
import models.data.BlendedMaterialLibraryBuilder;
import models.data.MaterialLibrary;
import models.library.terrain.RegularElevationTerrain3D;
import renderEngine.MasterRenderer;

public class BackgroundTerrain implements GeomContainer{

	SimpleGeom3D terrainGeom;
	
	private BackgroundTerrain() {
		//hidden
	}
	
	public static BackgroundTerrain create(MasterRenderer masterRenderer,CameraEntity cameraEntity, Entity entity, int size, int amplitude, String heightMap) throws IOException {
		BackgroundTerrain terrain = new BackgroundTerrain();
		TerrainBackgroundShader shader = TerrainBackgroundShader.create();
		BackgroundTerrainRenderer renderer = BackgroundTerrainRenderer.create(cameraEntity,shader);
		//TODO add OBJContent directly to SimpleGeom3DBuilder ? maybe hard to adapt for terrain...
		terrain.terrainGeom =  SimpleGeom3DBuilder.create(masterRenderer, renderer, "backgroundTerrain").withShader(shader).withEntity(entity).build();
		MaterialLibrary mtlLibrary = BlendedMaterialLibraryBuilder.create().addTexture("grass.png").addTexture("mud.png")
				.addTexture("grassFlowers.png").addTexture("path.png").addBlendTexturesAndBuild("blendMap.png");
		RegularElevationTerrain3D.generateAndLoadRegular(terrain.terrainGeom, Optional.of(mtlLibrary), entity, size, amplitude, heightMap);
		return terrain;
	}
	
	@Override
	public EditableGeom getEditableGeom() {
		return terrainGeom;
	}

	@Override
	public RenderableGeom getRenderableGeom() {
		return terrainGeom;
	}

	@Override
	public GeomEditor getGeomEditor() {
		return terrainGeom.getGeomEditor();
	}

}
