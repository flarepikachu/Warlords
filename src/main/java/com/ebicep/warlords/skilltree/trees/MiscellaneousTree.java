package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


public class MiscellaneousTree extends DoubleUltTree {

    public MiscellaneousTree(SkillTree skillTree) {
        super(skillTree, null, "Miscellaneous", new ItemStack(Material.YELLOW_FLOWER));
        firstUpgrade = new Upgrade(this, 4, 4, 75, 5, "Increase Passive Point Gain", "Increase passive point gain over time");

        leftUpgrades.add(new Upgrade(this, 3, 3, 150, 3, "Point Investment", "Receive points after a certain period of time"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 75, 3, "Passive Energy Gain", "Increase passive energy gain"));
        leftUpgrades.add(new Upgrade(this, 3, 1, 250, 3, "Passive Regeneration", "Reduces cooldown on passive regen / \nincrease effect of passive regen"));

        rightUpgrades.add(new Upgrade(this, 5, 3, 100, 3, "Point Loan", "Receive an amount of points immediately \nbut reduced passive point gain over a period of time"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 75, 3, "Active Energy Gain", "Increase energy received from each melee hit"));
        rightUpgrades.add(new Upgrade(this, 5, 1, 250, 3, "Reduced Respawn Time", "Reduces the time the player takes respawning"));
    }

    @Override
    public void doFirstUpgrade() {
        skillTree.getWarlordsPlayer().addPointGainRate(1);
        firstUpgrade.setCurrentEffect(firstUpgrade.getCounterPlusOne() + " more point" + (firstUpgrade.getCounterPlusOne() == 1 ? "" : "s") + " per second");
    }

    @Override
    public void doFirstLeftUpgrade() {
        leftUpgrades.getFirst().setLocked(true);
        int pointsInvested = 150;
        skillTree.getWarlordsPlayer().addPoints(-150);
        skillTree.getWarlordsPlayer().sendMessage(ChatColor.GREEN + "You invested " + pointsInvested + " points");
        new BukkitRunnable() {
            int timeLeft = 60;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    leftUpgrades.getFirst().setLocked(false);
                    skillTree.getWarlordsPlayer().addPoints(pointsInvested * 1.5f);
                    skillTree.getWarlordsPlayer().sendMessage(ChatColor.GREEN + "You received " + (int) (pointsInvested * 1.5) + " from your investment");
                    leftUpgrades.getFirst().setCurrentEffect("");
                    this.cancel();
                } else {
                    leftUpgrades.getFirst().setCurrentEffect(timeLeft + "s left until investment");
                }
                timeLeft--;
            }
        }.runTaskTimer(Warlords.getInstance(), 0, 20);
    }

    @Override
    public void doSecondLeftUpgrade() {
        skillTree.getWarlordsPlayer().getSpec().setEnergyPerSec(skillTree.getWarlordsPlayer().getSpec().getEnergyPerSec() + 5);
        leftUpgrades.get(1).setCurrentEffect((leftUpgrades.get(1).getCounterPlusOne() * 5) + " more energy per second");
    }

    @Override
    public void doThirdLeftUpgrade() {
        skillTree.getWarlordsPlayer().setRegenDivisor(skillTree.getWarlordsPlayer().getRegenDivisor() - 5);
        skillTree.getWarlordsPlayer().setBaseRegenTimer(skillTree.getWarlordsPlayer().getBaseRegenTimer() - 2);
        float baseRegen = skillTree.getWarlordsPlayer().getMaxHealth() / 55.3f;
        float newRegen = skillTree.getWarlordsPlayer().getMaxHealth() / skillTree.getWarlordsPlayer().getRegenDivisor();
        leftUpgrades.getLast().setCurrentEffect("\n" + Math.round(newRegen - baseRegen) + " more health per second" + "\n" +
                (leftUpgrades.getLast().getCounterPlusOne() * 2) + " less seconds to regen");
    }

    @Override
    public void doFirstRightUpgrade() {
        rightUpgrades.getFirst().setLocked(true);

        skillTree.getWarlordsPlayer().addPoints(100);
        //this doesnt effect firstupgrade == 5/2 = 2.5 + 1 = 3.5 vs 6/2 = 3
        skillTree.getWarlordsPlayer().setPointGainRate(skillTree.getWarlordsPlayer().getPointGainRate() / 2);
        new BukkitRunnable() {
            int timeLeft = 60;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    rightUpgrades.getFirst().setLocked(false);
                    skillTree.getWarlordsPlayer().setPointGainRate(5 + rightUpgrades.getFirst().getCounter());
                    rightUpgrades.getFirst().setCurrentEffect("");
                    this.cancel();
                } else {
                    rightUpgrades.getFirst().setCurrentEffect(timeLeft + "s left until investment");
                }
                timeLeft--;

            }
        }.runTaskTimer(Warlords.getInstance(), 0, 20);
    }

    @Override
    public void doSecondRightUpgrade() {
        skillTree.getWarlordsPlayer().getSpec().setEnergyOnHit((int) (skillTree.getWarlordsPlayer().getSpec().getEnergyOnHit() * 1.5));
        rightUpgrades.get(1).setCurrentEffect(((int) (skillTree.getWarlordsPlayer().getSpec().getEnergyOnHit() * .5)) + " more energy per hit");
    }

    @Override
    public void doThirdRightUpgrade() {
        skillTree.getWarlordsPlayer().setBaseAdditionalRespawn(skillTree.getWarlordsPlayer().getBaseAdditionalRespawn() - 1);
        rightUpgrades.getLast().setCurrentEffect(skillTree.getWarlordsPlayer().getBaseAdditionalRespawn() + " seconds on respawn");
    }
}

