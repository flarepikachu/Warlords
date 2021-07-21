package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class MountTree extends DoubleUltTree {

    public MountTree(SkillTree skillTree) {
        super(skillTree, null, "Mount", new ItemStack(Material.GOLD_BARDING));
        firstUpgrade = new Upgrade(this, 4, 4, 0, 1, "Mount protection", "TODO");
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Mount speed", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Turbo speed", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 0, 1, "Cooldown reduction", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Dismount resistance", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Horse armour", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 0, 1, "Divine mount?", "TODO"));
    }

}