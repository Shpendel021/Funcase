package by.shpendel.loneycase;

import by.shpendel.loneycase.command.CaseCommand;
import by.shpendel.loneycase.config.ConfigManager;
import by.shpendel.loneycase.data.CaseRepository;
import by.shpendel.loneycase.gui.CasesMenu;
import by.shpendel.loneycase.gui.OpenAnimationService;
import by.shpendel.loneycase.hologram.HologramService;
import by.shpendel.loneycase.model.Crate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoneyCasePlugin extends JavaPlugin {
    private ConfigManager configManager;
    private CaseRepository caseRepository;
    private HologramService hologramService;
    private OpenAnimationService openAnimationService;
    private CasesMenu casesMenu;
    private final Map<UUID, Boolean> openingNow = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("crates.yml", false);
        saveResource("cases_menu.yml", false);
        saveResource("messages.yml", false);

        this.configManager = new ConfigManager(this);
        configManager.load();

        this.caseRepository = new CaseRepository(this, configManager.getMainConfig());
        caseRepository.init();

        this.hologramService = new HologramService(this, configManager);
        hologramService.refresh();

        this.openAnimationService = new OpenAnimationService(this);
        this.casesMenu = new CasesMenu(this);

        CaseCommand caseCommand = new CaseCommand(this);
        getCommand("case").setExecutor(caseCommand);
        getCommand("case").setTabCompleter(caseCommand);
        getCommand("loneycase").setExecutor(caseCommand);
        getCommand("loneycase").setTabCompleter(caseCommand);

        Bukkit.getPluginManager().registerEvents(casesMenu, this);
        Bukkit.getPluginManager().registerEvents(openAnimationService, this);
    }

    @Override
    public void onDisable() {
        if (hologramService != null) {
            hologramService.removeAll();
        }
        if (caseRepository != null) {
            caseRepository.close();
        }
    }

    public void reloadAll() {
        reloadConfig();
        configManager.load();
        caseRepository.reconfigure(configManager.getMainConfig());
        hologramService.refresh();
    }

    public ConfigManager getConfigManager() { return configManager; }
    public CaseRepository getCaseRepository() { return caseRepository; }
    public HologramService getHologramService() { return hologramService; }
    public OpenAnimationService getOpenAnimationService() { return openAnimationService; }
    public CasesMenu getCasesMenu() { return casesMenu; }

    public boolean isOpening(UUID uuid) { return openingNow.getOrDefault(uuid, false); }
    public void setOpening(UUID uuid, boolean state) { openingNow.put(uuid, state); }

    public Crate getCrate(String id) { return configManager.getCrates().get(id.toLowerCase()); }
}
