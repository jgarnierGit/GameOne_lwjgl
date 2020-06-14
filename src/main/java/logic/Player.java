package logic;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.List;
import java.util.Optional;

import org.lwjglx.util.vector.Vector3f;

import entities.EntityTutos;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import modelsLibrary.Terrain3D;
import modelsManager.Model3D;
import renderEngine.DisplayManager;

public class Player extends InputInteractable {
	private static final float RUN_SPEED = 20;
	private static final float TURN_FLOAT = 160;
	private static final float GRAVITY = 50;
	private static final float JUMP_POWER = 30;
	
	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardSpeed = 0;
	private float falling = 0;
	private boolean isInAir = false;
	private EntityTutos entity;
	
	private Player(PlayerInputListener inputListener, Model3D model, Vector3f positions, float rotX, float rotY, float rotZ, float scale) {
		super(inputListener);
		entity = new EntityTutos(model, positions, rotX, rotY, rotZ, scale);
	}
	
	public static Player create(PlayerInputListener inputListener, Model3D model, Vector3f positions, float rotX, float rotY, float rotZ, float scale) {
		Player player = new Player(inputListener,model,positions,rotX,rotY,rotZ,scale);
		player.bindInputHanlder();
		return player;
	}
	
	@Override
	public void bindInputHanlder() {
		this.inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnPress(GLFW_KEY_W, () -> updateCurrentSpeed(RUN_SPEED));
			keyboardListener.addRunnerOnPress(GLFW_KEY_S,() -> updateCurrentSpeed(-RUN_SPEED));
			keyboardListener.addRunnerOnPress(GLFW_KEY_A, () -> updateCurrentTurnSpeed(TURN_FLOAT));
			keyboardListener.addRunnerOnPress(GLFW_KEY_D, () -> updateCurrentTurnSpeed(-TURN_FLOAT));
			keyboardListener.addRunnerOnRelease(GLFW_KEY_W, () -> updateCurrentSpeed(0));
			keyboardListener.addRunnerOnRelease(GLFW_KEY_S, () -> updateCurrentSpeed(0));
			keyboardListener.addRunnerOnRelease(GLFW_KEY_A, () -> updateCurrentTurnSpeed(0));
			keyboardListener.addRunnerOnRelease(GLFW_KEY_D, () -> updateCurrentTurnSpeed(0));
			keyboardListener.addRunnerOnUniquePress(GLFW_KEY_SPACE, this::jump);
		});
		
	}
	
	public void updateCurrentSpeed(float speed) {
		this.currentSpeed = speed;
	}
	
	public void updateCurrentTurnSpeed(float turn) {
			this.currentTurnSpeed = turn;
	}
	
	private void jump() {
		if(!isInAir) {
			this.upwardSpeed = JUMP_POWER;
			isInAir = true;
		}
	}
	
	public EntityTutos getEntity() {
		return this.entity;
	}
	
	public void move(List<Terrain3D> terrains) {
		//checkInputs();
		entity.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(entity.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(entity.getRotY())));
		entity.increasePosition(dx, 0, dz);
		upwardSpeed -= GRAVITY * DisplayManager.getFrameTimeSeconds();
		entity.increasePosition(0, upwardSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		
		// TODO improve this as specific developpment for GameOne: many terrain can overlap with different y
		Optional<Float> terrainHeight = Optional.empty();
		for(Terrain3D terrain : terrains) {
			terrainHeight = terrain.getHeight(entity.getPositions().x, entity.getPositions().z);
			if(terrainHeight.isPresent()) {
				break;
			}
		}
		if(terrainHeight.isPresent()) {
			float elevation = terrainHeight.get();
			if(entity.getPositions().y < elevation) {
				upwardSpeed = 0;
				entity.getPositions().y = elevation;
				isInAir = false;
				falling = 0;
			}
		}
		else {
			falling += DisplayManager.getFrameTimeSeconds();
			if(falling > 3) {
				entity.setPositions(new Vector3f(0,10,0));
			}
		}
		
	}
	

	/** FIXME combo are not allowed in this system.
	private void checkInputs() {
			if(!UserInputHandler.activateOnPress(GLFW_KEY_W) && !UserInputHandler.activateOnPress(GLFW_KEY_S)){
				currentSpeed = 0;
			}
			if(!UserInputHandler.activateOnPress(GLFW_KEY_A) && !UserInputHandler.activateOnPress(GLFW_KEY_D)){
				currentTurnSpeed = 0;
			}
	}
**/
}
