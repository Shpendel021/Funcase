package by.shpendel.loneycase.command;

import by.shpendel.loneycase.LoneyCasePlugin;
import by.shpendel.loneycase.model.Crate;
import by.shpendel.loneycase.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CaseCommand implements CommandExecutor, TabCompleter {
    private final LoneyCasePlugin plugin;

    public CaseCommand(LoneyCasePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                plugin.getCasesMenu().open((Player) sender);
            } else sender.sendMessage("Only player.");
            return true;
        }
        if (!sender.hasPermission("loneycase.admin")) {
            sender.sendMessage(ColorUtil.color("&cНет прав"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help":
                sender.sendMessage(ColorUtil.color("&e/case give <ник> <тип> <кол-во>"));
                sender.sendMessage(ColorUtil.color("&e/case remove <ник> <тип> <кол-во>"));
                sender.sendMessage(ColorUtil.color("&e/case reload"));
                break;
            case "give":
            case "remove":
                if (args.length < 4) return true;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ColorUtil.color("&cИгрок оффлайн"));
                    return true;
                }
                int amount;
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException ex) { sender.sendMessage("not number"); return true; }
                Crate crate = plugin.getCrate(args[2]);
                if (crate == null) {
                    sender.sendMessage(ColorUtil.color("&cКейс не найден"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("give")) {
                    plugin.getCaseRepository().add(target.getUniqueId(), crate.getId(), amount);
                    sender.sendMessage(ColorUtil.color("&aВыдано"));
                } else {
                    boolean ok = plugin.getCaseRepository().remove(target.getUniqueId(), crate.getId(), amount);
                    sender.sendMessage(ColorUtil.color(ok ? "&aУдалено" : "&cНедостаточно кейсов"));
                }
                break;
            case "setcase":
                if (!(sender instanceof Player) || args.length < 2) return true;
                Player p = (Player) sender;
                Location l = p.getLocation();
                List<String> locs = plugin.getConfig().getStringList("case_locs");
                locs.add(l.getWorld().getName() + ";" + l.getBlockX() + ";" + l.getBlockY() + ";" + l.getBlockZ());
                plugin.getConfig().set("case_locs", locs);
                plugin.saveConfig();
                sender.sendMessage(ColorUtil.color("&aЛокация добавлена"));
                break;
            case "reload":
                plugin.reloadAll();
                sender.sendMessage(ColorUtil.color("&aПерезагружено"));
                break;
            default:
                if (sender instanceof Player) plugin.getCasesMenu().open((Player) sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Arrays.asList("help", "give", "remove", "setcase", "reload");
        if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("remove"))) {
            return new ArrayList<>(plugin.getConfigManager().getCrates().keySet());
        }
        return Collections.emptyList();
    }
}
