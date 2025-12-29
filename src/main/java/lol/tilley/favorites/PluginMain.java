package lol.tilley.favorites;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.render.EventRender2D;
import org.rusherhack.client.api.plugin.Plugin;
import org.rusherhack.client.api.ui.panel.PanelBase;
import org.rusherhack.client.api.ui.panel.PanelHandlerBase;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;

public class PluginMain extends Plugin {
	private final PanelHandlerBase clickGuiHandler = RusherHackAPI.getThemeManager().getClickGuiHandler();
	private PanelBase<?> favoritesPanel;

	@Override
	public void onLoad() {
		this.getLogger().info("Plugin favorite-modules loaded");
		RusherHackAPI.getCommandManager().registerFeature(new FavCommand(this));
		//FavoriteConfig config = new FavoriteConfig(new FavCommand(this), this);
		//RusherHackAPI.getConfigManager().registerConfig(config, config);
		RusherHackAPI.getEventBus().subscribe(this);
	}

	@Override
	public void onUnload() {
		this.getLogger().info("Plugin favorite-modules unloaded!");
	}

	@Subscribe(stage = Stage.PRE)
	private void onSuitableInitEvent(EventRender2D event) {
		this.favoritesPanel = (PanelBase<?>) clickGuiHandler.createPanel("Favorites");
		clickGuiHandler.addPanel(favoritesPanel);
		RusherHackAPI.getEventBus().unsubscribe(this);
	}

	public PanelBase<?> getFavoritesPanel() {
		return this.favoritesPanel;
	}

}
