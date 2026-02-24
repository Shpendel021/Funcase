package by.shpendel.loneycase.gui;

import by.shpendel.loneycase.LoneyCasePlugin;
import by.shpendel.loneycase.model.Crate;
import by.shpendel.loneycase.model.Prize;
import by.shpendel.loneycase.util.ColorUtil;
import by.shpendel.loneycase.util.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OpenAnimationService implements Listener {
    private final LoneyCasePlugin plugin;
    private final Random random = new Random();

    public OpenAnimationService(LoneyCasePlugin plugin) { this.plugin = plugin; }

    public void open(Player player, Crate crate) {
        List<Prize> pool = weighted(crate.getPrizes());
        if (pool.isEmpty()) return;
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.color("&8Открытие: " + crate.getName()));
        player.openInventory(inv);

        int maxSteps = Math.max(40, plugin.getConfigManager().getMainConfig().waitSelectPrizeTime * 2);
        final int[] steps = {0};
        final int[] taskId = {0};
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!player.isOnline()) {
                Bukkit.getScheduler().cancelTask(taskId[0]);
                return;
            }
            Prize p = pool.get(random.nextInt(pool.size()));
            inv.setItem(13, ItemFactory.fromPrize(p));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
            steps[0]++;
            if (steps[0] < maxSteps) return;

            Prize win = pool.get(random.nextInt(pool.size()));
            inv.setItem(13, ItemFactory.fromPrize(win));
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (String cmd : win.getCommands()) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd.replace("{player}", player.getName()));
                }
                player.sendMessage(ColorUtil.color("&aВы выиграли: " + win.getDisplayName()));
                player.closeInventory();
                plugin.setOpening(player.getUniqueId(), false);
            }, 20L);
            Bukkit.getScheduler().cancelTask(taskId[0]);
        }, 0L, 2L);
    }

    private List<Prize> weighted(List<Prize> input) {
        List<Prize> out = new ArrayList<>();
        for (Prize p : input) {
            for (int i = 0; i < Math.max(1, p.getRate()); i++) out.add(p);
        }
        return out;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().contains("Открытие:")) e.setCancelled(true);
    }
}
