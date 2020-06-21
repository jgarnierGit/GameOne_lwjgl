package launcher;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjglx.util.vector.Vector3f;

import camera.behavior.CameraFreeFly;
import inputListeners.PlayerInputListener;
import inputListeners.PlayerInputListenerBuilder;
import logic.CameraCenterOverEntities;
import logic.CameraManager;
import logic.Player;
import logic.TerrainManager;
import models.Monkey;
import models.Water;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class MainGame {
	public static void main(String[] args) throws IOException {
		DisplayManager.createDisplay();
		PlayerInputListener playerInputListener = PlayerInputListenerBuilder.create().addMouseInputListener()
				.addKeyboardInputListener().build();
		// TODO document the fact that we need 3D folder and 2D folder in /resources
		CameraManager camera = CameraManager.create(playerInputListener, new Vector3f(-20, 20, 50), 20, 45);

		// TODO specify vertexShaders & FragmentShaders here.
		MasterRenderer masterRenderer = MasterRenderer.create(camera.getCamera());
		Monkey monkey = new Monkey(masterRenderer);
		Player player = Player.create(playerInputListener, monkey, new Vector3f(-5, 0, 0), 0, 90, 0, 1);
		Water water = Water.create(masterRenderer, "waterVertexShader.txt", "waterFragmentShader.txt");
		water.initWater();
		// TODO create interface Model3D to guide user for minimal structure

		// TODO put in there while (DisplayManager.isRunning()) { with all logic.
		GameExecutor gameExecutor = new GameExecutor();
		// InputListener inputListener = new InputListener();
		TerrainManager terrainGenerator = TerrainManager.create(masterRenderer, playerInputListener);
		// gameExecutor.render(masterRenderer,player,terrain);
		terrainGenerator.initiateTerrain();

		float deltaTime = 0;
		// FIXME weird to use getter to create cameraBehavior, and maybe not have to use
		// it.
		// CameraCenterOverEntities cameraLogic = camera.getCenterOverEntitiesCamera();
		camera.getFreeFlyCamera();
		camera.getCameraLockedToEntity(player.getEntity());
		while (DisplayManager.isRunning()) {
			playerInputListener.update();
			camera.update();
			// FIXME maybe it was a mistake to update once per frame viewMatrix?
			// need to find a way to update it once every transformation on camera are done.
			// (includes InputListener / gameLogic...)
			// some inputListener need viewMatrix value. (aka freeFlyCamera.)
			camera.updateViewMatrix();

			deltaTime += DisplayManager.getFrameTimeSeconds();
			if (deltaTime > 2) {
				// charge la donnée en mémoire au fur et à mesure des instanciations. peut-être le faire une fois juste avant le rendu.
				terrainGenerator.addTerrain();
				deltaTime = 0;
				if (camera.getActiveCameraBehavior() instanceof CameraCenterOverEntities) {
					// cameraLogic.centerOverEntities(terrainGenerator.getEntitiesGeom());
				}
			}

			float cameraYawUpdate = (float) DisplayManager.getCurrentTime() / 400;
			cameraYawUpdate = (float) Math.sin(cameraYawUpdate) / 5;
			// camera.updateYaw(cameraYawUpdate);
			player.move(terrainGenerator, camera);
			masterRenderer.processEntity(player.getEntity());
			masterRenderer.render(new ArrayList<>());
			DisplayManager.updateDisplay();
		}
		playerInputListener.clear();
		masterRenderer.cleanUp();
		DisplayManager.closeDisplay();
	}
}
