package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class FlagTree extends DoubleUltTree {

    public FlagTree(SkillTree skillTree) {
        super(skillTree, null, "Flag", new ItemStack(Material.BANNER));
        firstUpgrade = new Upgrade(this, 4, 4, 50, 1, "Healing Increase", "Receive more healing while holding the flag");

        leftUpgrades.add(new Upgrade(this, 3, 3, 50, 1, "Bonus Speed", "Increases effect of Speed Powerup"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 75, 1, "Flag Resistance", "Receive a short duration of reduced damage after picking up the flag"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 150, 1, "Flag Invisibility", "Receive a short period of invisibility after picking up the flag"));

        rightUpgrades.add(new Upgrade(this, 5, 3, 75, 1, "Reduced Respawn", "If killed while holding the flag, respawn timer is reduced by a certain amount"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 100, 1, "Flag Damage", "Increases damage while holding flag depending on flag percentage damage bonus"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 200, 1, "Flag Percentage Cap", "The flag extra percentage damage is capped"));
    }

    @Override
    public void doFirstUpgrade() {
        firstUpgrade.setCurrentEffect("Receive " + (firstUpgrade.getCounterPlusOne() * 10) + "% more healing while holding the flag");
    }

    @Override
    public void doFirstLeftUpgrade() {
        leftUpgrades.getFirst().setCurrentEffect("Receive " + (leftUpgrades.getFirst().getCounterPlusOne() * 5) + "% more speed from speed powerups");
    }

    @Override
    public void doSecondLeftUpgrade() {
        leftUpgrades.get(1).setCurrentEffect("Receive " + (leftUpgrades.get(1).getCounterPlusOne() * 10) + "% less damage after picking up the flag for 3s");
    }

    @Override
    public void doThirdLeftUpgrade() {
        leftUpgrades.getLast().setCurrentEffect("Receive " + (leftUpgrades.getLast().getCounterPlusOne() * 3) + "s of invisibility after picking up the flag");
    }

    @Override
    public void doFirstRightUpgrade() {
        rightUpgrades.getFirst().setCurrentEffect(-rightUpgrades.getFirst().getCounterPlusOne() + " second" + (rightUpgrades.getFirst().getCounterPlusOne() == 1 ? "" : "s") + " on respawn if you were holding the flag");
    }

    @Override
    public void doSecondRightUpgrade() {
        rightUpgrades.get(1).setCurrentEffect("Deal " + (rightUpgrades.get(1).getCounterPlusOne() * 10) + "% more damage while holding the flag");
    }

    @Override
    public void doThirdRightUpgrade() {

    }
}