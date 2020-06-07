package launcher;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjglx.util.vector.Vector3f;

import entities.Camera;
import entities.Player;
import entities.UserInputHandler;
import models.Monkey;
import models.Terrain;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;

public class MainGame {
	public static void main(String[] args) throws IOException {
		DisplayManager.createDisplay();
		UserInputHandler.updateInputHandler();
		//TODO document the fact that we need 3D folder and 2D folder in /resources
		Camera camera = new Camera();
		//TODO specify vertexShaders & FragmentShaders here.
		MasterRenderer masterRenderer = MasterRenderer.create(camera);
		Monkey monkey = new Monkey(masterRenderer);
		Player player = new Player(monkey, new Vector3f(-5,0,0), 0, 0, 0, 1);
		//TODO create interface Model3D to guide user for minimal structure
		//camera.attachToEntity(player);
		Terrain terrain  = new Terrain(masterRenderer, "terrain");
		//TODO put in there while (DisplayManager.isRunning()) { with all logic.
		GameExecutor gameExecutor = new GameExecutor();
		//InputListener inputListener = new InputListener();
		masterRenderer.reloadAndprocess(terrain);
		masterRenderer.sendForRendering(); //TODO hide this part as possible
	//	gameExecutor.render(masterRenderer,player,terrain);
		
		while (DisplayManager.isRunning()) {
			float cameraYawUpdate = (float) DisplayManager.getCurrentTime() / 400;
			cameraYawUpdate = (float) Math.sin(cameraYawUpdate)/5;
			camera.updateYaw(cameraYawUpdate);
			player.move(terrain);
			masterRenderer.processEntity(player);

			masterRenderer.render(new ArrayList<>());
			DisplayManager.updateDisplay();
		}
		masterRenderer.cleanUp();
		DisplayManager.closeDisplay();
	}
}
