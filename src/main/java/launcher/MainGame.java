package launcher;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Vector2f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import camera.behavior.CameraFreeFly;
import entities.GuiTexture;
import inputListeners.PlayerInputListener;
import inputListeners.PlayerInputListenerBuilder;
import logic.CameraCenterOverEntities;
import logic.CameraManager;
import logic.Player;
import logic.TerrainManager;
import models.Monkey;
import models.water.Water;
import models.water.WaterFrameBuffer;
import renderEngine.DisplayManager;
import renderEngine.GuiRenderer;
import renderEngine.MasterRenderer;

public class MainGame {
	public static void main(String[] args) throws IOException {
		DisplayManager.createDisplay();
		PlayerInputListener playerInputListener = PlayerInputListenerBuilder.create().addMouseInputListener()
				.addKeyboardInputListener().build();
		// TODO document the fact that we need 3D folder and 2D folder in /resources
		CameraManager camera = CameraManager.create(playerInputListener, new Vector3f(-20, 20, 50), 20, 45);

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
		WaterFrameBuffer waterFrameBuffer = new WaterFrameBuffer();
		GuiTexture guiReflection =new GuiTexture(waterFrameBuffer.getReflectionTexture(), new Vector2f(-0.5f,0.5f), new Vector2f(0.25f,0.25f));
		GuiTexture guiRefraction =new GuiTexture(waterFrameBuffer.getRefractionTexture(), new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f));
		GuiRenderer guiRenderer = new GuiRenderer(masterRenderer.getLoader());
		while (DisplayManager.isRunning()) {
			playerInputListener.update();
			camera.update();
			// Tuto ClippingPlane 
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
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
			
			Vector3f waterPosition = water.getGeoms().get(0).getRenderingParameters().getEntities().get(0).getPositions();
			
			//what is inside those 2 methods will be rendered to Frame Buffer Object.
			waterFrameBuffer.bindReflectionFrameBuffer();
			float cameraDistanceReflection = 2 * (camera.getCamera().getPosition().y - waterPosition.y);
			camera.getCamera().getPosition().y -= cameraDistanceReflection;
			camera.getCamera().invertPitch();
			//FIXME yuk... painful to get information from entity...
			// see math behind plane equation.
			masterRenderer.render(new ArrayList<>(), new Vector4f(0,1,0,-waterPosition.y));
			
			waterFrameBuffer.bindRefractionFrameBuffer();
			camera.getCamera().getPosition().y += cameraDistanceReflection;
			camera.getCamera().invertPitch();
			masterRenderer.render(new ArrayList<>(), new Vector4f(0,-1,0,waterPosition.y));
			
			
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			waterFrameBuffer.unbindCurrentFrameBuffer();
			//FIXME side effect while rendering twice. should be consistent
			masterRenderer.render(new ArrayList<>(), new Vector4f(0,-1,0,500));// 500 to avoid any clipping in world
			guiRenderer.addGui(guiReflection);
			guiRenderer.addGui(guiRefraction);
			guiRenderer.render();
			masterRenderer.clean();
			DisplayManager.updateDisplay();
		}
		playerInputListener.clear();
		masterRenderer.cleanUp();
		DisplayManager.closeDisplay();
	}
}
