package lol.tilley.favorites;

import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.ui.panel.IPanelItem;
import org.rusherhack.client.api.ui.panel.PanelBase;
import org.rusherhack.core.command.annotations.CommandExecutor;

import java.util.HashMap;

public class FavCommand extends Command {
	private final PluginMain pluginMain;
	public HashMap<IModule, IPanelItem> modules = new HashMap<>();

	public FavCommand(PluginMain pluginMain) {
		super("favs", "Add or remove modules from favorites");
		this.pluginMain = pluginMain;
	}

	@CommandExecutor(subCommand = "add")
	@CommandExecutor.Argument("module")
	private String addFavorite(IModule module) {
		PanelBase<?> favoritesPanel = pluginMain.getFavoritesPanel();
		if (this.modules.containsKey(module)) {
			return "Module " + module.getName() + " is already in favorites!";
		} else {
			this.modules.put(module, favoritesPanel.addItem(module));
			return "Added " + module.getName() + " to favorites (" + module.getReferenceKey() + ")";
		}
	}

	@CommandExecutor(subCommand = "del")
	@CommandExecutor.Argument("module")
	private String delFavorite(IModule module) {
		PanelBase<?> favoritesPanel = pluginMain.getFavoritesPanel();
		if (this.modules.containsKey(module)) {
			favoritesPanel.getItemList().remove(this.modules.get(module));
			this.modules.remove(module);
			return "Removed " + module.getName() + " from favorites";
		} else {
			return "Module " + module.getName() + " is not in favorites";
		}
	}

}
