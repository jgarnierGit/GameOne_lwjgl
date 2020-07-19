package models.backgroundTerrain;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.util.function.Function;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjglx.util.vector.Matrix4f;
import org.lwjglx.util.vector.Vector3f;
import org.lwjglx.util.vector.Vector4f;

import renderEngine.Loader.VBOIndex;
import shaderManager.IShader3D;
import shaderManager.ShaderProgram;

public class TerrainBackgroundShader extends ShaderProgram implements IShader3D {
	private static final String VERTEX_FILE = "terrainVertexShader.txt";
	private static final String FRAGMENT_FILE = "terrainFragmentShader.txt";
	private int locationTransformationMatrix;
	private int projectionMatrix;
	private int locationViewMatrix;
	private int locationShineDamper;
	private int locationReflectivity;
	private int locationSkyColour;
	private int locationBackgroundTexture;
	private int locationRTexture;
	private int locationGTexture;
	private int locationBTexture;
	private int locationBlendMap;
	private int locationPlaneClipping;

	private TerrainBackgroundShader(Function<String, InputStream> consumer, String vertexFile, String fragmentFile)
			throws IOException {
		super(consumer, vertexFile, fragmentFile);
	}
	
	public static TerrainBackgroundShader create() throws IOException {
		return new TerrainBackgroundShader(TerrainBackgroundShader.class::getResourceAsStream,VERTEX_FILE,FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		// binds attribute "position" of VertexShader in index 0 of vao
		super.bindAttribute(VBOIndex.POSITION_INDEX, "position");
		super.bindAttribute(VBOIndex.TEXTURE_INDEX, "textureCoords");
		super.bindAttribute(VBOIndex.NORMAL_INDEX, "normals");
	}

	@Override
	protected void getAllUniformLocation() {
		locationTransformationMatrix = super.getUniformLocation("transformationMatrix");
		projectionMatrix = super.getUniformLocation("projectionMatrix");
		locationViewMatrix = super.getUniformLocation("viewMatrix");
		locationShineDamper = super.getUniformLocation("shineDamper");
		locationReflectivity = super.getUniformLocation("reflectivity");
		locationSkyColour = super.getUniformLocation("skyColour");
		locationBackgroundTexture = super.getUniformLocation("backgroundTexture");
		locationRTexture = super.getUniformLocation("rTexture");
		locationGTexture = super.getUniformLocation("gTexture");
		locationBTexture = super.getUniformLocation("bTexture");
		locationBlendMap = super.getUniformLocation("blendMap");
		locationPlaneClipping = super.getUniformLocation("planeClipping");
	}

	public void connectTextureUnits() {
		super.loadInt(locationBackgroundTexture, 0);
		super.loadInt(locationRTexture, 1);
		super.loadInt(locationGTexture, 2);
		super.loadInt(locationBTexture, 3);
		super.loadInt(locationBlendMap, 4);

	}

	public void loadShineVariables(float shineDamper, float reflectivity) {
		super.loadFloat(locationShineDamper, shineDamper);
		super.loadFloat(locationReflectivity, reflectivity);
	}

	@Override
	public void loadClipPlane(Vector4f plane) {
		super.loadVector(locationPlaneClipping, plane);
	}

	@Override
	public void loadViewMatrix(Matrix4f viewMatrix) {
		super.loadMatrix(locationViewMatrix, viewMatrix);
	}

	@Override
	public void loadTransformationMatrix(Matrix4f matrix) {
		super.loadMatrix(locationTransformationMatrix, matrix);
	}

	@Override
	public void loadProjectionMatrix(Matrix4f projection) {
		super.loadMatrix(projectionMatrix, projection);
	}

	public void loadSkyColour(float red, float green, float blue) {
		super.loadVector(locationSkyColour, new Vector3f(red, green, blue));

	}

	@Override
	public int getColorShaderIndex() {
		return -1;
	}

	@Override
	public int getTextureShaderIndex() {
		return VBOIndex.TEXTURE_INDEX;
	}

	@Override
	public int getPositionShaderIndex() {
		return VBOIndex.POSITION_INDEX;
	}

	@Override
	public int getNormalShaderIndex() {
		return VBOIndex.NORMAL_INDEX;
	}

}
