package logic;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.List;
import java.util.Optional;

import org.lwjglx.util.vector.Vector3f;

import entities.Entity;
import entities.EntityTutos;
import entities.SimpleEntity;
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
	private float fallingTimeout = 0;
	private boolean jumping = false;
	private boolean isInAir = false;
	private EntityTutos entity;
	private Entity respawner;

	private Player(PlayerInputListener inputListener, Model3D model, Vector3f positions, float rotX, float rotY,
			float rotZ, float scale) {
		super(inputListener);
		entity = new EntityTutos(model, positions, rotX, rotY, rotZ, scale);

	}

	public static Player create(PlayerInputListener inputListener, Model3D model, Vector3f positions, float rotX,
			float rotY, float rotZ, float scale) {
		Player player = new Player(inputListener, model, positions, rotX, rotY, rotZ, scale);
		player.bindInputHanlder();
		player.respawner = new SimpleEntity(new Vector3f(0, 10, 0), rotX, rotY, rotZ, scale);
		return player;
	}

	@Override
	public void bindInputHanlder() {
		this.inputListener.getKeyboard().ifPresent(keyboardListener -> {
			keyboardListener.addRunnerOnPress(GLFW_KEY_W, () -> updateCurrentSpeed(RUN_SPEED));
			keyboardListener.addRunnerOnPress(GLFW_KEY_S, () -> updateCurrentSpeed(-RUN_SPEED));
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

	public void respawning() {
		jumping=false;
		entity.setPositions(
				new Vector3f(respawner.getPositions().x, respawner.getPositions().y+0.5f, respawner.getPositions().z));
		upwardSpeed = 0;
		fallingTimeout = 0;
	}

	private void jump() {
		if (!isInAir) {
			this.upwardSpeed = JUMP_POWER;
			isInAir = true;
			jumping = true;
		}
	}

	public EntityTutos getEntity() {
		return this.entity;
	}

	public void move(List<Terrain3D> terrains) {
		entity.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(entity.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(entity.getRotY())));
		entity.increasePosition(dx, 0, dz);
		if (isInAir) {
			upwardSpeed -= GRAVITY * DisplayManager.getFrameTimeSeconds();
			if (upwardSpeed <= 0) {
				jumping = false;
			}
			entity.increasePosition(0, upwardSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		}
		updateJumpingStatus(terrains);
	}

	private void updateJumpingStatus(List<Terrain3D> terrains) {
		Optional<Entity> nearestTerrain = getActiveTerrain(terrains);
		Optional<Float> currentHeightTerrain = Optional.empty();
		if (nearestTerrain.isPresent()) {
			currentHeightTerrain = Optional.of(nearestTerrain.get().getPositions().y);
		}
		if (currentHeightTerrain.isPresent()) {
			float elevation = currentHeightTerrain.get();
			if (!jumping && Math.abs(elevation - entity.getPositions().y) < 0.5) {
				setRespawner(nearestTerrain.get());
				upwardSpeed = 0;
				entity.getPositions().y = elevation;
				isInAir = false;
				fallingTimeout = 0;
			}
		} else {
			isInAir = true;
			fallingTimeout += DisplayManager.getFrameTimeSeconds();
			if (fallingTimeout > 3) {
				respawning();
			}
		}
	}

	private Optional<Entity> getActiveTerrain(List<Terrain3D> terrains) {
		Optional<Float> activeHeight = Optional.empty();
		// TODO shortcut to implement, test with active terrain if coordinates still
		// match.
		for (Terrain3D terrain : terrains) {
			/**
			 * Optional<Entity> nearestTerrain =
			 * SpatialComparator.getNearestEntityFromDirection(entity.getPositions(),Direction.BOTTOM,
			 * terrain.getRenderingParameters().getEntities());
			 * if(!nearestTerrain.isPresent()) { continue; }
			 **/
			Optional<Float> terrainHeight = terrain.getHeight(entity.getPositions().x, entity.getPositions().z);
			if (terrainHeight.isPresent() && terrainHeight.get() <= entity.getPositions().y) {
				// logger.log(Level.INFO, "upwardspeed :"+ upwardSpeed +" terrain : " +
				// terrainHeight.get() + " player" + entity.getPositions());
				if (!activeHeight.isPresent()) {
					activeHeight = terrainHeight;
				} else {
					if (activeHeight.get() < terrainHeight.get()) {
						activeHeight = terrainHeight;
					}
				}
			}
		}
		return !activeHeight.isPresent() ? Optional.empty()
				: Optional.of(new SimpleEntity(
						new Vector3f(entity.getPositions().x, activeHeight.get(), entity.getPositions().z),
						entity.getRotX(), entity.getRotY(), entity.getRotZ(), 1));
	}

	public void setRespawner(Entity entity) {
		respawner = entity;
	}
}
