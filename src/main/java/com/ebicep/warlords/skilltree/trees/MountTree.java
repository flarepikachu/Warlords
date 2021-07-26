package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public class MountTree extends DoubleUltTree {

    public MountTree(SkillTree skillTree) {
        super(skillTree, null, "Mount", new ItemStack(Material.GOLD_BARDING));
        firstUpgrade = new Upgrade(this, 4, 4, 100, 1, "Mount protection", "Any ability that dismounts the player does not damage the player");

        leftUpgrades.add(new Upgrade(this, 3, 3, 150, 1, "Mount speed", "Increase mount speed by a certain percentage"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 100, 1, "Turbo speed", "Activated ability, gain a significant increase to mount speed for a short duration"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 200, 1, "Cooldown reduction", "TODO"));

        rightUpgrades.add(new Upgrade(this, 5, 3, 100, 1, "Dismount resistance", "Upon dismounting, gain a damage reduction for a short period of time"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 150, 1, "Horse armour", "Mount can take a certain amount of damage before being dismounted"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 250, 1, "Divine mount?", "TODO"));
    }

    @Override
    public void doFirstUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().setDamageOnDismount(false);
    }

    @Override
    public void doFirstLeftUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().addSpeed(.05f);
        leftUpgrades.getFirst().setCurrentEffect("Increases horse speed by " + (leftUpgrades.getFirst().getCounterPlusOne() * 20) + "%");
    }

    @Override
    public void doSecondLeftUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().setHasTurboSpeed(true);
        leftUpgrades.get(1).setCurrentEffect("Right click sugar to activate turbo speed");
    }

    @Override
    public void doThirdLeftUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().decrementCooldown();
        leftUpgrades.getLast().setCurrentEffect("Reduces horse cooldown by " + leftUpgrades.getLast().getCounterPlusOne() + " seconds");
    }

    @Override
    public void doFirstRightUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().decrementDamageResistance();
        rightUpgrades.getFirst().setCurrentEffect("Reduces damage taken by " + Math.round((1 - skillTree.getWarlordsPlayer().getHorse().getDamageResistance()) * 100) + "% after dismounting");
    }

    @Override
    public void doSecondRightUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().setMaxHealth(500);
        rightUpgrades.get(1).setCurrentEffect("Your horse now has " + (int) skillTree.getWarlordsPlayer().getHorse().getMaxHealth2() + " health");
    }

    @Override
    public void doThirdRightUpgrade() {
        skillTree.getWarlordsPlayer().getHorse().setHasDivineMount(true);
    }
}