package logic;

import java.util.Optional;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector3f;

import camera.Camera;
import camera.CameraEntity;
import camera.behavior.CameraFreeFly;
import camera.behavior.CameraLockedToEntity;
import entities.Entity;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import renderEngine.GameBehavior;
import toolbox.CoordinatesSystemManager;

public class CameraManager extends InputInteractable implements GameBehavior{

	private CameraEntity camera;
	private Camera cameraBehavior;
	private CameraFreeFly cameraFreeFly;
	private CameraCenterOverEntities cameraCenterOverEntities;
	private CameraLockedToEntity cameraLockedToEntity;
	
	private Optional<Entity> lockedEntity;

	private CameraManager(PlayerInputListener inputListener, CameraEntity camera) {
		super(inputListener);
		// TODO isn't it weird to have camera specified here and in each cameraBehavior?
		this.camera = camera;
		cameraBehavior = null;
		cameraFreeFly = null;
		cameraCenterOverEntities = null;
		cameraLockedToEntity = null;
		lockedEntity = Optional.empty();
	}

	public static CameraManager create(PlayerInputListener inputListener, Vector3f position, int pitch, int yaw) {
		CameraManager cameraManager = new CameraManager(inputListener, new CameraEntity(position, pitch, yaw, 0, CoordinatesSystemManager.create()));
		cameraManager.bindInputHanlder();
		return cameraManager;
	}
	
	@Override
	public void bindInputHanlder() {
		inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnUniquePress(GLFW.GLFW_KEY_C, this::switchMovingSystem);
		});
	}
	
	@Override
	public void unbindInputHanlder() {
		//nothing to unbind
	}
	
	public CameraFreeFly getFreeFlyCamera(int glfwRotateInput,int  glfwDeltaTranslation) {
		if(cameraFreeFly == null) {
			cameraFreeFly = CameraFreeFly.create(inputListener, camera, glfwRotateInput, glfwDeltaTranslation);
		}
		cameraFreeFly.stopMoving();
		switchBehavior(cameraFreeFly);
		return cameraFreeFly;
	}
	
	public CameraFreeFly getFreeFlyCamera() {
		if(cameraFreeFly == null) {
			cameraFreeFly = CameraFreeFly.create(inputListener, camera, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, GLFW.GLFW_MOUSE_BUTTON_LEFT);
		}
		cameraFreeFly.stopMoving();
		switchBehavior(cameraFreeFly);
		return cameraFreeFly;
	}
	
	public void switchBehavior(Camera activeCameraBehavior) {
		if(this.cameraBehavior != null) {
			this.cameraBehavior.unbindInputHanlder();
		}
		activeCameraBehavior.bindInputHanlder();
		this.cameraBehavior = activeCameraBehavior;
	}

	public CameraLockedToEntity getCameraLockedToEntity(Entity entity) {
		if(cameraLockedToEntity == null) {
			cameraLockedToEntity = CameraLockedToEntity.create(inputListener, camera, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 40, 0, entity);
		}
		else {
			cameraLockedToEntity.lockToEntity(entity);
		}
		lockedEntity = Optional.of(entity);
		cameraLockedToEntity.stopMoving();
		switchBehavior(cameraLockedToEntity);
		return cameraLockedToEntity;
	}
	
	public CameraCenterOverEntities getCenterOverEntitiesCamera() {
		if(cameraCenterOverEntities == null) {
			cameraCenterOverEntities = CameraCenterOverEntities.create(inputListener, camera);
		}
		switchBehavior(cameraCenterOverEntities);
		return cameraCenterOverEntities;
	}

	public CameraEntity getCamera() {
		return this.camera;
	}

	// weird switch... I want it to switch over allowed cameraBehavior.
	private void switchMovingSystem() {
		if(cameraBehavior instanceof CameraLockedToEntity) {
			cameraBehavior = getFreeFlyCamera();
		}
		else {
			if(lockedEntity.isPresent()) {
				cameraBehavior = getCameraLockedToEntity(lockedEntity.get());
			}
			
		}
	}

	@Override
	public void update() {
		cameraBehavior.update();
	}

	public void updateViewMatrix() {
		this.camera.updateViewMatrix();
	}

	public Camera getActiveCameraBehavior() {
		return cameraBehavior;
	}
}
