package me.imbanana.vanillawaypoints.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.imbanana.vanillawaypoints.config.ModConfigScreenFactory;

public class ModMenuEntry implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfigScreenFactory::buildConfigScreen;
    }
}
