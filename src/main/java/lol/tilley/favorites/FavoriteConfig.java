package lol.tilley.favorites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.config.JsonConfiguration;
import org.rusherhack.core.serialize.JsonSerializable;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.ui.panel.PanelBase;

public class FavoriteConfig extends JsonConfiguration implements JsonSerializable {
    private final FavCommand favCommand;
    private final PluginMain pluginMain;

    public FavoriteConfig(FavCommand favCommand, PluginMain pluginMain) {
        super(RusherHackAPI.getConfigPath().resolve("favorites.json").toFile());
        this.favCommand = favCommand;
        this.pluginMain = pluginMain;
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();
        for (IModule module : favCommand.modules.keySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("moduleKey", module.getReferenceKey());
            array.add(obj);
        }
        return array;
    }

    @Override
    public boolean deserialize(JsonElement jsonElement) {
        if (!jsonElement.isJsonArray()) return false;
        boolean success = false;

        JsonArray array = jsonElement.getAsJsonArray();
        PanelBase<?> favoritesPanel = this.pluginMain.getFavoritesPanel();
        favCommand.modules.clear();

        for (JsonElement element : array) {
            String moduleKey = element.getAsJsonObject().get("moduleKey").getAsString();
            IModule module = getModuleByReferenceKey(moduleKey);
            if (module != null) {
                favCommand.modules.put(module, favoritesPanel.addItem(module));
                success = true;
            }
        }
        return success;
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

