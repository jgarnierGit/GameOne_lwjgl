package logic;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.util.vector.Vector3f;

import camera.Camera;
import camera.CameraEntity;
import camera.behavior.CameraFreeFly;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import renderEngine.GameBehavior;
import toolbox.CoordinatesSystemManager;

public class CameraManager extends InputInteractable implements GameBehavior{

	private CameraEntity camera;
	private Camera cameraBehavior;
	private CameraFreeFly cameraFreeFly;
	private CameraLockedToEntities cameraLockedToEntities;

	private CameraManager(PlayerInputListener inputListener, CameraEntity camera) {
		super(inputListener);
		this.camera = camera;
		cameraBehavior = null;
		cameraFreeFly = null;
		cameraLockedToEntities = null;
	}

	public static CameraManager create(PlayerInputListener inputListener, Vector3f position, int pitch, int yaw) {
		CameraManager cameraManager = new CameraManager(inputListener, new CameraEntity(position, pitch, yaw, 0, CoordinatesSystemManager.create()));
		cameraManager.bindInputHanlder();
		return cameraManager;
	}
	
	public CameraFreeFly getFreeFlyCamera(int glfwRotateInput,int  glfwDeltaTranslation) {
		if(cameraFreeFly == null) {
			cameraFreeFly = CameraFreeFly.create(inputListener, camera, glfwRotateInput, glfwDeltaTranslation);
		}
		cameraBehavior = cameraFreeFly;
		return cameraFreeFly;
	}
	
	public CameraFreeFly getFreeFlyCamera() {
		if(cameraFreeFly == null) {
			cameraFreeFly = CameraFreeFly.create(inputListener, camera, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, GLFW.GLFW_MOUSE_BUTTON_LEFT);
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

	//TODO add unbindInputHandler on option when switching cameraBehavior.
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
