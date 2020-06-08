package launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjglx.util.vector.Vector3f;

import entities.Camera;
import entities.Player;
import inputListeners.InputListeners;
import inputListeners.KeyboardInputListener;
import inputListeners.MouseInputListener;
import inputListeners.UserInputHandler;
import logic.TerrainManager;
import models.Monkey;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class MainGame {
	public static void main(String[] args) throws IOException {
		DisplayManager.createDisplay();
		MouseInputListener mouseInputHandler = new MouseInputListener();
		KeyboardInputListener keyboardInputHandler = new KeyboardInputListener();
		List<InputListeners> inputHandlers = new ArrayList<>();
		inputHandlers.add(mouseInputHandler);
		inputHandlers.add(keyboardInputHandler);
		//TODO document the fact that we need 3D folder and 2D folder in /resources
		Camera camera = new Camera(mouseInputHandler);
		
		//TODO specify vertexShaders & FragmentShaders here.
		MasterRenderer masterRenderer = MasterRenderer.create(camera);
		Monkey monkey = new Monkey(masterRenderer);
		Player player = new Player(keyboardInputHandler, monkey, new Vector3f(-5,0,0), 0, 0, 0, 1);
		
		player.bindInputHanlder();
		camera.bindInputHanlder();
		//TODO create interface Model3D to guide user for minimal structure
		//camera.attachToEntity(player);
		
		//TODO put in there while (DisplayManager.isRunning()) { with all logic.
		GameExecutor gameExecutor = new GameExecutor();
		//InputListener inputListener = new InputListener();
		TerrainManager terrainGenerator = new TerrainManager(masterRenderer);
	//	gameExecutor.render(masterRenderer,player,terrain);
		terrainGenerator.updateTerrains();
		UserInputHandler userInputHandler = UserInputHandler.create();
		while (DisplayManager.isRunning()) {
			userInputHandler.updateUserInputs();
			for(InputListeners inputHandler : inputHandlers) {
				inputHandler.listen();
			}
			float cameraYawUpdate = (float) DisplayManager.getCurrentTime() / 400;
			cameraYawUpdate = (float) Math.sin(cameraYawUpdate)/5;
			//camera.updateYaw(cameraYawUpdate);
			player.move(terrainGenerator.getTerrains());
			masterRenderer.processEntity(player.getEntity());
			terrainGenerator.render();
			masterRenderer.render(new ArrayList<>());
			DisplayManager.updateDisplay();
		}
		for(InputListeners inputHandler : inputHandlers) {
			inputHandler.clear();
		}
		masterRenderer.cleanUp();
		DisplayManager.closeDisplay();
	}
}
