package by.shpendel.loneycase.util;

import by.shpendel.loneycase.model.Prize;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public final class ItemFactory {
    private ItemFactory() {}

    public static ItemStack fromPrize(Prize prize) {
        Material material = Material.matchMaterial(prize.getDisplayItem());
        if (material == null) material = Material.CHEST;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(prize.getDisplayName()));
            meta.setLore(Collections.emptyList());
            if (prize.isEnchanted()) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
