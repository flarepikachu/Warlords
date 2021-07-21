package com.ebicep.warlords.skilltree;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractTree {

    //tree has upgrades depending on double vs single ult
    protected SkillTree skillTree;
    protected AbstractAbility ability;
    protected String name;
    protected ItemStack itemStack;

    public AbstractTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        this.skillTree = skillTree;
        this.ability = ability;
        this.name = name;
        this.itemStack = new ItemBuilder(itemStack).name(ChatColor.GOLD + name).get();
    }

    public abstract boolean canUpgrade(Upgrade upgrade);

    public abstract void openTreeMenu(Player player);

    public AbstractAbility getAbility() {
        return ability;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public SkillTree getSkillTree() {
        return skillTree;
    }
}
