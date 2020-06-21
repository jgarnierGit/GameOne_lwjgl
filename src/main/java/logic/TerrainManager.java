package logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import entities.Entity;
import entities.EntityContainer;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import modelsLibrary.ISimpleGeom;
import modelsLibrary.RawGeom;
import modelsLibrary.terrain.RegularFlatTerrain3D;
import modelsLibrary.terrain.Terrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;
import utils.Axis;
import utils.SpatialComparator;

public class TerrainManager extends InputInteractable implements EntityContainer {
	List<Terrain3D> terrains = new ArrayList<>();
	MasterRenderer masterRenderer;

	private TerrainManager(MasterRenderer masterRenderer, PlayerInputListener inputListener) {
		super(inputListener);
		this.masterRenderer = masterRenderer;
	}

	public static TerrainManager create(MasterRenderer masterRenderer, PlayerInputListener inputListener) {
		TerrainManager terrainManager = new TerrainManager(masterRenderer, inputListener);
		terrainManager.bindInputHanlder();
		return terrainManager;
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
		terrains.get(0).getSimpleGeom().getRenderingParameters().addEntity(new Vector3f(x, y, z), 0, 0, 0, 1);
		prepareForRender();
	}

	public void initiateTerrain() throws IOException {
		if(!terrains.isEmpty()) {
			return;
		}
		RegularFlatTerrain3D terrain = RegularFlatTerrain3D.generateRegular(masterRenderer, "terrain", 10, 0, 0, 0);
		setupTerrain(terrain);
		terrains.add(terrain);
		prepareForRender();
	}

	public List<Terrain3D> getTerrains() {
		return this.terrains;
	}

	public void prepareForRender() { // TODO try to automate this part.
		for (Terrain3D terrain : terrains) {
			masterRenderer.reloadAndprocess(terrain.getSimpleGeom());
		}
		masterRenderer.sendForRendering();
	}

	private void setupTerrain(Terrain3D terrain) {
		RenderingParameters terrainParameters = terrain.getSimpleGeom().getRenderingParameters();
		// TODO hide from this interface.
		// terrainParameters.disableRenderOptions();
		terrainParameters.setRenderMode(GL11.GL_TRIANGLES);
		terrain.getSimpleGeom().invertNormals();
	}

	//TODO extract in GeomContainer.
	@Override
	public List<ISimpleGeom> getEntitiesGeom() {
		List<ISimpleGeom> geoms = new ArrayList<>();
		geoms.addAll(this.terrains.stream().map(Terrain3D::getSimpleGeom).collect(Collectors.toList()));
		return geoms;
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
				terrain.getSimpleGeom().getRenderingParameters().getEntities());
		List<Vector3f> geomVertices = terrain.getSimpleGeom().buildVerticesList();
		
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
