package logic;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.SimpleEntity;
import inputListeners.InputInteractable;
import inputListeners.MouseInputListener;
import inputListeners.PlayerInputListener;
import modelsLibrary.SimpleGeom;
import modelsLibrary.Terrain3D;

public class CameraLogic extends InputInteractable {

	private Camera cameraModel;
	private float speed;
	private float angleAroundPlayer;
	private Set<SimpleEntity> entities;

	private Entity player;

	private CameraLogic(PlayerInputListener inputListener) {
		super(inputListener);
		angleAroundPlayer = 0;
		speed = 0;
		cameraModel = new Camera(new Vector3f(0, 10, 50), 0, 20, 0);
		entities = new HashSet<>();
	}

	public static CameraLogic create(PlayerInputListener inputListener) {
		CameraLogic cameraLogic = new CameraLogic(inputListener);
		cameraLogic.bindInputHanlder();
		return cameraLogic;
	}

	public Camera getCamera() {
		return this.cameraModel;
	}

	public void cleanEntities() {
		this.entities.clear();
	}

	@Override
	public void bindInputHanlder() {
		inputListener.getMouse().ifPresent(mouseListener -> {
			mouseListener.addRunnerOnPress(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, () -> calculatePitch(mouseListener));
			mouseListener.addRunnerOnPress(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, () -> calculateYaw(mouseListener));
		});

		inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnUniquePress(GLFW.GLFW_KEY_C, this::switchMovingSystem);
		});
	}

	public void attachToEntity(Entity entity) {
		player = entity;
	}

	private void switchMovingSystem() {
		throw new NotImplementedException();
	}

	private void calculateCameraPosition(Terrain3D terrain, float x, float y, float z) {
		/**
		 * Optional<Float> oHeight = terrain.getHeight(x, z); if(oHeight.isPresent()) {
		 * y= oHeight.get() + 1 > y ? oHeight.get() + 1 : y; }
		 **/
		Vector3f position = new Vector3f(x, y, z);
		this.cameraModel.setPosition(position);
	}

	/**
	 * getting horizontal distance , by hypothenuse * cos(theta) cos(theta) =
	 * adjacent / hypothenuse x = adjacent
	 * 
	 * @param distanceFromCamera distance from point reference
	 * @return adjacent length
	 */
	private float calculateHorizontalDeltaForDistance(float distanceFromCamera) {
		return (float) (distanceFromCamera * Math.cos(Math.toRadians(cameraModel.getPitch())));
	}

	/**
	 * getting vertical distance, by hypothenuse * sin(theta) sin(theta) = opposite
	 * / hypothenuse y = opposite
	 * 
	 * @param distanceFromCamera distance from point reference
	 * @return opposite length
	 */
	private float calculateVerticalDeltaForDistance(float distanceFromCamera) {
		return (float) (distanceFromCamera * Math.sin(Math.toRadians(cameraModel.getPitch())));
	}

	private void calculateSpeed() {
		this.inputListener.getMouse().ifPresent(mouseListener -> {
			float inputScroll = mouseListener.getScrollValue();
			if (inputScroll == 0) {
				this.speed = 0;
			} else {
				if (Math.abs(this.speed) < Math.abs(inputScroll) / 10) {
					logger.log(Level.INFO, " " + this.speed + " " + inputScroll);
					float speed = inputScroll * 0.05f;
					this.speed += speed;
				}
			}
		});
	}

	private void calculatePitch(MouseInputListener mouseListener) {
		float ypos = mouseListener.getMouseDeltaY();
		float pitch = cameraModel.getPitch() - (-ypos * 0.5f);
		cameraModel.setPitch(pitch);
	}

	private void calculateYaw(MouseInputListener mouseListener) {
		logger.log(Level.INFO, "yaw update");
		float xpos = mouseListener.getMouseDeltaX();
		float yaw = cameraModel.getYaw() + xpos * 0.5f;
		cameraModel.setYaw(yaw);
	}

	/**
	 * cos(0) = 1; cos(1) = 0 sin(0) = 0; sin(1) = 1 yaw anti-counter clockwise (0 =
	 * z-forward / x-left) pitch counter clockwise (0 = horizontal)
	 * 
	 * @param terrain
	 */
	public void freeFly() {
		float cameraXDirection = (float) (Math.sin(-Math.toRadians(cameraModel.getYaw()))
				* Math.cos(Math.toRadians(cameraModel.getPitch())));
		float cameraZDirection = (float) (Math.cos(-Math.toRadians(cameraModel.getYaw()))
				* Math.cos(Math.toRadians(cameraModel.getPitch())));
		float cameraYDirection = (float) (Math.sin(Math.toRadians(cameraModel.getPitch())));

		Vector3f unitVectorCamera = new Vector3f(cameraXDirection, cameraYDirection, cameraZDirection);
		calculateSpeed();
		unitVectorCamera.scale(this.speed);
		Vector3f newPosition = Vector3f.add(cameraModel.getPosition(), unitVectorCamera, null);
		cameraModel.setPosition(newPosition);
	}

	public void centerOverEntities(List<SimpleGeom> terrains) {
		// updateEntitiesCache(terrains);

	}

	private void updateEntitiesCache(List<SimpleGeom> geoms) {
		for (SimpleGeom geom : geoms) {
			if (geom.getRenderingParameters().isNotUsingEntities()) {
				throw new IllegalStateException("Geom must have entities to process");
			}
			// must be updated each frame as we may add entities to geom.
			this.entities.addAll(geom.getRenderingParameters().getEntities());
		}
	}
}
