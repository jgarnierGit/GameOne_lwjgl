package launcher;

import java.util.ArrayList;

import logic.Player;
import renderEngine.DisplayManager;
import renderEngine.MasterRenderer;

public class GameExecutor {

	public void render(MasterRenderer masterRenderer, Player player) {
		while (DisplayManager.isRunning()) {
			masterRenderer.processEntity(player.getEntity());
			masterRenderer.processEntity(player.getEntity());
			//masterRenderer.render(new ArrayList<>());
		}
		masterRenderer.cleanUp();
	}
}
