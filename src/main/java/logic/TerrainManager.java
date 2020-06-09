package logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import inputListeners.InputInteractable;
import inputListeners.InputListeners;
import modelsLibrary.RegularFlatTerrain3D;
import modelsLibrary.Terrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;

public class TerrainManager extends InputInteractable{
	List<Terrain3D> terrains = new ArrayList<>();
	MasterRenderer masterRenderer;
	Random random;
	
	public TerrainManager(MasterRenderer masterRenderer,InputListeners inputListener) {
		super(inputListener);
		this.masterRenderer = masterRenderer;
		random = new Random();
	}
	
	@Override
	public void bindInputHanlder() {
		this.inputListener.addRunnerOnUniquePress(GLFW.GLFW_KEY_S, () -> addTerrain());
		
	}
	
	private void addTerrain() {
		float z = random.nextFloat() * 10;
		RegularFlatTerrain3D terrain = RegularFlatTerrain3D.generateRegular(masterRenderer, "terrain", 10, 0, 0, z);
		setupTerrain(terrain);
		terrains.add(terrain);
	}

	public void initiateTerrain() {
		RegularFlatTerrain3D terrain = RegularFlatTerrain3D.generateRegular(masterRenderer, "terrain", 10, 0, 0, 0);
		setupTerrain(terrain);
		terrains.add(terrain);
	}
	
	public List<Terrain3D> getTerrains(){
		return this.terrains;
	}
	
	public void render() {
		for(Terrain3D terrain : terrains) {
			masterRenderer.reloadAndprocess(terrain);
		}
		masterRenderer.sendForRendering();
	}
	
	private void setupTerrain(Terrain3D terrain) {
		RenderingParameters terrainParameters = terrain.getRenderingParameters();
		// TODO hide from this interface.
		//terrainParameters.disableRenderOptions();
		terrainParameters.doNotUseEntities();
		terrainParameters.setRenderMode(GL11.GL_TRIANGLES);
		//this.invertNormals();
	}
	
}
