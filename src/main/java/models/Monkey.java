package models;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import entities.GeomContainer;
import models.importer.OBJImporter;
import renderEngine.MasterRenderer;
import renderEngine.RenderingParameters;

public class Monkey implements GeomContainer{
	private static final String OBJECT_DESCRIPTOR = "JM.obj";
	private static final String TEXTURE_DESCRIPTOR = "JM.mtl";
	SimpleGeom3D monkeyGeom;
	
	private Monkey() {
		
	}
	

	public static Monkey create(MasterRenderer masterRenderer) throws IOException {
		Monkey monkey = new Monkey();
		monkey.monkeyGeom = SimpleGeom3DBuilder.create(masterRenderer, masterRenderer.getDefault3DRenderer(), "monkey").withDefaultShader().build();
		monkey.monkeyGeom.getRenderer().bindContentToGeomVAO(monkey.monkeyGeom, OBJImporter.parse(OBJECT_DESCRIPTOR,TEXTURE_DESCRIPTOR));
		monkey.setup();
		return monkey;
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
