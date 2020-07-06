package models.water;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector4f;

import shaderManager.IShader3D;
import shaderManager.ShaderProgram;

public class WaterShader extends ShaderProgram implements IShader3D{
private int locationReflectionTexture;
private int locationRefractionTexture;
//TODO try to extract this part
private int transformationMatrix;
private int projectionMatrix;
private int locationViewMatrix;
	
	private WaterShader(Function<String, InputStream> consumer, String vertexFile, String fragmentFile) throws IOException {
		super(consumer, vertexFile, fragmentFile);
	}
	
	public static WaterShader create( String vertexFile, String fragmentFile) throws IOException{
		WaterShader waterShader = new WaterShader(WaterShader.class::getResourceAsStream, vertexFile, fragmentFile);
		return waterShader;
	}

	@Override
	public void bindAttributes() {
		this.projectionMatrix = this.getUniformLocation("transformationMatrix");
		this.transformationMatrix = this.getUniformLocation("viewMatrix");
		this.locationViewMatrix = this.getUniformLocation("projectionMatrix");
		this.locationReflectionTexture = this.getUniformLocation("reflectionTexture");
		this.locationRefractionTexture = this.getUniformLocation("refractionTexture");
	}
	
	public void connectTextureUnits() {
		super.loadInt(locationReflectionTexture, 0);
		super.loadInt(locationRefractionTexture, 1);
	}

	@Override
	public void getAllUniformLocation() {
		//TODO try to extract this common 3D part.
		transformationMatrix = super.getUniformLocation("transformationMatrix");
		projectionMatrix = super.getUniformLocation("projectionMatrix");
		locationViewMatrix = super.getUniformLocation("viewMatrix");
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

	@Override
	public void loadClipPlane(Vector4f plane) {
		// Nothing to do.
		
	}

	@Override
	public void setUseImage(boolean useImage) {
		// TODO Auto-generated method stub
		
	}
}