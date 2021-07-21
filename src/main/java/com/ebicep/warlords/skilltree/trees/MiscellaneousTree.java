package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


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
        skillTree.getWarlordsPlayer().addPointGainRate(1);
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
        rightUpgrades.getFirst().setLocked(true);

        skillTree.getWarlordsPlayer().addPoints(100);
        //this doesnt effect firstupgrade == 5/2 = 2.5 + 1 = 3.5 vs 6/2 = 3
        skillTree.getWarlordsPlayer().setPointGainRate(skillTree.getWarlordsPlayer().getPointGainRate() / 2);

        new BukkitRunnable() {
            @Override
            public void run() {
                rightUpgrades.getFirst().setLocked(false);
                skillTree.getWarlordsPlayer().setPointGainRate(5 + rightUpgrades.getFirst().getCounter());

            }
        }.runTaskLater(Warlords.getInstance(), 60 * 20);
    }

    @Override
    public void doSecondRightUpgrade() {

    }

    @Override
    public void doThirdRightUpgrade() {

    }

}

