package by.shpendel.loneycase.model;

import java.util.List;

public class Prize {
    private final String id;
    private final String displayName;
    private final String displayItem;
    private final String texture;
    private final boolean enchanted;
    private final int rate;
    private final List<String> commands;

    public Prize(String id, String displayName, String displayItem, String texture, boolean enchanted, int rate, List<String> commands) {
        this.id = id;
        this.displayName = displayName;
        this.displayItem = displayItem;
        this.texture = texture;
        this.enchanted = enchanted;
        this.rate = rate;
        this.commands = commands;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDisplayItem() { return displayItem; }
    public String getTexture() { return texture; }
    public boolean isEnchanted() { return enchanted; }
    public int getRate() { return rate; }
    public List<String> getCommands() { return commands; }
}
