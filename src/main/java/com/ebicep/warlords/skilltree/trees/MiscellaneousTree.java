package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class MiscellaneousTree extends DoubleUltTree implements IDoubleUlt {

    public MiscellaneousTree(SkillTree skillTree) {
        super(skillTree, null, "Miscellaneous", new ItemStack(Material.YELLOW_FLOWER));
        firstUpgrade = new Upgrade(this, 4, 4, 0, 1, "Increase Passive Point Gain", "TODO");
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Point Investment", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Passive Energy Gain", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 0, 1, "Passive Regeneration", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Point Loan", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Active Energy Gain", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 0, 1, "Reduced Respawn Time", "TODO"));
    }

    @Override
    public void doFirstUpgrade() {

    }

    @Override
    public void doFirstLeftUpgrade() {

    }

    @Override
    public void doSecondLeftUpgrade() {

    }

    @Override
    public void doThirdLeftUpgrade() {

    }

    @Override
    public void doFirstRightUpgrade() {

    }

    @Override
    public void doSecondRightUpgrade() {

    }

    @Override
    public void doThirdRightUpgrade() {

    }
}
