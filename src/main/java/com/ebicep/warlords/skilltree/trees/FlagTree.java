package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class FlagTree extends DoubleUltTree {

    public FlagTree(SkillTree skillTree) {
        super(skillTree, null, "Flag", new ItemStack(Material.BANNER));
        firstUpgrade = new Upgrade(this, 4, 4, 0, 1, "TODO", "TODO");
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "TODO", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "TODO", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 0, 1, "TODO", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "TODO", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "TODO", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 0, 1, "TODO", "TODO"));
    }

}