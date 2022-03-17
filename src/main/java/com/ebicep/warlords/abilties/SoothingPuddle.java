package com.ebicep.warlords.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.effects.circle.AreaEffect;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.effects.circle.CircumferenceEffect;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.bukkit.Matrix4d;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class SoothingPuddle extends AbstractAbility {

    private static final double SPEED = 0.220;
    private static final double GRAVITY = -0.008;
    private static final float HITBOX = 5;

    private final int puddleMinHealing = 178;
    private final int puddleMaxHealing = 224;

    public SoothingPuddle() {
        super("Soothing Puddle", 551, 648, 8, 60, 25, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Throw a short range projectile, healing\n" +
                "§7allies for §a" + format(minDamageHeal) + " §7- §a" + format(maxDamageHeal) + " §7health upon impact.\n" +
                "§7A small puddle is formed where the projectile\n" +
                "§7lands, healing allies for §a" + puddleMinHealing + " §7- §a" + puddleMaxHealing + " §7health per\n" +
                "§7second for §64 §7seconds.";
    }

    @Override
    public boolean onActivate(WarlordsPlayer wp, Player player) {
        wp.subtractEnergy(energyCost);

        Location location = player.getLocation();
        Vector speed = player.getLocation().getDirection().multiply(SPEED);
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setHelmet(new ItemStack(Material.STAINED_GLASS, 1, (short) 6));
        stand.setGravity(false);
        stand.setVisible(false);
        new GameRunnable(wp.getGame()) {
            int timer = 0;
            @Override
            public void run() {
                quarterStep(false);
                quarterStep(false);
                quarterStep(false);
                quarterStep(false);
                quarterStep(false);
                quarterStep(false);
                quarterStep(true);
            }

            private void quarterStep(boolean last) {

                if (!stand.isValid()) {
                    this.cancel();
                    return;
                }

                speed.add(new Vector(0, GRAVITY * SPEED, 0));
                Location newLoc = stand.getLocation();
                newLoc.add(speed);
                stand.teleport(newLoc);
                newLoc.add(0, 1.75, 0);

                stand.setHeadPose(new EulerAngle(-speed.getY() * 3, 0, 0));

                boolean shouldExplode;

                timer++;

                if (last) {
                    Matrix4d center = new Matrix4d(newLoc);
                    for (float i = 0; i < 6; i++) {
                        double angle = Math.toRadians(i * 90) + timer * 0.3;
                        double width = 0.4D;
                        ParticleEffect.VILLAGER_HAPPY.display(0, 0, 0, 0, 2,
                                center.translateVector(newLoc.getWorld(), 0, Math.sin(angle) * width, Math.cos(angle) * width), 500);
                    }
                }

                WarlordsPlayer directHit;
                if (!newLoc.getBlock().isEmpty()
                        && newLoc.getBlock().getType() != Material.GRASS
                        && newLoc.getBlock().getType() != Material.BARRIER
                        && newLoc.getBlock().getType() != Material.VINE
                ) {
                    // Explode based on collision
                    shouldExplode = true;
                } else {
                    directHit = PlayerFilter
                            .entitiesAroundRectangle(newLoc, 1, 2, 1)
                            .aliveTeammatesOfExcludingSelf(wp).findFirstOrNull();
                    shouldExplode = directHit != null;
                    newLoc.add(0, -1, 0);
                }

                newLoc.add(0, 1, 0);

                if (shouldExplode) {
                    stand.remove();
                    Utils.playGlobalSound(newLoc, "rogue.healingremedy.impact", 1.5f, 0.2f);
                    Utils.playGlobalSound(newLoc, Sound.GLASS, 1.5f, 0.7f);
                    Utils.playGlobalSound(newLoc, "mage.waterbolt.impact", 1.5f, 0.3f);

                    FireWorkEffectPlayer.playFirework(newLoc, FireworkEffect.builder()
                            .withColor(Color.WHITE)
                            .with(FireworkEffect.Type.BURST)
                            .build());

                    for (WarlordsPlayer nearEntity : PlayerFilter
                            .entitiesAround(newLoc, HITBOX, HITBOX, HITBOX)
                            .aliveTeammatesOf(wp)
                    ) {
                        nearEntity.addHealingInstance(
                                wp,
                                name,
                                minDamageHeal,
                                maxDamageHeal,
                                critChance,
                                critMultiplier,
                                false,
                                false);
                    }

                    CircleEffect circleEffect = new CircleEffect(
                            wp.getGame(),
                            wp.getTeam(),
                            newLoc,
                            HITBOX,
                            new CircumferenceEffect(ParticleEffect.VILLAGER_HAPPY, ParticleEffect.REDSTONE),
                            new AreaEffect(1, ParticleEffect.DRIP_WATER).particlesPerSurface(0.025)
                    );

                    BukkitTask task = Bukkit.getScheduler().runTaskTimer(Warlords.getInstance(), circleEffect::playEffects, 0, 1);
                    wp.getGame().registerGameTask(task);
                    new GameRunnable(wp.getGame()) {
                        int timeLeft = 4;

                        @Override
                        public void run() {
                            PlayerFilter.entitiesAround(newLoc, HITBOX, HITBOX, HITBOX)
                                    .aliveTeammatesOf(wp)
                                    .forEach((ally) -> ally.addHealingInstance(
                                            wp,
                                            name,
                                            puddleMinHealing,
                                            puddleMaxHealing,
                                            critChance,
                                            critMultiplier,
                                            false,
                                            false));

                            timeLeft--;

                            if (timeLeft < 0) {
                                this.cancel();
                                task.cancel();
                            }
                        }

                    }.runTaskTimer(20, 20);

                    this.cancel();
                }
            }

        }.runTaskTimer(0, 1);

        Utils.playGlobalSound(player.getLocation(), "mage.frostbolt.activation", 2, 0.7f);

        return true;
    }
}