package launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Vector2f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import entities.GeomContainer;
import entities.GuiTexture;
import entities.Light;
import inputListeners.PlayerInputListener;
import inputListeners.PlayerInputListenerBuilder;
import logic.CameraCenterOverEntities;
import logic.CameraManager;
import logic.Player;
import logic.TerrainManager;
import models.Monkey;
import models.backgroundTerrain.BackgroundTerrainRenderer;
import models.library.SkyboxDayNight;
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
		Monkey monkey = Monkey.create(masterRenderer);
		Player player = Player.create(playerInputListener, monkey.getRenderableGeom(), new Vector3f(-5, 0, 0), 0, 90, 0, 1);
		SkyboxDayNight skybox = SkyboxDayNight.create(masterRenderer, camera.getCamera());
		// TODO create interface Model3D to guide user for minimal structure

		TerrainManager terrainGenerator = TerrainManager.create(masterRenderer, playerInputListener, camera.getCamera());
		// gameExecutor.render(masterRenderer,player,terrain);

		float deltaTime = 0;
		// FIXME weird to use getter to create cameraBehavior, and maybe not have to use
		// it.
		// CameraCenterOverEntities cameraLogic = camera.getCenterOverEntitiesCamera();
		camera.getFreeFlyCamera();
		camera.getCameraLockedToEntity(player.getEntity());
		WaterFrameBuffer waterFrameBuffer = new WaterFrameBuffer();
		Light sun = new Light(new Vector3f(-30.0f,100.0f,-100.0f), new Vector3f(1.0f,0.95f,1.0f));
		Water water = Water.create(masterRenderer, waterFrameBuffer, camera.getCamera(), sun, "waterVertexShader.txt", "waterFragmentShader.txt");
		/** example of using FrameBuffer as Gui Texture 
		GuiTexture guiReflection =new GuiTexture(waterFrameBuffer.getReflectionTexture(), new Vector2f(-0.5f,0.5f), new Vector2f(0.25f,0.25f));
		GuiTexture guiRefraction =new GuiTexture(waterFrameBuffer.getRefractionTexture(), new Vector2f(0.5f,0.5f), new Vector2f(0.25f,0.25f));
		GuiRenderer guiRenderer = new GuiRenderer(masterRenderer.getLoader());
		guiRenderer.addGui(guiReflection);
		guiRenderer.addGui(guiRefraction);
		**/
		while (DisplayManager.isRunning()) {
			playerInputListener.update();
			camera.update();
			// Tuto ClippingPlane 
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			camera.updateViewMatrix();

			deltaTime += DisplayManager.getFrameTimeSeconds();
			if (deltaTime > 2) {
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
			Vector3f waterPosition = water.getRenderableGeom().getRenderingParameters().getEntities().get(0).getPositions();
			Vector4f clipPlane = new Vector4f();
			
			List<GeomContainer> toRender = new ArrayList<>();
			toRender.addAll(terrainGenerator.getTerrains());
			toRender.add(terrainGenerator.getUnderGroundTerrain());
			toRender.add(monkey);
			toRender.add(skybox);
			
			//what is inside those 2 methods will be rendered to Frame Buffer Object.
			
			waterFrameBuffer.bindReflectionFrameBuffer();
			// clip plane upward (0,1,0) for reflection
			clipPlane = new Vector4f(0,1,0,-waterPosition.y+0.1f);
			//couldn't been worst
			((BackgroundTerrainRenderer) terrainGenerator.getUnderGroundTerrain().getRenderableGeom().getRenderer()).setClipPlane(clipPlane);
			float cameraDistanceReflection = 2 * (camera.getCamera().getPosition().y - waterPosition.y);
			camera.getCamera().getPosition().y -= cameraDistanceReflection;
			camera.getCamera().invertPitch(); 
			//FIXME yuk... painful to get information from entity...
			// see math behind plane equation.
			// new Vector4f(a,b,c,d)
			//a,b,c = normal plane
			//d = signed distance from origin
			masterRenderer.reloadRenderingDatas(new ArrayList<>(), toRender, clipPlane);
			masterRenderer.render();

			waterFrameBuffer.bindRefractionFrameBuffer();
			//downward for refraction
			clipPlane = new Vector4f(0,-1,0,waterPosition.y);
			camera.getCamera().getPosition().y += cameraDistanceReflection;
			camera.getCamera().invertPitch();
			((BackgroundTerrainRenderer) terrainGenerator.getUnderGroundTerrain().getRenderableGeom().getRenderer()).setClipPlane(clipPlane);
			masterRenderer.reloadRenderingDatas(new ArrayList<>(), toRender, clipPlane);
			masterRenderer.render();
			
			// may I add this to rendering parameters? don't know.
			// manually disable clipping for each renderer using it
			// some drivers ignore this command so it is required for compatibilities to specify a clip of 0,0,0
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			clipPlane =new Vector4f(0,0,0,0);
			waterFrameBuffer.unbindCurrentFrameBuffer();
			toRender.add(water);
			//couldn't been worst
			((BackgroundTerrainRenderer) terrainGenerator.getUnderGroundTerrain().getRenderableGeom().getRenderer()).setClipPlane(clipPlane);
			masterRenderer.reloadRenderingDatas(new ArrayList<>(), toRender, clipPlane);
			masterRenderer.render();
		//	guiRenderer.render();
			DisplayManager.updateDisplay();
		}
		playerInputListener.clear();
		masterRenderer.cleanUp();
		DisplayManager.closeDisplay();
	}
}
