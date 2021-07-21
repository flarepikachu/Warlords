package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class MiscellaneousTree extends DoubleUltTree implements IDoubleUlt {

    public MiscellaneousTree(SkillTree skillTree) {
        super(skillTree, null, "Miscellaneous", new ItemStack(Material.YELLOW_FLOWER));
        firstUpgrade = new Upgrade(this, 4, 4, 0, 1, "Increase passive point gain", "TODO");
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Point investment", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Passive energy gain", "TODO"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 0, 1, "Passive regeneration", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Point loan", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Active energy gain", "TODO"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 0, 1, "Reduced respawn time", "TODO"));
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
