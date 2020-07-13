package models.water;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix4f;

import camera.CameraEntity;
import models.data.OBJContent;
import renderEngine.DrawRenderer;
import renderEngine.Loader.VBOIndex;
import renderEngine.RenderingParameters;
import toolbox.Maths;

public class WaterRenderer extends DrawRenderer{

private WaterFrameBuffer frameBuffer;

//TODO extract in abstract class specific for 3D
private CameraEntity camera;
	private WaterRenderer(WaterFrameBuffer frameBuffer, CameraEntity camera) {
		this.frameBuffer = frameBuffer;
		this.camera = camera;
	}
	
	public static WaterRenderer create(WaterFrameBuffer frameBuffer, WaterShader waterShader, CameraEntity camera) {
		waterShader.start();
		waterShader.connectTextureUnits();
		waterShader.stop();
		return new WaterRenderer(frameBuffer, camera);
	}

	@Override
	public void render() {
		for (RenderingParameters params : renderingParams) {
			WaterShader draw3DShader = (WaterShader) params.getShader();
			draw3DShader.start();
			prepare(params.getVAOGeom().getVaoId());
		//	GL13.glActiveTexture(GL13.GL_TEXTURE2);
		//	GL11.glBindTexture(GL11.GL_TEXTURE_2D, (int) params.getVAOGeom().getTextures().toArray()[0]); //DudvMap
			
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
		GL20.glEnableVertexAttribArray(VBOIndex.POSITION_INDEX);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getReflectionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  frameBuffer.getRefractionTexture());
	}

	@Override
	protected void unbindGeom() {
		GL20.glDisableVertexAttribArray(VBOIndex.POSITION_INDEX);
		GL30.glBindVertexArray(0);
	}
}
