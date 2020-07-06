package models.backgroundTerrain;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix4f;

import camera.CameraEntity;
import renderEngine.DrawRenderer;
import renderEngine.Loader.VBOIndex;
import renderEngine.RenderingParameters;
import shaderManager.IShader3D;
import toolbox.Maths;

public class BackgroundTerrainRenderer extends DrawRenderer{
//TODO extract in abstract class specific for 3D
private CameraEntity camera;

	private BackgroundTerrainRenderer(CameraEntity camera) {
		this.camera = camera;
	}
	
	public static BackgroundTerrainRenderer create( CameraEntity camera) {
		return new BackgroundTerrainRenderer(camera);
	}

	@Override
	public void render() {
		for (RenderingParameters params : renderingParams) {
			IShader3D draw3DShader = (IShader3D) params.getShader();
			draw3DShader.start();
			prepare(params.getVAOGeom().getVaoId());
			Matrix4f viewMatrix = camera.getViewMatrix();
			draw3DShader.loadViewMatrix(viewMatrix);
			//generic part to extract, works also with SkyboxRenderer as its shader doesn't implements transformationMatrix.
			params.getEntities().forEach(entity -> {
				Matrix4f transformationM = Maths.createTransformationMatrix(entity.getPositions(), entity.getRotX(),
						entity.getRotY(), entity.getRotZ(), entity.getScale());
				draw3DShader.loadTransformationMatrix(transformationM);
				genericDrawRender(params);
			});
			unbindGeom();
			draw3DShader.stop();
		}
	}

	@Override
	protected void prepare(int vaoId) {
		GL30.glBindVertexArray(vaoId);
		//TODO extract those to a ShaderBinder as list so it can be read by prepare and unbind, and make a more generic Renderer 
		// and Shader with more explicit relation between shader files index and code index.
		GL20.glEnableVertexAttribArray(VBOIndex.POSITION_INDEX);
		GL20.glEnableVertexAttribArray(VBOIndex.TEXTURE_INDEX);
		GL20.glEnableVertexAttribArray(VBOIndex.NORMAL_INDEX);
	}

	@Override
	protected void unbindGeom() {
		GL20.glDisableVertexAttribArray(VBOIndex.NORMAL_INDEX);
		GL20.glDisableVertexAttribArray(VBOIndex.TEXTURE_INDEX);
		GL20.glDisableVertexAttribArray(VBOIndex.POSITION_INDEX);
		GL30.glBindVertexArray(0);
	}
}
