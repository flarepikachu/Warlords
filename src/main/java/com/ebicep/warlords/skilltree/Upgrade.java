package com.ebicep.warlords.skilltree;

import com.ebicep.warlords.skilltree.trees.IDoubleUlt;
import com.ebicep.warlords.skilltree.trees.ISingleUlt;
import com.ebicep.warlords.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Upgrade {

    private AbstractTree tree;
    private int x;
    private int y;
    private int counter;
    private int maxCounter;
    private int cost;
    private String name;
    private String description;
    private String subDescription = "";
    private String currentEffect = "";
    private boolean locked = false;

    public Upgrade(AbstractTree tree, int x, int y, int cost, int maxCounter, String name, String description) {
        this.tree = tree;
        this.x = x;
        this.y = y;
        this.counter = 0;
        this.maxCounter = maxCounter;
        this.cost = cost;
        this.name = name;
        this.description = description;
    }

    public void onClick() {
        if (tree.canUpgrade(this)) {
            if (tree.getSkillTree().getWarlordsPlayer().getPoints() < cost) {
                tree.skillTree.getWarlordsPlayer().sendMessage(ChatColor.RED + "Insufficient Amount of Points! retard");
            } else {
                if (tree.ability instanceof IDoubleUlt) {
                    switch (getUpgradeNumber()) {
                        case 1:
                            ((IDoubleUlt) tree.ability).doFirstLeftUpgrade();
                            break;
                        case 2:
                            ((IDoubleUlt) tree.ability).doSecondLeftUpgrade();
                            break;
                        case 3:
                            ((IDoubleUlt) tree.ability).doThirdLeftUpgrade();
                            break;
                        case 4:
                            ((IDoubleUlt) tree.ability).doFirstUpgrade();
                            break;
                        case 5:
                            ((IDoubleUlt) tree.ability).doFirstRightUpgrade();
                            break;
                        case 6:
                            ((IDoubleUlt) tree.ability).doSecondRightUpgrade();
                            break;
                        case 7:
                            ((IDoubleUlt) tree.ability).doThirdRightUpgrade();
                            break;
                    }
                } else if (tree.ability instanceof ISingleUlt) {
                    switch (getUpgradeNumber()) {
                        case 1:
                            ((ISingleUlt) tree.ability).doFirstLeftUpgrade();
                            break;
                        case 2:
                            ((ISingleUlt) tree.ability).doSecondLeftUpgrade();
                            break;
                        case 3:
                            ((ISingleUlt) tree.ability).doThirdLeftUpgrade();
                            break;
                        case 4:
                            ((ISingleUlt) tree.ability).doLastUpgrade();
                            break;
                        case 5:
                            ((ISingleUlt) tree.ability).doFirstRightUpgrade();
                            break;
                        case 6:
                            ((ISingleUlt) tree.ability).doSecondRightUpgrade();
                            break;
                        case 7:
                            ((ISingleUlt) tree.ability).doThirdRightUpgrade();
                            break;
                    }
                } else if (tree instanceof IDoubleUlt) {
                    switch (getUpgradeNumber()) {
                        case 1:
                            ((IDoubleUlt) tree).doFirstLeftUpgrade();
                            break;
                        case 2:
                            ((IDoubleUlt) tree).doSecondLeftUpgrade();
                            break;
                        case 3:
                            ((IDoubleUlt) tree).doThirdLeftUpgrade();
                            break;
                        case 4:
                            ((IDoubleUlt) tree).doFirstUpgrade();
                            break;
                        case 5:
                            ((IDoubleUlt) tree).doFirstRightUpgrade();
                            break;
                        case 6:
                            ((IDoubleUlt) tree).doSecondRightUpgrade();
                            break;
                        case 7:
                            ((IDoubleUlt) tree).doThirdRightUpgrade();
                            break;
                    }
                }
                this.counter++;
                tree.skillTree.getWarlordsPlayer().subtractPoints(cost);
                tree.skillTree.getWarlordsPlayer().sendMessage(ChatColor.GREEN + "Upgraded " + name);
                tree.openTreeMenu((Player) tree.skillTree.getWarlordsPlayer().getEntity());
            }
        } else {
            if (counter >= maxCounter) {
                tree.skillTree.getWarlordsPlayer().sendMessage(ChatColor.RED + "You Maxed This Bitch Out!!!");
            } else {
                tree.skillTree.getWarlordsPlayer().sendMessage(ChatColor.RED + "You Must Unlock The Previous Upgrades First! dumbass");
            }
        }
    }

    //returns 1-7, 1 is first left, 4 is middle, 5 is first right
    private int getUpgradeNumber() {
        switch (x) {
            case 3:
                if (tree.ability instanceof IDoubleUlt || tree instanceof IDoubleUlt)
                    return 4 - y;
                else
                    return 5 - y;
            case 5:
                if (tree.ability instanceof IDoubleUlt || tree instanceof IDoubleUlt)
                    return 8 - y;
                else
                    return 9 - y;
            case 4:
                return 4;
        }
        return -1;
    }

    public ItemStack getItem() {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GREEN + cost);
        lore.add(ChatColor.GRAY.toString() + "Unlocked: " + ChatColor.YELLOW + counter + "/" + maxCounter);
        if (!description.isEmpty()) {
            lore.add(ChatColor.GRAY.toString() + "Description: " + ChatColor.LIGHT_PURPLE + description);
        }
        if (!subDescription.isEmpty()) {
            lore.add(ChatColor.GRAY.toString() + subDescription);
        }
        if (!currentEffect.isEmpty()) {
            lore.add(ChatColor.GRAY.toString() + "Current Effect: " + ChatColor.AQUA + currentEffect);
        } else if (counter != 0) {
            lore.add(ChatColor.GRAY.toString() + "Current Effect: " + ChatColor.AQUA + description);
        }
        if (locked) {
            return new ItemBuilder(Material.STAINED_CLAY, counter, (byte) 14)
                    .name(ChatColor.GOLD + name + ChatColor.GRAY + " (LOCKED)")
                    .lore(lore)
                    .get();
        } else if (counter != 0) {
            return new ItemBuilder(Material.STAINED_CLAY, counter, (byte) 5)
                    .name(ChatColor.GOLD + name)
                    .lore(lore)
                    .get();
        } else {
            return new ItemBuilder(Material.STAINED_CLAY)
                    .name(ChatColor.GOLD + name)
                    .lore(lore)
                    .get();
        }
    }

    public AbstractTree getTree() {
        return tree;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCounter() {
        return counter;
    }

    public int getCounterPlusOne() {
        return counter + 1;
    }

    public int getMaxCounter() {
        return maxCounter;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setSubDescription(String subDescription) {
        this.subDescription = subDescription;
    }

    public void setCurrentEffect(String currentEffect) {
        this.currentEffect = currentEffect;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
