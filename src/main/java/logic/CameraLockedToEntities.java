package logic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.lwjglx.util.vector.Vector3f;

import camera.Camera;
import camera.CameraEntity;
import entities.Entity;
import inputListeners.PlayerInputListener;
import modelsLibrary.SimpleGeom;
import modelsLibrary.Terrain3D;

public class CameraLockedToEntities extends Camera{
	private Set<Entity> entities;
	private float angleAroundPlayer;
	private float distanceFromEntity;
	private Vector3f centerLock;
	
	private CameraLockedToEntities(PlayerInputListener inputListener, CameraEntity camera) {
		super(inputListener, camera);
		angleAroundPlayer = 0;
		distanceFromEntity=30;
		entities = new HashSet<>();
		centerLock = new Vector3f();
	}
	
	
	public static CameraLockedToEntities create(PlayerInputListener inputListener, CameraEntity camera) {
		CameraLockedToEntities cameraBehavior = new CameraLockedToEntities(inputListener, camera);
		cameraBehavior.bindInputHanlder();
		return cameraBehavior;
	}
	

	@Override
	public void bindInputHanlder() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * TODO find an implementation to allow this one or freeFly but not together.
	 * @param terrains
	 */
	public void centerOverEntities(List<SimpleGeom> terrains) {
		 updateEntitiesCache(terrains);
		 centerLock = null;
		 for(Entity entity :this.entities) {
			 if(centerLock == null) {
				 centerLock =  new Vector3f(entity.getPositions().x,entity.getPositions().y,entity.getPositions().z);
			 }
			 else {
				Vector3f.add(centerLock,(Vector3f) entity.getPositions(), centerLock);
			 }
		}
		 float scale = (float)1/this.entities.size();
		 centerLock.scale(scale);
		 logger.log(Level.INFO, centerLock.toString());
		 
	}
	
	@Override
	public void update() {
		float hDelta = calculateHorizontalDeltaForDistance(distanceFromEntity);
		 float vDelta = calculateVerticalDeltaForDistance(distanceFromEntity);
		 float theta = 20;
			float offsetX = (float) (hDelta * Math.sin(Math.toRadians(theta)));
			float offsetZ = (float) (hDelta * Math.cos(Math.toRadians(theta)));
			float x = centerLock.x - offsetX;
			float z = centerLock.z - offsetZ;
			float y = centerLock.y + vDelta;
			camera.setPosition(new Vector3f(x,y,z));
	}
	
	public void cleanEntities() {
		this.entities.clear();
	}
	
	private void calculateCameraPosition(Terrain3D terrain, float x, float y, float z) {
		/**
		 * Optional<Float> oHeight = terrain.getHeight(x, z); if(oHeight.isPresent()) {
		 * y= oHeight.get() + 1 > y ? oHeight.get() + 1 : y; }
		 **/
		Vector3f position = new Vector3f(x, y, z);
		this.camera.setPosition(position);
	}

	/**
	 * getting horizontal distance , by hypothenuse * cos(theta) cos(theta) =
	 * adjacent / hypothenuse x = adjacent
	 * 
	 * @param distanceFromCamera distance from point reference
	 * @return adjacent length
	 */
	private float calculateHorizontalDeltaForDistance(float distanceFromCamera) {
		return (float) (distanceFromCamera * Math.cos(Math.toRadians(camera.getPitch())));
	}

	/**
	 * getting vertical distance, by hypothenuse * sin(theta) sin(theta) = opposite
	 * / hypothenuse y = opposite
	 * 
	 * @param distanceFromCamera distance from point reference
	 * @return opposite length
	 */
	private float calculateVerticalDeltaForDistance(float distanceFromCamera) {
		return (float) (distanceFromCamera * Math.sin(Math.toRadians(camera.getPitch())));
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
