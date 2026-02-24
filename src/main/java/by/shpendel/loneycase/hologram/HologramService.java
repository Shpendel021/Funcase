package by.shpendel.loneycase.hologram;

import by.shpendel.loneycase.LoneyCasePlugin;
import by.shpendel.loneycase.config.ConfigManager;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class HologramService {
    private final LoneyCasePlugin plugin;
    private final ConfigManager configManager;
    private final List<String> ids = new ArrayList<>();

    public HologramService(LoneyCasePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void refresh() {
        removeAll();
        if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") == null) return;
        int i = 0;
        for (Location location : configManager.getMainConfig().caseLocations) {
            String id = "loneycase_" + i++;
            ids.add(id);
            String cmd = String.format("dh create %s %s %.2f %.2f %.2f \"&bLoneyCase\"", id,
                    location.getWorld().getName(), location.getX(), location.getY() + 0.8, location.getZ());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
        }
    }

    public void removeAll() {
        if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") == null) return;
        for (String id : ids) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "dh remove " + id);
        }
        ids.clear();
    }
}
