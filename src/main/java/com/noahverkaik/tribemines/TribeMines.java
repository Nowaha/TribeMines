package com.noahverkaik.tribemines;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.noahverkaik.tribepipes.TribePipes;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public final class TribeMines extends JavaPlugin implements Listener {

    public static TribeMines instance;

    List<Mine> mines = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        getServer().getPluginManager().registerEvents(this, this);
        targetBlacklist.add(Material.TALL_GRASS);
        targetBlacklist.add(Material.GRASS);
        targetBlacklist.add(Material.AIR);
        targetBlacklist.add(Material.TORCH);
        targetBlacklist.add(Material.WALL_TORCH);
        targetBlacklist.add(Material.ACACIA_SAPLING);
        targetBlacklist.add(Material.SPRUCE_SAPLING);
        targetBlacklist.add(Material.BIRCH_SAPLING);
        targetBlacklist.add(Material.DARK_OAK_SAPLING);
        targetBlacklist.add(Material.JUNGLE_SAPLING);
        targetBlacklist.add(Material.OAK_SAPLING);
        targetBlacklist.add(Material.POPPY);
        targetBlacklist.add(Material.OXEYE_DAISY);
        targetBlacklist.add(Material.BLUE_ORCHID);
        targetBlacklist.add(Material.ALLIUM);
        targetBlacklist.add(Material.AZURE_BLUET);
        targetBlacklist.add(Material.RED_TULIP);
        targetBlacklist.add(Material.ORANGE_TULIP);
        targetBlacklist.add(Material.WHITE_TULIP);
        targetBlacklist.add(Material.PINK_TULIP);
        targetBlacklist.add(Material.BROWN_MUSHROOM);
        targetBlacklist.add(Material.RED_MUSHROOM);
        targetBlacklist.add(Material.FERN);
        targetBlacklist.add(Material.LARGE_FERN);
        targetBlacklist.add(Material.DEAD_BUSH);
        targetBlacklist.add(Material.DANDELION);

        try {
            for (String mineUUIDString : getConfig().getConfigurationSection("mines").getKeys(false)) {
                UUID mineUUID = UUID.fromString(mineUUIDString);
                Mine mine = Mine.loadFromConfig(mineUUID);

                if (mine != null) {
                    mines.add(mine);
                }
            }
        } catch (Exception ignored) {}


        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Mine mine : mines) {
                if (!mine.isUnderConstruction()) {
                    Location circleCenter = mine.getCenter().clone().add(0, 0, 0);

                    if (circleCenter.clone().add(-2, 0, 2).getBlock().getType() != Material.BEDROCK) {
                        for (int x = -2; x <= 2; x++) {
                            circleCenter.clone().add(x, 0, 2).getBlock().setType(Material.BEDROCK);
                            circleCenter.clone().add(x, 0, -2).getBlock().setType(Material.BEDROCK);
                        }

                        for (int z = -1; z <= 1; z++) {
                            circleCenter.clone().add(2, 0, z).getBlock().setType(Material.BEDROCK);
                            circleCenter.clone().add(-2, 0, z).getBlock().setType(Material.BEDROCK);
                        }
                    }

                    mine.addGold(8);
                }
            }
        }, 0, 20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Mine mine :
                mines) {
            mine.saveToConfig();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        /*if (placing.contains(player.getUniqueId())) {
            player.sendMessage("§c§lno!");
            return false;
        } else {
            placing.add(player.getUniqueId());

            WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity();
            Integer entityID = (int)(Math.random() * Integer.MAX_VALUE);
            packet.setEntityID(entityID);
            packet.setType(78);
            packet.setX(player.getLocation().getX());
            packet.setY(player.getLocation().getY());
            packet.setZ(player.getLocation().getZ());
            packet.sendPacket(player);

            WrapperPlayServerEntityMetadata packet2 = new WrapperPlayServerEntityMetadata();
            packet2.setEntityID(entityID);
            WrappedDataWatcher dataWatcher = new WrappedDataWatcher(packet2.getMetadata());
            WrappedDataWatcher.WrappedDataWatcherObject noGravityIndex = new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));
            dataWatcher.setObject(noGravityIndex, true);
            packet2.setMetadata(dataWatcher.getWatchableObjects());
            packet2.sendPacket(player);

            armorstands.put(player.getUniqueId(), entityID);
            player.sendMessage("§aplacing");
            return true;
        }*/
        return true;
    }

    Set<Material> targetBlacklist = new HashSet<>();
    HashMap<UUID, Integer> armorstands = new HashMap<>();
    HashMap<Integer, Location> locations = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().contains(" Mine") && e.getView().getTitle().contains("§0")) {
            if (e.getCurrentItem().getType().equals(Material.MINECART)) {
                e.setCancelled(true);

                Location loc = e.getClickedInventory().getLocation();
                Mine mine = null;
                for (Mine testMine : mines) {
                    if (testMine.getChest().getLocation().equals(loc)) {
                        mine = testMine;
                        break;
                    }
                }

                if (mine != null) {
                    mine.destroy();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());

        try {
            if (item.getItemMeta().getDisplayName().contains("§eMine") && !armorstands.containsKey(player.getUniqueId())) {
                WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity();
                Integer entityID = (int)(Math.random() * Integer.MAX_VALUE);
                packet.setEntityID(entityID);
                packet.setType(78);

                Block target = player.getTargetBlock(targetBlacklist, 10);
                Location loc = target.getLocation().getWorld().getHighestBlockAt(target.getLocation()).getLocation();

                packet.setX(loc.getX());
                packet.setY(loc.clone().getY() - 1.5f);
                packet.setZ(loc.getZ());
                packet.sendPacket(player);

                checkArmorStand(player, entityID, loc);

                WrapperPlayServerEntityMetadata packet2 = new WrapperPlayServerEntityMetadata();
                packet2.setEntityID(entityID);
                WrappedDataWatcher dataWatcher = new WrappedDataWatcher(packet2.getMetadata());
                WrappedDataWatcher.WrappedDataWatcherObject noGravityIndex = new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class));
                WrappedDataWatcher.WrappedDataWatcherObject invisibleIndex = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
                dataWatcher.setObject(invisibleIndex, (byte)0x20);
                dataWatcher.setObject(noGravityIndex, true);
                packet2.setMetadata(dataWatcher.getWatchableObjects());
                packet2.sendPacket(player);

                armorstands.put(player.getUniqueId(), entityID);
                player.sendMessage("§aPlacing");
            } else {
                if (!item.getItemMeta().getDisplayName().contains("§eMine")) {
                    if (armorstands.containsKey(player.getUniqueId())) {
                        int id = armorstands.get(player.getUniqueId());

                        WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                        destroy.setEntityIds(new int[] {id});
                        destroy.sendPacket(player);

                        locations.remove(id);
                        armorstands.remove(player.getUniqueId());
                    }
                }
            }
        } catch (Exception ex) {
            if (armorstands.containsKey(player.getUniqueId())) {
                int id = armorstands.get(player.getUniqueId());

                WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                destroy.setEntityIds(new int[] {id});
                destroy.sendPacket(player);

                locations.remove(id);
                armorstands.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (TribePipes.instance.isPipe(e.getBlock().getLocation()) != null) return;
        Bukkit.getPlayer("Nowaha").sendMessage("not pipe??/");


        for (Mine mine : mines) {
            if (mine.getBlocks().contains(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getType().equals(Material.END_ROD) && e.getItemInHand().hasItemMeta() && e.getItemInHand().getItemMeta().hasDisplayName() && e.getItemInHand().getItemMeta().getDisplayName().contains("§dPipe")) return;
        for (Mine mine : mines) {
            if (mine.getBlocks().contains(e.getBlock().getLocation())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    void checkArmorStand(Player player, int id, Location loc) {
        WrapperPlayServerEntityEquipment equipment = new WrapperPlayServerEntityEquipment();
        equipment.setEntityID(id);
        equipment.setSlot(EnumWrappers.ItemSlot.HEAD);

        //WrapperPlayServerEntityEquipment equipment2 = new WrapperPlayServerEntityEquipment();
        //equipment2.setEntityID(id);
        //equipment2.setSlot(EnumWrappers.ItemSlot.LEGS);
        if (isInMineRange(loc)) {
            /*ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);

            LeatherArmorMeta bootsmeta = (LeatherArmorMeta) boots.getItemMeta();
            LeatherArmorMeta leggingsmeta = (LeatherArmorMeta) leggings.getItemMeta();

            bootsmeta.setColor(Color.RED);
            leggingsmeta.setColor(Color.RED);

            boots.setItemMeta(bootsmeta);
            leggings.setItemMeta(leggingsmeta);

            equipment.setItem(boots);
            equipment2.setItem(leggings);*/
            equipment.setItem(new ItemStack(Material.RED_WOOL));
        } else {
            /*ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            //ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);

            LeatherArmorMeta bootsmeta = (LeatherArmorMeta) boots.getItemMeta();
            //LeatherArmorMeta leggingsmeta = (LeatherArmorMeta) leggings.getItemMeta();

            bootsmeta.setColor(Color.GREEN);
            //leggingsmeta.setColor(Color.GREEN);

            boots.setItemMeta(bootsmeta);
            //leggings.setItemMeta(leggingsmeta);

            equipment.setItem(boots);
            //equipment2.setItem(leggings);*/

            equipment.setItem(new ItemStack(Material.LIME_WOOL));
        }
        equipment.sendPacket(player);
        //equipment2.sendPacket(player);

        locations.put(id, loc);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        try {
            Block target = player.getTargetBlock(targetBlacklist, 10);
            Location loc = target.getLocation().getWorld().getHighestBlockAt(target.getLocation()).getLocation();
            int entityID = armorstands.get(player.getUniqueId());

            Location oldLoc = locations.get(entityID);
            if (oldLoc.getX() != loc.getX() || oldLoc.getY() != loc.getY() || oldLoc.getZ() != loc.getZ()) {
                WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport();
                teleport.setEntityID(entityID);
                teleport.setX((float) loc.getBlockX() + 0.5f);
                teleport.setY(loc.clone().getBlockY() - 1.5f);
                teleport.setZ((float) loc.getBlockZ() + 0.5f);
                teleport.sendPacket(player);

                checkArmorStand(player, entityID, loc);
            }

            WrapperPlayServerEntityLook look = new WrapperPlayServerEntityLook();
            look.setEntityID(entityID);
            look.setYaw(((float) Math.round(player.getLocation().getYaw() / 90f) * -90f));
            look.sendPacket(player);

            locations.put(entityID, loc.getBlock().getLocation());
        } catch (Exception ex) {

        }
    }

    List<UUID> placing = new ArrayList<>();

    List<UUID> doublePrevention = new ArrayList<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (doublePrevention.contains(player.getUniqueId())) return;
        doublePrevention.add(e.getPlayer().getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            doublePrevention.remove(e.getPlayer().getUniqueId());
        }, 3);

        ItemStack item = player.getItemInHand();

        if (placing.contains(player.getUniqueId())) return;
        if (!armorstands.containsKey(player.getUniqueId())) return;

        try {
            int id = armorstands.get(player.getUniqueId());
            Location loc = locations.get(id);
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
                if (isInMineRange(loc)) {
                    player.sendMessage("§cYou can't place this here!");
                    e.setCancelled(true);
                    return;
                }
                e.setCancelled(true);
                if (item.getAmount() - 1 == 0) {
                    ItemStack itemClone = item.clone();
                    item.setAmount(0);
                    WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
                    destroy.setEntityIds(new int[] {id});
                    destroy.sendPacket(player);

                    placing.add(player.getUniqueId());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                        spawnMine(loc, 5, player.getFacing().getOppositeFace());

                        player.sendMessage("§ePlaced " + ChatColor.stripColor(itemClone.getItemMeta().getDisplayName()) + "!");
                        placing.remove(player.getUniqueId());
                    });

                    locations.remove(id);
                    armorstands.remove(player.getUniqueId());
                } else {
                    ItemStack itemClone = item.clone();
                    item.setAmount(item.getAmount() - 1);

                    placing.add(player.getUniqueId());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                        spawnMine(loc, 5, player.getFacing().getOppositeFace());
                        locations.put(id, loc.getWorld().getHighestBlockAt(locations.get(id)).getLocation());

                        player.sendMessage("§ePlaced " + ChatColor.stripColor(itemClone.getItemMeta().getDisplayName()) + "!");
                        placing.remove(player.getUniqueId());
                    });
                }
            }
        } catch (Exception ex) {
            if (armorstands.containsKey(player.getUniqueId())) {
                locations.remove(armorstands.get(player.getUniqueId()));
                armorstands.remove(player.getUniqueId());
            }
        }
    }

    void spawnMine(Location loc, int level, BlockFace rotation) {
        try {
            File schematic = new File(getDataFolder().getParentFile(), "WorldEdit/schematics/mine" + level + ".schem");

            ClipboardFormat format = ClipboardFormats.findByFile(schematic);
            ClipboardReader reader = format.getReader(new FileInputStream(schematic));
            Clipboard clipboard = reader.read();

            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(loc.getWorld());
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, -1);
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(loc.getBlockX(), loc.clone().getBlockY() + 5, loc.getBlockZ())).ignoreAirBlocks(false).build();

            Operations.complete(operation);
            editSession.flushSession();

            Block block = new Location(loc.getWorld(), loc.getBlockX(), loc.clone().getBlockY() + 1, loc.getBlockZ()).getBlock();
            org.bukkit.block.data.type.Chest data = (org.bukkit.block.data.type.Chest) block.getBlockData();
            data.setFacing(rotation);
            block.setBlockData(data);

            Location centerBlock = loc.clone().subtract(0, 1, 0);

            Chest chest = (Chest) block.getState();

            Mine mine = new Mine(1, centerBlock, chest);
            mine.startConstruction(new Date().getTime() + (1000 * 60));
            mines.add(mine);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    boolean isInMineRange(Location loc) {
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location loc2 = loc.clone().add(x, y, z);
                    if (loc2.getBlock().getType().equals(Material.AIR)) continue;
                    if (!targetBlacklist.contains(loc2.getBlock().getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
