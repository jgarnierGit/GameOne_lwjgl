package launcher;

import java.util.ArrayList;

import entities.Player;
import models.Terrain;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class GameExecutor {

	public void render(MasterRenderer masterRenderer, Player player, Terrain terrain) {
		while (DisplayManager.isRunning()) {
			masterRenderer.processEntity(player);
			masterRenderer.processEntity(player);
			masterRenderer.render(new ArrayList<>());
		}
		masterRenderer.cleanUp();
	}
}