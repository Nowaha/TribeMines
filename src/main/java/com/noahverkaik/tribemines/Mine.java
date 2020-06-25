package com.noahverkaik.tribemines;

import com.comphenix.protocol.events.PacketListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.sql.Time;
import java.util.*;

public class Mine {
    @Getter
    private int level;
    @Getter
    private Location center;
    @Getter
    private Chest chest;
    @Getter
    private List<Location> blocks;
    @Getter
    private UUID uniqueID;
    @Getter
    private long constructionEnd;
    @Getter
    private int gold;

    public Mine(int level, Location center, Chest chest) {
        this.level = level;
        this.center = center;
        this.chest = chest;
        this.uniqueID = UUID.randomUUID();
        this.blocks = new ArrayList<>();
        this.gold = 0;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 6; y++) {
                    blocks.add(center.clone().add(x, y, z));
                }
            }
        }

        chest.setCustomName("           §0§l⚒ §8Gold Mine §0§l⚒");
        chest.update();
        chest.getInventory().clear();

        chest.getInventory().setItem(13, new ItemStackBuilder(Material.MINECART).displayName("fucking mine").lore(new String[] {"§cif you take it you take it!!!", "§clol", "&eclick to take"}).build());

        for (int i = 0; i < gold; i++) {
            chest.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
        }
    }

    private Mine(int level, Location center, Chest chest, List<Location> blocks, UUID uniqueID, long constructionEnd, int gold) {
        this.level = level;
        this.center = center;
        this.chest = chest;
        this.blocks = blocks;
        this.uniqueID = uniqueID;
        this.constructionEnd = constructionEnd;
        this.gold = gold;
    }

    void saveToConfig() {
        String path = "mines." + uniqueID.toString();

        TribeMines.instance.getConfig().set(path + ".level", level);
        TribeMines.instance.getConfig().set(path + ".gold", gold);
        TribeMines.instance.getConfig().set(path + ".center", center);
        TribeMines.instance.getConfig().set(path + ".chest", chest.getLocation());
        TribeMines.instance.getConfig().set(path + ".constructionEnd", constructionEnd);
        TribeMines.instance.saveConfig();;
    }

    boolean isUnderConstruction() {
        return new Date().getTime() < constructionEnd;
    }

    void startConstruction(long end) {
        if (!isUnderConstruction()) {
            constructionEnd = end;
            Location circleCenter = center.clone().add(0, 0, 0);

            double oof = Math.random();
            Material glassType = Material.YELLOW_CONCRETE;
            if (oof >= 0.5d) {
                glassType = Material.BLACK_CONCRETE;
            }
            for (int x = -2; x <= 2; x++) {
                if (glassType == Material.YELLOW_CONCRETE) {
                    glassType = Material.BLACK_CONCRETE;
                } else {
                    glassType = Material.YELLOW_CONCRETE;
                }

                circleCenter.clone().add(x, 0, 2).getBlock().setType(glassType);
                circleCenter.clone().add(x, 0, -2).getBlock().setType(glassType);
            }

            for (int z = -1; z <= 1; z++) {
                if (glassType == Material.YELLOW_CONCRETE) {
                    glassType = Material.BLACK_CONCRETE;
                } else {
                    glassType = Material.YELLOW_CONCRETE;
                }

                circleCenter.clone().add(2, 0, z).getBlock().setType(glassType);
                circleCenter.clone().add(-2, 0, z).getBlock().setType(glassType);
            }
        }
    }

    void upgrade() {

    }

    void destroy() {
        List<HumanEntity> viewers = new ArrayList<>(getChest().getInventory().getViewers());
        for (HumanEntity viewer : viewers) {
            viewer.closeInventory();
        }

        chest.getInventory().clear();
        chest.getBlock().setType(Material.AIR);

        int index = 0;
        for (int y = 6; y >= 0; y--) {
            int finalY = y;
            Bukkit.getScheduler().scheduleSyncDelayedTask(TribeMines.instance, () -> {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        if (finalY > 0) {
                            Block block = center.clone().add(x, finalY, z).getBlock();
                            block.setType(Material.FIRE);
                        } else {
                            Block block = center.clone().add(x, finalY, z).getBlock();
                            block.setType(Material.COBBLESTONE);
                        }

                        Block block2 = center.clone().add(x, finalY + 1, z).getBlock();
                        block2.setType(Material.AIR);
                    }
                }
            }, index * 20);

            index++;
        }


        TribeMines.instance.getConfig().set("mines." + uniqueID.toString(), null);
        TribeMines.instance.saveConfig();
        TribeMines.instance.mines.remove(this);
    }

    void addGold(int amount) {
        chest.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, amount));
        /*if (gold + amount <= level * 600) {
            gold += amount;
            chest.getInventory().setItem(13, new ItemStackBuilder(Material.GOLD_INGOT).displayName("§6Gold: " + gold).lore(new String[] {"§7Level §f" + level + "§7 mine", "§7Gold rate: §f60 / min", "§7Storage limit: §f" + gold + "/600", "", "§6Click to pick this mine up"}).build());
        }*/
    }

    public static Mine loadFromConfig(UUID uuid) {
        String path = "mines." + uuid.toString();
        FileConfiguration config = TribeMines.instance.getConfig();

        if (config.isSet(path)) {
            try {
                int loadedLevel = config.getInt(path + ".level", 1);
                Location loadedCenter = ((Location)config.get(path + ".center"));
                Chest loadedChest = (Chest) ((Location)config.get(path + ".chest")).getBlock().getState();
                int loadedGold = config.getInt(path + ".gold", 0);

                loadedChest.setCustomName("           §0§l⚒ §8Gold Mine §0§l⚒");
                loadedChest.update();
                loadedChest.getInventory().clear();

                //loadedChest.getInventory().clear();
                //loadedChest.getInventory().setItem(13, new ItemStackBuilder(Material.GOLD_INGOT).displayName("§6Gold: " + loadedGold).lore(new String[] {"§7Level §f" + loadedLevel + "§7 mine", "§7Gold rate: §f60 / min", "§7Storage limit: §f" + loadedGold + "/600", "", "§6Click to pick this mine up"}).build());
                //loadedChest.getInventory().setItem(11, new ItemStackBuilder(Material.CHEST).displayName("§eTake Gold").lore(new String[] {"§7Right-clicking this will give you as much", "§7gold as you can fit in your inventory.", "", "§eClick to fill inventory"}).build());
                //loadedChest.getInventory().setItem(15, new ItemStackBuilder(Material.EMERALD).displayName("§aSell Gold").lore(new String[] {"§7Right-clicking this will sell all the gold", "§7stored in this mine.", "", "§aClick to sell all gold"}).build());

                //ItemStack glass = new ItemStackBuilder(Material.BLACK_STAINED_GLASS_PANE).displayName("§awww.tribewars.net").build();

                //int slot = -1;
                //for (ItemStack item : loadedChest.getInventory()) {
                //    slot++;
                //    if (slot == 14 || slot == 12 || slot == 10 || slot == 16) continue;
                //    if (item == null) {
                //        loadedChest.getInventory().setItem(slot, glass);
                //    }
                //}

                loadedChest.getInventory().setItem(13, new ItemStackBuilder(Material.MINECART).displayName("fucking mine").lore(new String[] {"§cif you take it you take it!!!", "§clol", "&eclick to take"}).build());

                for (int i = 0; i < loadedGold; i++) {
                    loadedChest.getInventory().addItem(new ItemStack(Material.GOLD_INGOT));
                }
                
                UUID loadedID = uuid;
                long loadedConstructionEnd = config.getLong(path + ".constructionEnd", 0);

                List<Location> loadedBlocks = new ArrayList<>();

                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        for (int y = 0; y <= 6; y++) {
                            loadedBlocks.add(loadedCenter.clone().add(x, y, z));
                        }
                    }
                }

                return new Mine(loadedLevel, loadedCenter, loadedChest, loadedBlocks, loadedID, loadedConstructionEnd, loadedGold);
            } catch (Exception ex) {

            }
        }

        return null;
    }
}
