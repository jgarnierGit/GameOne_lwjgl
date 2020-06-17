package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector3f;

import entities.EntityContainer;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import modelsLibrary.RegularFlatTerrain3D;
import modelsLibrary.SimpleGeom;
import modelsLibrary.Terrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;

public class TerrainManager extends InputInteractable implements EntityContainer{
	List<Terrain3D> terrains = new ArrayList<>();
	MasterRenderer masterRenderer;
	Random random;
	
	private TerrainManager(MasterRenderer masterRenderer,PlayerInputListener inputListener) {
		super(inputListener);
		this.masterRenderer = masterRenderer;
		random = new Random();
	}
	
	public static TerrainManager create(MasterRenderer masterRenderer,PlayerInputListener inputListener) {
		TerrainManager terrainManager  = new TerrainManager( masterRenderer, inputListener);
		terrainManager.bindInputHanlder();
		return terrainManager;
	}
	
	@Override
	public void bindInputHanlder() {
		this.inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnUniquePress(GLFW.GLFW_KEY_SPACE, this::addTerrain);
		});
		
	}
	
	public void addTerrain() {
		float x = random.nextFloat() * 50;
		float y = random.nextFloat() * 100; //elevation
		float z = random.nextFloat() * 50;
		terrains.get(0).getRenderingParameters().addEntity(new Vector3f(x,y,z), 0, 0, 0, 1);
		prepareForRender();
	}

	public void initiateTerrain() {
		RegularFlatTerrain3D terrain = RegularFlatTerrain3D.generateRegular(masterRenderer, "terrain", 10, 0, 0, 0);
		setupTerrain(terrain);
		terrains.add(terrain);
		prepareForRender();
	}
	
	public List<Terrain3D> getTerrains(){
		return this.terrains;
	}
	
	public void prepareForRender() { //TODO try to automate this part.
		for(Terrain3D terrain : terrains) {
			masterRenderer.reloadAndprocess(terrain);
		}
		masterRenderer.sendForRendering();
	}
	
	private void setupTerrain(Terrain3D terrain) {
		RenderingParameters terrainParameters = terrain.getRenderingParameters();
		// TODO hide from this interface.
		//terrainParameters.disableRenderOptions();
		terrainParameters.setRenderMode(GL11.GL_TRIANGLES);
		//this.invertNormals();
	}

	@Override
	public List<SimpleGeom> getEntitiesGeom() {
		List<SimpleGeom> geoms = new ArrayList<>();
		geoms.addAll(this.terrains);
		return geoms;
	}
	
}
