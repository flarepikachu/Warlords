package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.classes.internal.AbstractProjectileBase;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import com.ebicep.warlords.skilltree.trees.SingleUltTree;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.PlayerFilter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WaterBolt extends AbstractProjectileBase {

    private int maxFullDamageDistance = 40;
    private double directHitMultiplier = 1.15;
    private float hitbox = 4;

    private WaterBoltTree waterBoltTree;
    private float selfHealMultiplier = 1;
    private boolean reduceCooldownRain = false;
    private boolean reduceCooldownTeammates = false;


    public WaterBolt() {
        super("Water Bolt", 328, 452, 0, 85, 20, 175, 2, 300, true);
    }

    @Override
    protected String getActivationSound() {
        return "mage.waterbolt.activation";
    }

    @Override
    protected void playEffect(Location currentLocation, int animationTimer) {
        ParticleEffect.DRIP_WATER.display(0.3f, 0.3f, 0.3f, 0.1F, 2, currentLocation, 500);
        ParticleEffect.ENCHANTMENT_TABLE.display(0, 0, 0, 0.1F, 1, currentLocation, 500);
        ParticleEffect.VILLAGER_HAPPY.display(0, 0, 0, 0.1F, 1, currentLocation, 500);
        ParticleEffect.CLOUD.display(0, 0, 0, 0F, 1, currentLocation, 500);
    }

    @Override
    protected void onHit(WarlordsPlayer shooter, Location currentLocation, Location startingLocation, WarlordsPlayer victim) {
        ParticleEffect.HEART.display(1, 1, 1, 0.2F, 3, currentLocation, 500);
        ParticleEffect.VILLAGER_HAPPY.display(1, 1, 1, 0.2F, 5, currentLocation, 500);

        for (Player player1 : shooter.getWorld().getPlayers()) {
            player1.playSound(currentLocation, "mage.waterbolt.impact", 2, 1);
        }

        double distanceSquared = currentLocation.distanceSquared(startingLocation);
        double toReduceBy = maxFullDamageDistance * maxFullDamageDistance > distanceSquared ? 1 :
                1 - (Math.sqrt(distanceSquared) - maxFullDamageDistance) / 100.;
        if (toReduceBy < 0) toReduceBy = 0;
        if (victim != null) {
            if (victim.isTeammateAlive(shooter)) {
                victim.addHealth(shooter,
                        name,
                        (float) (minDamageHeal * directHitMultiplier * toReduceBy),
                        (float) (maxDamageHeal * directHitMultiplier * toReduceBy),
                        critChance,
                        critMultiplier
                );
                if (reduceCooldownRain) {
                    reduceHealingRainCooldown(shooter);
                }
            } else {
                victim.addHealth(shooter,
                        name,
                        (float) (-231 * directHitMultiplier * toReduceBy),
                        (float) (-299 * directHitMultiplier * toReduceBy),
                        critChance,
                        critMultiplier
                );
            }
        }
        for (WarlordsPlayer nearEntity : PlayerFilter
                .entitiesAround(currentLocation, hitbox, hitbox, hitbox)
                .excluding(victim)
                .isAlive()
        ) {
            if (nearEntity.isTeammateAlive(shooter)) {
                nearEntity.addHealth(
                        shooter,
                        name,
                        (float) (minDamageHeal * toReduceBy * (nearEntity == shooter ? selfHealMultiplier : 1)),
                        (float) (maxDamageHeal * toReduceBy * (nearEntity == shooter ? selfHealMultiplier : 1)),
                        critChance,
                        critMultiplier
                );
                if (reduceCooldownRain) {
                    reduceHealingRainCooldown(shooter);
                }
                if (reduceCooldownTeammates && nearEntity != shooter) {
                    reduceCooldownTeammate(nearEntity);
                }
            } else {
                nearEntity.addHealth(
                        shooter,
                        name,
                        (float) (-231 * toReduceBy),
                        (float) (-299 * toReduceBy),
                        critChance,
                        critMultiplier
                );
            }
        }
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Shoot a bolt of water that will burst\n" +
                "§7for §c231 §7- §c299 §7damage and restore\n" +
                "§a" + minDamageHeal + " §7- §a" + maxDamageHeal + " §7health to allies. A\n" +
                "§7direct hit will cause §a15% §7increased\n" +
                "§7damage or healing for the target hit.\n" +
                "§7Has an optimal range of §e" + maxFullDamageDistance + " §7blocks.";
    }

    @Override
    public void createSkillTreeAbility(WarlordsPlayer warlordsPlayer, SkillTree skillTree) {
        waterBoltTree = new WaterBoltTree(skillTree, this, name, new ItemStack(warlordsPlayer.getWeapon().item));
        setSkillTree(waterBoltTree);
    }

    public void addMaxFullDamageDistance(int amount) {
        this.maxFullDamageDistance += amount;
    }

    public void addDirectHitMultiplier(float amount) {
        this.directHitMultiplier += amount;
    }

    public void addHitbox(float amount) {
        this.hitbox += amount;
    }

    public void addSelfHealMultiplier(float amount) {
        this.selfHealMultiplier += amount;
    }

    public void setReduceCooldownRain(boolean reduceCooldownRain) {
        this.reduceCooldownRain = reduceCooldownRain;
    }

    private void reduceHealingRainCooldown(WarlordsPlayer warlordsPlayer) {
        warlordsPlayer.getSpec().getOrange().subtractCooldown(.05f);
        warlordsPlayer.updateOrangeItem();
    }

    public void setReduceCooldownTeammates(boolean reduceCooldownTeammates) {
        this.reduceCooldownTeammates = reduceCooldownTeammates;
    }

    private void reduceCooldownTeammate(WarlordsPlayer warlordsPlayer) {
        warlordsPlayer.getSpec().getRed().subtractCooldown(.25f);
        warlordsPlayer.getSpec().getPurple().subtractCooldown(.25f);
        warlordsPlayer.getSpec().getBlue().subtractCooldown(.25f);
        warlordsPlayer.getSpec().getOrange().subtractCooldown(.25f);
        warlordsPlayer.updateAllAbilityItems();
    }
}

class WaterBoltTree extends SingleUltTree {

    public WaterBoltTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);

        leftUpgrades.add(new Upgrade(this, 3, 4, 0, 1, "Increased Range", "The maximum damage + healing range is increased"));
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Self Healing", "Increase healing received by self from Water Bolt"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Direct Hit Healing", "Direct hits bonus healing is increased"));

        rightUpgrades.add(new Upgrade(this, 5, 4, 0, 1, "Increased Splash", "Water Bolt now has increased splash effect"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Increased Healing", "Water Bolt heals more"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Cooldown Reduction", "Each teammate healed by Water Bolt reduces the cooldown of Healing Rain"));

        lastUpgrade = new Upgrade(this, 4, 1, 0, 1, "Rejuvenating Water", "Teammates receive cooldown reduction from Water Bolts");
    }

    @Override
    public void doFirstLeftUpgrade() {
        ((WaterBolt) ability).addMaxFullDamageDistance(15);
        ((WaterBolt) ability).addMaxDistance(15);
    }

    @Override
    public void doSecondLeftUpgrade() {
        ((WaterBolt) ability).addSelfHealMultiplier(.15f);
    }

    @Override
    public void doThirdLeftUpgrade() {
        ((WaterBolt) ability).addDirectHitMultiplier(.05f);
    }

    @Override
    public void doFirstRightUpgrade() {
        ((WaterBolt) ability).addHitbox(.5f);
    }

    @Override
    public void doSecondRightUpgrade() {
        ability.addDamageHeal(25);
    }

    @Override
    public void doThirdRightUpgrade() {
        ((WaterBolt) ability).setReduceCooldownRain(true);

    }

    @Override
    public void doLastUpgrade() {
        ((WaterBolt) ability).setReduceCooldownTeammates(true);
    }
}
