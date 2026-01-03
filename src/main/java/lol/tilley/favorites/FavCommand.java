package lol.tilley.favorites;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.ui.panel.IPanelItem;
import org.rusherhack.client.api.ui.panel.PanelBase;
import org.rusherhack.core.command.annotations.CommandExecutor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class FavCommand extends Command {
	private final PluginMain pluginMain;
	private final File configFile;
	public final HashMap<IModule, IPanelItem> modules = new HashMap<>();

	public FavCommand(PluginMain pluginMain, File configFile) {
		super("favs", "Add or remove modules from favorites");
		this.pluginMain = pluginMain;
		this.configFile = configFile;

		if (configFile.exists()) {
			try (FileReader fileReader = new FileReader(configFile)) {
				for (JsonElement jsonElement : JsonParser.parseReader(fileReader).getAsJsonArray()) {
					IModule module = getModuleByReferenceKey(jsonElement.getAsString());
					if (module != null) addFavorite(module);
				}
			} catch (Exception exception) {
				this.pluginMain.getLogger().error(exception.getMessage());
			}
		}

	}

	@CommandExecutor(subCommand = "add")
	@CommandExecutor.Argument("module")
	private String addFavorite(IModule module) {
		PanelBase<?> favoritesPanel = pluginMain.getFavoritesPanel();
		if (this.modules.containsKey(module)) {
			return "Module " + module.getName() + " is already in favorites!";
		} else {
			this.modules.put(module, favoritesPanel.addItem(module));
			saveConfig();
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
			saveConfig();
			return "Removed " + module.getName() + " from favorites";
		} else {
			return "Module " + module.getName() + " is not in favorites";
		}
	}

	private void saveConfig() {
		try {
			JsonArray keyJsonArray = new JsonArray();
			for (IModule module : modules.keySet()) keyJsonArray.add(module.getReferenceKey());
			try (FileWriter fileWriter = new FileWriter(configFile)) {
				new GsonBuilder().setPrettyPrinting().create().toJson(keyJsonArray, fileWriter);
			}
		} catch (Exception exception) {
			this.pluginMain.getLogger().error(exception.getMessage());
		}
	}

	private IModule getModuleByReferenceKey(String referenceKey) {
		for (IModule module : RusherHackAPI.getModuleManager().getFeatures()) {
			if (referenceKey.equals(module.getReferenceKey())) {
				return module;
			}
		}
		return null;
	}

}
