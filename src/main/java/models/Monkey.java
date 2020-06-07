package models;

import java.io.FileNotFoundException;
import java.io.IOException;

import modelsManager.Model3D;
import modelsManager.Model3DImporter;
import modelsManager.ModelUtils;
import renderEngine.Loader;
import renderEngine.MasterRenderer;

public class Monkey extends Model3D{
	private static final String OBJECT_DESCRIPTOR = "JM.obj";
	private static final String TEXTURE_DESCRIPTOR = "JM.mtl";
	
	public Monkey(MasterRenderer masterRenderer) throws FileNotFoundException, IOException {
		super();
		createModel(ModelUtils.importModel(Model3DImporter.importOBJ(OBJECT_DESCRIPTOR),
				Model3DImporter.importMTL(TEXTURE_DESCRIPTOR)),masterRenderer.getLoader());
	}

}
