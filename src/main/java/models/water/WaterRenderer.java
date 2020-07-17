package models.water;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjglx.util.vector.Matrix4f;

import camera.CameraEntity;
import entities.Light;
import renderEngine.DisplayManager;
import renderEngine.DrawRendererCommon;
import renderEngine.Loader.VBOIndex;
import renderEngine.RenderingParameters;
import toolbox.Maths;

public class WaterRenderer extends DrawRendererCommon{

private WaterFrameBuffer frameBuffer;
private static final float WAVE_SPEED = 0.03f;

private float moveFactor =0;

private int dudv;
private int normalMap;
private Light sun;

//TODO extract in abstract class specific for 3D
private CameraEntity camera;
	private WaterRenderer(WaterFrameBuffer frameBuffer, CameraEntity camera, int dudv, int normalMap, Light sun) {
		this.frameBuffer = frameBuffer;
		this.camera = camera;
		this.dudv = dudv;
		this.normalMap = normalMap;
		this.sun = sun;
	}
	
	public static WaterRenderer create(WaterFrameBuffer frameBuffer, WaterShader waterShader, CameraEntity camera, int dudv, int normalMap, Light sun) {
		waterShader.start();
		waterShader.connectTextureUnits();
		waterShader.stop();
		return new WaterRenderer(frameBuffer, camera, dudv, normalMap, sun);
	}

	@Override
	public void render() {
		for (RenderingParameters params : renderingParams) {
			WaterShader draw3DShader = (WaterShader) params.getShader();
			draw3DShader.start();
			moveFactor += WAVE_SPEED * DisplayManager.getFrameTimeSeconds();
			moveFactor %=1;
			draw3DShader.loadMovmentFactor(moveFactor);
			draw3DShader.loadLight(sun);
			prepare(params.getVAOGeom().getVaoId());
		//	GL13.glActiveTexture(GL13.GL_TEXTURE2);
		//	GL11.glBindTexture(GL11.GL_TEXTURE_2D, (int) params.getVAOGeom().getTextures().toArray()[0]); //DudvMap
			
			Matrix4f viewMatrix = camera.getViewMatrix();
			draw3DShader.loadViewMatrix(viewMatrix);
			draw3DShader.loadCameraPosition(camera.getPosition());
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
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  this.dudv);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  this.normalMap);
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  frameBuffer.getRefractionDepthTexture());
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	protected void unbindGeom() {
		GL20.glDisableVertexAttribArray(VBOIndex.POSITION_INDEX);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
