package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.classes.internal.AbstractProjectileBase;
import com.ebicep.warlords.player.CooldownTypes;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import com.ebicep.warlords.skilltree.trees.SingleUltTree;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.PlayerFilter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class Fireball extends AbstractProjectileBase {

    private int maxFullDamageDistance = 50;
    private double directHitMultiplier = 1.15;
    private float hitbox = 4;

    private FireballTree fireBallTree;
    private boolean burn = false;
    private boolean reduceCooldown = false;

    public Fireball() {
        super("Fireball", -334.4f, -433.4f, 0, 70, 20, 175, 2, 300, false);
    }

    @Override
    protected String getActivationSound() {
        return "mage.fireball.activation";
    }

    @Override
    protected void playEffect(Location currentLocation, int animationTimer) {
        ParticleEffect.DRIP_LAVA.display(0, 0, 0, 0.35F, 5, currentLocation, 500);
        ParticleEffect.SMOKE_NORMAL.display(0, 0, 0, 0.001F, 7, currentLocation, 500);
        ParticleEffect.FLAME.display(0, 0, 0, 0.06F, 1, currentLocation, 500);
    }

    @Override
    protected void onSpawn(@Nonnull InternalProjectile projectile) {
        super.onSpawn(projectile);
        this.playEffect(projectile);
    }

    @Override
    protected void onHit(WarlordsPlayer shooter, Location currentLocation, Location startingLocation, WarlordsPlayer victim) {
        ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.5F, 1, currentLocation, 500);
        ParticleEffect.LAVA.display(0.5F, 0, 0.5F, 1.5f, 10, currentLocation, 500);
        ParticleEffect.CLOUD.display(0.3F, 0.3F, 0.3F, 1F, 3, currentLocation, 500);

        for (Player player1 : currentLocation.getWorld().getPlayers()) {
            player1.playSound(currentLocation, "mage.fireball.impact", 2, 1);
        }

        boolean hitEnemy = false;
        double distanceSquared = currentLocation.distanceSquared(startingLocation);
        double toReduceBy = maxFullDamageDistance * maxFullDamageDistance > distanceSquared ? 1 :
                1 - (Math.sqrt(distanceSquared) - maxFullDamageDistance) / 85.;
        if (toReduceBy < 0) toReduceBy = 0;
        if (victim != null) {
            victim.addHealth(
                    shooter,
                    name,
                    (float) (minDamageHeal * directHitMultiplier * toReduceBy),
                    (float) (maxDamageHeal * directHitMultiplier * toReduceBy),
                    critChance,
                    critMultiplier,
                    false);
            if (burn) {
                if (toReduceBy == 1) {
                    victim.getCooldownManager().removeCooldown("BURN");
                    victim.getCooldownManager().addCooldown("Burn", Fireball.class, new Fireball(), "BURN", 3, shooter, CooldownTypes.DEBUFF);
                }
            }
            hitEnemy = true;
        }

        for (WarlordsPlayer nearEntity : PlayerFilter
                .entitiesAround(currentLocation, hitbox, hitbox, hitbox)
                .excluding(victim)
                .aliveEnemiesOf(shooter)
        ) {
            nearEntity.addHealth(
                    shooter,
                    name,
                    (float) (minDamageHeal * toReduceBy),
                    (float) (maxDamageHeal * toReduceBy),
                    critChance,
                    critMultiplier,
                    false);
            if (burn) {
                if (toReduceBy == 1) {
                    nearEntity.getCooldownManager().removeCooldown("BURN");
                    nearEntity.getCooldownManager().addCooldown("Burn", Fireball.class, new Fireball(), "BURN", 3, shooter, CooldownTypes.DEBUFF);
                }
            }
            hitEnemy = true;
        }

        if (hitEnemy && reduceCooldown) {
            shooter.getSpec().getRed().subtractCooldown(.5f);
            shooter.updateRedItem();
            shooter.getSpec().getOrange().subtractCooldown(1);
            shooter.updateOrangeItem();
        }
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Shoot a fireball that will explode\n" +
                "§7for §c" + -minDamageHeal + " §7- §c" + -maxDamageHeal + " §7damage. A\n" +
                "§7direct hit will cause the enemy\n" +
                "§7to take an additional §c15% §7extra\n" +
                "§7damage. §7Has an optimal range of §e" + maxFullDamageDistance + " §7blocks.";
    }

    @Override
    public void createSkillTreeAbility(WarlordsPlayer warlordsPlayer, SkillTree skillTree) {
        fireBallTree = new FireballTree(skillTree, this, name, new ItemStack(warlordsPlayer.getWeapon().item));
        setSkillTree(fireBallTree);
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

    public void setBurn(boolean burn) {
        this.burn = burn;
    }

    public void setReduceCooldown(boolean reduceCooldown) {
        this.reduceCooldown = reduceCooldown;
    }
}

class FireballTree extends SingleUltTree {

    public FireballTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);
        leftUpgrades.add(new Upgrade(this, 3, 4, 0, 1, "Increased Range", "The maximum damage range is increased"));
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Increased Damage", "Basic increase in damage"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Direct Hit Damage", "Direct hits bonus damage is increased"));

        rightUpgrades.add(new Upgrade(this, 5, 4, 0, 1, "Increased Splash", "Fireball now has increased splash effect"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Increased Velocity", "Fireballs travel faster"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Burn", "Fireballs burn the target, making them \ntake damage over a short period of time"));

        lastUpgrade = new Upgrade(this, 4, 1, 0, 1, "Cooldown Reduction", "Each Fireball hit reduces the \ncooldown of Flame Burst / Inferno");
    }

    @Override
    public void doFirstLeftUpgrade() {
        ((Fireball) ability).addMaxFullDamageDistance(15);
        ((Fireball) ability).addMaxDistance(15);
    }

    @Override
    public void doSecondLeftUpgrade() {
        ability.addDamageHeal(-25);
    }

    @Override
    public void doThirdLeftUpgrade() {
        ((Fireball) ability).addDirectHitMultiplier(.05f);
    }

    @Override
    public void doFirstRightUpgrade() {
        ((Fireball) ability).addHitbox(.5f);
    }

    @Override
    public void doSecondRightUpgrade() {
        ((Fireball) ability).addProjectileSpeed(.25f);
    }

    @Override
    public void doThirdRightUpgrade() {
        ((Fireball) ability).setBurn(true);
    }

    @Override
    public void doLastUpgrade() {
        ((Fireball) ability).setReduceCooldown(true);
    }
}
