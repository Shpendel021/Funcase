package by.shpendel.loneycase.config;

import by.shpendel.loneycase.LoneyCasePlugin;
import by.shpendel.loneycase.model.Crate;
import by.shpendel.loneycase.model.Prize;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ConfigManager {
    private final LoneyCasePlugin plugin;
    private MainConfig mainConfig;
    private FileConfiguration cratesConfig;
    private FileConfiguration menuConfig;
    private FileConfiguration messagesConfig;
    private final Map<String, Crate> crates = new LinkedHashMap<>();

    public ConfigManager(LoneyCasePlugin plugin) { this.plugin = plugin; }

    public void load() {
        plugin.reloadConfig();
        mainConfig = parseMain(plugin.getConfig());
        cratesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "crates.yml"));
        menuConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "cases_menu.yml"));
        messagesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        parseCrates();
    }

    private MainConfig parseMain(FileConfiguration cfg) {
        MainConfig m = new MainConfig();
        m.waitSelectPrizeTime = cfg.getInt("wait_select_prize_time", 25);
        m.checkerDelay = cfg.getInt("checker_delay", 120);
        m.saveTime = cfg.getInt("save_time", 72800);
        m.mysqlHost = cfg.getString("mysql.host", "localhost");
        m.mysqlPort = cfg.getInt("mysql.port", 3306);
        m.mysqlDatabase = cfg.getString("mysql.database", "loneycase");
        m.mysqlUsername = cfg.getString("mysql.username", "root");
        m.mysqlPassword = cfg.getString("mysql.password", "");
        m.colors = cfg.getStringList("colors");
        m.caseLocations = new ArrayList<>();
        for (String raw : cfg.getStringList("case_locs")) {
            String[] p = raw.split(";");
            if (p.length < 4) continue;
            World world = Bukkit.getWorld(p[0]);
            if (world == null) continue;
            m.caseLocations.add(new Location(world, Double.parseDouble(p[1]), Double.parseDouble(p[2]), Double.parseDouble(p[3])));
        }
        return m;
    }

    private void parseCrates() {
        crates.clear();
        ConfigurationSection root = cratesConfig.getConfigurationSection("crates");
        if (root == null) return;
        for (String crateId : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(crateId);
            if (section == null) continue;
            List<Prize> prizes = new ArrayList<>();
            ConfigurationSection prizesSec = section.getConfigurationSection("prizes");
            if (prizesSec != null) {
                for (String prizeId : prizesSec.getKeys(false)) {
                    ConfigurationSection p = prizesSec.getConfigurationSection(prizeId);
                    if (p == null) continue;
                    prizes.add(new Prize(
                            prizeId,
                            p.getString("displayName", prizeId),
                            p.getString("displayItem", "STONE"),
                            p.getString("texture", ""),
                            p.getBoolean("enchanted", false),
                            p.getInt("rate", 1),
                            p.getStringList("commands")
                    ));
                }
            }
            crates.put(crateId.toLowerCase(), new Crate(
                    crateId,
                    section.getString("name", crateId),
                    section.getString("item_name", crateId),
                    section.getStringList("item_lore"),
                    section.getString("skull_name", ""),
                    prizes
            ));
        }
    }

    public MainConfig getMainConfig() { return mainConfig; }
    public FileConfiguration getMenuConfig() { return menuConfig; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public Map<String, Crate> getCrates() { return crates; }
}
