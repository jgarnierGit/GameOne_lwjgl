package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import camera.CameraEntity;
import entities.Entity;
import entities.SimpleEntity;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import models.backgroundTerrain.BackgroundTerrain;
import modelsLibrary.SimpleGeom3D;
import modelsLibrary.SimpleGeom3DBuilder;
import modelsLibrary.terrain.RegularFlatTerrain3D;
import modelsLibrary.terrain.Terrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;
import utils.Axis;
import utils.SpatialComparator;

public class TerrainManager extends InputInteractable {
	List<Terrain3D> terrains = new ArrayList<>();
	BackgroundTerrain groundTerrain;
	MasterRenderer masterRenderer;

	private TerrainManager(MasterRenderer masterRenderer, PlayerInputListener inputListener) {
		super(inputListener);
		this.masterRenderer = masterRenderer;
	}

	public static TerrainManager create(MasterRenderer masterRenderer, PlayerInputListener inputListener, CameraEntity cameraEntity) throws IOException {
		TerrainManager terrainManager = new TerrainManager(masterRenderer, inputListener);
		terrainManager.bindInputHanlder();
		terrainManager.initiateTerrain();
	//	terrainManager.initiateGroundTerrain(cameraEntity);
		return terrainManager;
	}

	private void initiateGroundTerrain(CameraEntity cameraEntity) throws IOException {
		if(groundTerrain != null) {
			return;
		}
		SimpleEntity entity = new SimpleEntity(new Vector3f(-50,-20,-50), 0, 0, 0, 1);
		groundTerrain = BackgroundTerrain.create(masterRenderer, cameraEntity, entity, 100, 30,"heightmap.png");
	}

	@Override
	public void bindInputHanlder() {
		this.inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnUniquePress(GLFW.GLFW_KEY_SPACE, this::addTerrain);
		});
	}

	@Override
	public void unbindInputHanlder() {
		// nothing to unbind
	}

	public void addTerrain() {
		float x = (float) ThreadLocalRandom.current().nextDouble(0, 100);
		float y = (float) ThreadLocalRandom.current().nextDouble(-10, 30); // elevation
		float z = (float) ThreadLocalRandom.current().nextDouble(-30, 30);
		terrains.get(0).getRenderableGeom().getRenderingParameters().addEntity(new Vector3f(x, y, z), 0, 0, 0, 1);
		prepareForRender();
	}

	private void initiateTerrain() throws IOException {
		if(!terrains.isEmpty()) {
			return;
		}
		SimpleEntity entity = new SimpleEntity(new Vector3f(0, 0, 0), 0, 0, 0, 1);
		SimpleGeom3D terrainGeom =  SimpleGeom3DBuilder.create(masterRenderer.getLoader(), masterRenderer.get3DRenderer(), "terrain").withDefaultShader().withEntity(entity).build();
		RegularFlatTerrain3D terrain = RegularFlatTerrain3D.generateRegular(terrainGeom, entity, 10);
		setupTerrain(terrain);
		terrains.add(terrain);
		prepareForRender();
	}

	public List<Terrain3D> getTerrains() {
		return this.terrains;
	}

	public void prepareForRender() { // TODO try to automate this part. try using visitor
		for (Terrain3D terrain : terrains) {
			masterRenderer.reloadAndprocess(terrain.getRenderableGeom());
		}
		masterRenderer.sendForRendering();
	}

	private void setupTerrain(Terrain3D terrain) {
		RenderingParameters terrainParameters = terrain.getRenderableGeom().getRenderingParameters();
		// TODO hide from this interface.
		// terrainParameters.disableRenderOptions();
		terrainParameters.setRenderMode(GL11.GL_TRIANGLES);
		terrain.getEditableGeom().invertNormals();
	}

	/**
	 * returns each intersecting Entity with position.
	 * TODO create intersection method in SpatialComparator
	 * @param positions
	 * @return
	 */
	public List<Entity> getActiveTerrain(Vector3f position, Terrain3D terrain) {
		List<Entity> intersectedTerrains = new ArrayList<>();
		Optional<Float> terrainHeight = terrain.getHeight(position);
		if (!terrainHeight.isPresent()) {
			return intersectedTerrains;
		}
		Float terrainMeasure = terrainHeight.get();

		List<Entity> terrainEntities = SpatialComparator.filterEntitiesByValueEquality(terrainMeasure, Axis.Y,
				terrain.getRenderableGeom().getRenderingParameters().getEntities());
		List<Vector3f> geomVertices = (List<Vector3f>) terrain.getRenderableGeom().getVertices();
		
		for (Entity entity : terrainEntities) {
			if(testItersectionWithEntity(entity, geomVertices)) {
				intersectedTerrains.add(entity);
			}
		}
		return intersectedTerrains;
	}
	
	public boolean testItersectionWithEntity(Entity entity, List<Vector3f> geomVertices) {
		Vector3f geomNearLeft = geomVertices.get(0);
		Vector3f geomFarRight = geomVertices.get(geomVertices.size() - 1);
		Vector3f worldNearLeft = Vector3f.add(entity.getPositions(), geomNearLeft, null);
		Vector3f worldFarRight = Vector3f.add(entity.getPositions(), geomFarRight, null);
		return entity.getPositions().x >= worldNearLeft.x && entity.getPositions().x <= worldFarRight.x
				&& entity.getPositions().z >= worldNearLeft.z && entity.getPositions().z <= worldFarRight.z;
	}

}
