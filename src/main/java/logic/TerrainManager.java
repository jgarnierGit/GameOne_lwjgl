package logic;

import java.util.ArrayList;
import java.util.List;

import models.Terrain;
import modelsLibrary.Terrain3D;
import renderEngine.MasterRenderer;

public class TerrainManager {
	List<Terrain3D> terrains = new ArrayList<>();
	MasterRenderer masterRenderer;
	
	public TerrainManager(MasterRenderer masterRenderer) {
		this.masterRenderer = masterRenderer;
	}
	
	public void updateTerrains() {
		Terrain terrain  = new Terrain(masterRenderer, "terrain", 10, 1, 0, 0, 0);
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
	
}
