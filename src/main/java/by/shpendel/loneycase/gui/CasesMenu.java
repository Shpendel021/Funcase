package by.shpendel.loneycase.gui;

import by.shpendel.loneycase.LoneyCasePlugin;
import by.shpendel.loneycase.model.Crate;
import by.shpendel.loneycase.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CasesMenu implements Listener {
    private final LoneyCasePlugin plugin;

    public CasesMenu(LoneyCasePlugin plugin) { this.plugin = plugin; }

    public void open(Player player) {
        String title = ColorUtil.color(plugin.getConfigManager().getMenuConfig().getString("name", "Кейсы"));
        int size = plugin.getConfigManager().getMenuConfig().getInt("size", 45);
        Inventory inv = Bukkit.createInventory(null, size, title);

        Map<String, Integer> playerCases = plugin.getCaseRepository().getCases(player.getUniqueId());
        List<Integer> slots = plugin.getConfigManager().getMenuConfig().getIntegerList("slots");
        int idx = 0;
        for (Map.Entry<String, Integer> entry : playerCases.entrySet()) {
            Crate crate = plugin.getCrate(entry.getKey());
            if (crate == null || idx >= slots.size()) continue;
            inv.setItem(slots.get(idx++), createCaseItem(crate, entry.getValue()));
        }
        drawDecor(inv);
        if (playerCases.isEmpty()) inv.setItem(22, simpleItem(Material.BARRIER, "&cУ вас нет кейсов"));
        player.openInventory(inv);
    }

    private void drawDecor(Inventory inv) {
        ConfigurationSection decor = plugin.getConfigManager().getMenuConfig().getConfigurationSection("decor");
        if (decor == null) return;
        for (String key : decor.getKeys(false)) {
            ConfigurationSection d = decor.getConfigurationSection(key);
            if (d == null) continue;
            Material m = Material.matchMaterial(d.getString("material", "GRAY_STAINED_GLASS_PANE"));
            if (m == null) m = Material.GRAY_STAINED_GLASS_PANE;
            ItemStack it = simpleItem(m, d.getString("name", " "));
            for (int slot : d.getIntegerList("slots")) inv.setItem(slot, it);
        }
    }

    private ItemStack createCaseItem(Crate crate, int amount) {
        return simpleItem(Material.ENDER_CHEST, crate.getItemName() + " &7x" + amount);
    }

    private ItemStack simpleItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(name));
            meta.setLore(new ArrayList<>());
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("Кейсы")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack current = e.getCurrentItem();
        if (current == null || current.getType() != Material.ENDER_CHEST || current.getItemMeta() == null) return;
        String stripped = org.bukkit.ChatColor.stripColor(current.getItemMeta().getDisplayName());
        for (Crate crate : plugin.getConfigManager().getCrates().values()) {
            if (!org.bukkit.ChatColor.stripColor(ColorUtil.color(crate.getItemName())).equals(stripped.replaceAll(" x\\d+$", ""))) continue;
            if (plugin.isOpening(p.getUniqueId())) return;
            if (!plugin.getCaseRepository().remove(p.getUniqueId(), crate.getId(), 1)) return;
            plugin.setOpening(p.getUniqueId(), true);
            plugin.getOpenAnimationService().open(p, crate);
            return;
        }
    }
}
