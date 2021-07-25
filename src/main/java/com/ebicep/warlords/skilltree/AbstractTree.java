package com.ebicep.warlords.skilltree;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.skilltree.trees.DoubleUltTree;
import com.ebicep.warlords.skilltree.trees.SingleUltTree;
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
        this.itemStack = itemStack;
    }

    public abstract boolean canUpgrade(Upgrade upgrade);

    public abstract void openTreeMenu(Player player);

    //■
    public ItemStack getTreeItemStack() {
        ItemBuilder itemBuilder = new ItemBuilder(itemStack)
                .name(ChatColor.GOLD + name);
        if (this instanceof DoubleUltTree) {
            itemBuilder.lore(
                    ((DoubleUltTree) this).getFirstUpgrade().getUpgradeInfo(),
                    "",
                    ((DoubleUltTree) this).getLeftUpgrades().getFirst().getUpgradeInfo(),
                    ((DoubleUltTree) this).getLeftUpgrades().get(1).getUpgradeInfo(),
                    ((DoubleUltTree) this).getLeftUpgrades().getLast().getUpgradeInfo(),
                    "",
                    ((DoubleUltTree) this).getRightUpgrades().getFirst().getUpgradeInfo(),
                    ((DoubleUltTree) this).getRightUpgrades().get(1).getUpgradeInfo(),
                    ((DoubleUltTree) this).getRightUpgrades().getFirst().getUpgradeInfo()
            );
        } else if (this instanceof SingleUltTree) {
            itemBuilder.lore(
                    ((SingleUltTree) this).getLeftUpgrades().getFirst().getUpgradeInfo(),
                    ((SingleUltTree) this).getLeftUpgrades().get(1).getUpgradeInfo(),
                    ((SingleUltTree) this).getLeftUpgrades().getLast().getUpgradeInfo(),
                    "",
                    ((SingleUltTree) this).getRightUpgrades().getFirst().getUpgradeInfo(),
                    ((SingleUltTree) this).getRightUpgrades().get(1).getUpgradeInfo(),
                    ((SingleUltTree) this).getRightUpgrades().getLast().getUpgradeInfo(),
                    "",
                    ((SingleUltTree) this).getLastUpgrade().getUpgradeInfo()
            );
        }
        return itemBuilder.get();
    }

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
