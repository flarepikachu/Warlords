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

public class FrostBolt extends AbstractProjectileBase {

    private int maxFullDamageDistance = 30;
    private double directHitMultiplier = 1.15;
    private float hitbox = 4;
    private int directHitSlowness = -25;

    private FrostBoltTree frostBoltTree;
    private boolean reduceCooldownBarrier = false;
    private boolean reduceCooldownBreath = false;


    public FrostBolt() {
        super("Frostbolt", -268.8f, -345.45f, 0, 70, 20, 175, 2, 250, false);
    }

    @Override
    protected String getActivationSound() {
        return "mage.frostbolt.activation";
    }

    @Override
    protected void playEffect(Location currentLocation, int animationTimer) {
        ParticleEffect.CLOUD.display(0, 0, 0, 0F, 1, currentLocation, 500);
    }

    @Override
    protected void onHit(WarlordsPlayer shooter, Location currentLocation, Location startingLocation, WarlordsPlayer victim) {
        ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.0F, 1, currentLocation, 500);
        ParticleEffect.CLOUD.display(0.3F, 0.3F, 0.3F, 1F, 3, currentLocation, 500);

        for (Player player1 : currentLocation.getWorld().getPlayers()) {
            player1.playSound(currentLocation, "mage.frostbolt.impact", 2, 1);
        }

        boolean hitEnemy = false;
        double distanceSquared = currentLocation.distanceSquared(startingLocation);
        double toReduceBy = maxFullDamageDistance * maxFullDamageDistance > distanceSquared ? 1 :
                1 - (Math.sqrt(distanceSquared) - maxFullDamageDistance) / 100.;
        if (toReduceBy < 0) toReduceBy = 0;
        if (victim != null && victim.isEnemyAlive(shooter)) {
            victim.getSpeed().addSpeedModifier("Frostbolt", directHitSlowness, 2 * 20);
            victim.addHealth(
                    shooter,
                    name,
                    (float) (minDamageHeal * directHitMultiplier * toReduceBy),
                    (float) (maxDamageHeal * directHitMultiplier * toReduceBy),
                    critChance,
                    critMultiplier
            );
            hitEnemy = true;
        }

        for (WarlordsPlayer nearEntity : PlayerFilter
                .entitiesAround(currentLocation, hitbox, hitbox, hitbox)
                .excluding(victim)
                .aliveEnemiesOf(shooter)
        ) {
            nearEntity.getSpeed().addSpeedModifier("Frostbolt", -25, 2 * 20);
            nearEntity.addHealth(
                    shooter,
                    name,
                    (float) (minDamageHeal * toReduceBy),
                    (float) (maxDamageHeal * toReduceBy),
                    critChance,
                    critMultiplier
            );
            hitEnemy = true;
        }

        if (hitEnemy) {
            if (reduceCooldownBarrier) {
                shooter.getSpec().getOrange().subtractCooldown(.5f);
                shooter.updateOrangeItem();
            }
            if (reduceCooldownBreath) {
                shooter.getSpec().getRed().subtractCooldown(.5f);
                shooter.updateRedItem();
            }
        }
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Shoot a frostbolt that will shatter\n" +
                "§7for §c" + -minDamageHeal + " §7- §c" + -maxDamageHeal + " §7damage and slow\n" +
                "§7by §e20% §7for §62 §7seconds. A\n" +
                "§7direct hit will cause the enemy\n" +
                "§7to take an additional §c15% §7extra\n" +
                "§7damage." + "\n\n§7Has an optimal range of §e" + maxFullDamageDistance + "\n" +
                "§7blocks.";
    }

    @Override
    public void createSkillTreeAbility(WarlordsPlayer warlordsPlayer, SkillTree skillTree) {
        frostBoltTree = new FrostBoltTree(skillTree, this, name, new ItemStack(warlordsPlayer.getWeapon().item));
        setSkillTree(frostBoltTree);
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

    public void addDirectHitSlowness(int amount) {
        this.directHitSlowness += amount;
    }

    public void setReduceCooldownBarrier(boolean reduceCooldownBarrier) {
        this.reduceCooldownBarrier = reduceCooldownBarrier;
    }

    public void setReduceCooldownBreath(boolean reduceCooldownBreath) {
        this.reduceCooldownBreath = reduceCooldownBreath;
    }
}

class FrostBoltTree extends SingleUltTree {

    public FrostBoltTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);

        leftUpgrades.add(new Upgrade(this, 3, 4, 0, 1, "Increased Splash", "Frostbolt now has increased splash effect"));
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Direct Hit Slowness", "Direct Hits now cause extra slowness"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Barrier Cooldown", "Each Frostbolt hit reduces the cooldown of Ice Barrier"));

        rightUpgrades.add(new Upgrade(this, 5, 4, 0, 1, "Increased Range", "The maximum damage range is increased"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Direct Hit Damage", "Direct hits bonus damage is increased"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Breath Cooldown", "Each Frostbolt hit reduces the cooldown of Freezing Breath"));

        lastUpgrade = new Upgrade(this, 4, 1, 0, 1, "Energy Reduction", "Energy cost of Frostbolt is reduced");
    }

    @Override
    public void doFirstLeftUpgrade() {
        ((FrostBolt) ability).addHitbox(.5f);
    }

    @Override
    public void doSecondLeftUpgrade() {
        ((FrostBolt) ability).addDirectHitSlowness(-2);
    }

    @Override
    public void doThirdLeftUpgrade() {
        ((FrostBolt) ability).setReduceCooldownBarrier(true);
    }

    @Override
    public void doFirstRightUpgrade() {
        ((FrostBolt) ability).addMaxFullDamageDistance(15);
        ((FrostBolt) ability).addMaxDistance(15);
    }

    @Override
    public void doSecondRightUpgrade() {
        ((FrostBolt) ability).addDirectHitMultiplier(.05f);
    }

    @Override
    public void doThirdRightUpgrade() {
        ((FrostBolt) ability).setReduceCooldownBreath(true);

    }

    @Override
    public void doLastUpgrade() {
        ability.setEnergyCost(ability.getEnergyCost() - 5);
    }
}
