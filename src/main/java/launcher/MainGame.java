package launcher;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjglx.util.vector.Vector3f;

import inputListeners.PlayerInputListener;
import inputListeners.PlayerInputListenerBuilder;
import logic.CameraLogic;
import logic.Player;
import logic.TerrainManager;
import models.Monkey;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class MainGame {
	public static void main(String[] args) throws IOException {
		DisplayManager.createDisplay();
		PlayerInputListener playerInputListener = PlayerInputListenerBuilder.create().addMouseInputListener()
				.addKeyboardInputListener().build();
		//TODO document the fact that we need 3D folder and 2D folder in /resources
		CameraLogic camera = CameraLogic.create(playerInputListener);
		
		//TODO specify vertexShaders & FragmentShaders here.
		MasterRenderer masterRenderer = MasterRenderer.create(camera.getCamera());
		Monkey monkey = new Monkey(masterRenderer);
		Player player = Player.create(playerInputListener, monkey, new Vector3f(-5,0,0), 0, 0, 0, 1);
		
		//TODO create interface Model3D to guide user for minimal structure
		camera.attachToEntity(player.getEntity());
		
		//TODO put in there while (DisplayManager.isRunning()) { with all logic.
		GameExecutor gameExecutor = new GameExecutor();
		//InputListener inputListener = new InputListener();
		TerrainManager terrainGenerator = TerrainManager.create(masterRenderer, playerInputListener);
	//	gameExecutor.render(masterRenderer,player,terrain);
		terrainGenerator.initiateTerrain();
		
		float deltaTime = 0;
		while (DisplayManager.isRunning()) {
			deltaTime += DisplayManager.getFrameTimeSeconds();
			if(deltaTime > 2) {
				terrainGenerator.addTerrain();
				deltaTime = 0;
				camera.centerOverEntities(terrainGenerator.getEntitiesGeom());
			}
			playerInputListener.update();

			float cameraYawUpdate = (float) DisplayManager.getCurrentTime() / 400;
			cameraYawUpdate = (float) Math.sin(cameraYawUpdate)/5;
			//camera.updateYaw(cameraYawUpdate);
			player.move(terrainGenerator.getTerrains());
			camera.freeFly();
			masterRenderer.processEntity(player.getEntity());
			masterRenderer.render(new ArrayList<>());
			DisplayManager.updateDisplay();
		}
		playerInputListener.clear();
		camera.cleanEntities();
		masterRenderer.cleanUp();
		DisplayManager.closeDisplay();
	}
}
