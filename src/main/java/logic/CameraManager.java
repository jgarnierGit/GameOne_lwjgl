package logic;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector3f;

import camera.Camera;
import camera.CameraEntity;
import camera.behavior.CameraFreeFly;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import renderEngine.GameBehavior;

public class CameraManager extends InputInteractable implements GameBehavior{

	private CameraEntity camera;
	private Camera cameraBehavior;
	private CameraFreeFly cameraFreeFly;
	private CameraLockedToEntities cameraLockedToEntities;

	private CameraManager(PlayerInputListener inputListener) {
		super(inputListener);
		camera = new CameraEntity(new Vector3f(0, 10, 50), 0, 20, 0);
		cameraBehavior = null;
		cameraFreeFly = null;
		cameraLockedToEntities = null;
	}

	public static CameraManager create(PlayerInputListener inputListener) {
		CameraManager cameraManager = new CameraManager(inputListener);
		cameraManager.bindInputHanlder();
		return cameraManager;
	}
	
	public CameraFreeFly getFreeFlyCamera(int glfwPitch,int  glfwYaw) {
		if(cameraFreeFly == null) {
			cameraFreeFly = CameraFreeFly.create(inputListener, camera, glfwPitch, glfwYaw);
		}
		cameraBehavior = cameraFreeFly;
		return cameraFreeFly;
	}
	
	public CameraFreeFly getFreeFlyCamera() {
		if(cameraFreeFly == null) {
			cameraFreeFly = CameraFreeFly.create(inputListener, camera, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
		}
		cameraBehavior = cameraFreeFly;
		return cameraFreeFly;
	}
	
	public CameraLockedToEntities getLockedToEntityCamera() {
		if(cameraLockedToEntities == null) {
			cameraLockedToEntities = CameraLockedToEntities.create(inputListener, camera);
		}
		cameraBehavior= cameraLockedToEntities;
		return cameraLockedToEntities;
	}

	public CameraEntity getCamera() {
		return this.camera;
	}

	@Override
	public void bindInputHanlder() {
		inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnUniquePress(GLFW.GLFW_KEY_C, this::switchMovingSystem);
		});
	}

	private void switchMovingSystem() {
		if(cameraBehavior instanceof CameraLockedToEntities) {
			cameraBehavior = getFreeFlyCamera();
		}
		else {
			cameraBehavior = getLockedToEntityCamera();
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
