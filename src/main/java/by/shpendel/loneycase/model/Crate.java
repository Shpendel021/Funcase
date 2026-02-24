package by.shpendel.loneycase.model;

import java.util.List;

public class Crate {
    private final String id;
    private final String name;
    private final String itemName;
    private final List<String> itemLore;
    private final String skullName;
    private final List<Prize> prizes;

    public Crate(String id, String name, String itemName, List<String> itemLore, String skullName, List<Prize> prizes) {
        this.id = id;
        this.name = name;
        this.itemName = itemName;
        this.itemLore = itemLore;
        this.skullName = skullName;
        this.prizes = prizes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getItemName() { return itemName; }
    public List<String> getItemLore() { return itemLore; }
    public String getSkullName() { return skullName; }
    public List<Prize> getPrizes() { return prizes; }
}
