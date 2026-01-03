package lol.tilley.favorites;

import com.google.gson.*;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.client.screen.EventScreen;
import org.rusherhack.client.api.feature.command.Command;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.ui.panel.IPanelItem;
import org.rusherhack.client.api.ui.panel.PanelBase;
import org.rusherhack.core.command.annotations.CommandExecutor;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class FavCommand extends Command {
	private final PluginMain pluginMain;
	private final PanelBase<?> favoritesPanel;
	private File configFile = RusherHackAPI.getConfigPath().resolve("favorites.json").toFile();
	private File panelConfigFile = RusherHackAPI.getConfigPath().resolve("favoritespanel.json").toFile();
	public final HashMap<IModule, IPanelItem> modules = new HashMap<>();

	public FavCommand(PluginMain pluginMain) {
		super("favs", "Add or remove modules from favorites");
		this.pluginMain = pluginMain;
		this.favoritesPanel = pluginMain.getFavoritesPanel();

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

		// See onScreenChange for rant about why I need this
		if (panelConfigFile.exists()) {
			try (java.io.FileReader fileReader = new java.io.FileReader(panelConfigFile)) {
				favoritesPanel.deserialize(JsonParser.parseReader(fileReader).getAsJsonObject());
			} catch (Exception exception) {
				this.pluginMain.getLogger().error(exception.getMessage());
			}
		}

		RusherHackAPI.getEventBus().subscribe(this);
	}

	@CommandExecutor(subCommand = "add")
	@CommandExecutor.Argument("module")
	private String addFavorite(IModule module) {
		if (this.modules.containsKey(module)) {
			return "Module " + module.getName() + " is already in favorites";
		} else {
			this.modules.put(module, favoritesPanel.addItem(module));
			if (saveConfig()) return "Added " + module.getName() + " to favorites";
			else return "Unable to add " + module.getName() + " to favorites";
		}
	}

	@CommandExecutor(subCommand = "del")
	@CommandExecutor.Argument("module")
	private String delFavorite(IModule module) {
		if (this.modules.containsKey(module)) {
			favoritesPanel.getItemList().remove(this.modules.get(module));
			this.modules.remove(module);
			if (saveConfig()) return "Removed " + module.getName() + " from favorites";
			else return "Unable to delete " + module.getName() + " from favorites";
		} else {
			return "Module " + module.getName() + " is not in favorites";
		}
	}

	private boolean saveConfig() {
		try {
			JsonArray keyJsonArray = new JsonArray();
			for (IModule module : modules.keySet()) keyJsonArray.add(module.getReferenceKey());
			try (FileWriter fileWriter = new FileWriter(configFile)) {
				new GsonBuilder().setPrettyPrinting().create().toJson(keyJsonArray, fileWriter);
			}
			return true;
		} catch (Exception exception) {
			this.pluginMain.getLogger().error(exception.getMessage());
			return false;
		}
	}

	// John pls add this to api
	private IModule getModuleByReferenceKey(String referenceKey) {
		for (IModule module : RusherHackAPI.getModuleManager().getFeatures()) {
			if (referenceKey.equals(module.getReferenceKey())) {
				return module;
			}
		}
		return null;
	}

	// I shouldn't need all of this, if john made it so the freakin Classic.json config file
	// didn't delete the config for my favorites panel on every reload
	// (it literally sets it back to 0.0, 0.0 every reload I kid you not so I have to add all this in)
	// @john200410 please fix

	@Subscribe(stage = Stage.PRE)
	private void onScreenChange(EventScreen.Change event) {
		if (event.getFrom() != null && event.getFrom().getTitle().getString().contains("ClickGui")) {
			try (FileWriter fileWriter = new FileWriter(panelConfigFile)) {
				new Gson().toJson(favoritesPanel.serialize().getAsJsonObject(), fileWriter);
			} catch (Exception exception) {
				this.pluginMain.getLogger().error(exception.getMessage());
			}
		}
	}

}
