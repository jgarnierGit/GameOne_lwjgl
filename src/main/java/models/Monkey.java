package models;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import entities.GeomContainer;
import models.GeomEditor;
import models.IEditableGeom;
import models.IRenderableGeom;
import models.SimpleGeom3D;
import models.SimpleGeom3DBuilder;
import models.importer.OBJImporter;
import models.library.terrain.Terrain3D;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;

public class Monkey implements GeomContainer{
	private static final String OBJECT_DESCRIPTOR = "JM.obj";
	private static final String TEXTURE_DESCRIPTOR = "JM.mtl";
	SimpleGeom3D monkeyGeom;
	private MasterRenderer masterRenderer;
	
	private Monkey() {
		
	}
	

	public static Monkey create(MasterRenderer masterRenderer) throws IOException {
		Monkey monkey = new Monkey();
		monkey.monkeyGeom = SimpleGeom3DBuilder.create(masterRenderer.getLoader(), masterRenderer.get3DRenderer(), "monkey").withDefaultShader().build();
		monkey.monkeyGeom.getVAOGeom().loadContent(OBJImporter.parse(OBJECT_DESCRIPTOR,TEXTURE_DESCRIPTOR));
		monkey.masterRenderer = masterRenderer;
		monkey.setup();
		monkey.prepareForRender();
		return monkey;
	}
	
	public void prepareForRender() { // TODO try to automate this part. try using visitor
		masterRenderer.reloadAndprocess(this.getRenderableGeom());
		masterRenderer.sendForRendering();
	}
	
	private void setup() {
		RenderingParameters terrainParameters = this.getRenderableGeom().getRenderingParameters();
		// TODO hide from this interface.
		// terrainParameters.disableRenderOptions();
		terrainParameters.setRenderMode(GL11.GL_TRIANGLES);
	}


	@Override
	public IEditableGeom getEditableGeom() {
		return monkeyGeom;
	}


	@Override
	public IRenderableGeom getRenderableGeom() {
		return monkeyGeom;
	}


	@Override
	public GeomEditor getGeomEditor() {
		return monkeyGeom.getGeomEditor();
	}
}
