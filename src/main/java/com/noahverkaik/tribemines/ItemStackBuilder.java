package com.noahverkaik.tribemines;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/***
 * @author Noah Verkaik
 *
 * This class allows you to easily create item stacks,
 * much like a StringBuilder allows you to create strings.
 * Most of the properties are editable with simple
 * modifiers.
 */

public class ItemStackBuilder implements Listener {

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    /***
     * Initializes a new ItemStackBuilder
     * @param material The material the item should start off being.
     */
    public ItemStackBuilder(Material material)
    {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
    }

    /***
     * Change the amount of the item.
     * @param amount The new amount
     */
    public ItemStackBuilder amount(int amount)
    {
        itemStack.setAmount(amount);
        return this;
    }

    /***
     * Change the durability of the item.
     * @param durability The new durability
     */
    public ItemStackBuilder durability(short durability)
    {
        itemStack.setDurability(durability);
        return this;
    }

    /***
     * Change the display name of the item.
     * @param displayName The new display name
     */
    public ItemStackBuilder displayName(String displayName)
    {
        itemMeta.setDisplayName(displayName);
        return this;
    }

    /***
     * Change the lore of the item.
     * @param lore The new lore
     */
    public ItemStackBuilder lore(String[] lore)
    {
        List<String> loreArray = new ArrayList<String>();

        for (String loreBit : lore)
        {
            // Might want to replace this with ChatColor.WHITE in your code.
            loreArray.add(ChatColor.WHITE + loreBit);
        }

        itemMeta.setLore(loreArray);
        return this;
    }

    public ItemStackBuilder enchant(Enchantment enchanement, int level, boolean ignoreLevelRestriction)
    {
        itemMeta.addEnchant(enchanement, level, ignoreLevelRestriction);
        return this;
    }

    public ItemStackBuilder itemFlags(ItemFlag... flags)
    {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemStackBuilder skullOwner(Player player) {
        SkullMeta playerheadmeta = (SkullMeta) itemMeta;
        playerheadmeta.setOwner(player.getName());
        return this;
    }

    /***
     * Turns the builder into an actual ItemStack which is able to be
     * used like you normally would.
     * @return The final ItemStack
     */
    public ItemStack build()
    {
        ItemStack clonedStack = itemStack.clone();
        clonedStack.setItemMeta(itemMeta.clone());
        return clonedStack;
    }
}


