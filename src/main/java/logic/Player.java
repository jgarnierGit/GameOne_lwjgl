package logic;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lwjglx.util.vector.Vector3f;

import entities.Entity;
import entities.EntityTutos;
import entities.SimpleEntity;
import inputListeners.InputInteractable;
import inputListeners.PlayerInputListener;
import modelsLibrary.terrain.Terrain3D;
import modelsManager.Model3D;
import renderEngine.DisplayManager;
import utils.Direction;
import utils.Operator;
import utils.SpatialComparator;

public class Player extends InputInteractable {
	private static final float RUN_SPEED = 20;
	private static final float TURN_FLOAT = 160;
	private static final float GRAVITY = 50;
	private static final float JUMP_POWER = 30;

	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private float upwardSpeed = 0;
	private float fallingTimeout = 0;
	private float jumpingStillAllowed = 0;
	private boolean jumping = false;
	private boolean isInAir = false;
	private EntityTutos entity;
	private Entity respawner;
	private Entity activeTerrain;
	private Runnable increaseSpeed;
	private Runnable decreaseSpeed;
	private Runnable increaseTurn;
	private Runnable decreaseTurn;
	private Runnable resetSpeed;
	private Runnable resetTurn;

	private Player(PlayerInputListener inputListener, Model3D model, Vector3f positions, float rotX, float rotY,
			float rotZ, float scale) {
		super(inputListener);
		activeTerrain = null;
		entity = new EntityTutos(model, positions, rotX, rotY, rotZ, scale);
		increaseSpeed = null;
		decreaseSpeed = null;
		increaseTurn = null;
		decreaseTurn = null;
		resetSpeed = null;
		resetTurn = null;
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
			initKeyboarRunnable();
			keyboardListener.addRunnerOnPress(GLFW_KEY_W, increaseSpeed);
			keyboardListener.addRunnerOnPress(GLFW_KEY_S, decreaseSpeed);
			keyboardListener.addRunnerOnPress(GLFW_KEY_A, increaseTurn);
			keyboardListener.addRunnerOnPress(GLFW_KEY_D, decreaseTurn);
			keyboardListener.addRunnerOnRelease(GLFW_KEY_W, resetSpeed);
			keyboardListener.addRunnerOnRelease(GLFW_KEY_S, resetSpeed);
			keyboardListener.addRunnerOnRelease(GLFW_KEY_A, resetTurn);
			keyboardListener.addRunnerOnRelease(GLFW_KEY_D, resetTurn);
			keyboardListener.addRunnerOnUniquePress(GLFW_KEY_SPACE, this::jump);
		});
	}

	private void initKeyboarRunnable() {
		if (increaseSpeed == null) {
			increaseSpeed = () -> updateCurrentSpeed(RUN_SPEED);
		}
		if (decreaseSpeed == null) {
			decreaseSpeed = () -> updateCurrentSpeed(-RUN_SPEED);
		}
		if (increaseTurn == null) {
			increaseTurn = () -> updateCurrentTurnSpeed(TURN_FLOAT);
		}
		if (decreaseTurn == null) {
			decreaseTurn = () -> updateCurrentTurnSpeed(-TURN_FLOAT);
		}
		if (resetSpeed == null) {
			resetSpeed = () -> updateCurrentSpeed(0);
		}
		if (resetTurn == null) {
			resetTurn = () -> updateCurrentTurnSpeed(0);
		}
	}

	@Override
	public void unbindInputHanlder() {
		// nothing to unbind
	}

	public void updateCurrentSpeed(float speed) {
		this.currentSpeed = speed;
	}

	public void updateCurrentTurnSpeed(float turn) {
		this.currentTurnSpeed = turn;
	}

	public void respawning(CameraManager camera) {
		jumping = false;
		camera.getCameraLockedToEntity(entity);
		entity.setPositions(new Vector3f(respawner.getPositions().x, respawner.getPositions().y + 0.5f,
				respawner.getPositions().z));
		upwardSpeed = 0;
		fallingTimeout = 0;
	}

	private void jump() {
		if (!isInAir || (!jumping && jumpingStillAllowed < 0.5)) {
			this.upwardSpeed = JUMP_POWER;
			isInAir = true;
			jumping = true;
		}
	}

	public EntityTutos getEntity() {
		return this.entity;
	}

	public void move(TerrainManager terrainManager, CameraManager camera) {
		entity.increaseRotation(0, currentTurnSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = (float) (distance * Math.sin(Math.toRadians(entity.getRotY())));
		float dz = (float) (distance * Math.cos(Math.toRadians(entity.getRotY())));
		float dy = getYSpeed();
		entity.increasePosition(dx, dy, dz);
		adjustYPositionToTerrains(terrainManager, camera, Math.abs(dy));
	}

	private float getYSpeed() {
		float ySpeed = 0;
		if (isInAir) {
			upwardSpeed -= GRAVITY * DisplayManager.getFrameTimeSeconds();
			jumpingStillAllowed += DisplayManager.getFrameTimeSeconds();
			if (upwardSpeed <= 0) {
				jumping = false;
			}
			ySpeed = upwardSpeed * DisplayManager.getFrameTimeSeconds();
		}
		else {
			jumpingStillAllowed = 0;
		}
		return ySpeed;
	}

	private void adjustYPositionToTerrains(TerrainManager terrainManager, CameraManager camera, float finalspeed) {
		Optional<Entity> terrain = getActiveTerrain(terrainManager); 
		if (terrain.isPresent()) {
			Entity terrainEntity = terrain.get();
			float elevation = terrainEntity.getPositions().y;
			//avoid passing through at high velocity
			finalspeed = finalspeed < 0.5f ? (float) 0.5 : finalspeed;
			if (!jumping && Math.abs(elevation - entity.getPositions().y) < finalspeed) {
				Entity respawnerEntity = new SimpleEntity(
						new Vector3f(entity.getPositions().x, terrainEntity.getPositions().y, entity.getPositions().z),
						entity.getRotX(), entity.getRotY(), entity.getRotZ(), 1);
				setRespawner(respawnerEntity);
				upwardSpeed = 0;
				entity.getPositions().y = elevation;
				isInAir = false;
				fallingTimeout = 0;
				return;
			}
		}
		testIsFallingOutOfWorld(entity.getPositions(), camera, terrainManager.getTerrains());
	}

	private Optional<Entity> getActiveTerrain(TerrainManager terrainManager) {
		List<Entity> activeTerrainEntities = new ArrayList<>();
		Optional<Entity> activeEntityterrain = Optional.empty();
		for(Terrain3D terrain : terrainManager.getTerrains()) {
			activeTerrainEntities.addAll(terrainManager.getActiveTerrain(entity.getPositions(), terrain));
		}
		if(activeTerrainEntities.isEmpty()) {
			return activeEntityterrain;
		}
		if(activeTerrain == null) {
			activeEntityterrain = Optional.of(activeTerrainEntities.get(0));
		}
		else if(activeTerrainEntities.contains(activeTerrain)) {
			activeEntityterrain = Optional.of(activeTerrain);
		}
		return activeEntityterrain;
	}

	/**
	 * falling out of world means no more reachable terrain are below player.
	 * @param worldPosition
	 * @param camera
	 * @param terrains
	 */
	private void testIsFallingOutOfWorld(Vector3f worldPosition, CameraManager camera, List<Terrain3D> terrains) {
		if (fallingTimeout == 0) {
			List<Entity> filteredTerrainEntities = new ArrayList<>();
			for (Terrain3D terrain : terrains) {
				filteredTerrainEntities.addAll(SpatialComparator.filterEntitiesByDirection(worldPosition,
						Direction.BOTTOM, Operator.INCLUSIVE, terrain.getSimpleGeom().getRenderingParameters().getEntities()));
			}
			if (filteredTerrainEntities.isEmpty()) {
				// init falling
				camera.getFreeFlyCamera();
				isInAir = true;
				fallingTimeout += DisplayManager.getFrameTimeSeconds();
			} else {
				isInAir = true;
				return;
			}
		}

		// in any other case player is falling.
		fallingTimeout += DisplayManager.getFrameTimeSeconds();
		if (fallingTimeout > 3) {
			respawning(camera);
		}
	}

	public void setRespawner(Entity entity) {
		respawner = entity;
	}
}
