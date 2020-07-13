package models.water;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import renderEngine.Loader.VBOIndex;
import shaderManager.IShader3D;
import shaderManager.ShaderProgram;

public class WaterShader extends ShaderProgram implements IShader3D{
private int locationReflectionTexture;
private int locationRefractionTexture;
private int locationDudvMap;
private int locationMoveFactor;

//TODO try to extract this part
private int transformationMatrix;
private int projectionMatrix;
private int locationViewMatrix;
private int locationCameraPosition;

	
	private WaterShader(Function<String, InputStream> consumer, String vertexFile, String fragmentFile) throws IOException {
		super(consumer, vertexFile, fragmentFile);
	}
	
	public static WaterShader create( String vertexFile, String fragmentFile) throws IOException{
		return new WaterShader(WaterShader.class::getResourceAsStream, vertexFile, fragmentFile);
	}

	@Override
	public void bindAttributes() {
		super.bindAttribute(VBOIndex.POSITION_INDEX, "position");
	}
	
	public void connectTextureUnits() {
		super.loadInt(locationReflectionTexture, 0);
		super.loadInt(locationRefractionTexture, 1);
		super.loadInt(locationDudvMap,2);
	}

	@Override
	public void getAllUniformLocation() {
		//TODO try to extract this common 3D part.
		transformationMatrix = super.getUniformLocation("transformationMatrix");
		projectionMatrix = super.getUniformLocation("projectionMatrix");
		locationViewMatrix = super.getUniformLocation("viewMatrix");
		this.locationReflectionTexture = this.getUniformLocation("reflectionTexture");
		this.locationRefractionTexture = this.getUniformLocation("refractionTexture");
		this.locationDudvMap = this.getUniformLocation("dudvMap");
		this.locationMoveFactor = this.getUniformLocation("moveFactor");
		this.locationCameraPosition =  this.getUniformLocation("cameraPosition"); 
	}
	//TODO try to extract this common 3D part.
	public void loadTransformationMatrix(Matrix4f transformation) {
		super.loadMatrix(transformationMatrix, transformation);
	}
	//TODO try to extract this common 3D part.
	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrix(projectionMatrix, projection);
	}
	//TODO try to extract this common 3D part.
	public void loadViewMatrix(Matrix4f viewMatrix) {
		super.loadMatrix(locationViewMatrix, viewMatrix);
	}
	
	public void loadCameraPosition(Vector3f position) {
		super.loadVector(this.locationCameraPosition, position);
	}
	
	public void loadMovmentFactor(float factor) {
		super.loadFloat(locationMoveFactor, factor);
	}

	@Override
	public void loadClipPlane(Vector4f plane) {
		// Nothing to do.
		
	}
}