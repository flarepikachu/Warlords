package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.util.Matrix4d;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.Utils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Projectile extends AbstractAbility {

    private static final float hitBox = 1.3F;
    private final int maxDistance;

    public Projectile(String name, float minDamageHeal, float maxDamageHeal, float cooldown, int energyCost, int critChance, int critMultiplier, int maxDistance) {
        super(name, minDamageHeal, maxDamageHeal, cooldown, energyCost, critChance, critMultiplier);
        this.maxDistance = maxDistance;
        updateDescription();
    }

    @Override
    public void updateDescription() {
        if (name.contains("Fire")) {
            description = "§7Shoot a fireball that will explode\n" +
                    "§7for §c" + -minDamageHeal + " §7- §c" + -maxDamageHeal + " §7damage. A\n" +
                    "§7direct hit will cause the enemy\n" +
                    "§7to take an additional §c15% §7extra\n" +
                    "§7damage. §7Has an optimal range of §e50 §7blocks.";
        } else if (name.contains("Frost")) {
            description = "§7Shoot a frostbolt that will shatter\n" +
                    "§7for §c" + -minDamageHeal + " §7- §c" + -maxDamageHeal + " §7damage and slow\n" +
                    "§7by §e20% §7for §62 §7seconds. A\n" +
                    "§7direct hit will cause the enemy\n" +
                    "§7to take an additional §c30% §7extra\n" +
                    "§7damage." + "\n\n§7Has an optimal range of §e" + maxDistance + "\n" +
                    "§7blocks.";
        } else if (name.contains("Water")) {
            description = "§7Shoot a bolt of water that will burst\n" +
                    "§7for §c231 §7- §c299 §7damage and restore\n" +
                    "§a" + minDamageHeal + " §7- §a" + maxDamageHeal + " §7health to allies. A\n" +
                    "§7direct hit will cause §a15% §7increased\n" +
                    "§7damage or healing for the target hit.\n" +
                    "§7Has an optimal range of §e" + maxDistance + " §7blocks.";
        } else if (name.contains("Flame")) {
            description = "§7Launch a flame burst that will explode\n" +
                    "§7for §c" + -minDamageHeal + " §7- §c" + -maxDamageHeal + " §7damage. The critical\n" +
                    "§7chance increases by §c1% §7for each\n" +
                    "§7travelled block. Up to 100%.";
        }
    }

    @Override
    public void onActivate(Player player) {
        Warlords.getPlayer(player).subtractEnergy(energyCost);

        CustomProjectile customProjectile = new CustomProjectile(player, player.getLocation(), player.getLocation(), player.getLocation().getDirection(), maxDistance,
                new Projectile(name, minDamageHeal, maxDamageHeal, cooldown, energyCost, critChance, critMultiplier, maxDistance));

        // SOUNDS
        if (customProjectile.getBall().getName().contains("Fire")) {
            for (Player player1 : player.getWorld().getPlayers()) {
                player1.playSound(player.getLocation(), "mage.fireball.activation", 2, 1);
            }
        } else if (customProjectile.getBall().getName().contains("Frost")) {
            for (Player player1 : player.getWorld().getPlayers()) {
                player1.playSound(player.getLocation(), "mage.frostbolt.activation", 2, 1);
            }
        } else if (customProjectile.getBall().getName().contains("Water")) {
            for (Player player1 : player.getWorld().getPlayers()) {
                player1.playSound(player.getLocation(), "mage.waterbolt.activation", 2, 1);
            }
        } else if (customProjectile.getBall().getName().contains("Flame")) {
            for (Player player1 : player.getWorld().getPlayers()) {
                player1.playSound(player.getLocation(), "mage.fireball.activation", 2, 1);
            }
        }


        new BukkitRunnable() {

            int animationTimer = 0;

            @Override
            public void run() {
                Location location = customProjectile.getCurrentLocation();
                boolean hitPlayer = false;
                //BALLS
                if (customProjectile.getBall().getName().contains("Fire")) {
                    location.add(customProjectile.getDirection().clone().multiply(2));
                    location.add(0, 1.5, 0);
                    ParticleEffect.DRIP_LAVA.display(0, 0, 0, 0.35F, 5, location, 500);
                    ParticleEffect.SMOKE_NORMAL.display(0, 0, 0, 0.001F, 7, location, 500);
                    ParticleEffect.FLAME.display(0, 0, 0, 0.06F, 1, location, 500);
                    List<Entity> entities = (List<Entity>) location.getWorld().getNearbyEntities(location, 5, 5, 5);
                    entities = Utils.filterOutTeammates(entities, customProjectile.getShooter());
                    for (Entity entity : entities) {
                        if (entity instanceof Player && ((Player) entity).getGameMode() != GameMode.SPECTATOR && entity != customProjectile.getShooter()) {
                            if (entity.getLocation().clone().add(0, 1, 0).distanceSquared(location) < hitBox * hitBox) {
                                hitPlayer = true;
                                ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.5F, 1, entity.getLocation().add(0, 1, 0), 500);
                                ParticleEffect.LAVA.display(0.5F, 0, 0.5F, 2F, 10, entity.getLocation().add(0, 1, 0), 500);
                                ParticleEffect.CLOUD.display(0.3F, 0.3F, 0.3F, 1F, 3, entity.getLocation().add(0, 1, 0), 500);
                                Player victim = (Player) entity;

                                for (Player player1 : victim.getWorld().getPlayers()) {
                                    player1.playSound(victim.getLocation(), "mage.fireball.impact", 2, 1);
                                }

                                if (location.distanceSquared(customProjectile.getStartingLocation()) >= customProjectile.getMaxDistance() * customProjectile.getMaxDistance()) {
                                    double toReduceBy = (1 - ((location.distance(customProjectile.getStartingLocation()) - customProjectile.getMaxDistance()) / 100.0));
                                    if (toReduceBy < 0) toReduceBy = 0;
                                    Warlords.getPlayer(victim).addHealth(
                                            Warlords.getPlayer(customProjectile.getShooter()),
                                            customProjectile.getBall().getName(),
                                            (float) (customProjectile.getBall().getMinDamageHeal() * 1.15 * toReduceBy),
                                            (float) (customProjectile.getBall().getMaxDamageHeal() * 1.15 * toReduceBy),
                                            customProjectile.getBall().getCritChance(),
                                            customProjectile.getBall().getCritMultiplier()
                                    );

                                    List<Entity> near = victim.getNearbyEntities(3.25D, 3.25D, 3.25D);
                                    near = Utils.filterOutTeammates(near, customProjectile.getShooter());
                                    for (Entity nearEntity : near) {
                                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    (float) (customProjectile.getBall().getMinDamageHeal() * toReduceBy),
                                                    (float) (customProjectile.getBall().getMaxDamageHeal() * toReduceBy),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    }
                                } else {
                                    Warlords.getPlayer(victim).addHealth(
                                            Warlords.getPlayer(customProjectile.getShooter()),
                                            customProjectile.getBall().getName(),
                                            (float) (customProjectile.getBall().getMinDamageHeal() * 1.15),
                                            (float) (customProjectile.getBall().getMaxDamageHeal() * 1.15),
                                            customProjectile.getBall().getCritChance(),
                                            customProjectile.getBall().getCritMultiplier());

                                    List<Entity> near = victim.getNearbyEntities(3.25D, 3.25D, 3.25D);
                                    near = Utils.filterOutTeammates(near, customProjectile.getShooter());
                                    for (Entity nearEntity : near) {
                                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    customProjectile.getBall().getMinDamageHeal(),
                                                    customProjectile.getBall().getMaxDamageHeal(),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    }
                                }
                                customProjectile.setRemove(true);
                                break;
                            }
                        }
                    }
                } else if (customProjectile.getBall().getName().contains("Frost")) {
                    location.add(customProjectile.getDirection().clone().multiply(2));
                    location.add(0, 1.5, 0);
                    ParticleEffect.CLOUD.display(0, 0, 0, 0F, 1, location, 500);
                    List<Entity> entities = (List<Entity>) location.getWorld().getNearbyEntities(location, 5, 5, 5);
                    entities = Utils.filterOutTeammates(entities, customProjectile.getShooter());
                    for (Entity entity : entities) {
                        if (entity instanceof Player && ((Player) entity).getGameMode() != GameMode.SPECTATOR && entity != customProjectile.getShooter()) {
                            if (entity.getLocation().clone().add(0, 1, 0).distanceSquared(location) < hitBox * hitBox) {
                                hitPlayer = true;
                                ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.0F, 1, entity.getLocation().add(0, 1, 0), 500);
                                ParticleEffect.CLOUD.display(0.3F, 0.3F, 0.3F, 1F, 3, entity.getLocation().add(0, 1, 0), 500);
                                Player victim = (Player) entity;

                                for (Player player1 : player.getWorld().getPlayers()) {
                                    player1.playSound(location, "mage.frostbolt.impact", 2, 1);
                                }

                                Warlords.getPlayer(victim).getSpeed().changeCurrentSpeed("Frostbolt", -25, 2 * 20);
                                if (location.distanceSquared(customProjectile.getStartingLocation()) >= customProjectile.getMaxDistance() * customProjectile.getMaxDistance()) {
                                    double toReduceBy = (1 - ((location.distance(customProjectile.getStartingLocation()) - customProjectile.getMaxDistance()) / 100.0));
                                    if (toReduceBy < 0) toReduceBy = 0;
                                    Warlords.getPlayer(victim).addHealth(
                                            Warlords.getPlayer(customProjectile.getShooter()),
                                            customProjectile.getBall().getName(),
                                            (float) (customProjectile.getBall().getMinDamageHeal() * 1.3 * toReduceBy),
                                            (float) (customProjectile.getBall().getMaxDamageHeal() * 1.3 * toReduceBy),
                                            customProjectile.getBall().getCritChance(),
                                            customProjectile.getBall().getCritMultiplier()
                                    );
                                    List<Entity> near = victim.getNearbyEntities(3.5D, 3.5D, 3.5D);
                                    near = Utils.filterOutTeammates(near, customProjectile.getShooter());
                                    for (Entity nearEntity : near) {
                                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    (float) (customProjectile.getBall().getMinDamageHeal() * toReduceBy),
                                                    (float) (customProjectile.getBall().getMaxDamageHeal() * toReduceBy),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    }
                                } else {
                                    Warlords.getPlayer(victim).addHealth(
                                            Warlords.getPlayer(customProjectile.getShooter()),
                                            customProjectile.getBall().getName(),
                                            (float) (customProjectile.getBall().getMinDamageHeal() * 1.3),
                                            (float) (customProjectile.getBall().getMaxDamageHeal() * 1.3),
                                            customProjectile.getBall().getCritChance(),
                                            customProjectile.getBall().getCritMultiplier()
                                    );
                                    List<Entity> near = victim.getNearbyEntities(3.5D, 3.5D, 3.5D);
                                    near = Utils.filterOutTeammates(near, customProjectile.getShooter());
                                    for (Entity nearEntity : near) {
                                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    customProjectile.getBall().getMinDamageHeal(),
                                                    customProjectile.getBall().getMaxDamageHeal(),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    }
                                }
                                customProjectile.setRemove(true);
                                break;
                            }
                        }
                    }
                } else if (customProjectile.getBall().getName().contains("Water")) {
                    location.add(customProjectile.getDirection().clone().multiply(2));
                    location.add(0, 1.5, 0);
                    ParticleEffect.DRIP_WATER.display(0.3f, 0.3f, 0.3f, 0.1F, 2, location, 500);
                    ParticleEffect.ENCHANTMENT_TABLE.display(0, 0, 0, 0.1F, 1, location, 500);
                    ParticleEffect.VILLAGER_HAPPY.display(0, 0, 0, 0.1F, 1, location, 500);
                    ParticleEffect.CLOUD.display(0, 0, 0, 0F, 1, location, 500);
                    List<Entity> entities = (List<Entity>) location.getWorld().getNearbyEntities(location, 5, 5, 5);
                    for (Entity entity : entities) {
                        if (entity instanceof Player && ((Player) entity).getGameMode() != GameMode.SPECTATOR && entity != customProjectile.getShooter()) {
                            if (entity.getLocation().clone().add(0, 1, 0).distanceSquared(location) < hitBox * hitBox) {
                                hitPlayer = true;
                                ParticleEffect.HEART.display(1.5F, 1.5F, 1.5F, 0.2F, 2, entity.getLocation().add(0, 1, 0), 500);
                                ParticleEffect.VILLAGER_HAPPY.display(1.5F, 1.5F, 1.5F, 0.2F, 3, entity.getLocation().add(0, 1, 0), 500);
                                Player victim = (Player) entity;

                                for (Player player1 : player.getWorld().getPlayers()) {
                                    player1.playSound(entity.getLocation(), "mage.waterbolt.impact", 2, 1);
                                }

                                if (location.distanceSquared(customProjectile.getStartingLocation()) >= customProjectile.getMaxDistance() * customProjectile.getMaxDistance()) {
                                    double toReduceBy = (1 - ((location.distance(customProjectile.getStartingLocation()) - customProjectile.getMaxDistance()) / 100.0));
                                    if (toReduceBy < 0) toReduceBy = 0;
                                    if (Warlords.game.onSameTeam((Player) entity, customProjectile.getShooter())) {
                                        Warlords.getPlayer((Player) entity).addHealth(
                                                Warlords.getPlayer(customProjectile.getShooter()),
                                                customProjectile.getBall().getName(),
                                                (float) (customProjectile.getBall().getMinDamageHeal() * 1.15 * toReduceBy),
                                                (float) (customProjectile.getBall().getMaxDamageHeal() * 1.15 * toReduceBy),
                                                customProjectile.getBall().getCritChance(),
                                                customProjectile.getBall().getCritMultiplier()
                                        );
                                    } else {
                                        Warlords.getPlayer((Player) entity).addHealth(
                                                Warlords.getPlayer(customProjectile.getShooter()),
                                                customProjectile.getBall().getName(),
                                                (float) (-231 * 1.15 * toReduceBy),
                                                (float) (-299 * 1.15 * toReduceBy),
                                                customProjectile.getBall().getCritChance(),
                                                customProjectile.getBall().getCritMultiplier()
                                        );
                                    }
                                    List<Entity> near = victim.getNearbyEntities(3.5D, 3.5D, 3.5D);
                                    for (Entity nearEntity : near) {
                                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                            if (Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter())) {
                                                Warlords.getPlayer((Player) nearEntity).addHealth(
                                                        Warlords.getPlayer(customProjectile.getShooter()),
                                                        customProjectile.getBall().getName(),
                                                        (float) (customProjectile.getBall().getMinDamageHeal() * toReduceBy),
                                                        (float) (customProjectile.getBall().getMaxDamageHeal() * toReduceBy),
                                                        customProjectile.getBall().getCritChance(),
                                                        customProjectile.getBall().getCritMultiplier()
                                                );
                                            } else {
                                                Warlords.getPlayer((Player) nearEntity).addHealth(
                                                        Warlords.getPlayer(customProjectile.getShooter()),
                                                        customProjectile.getBall().getName(),
                                                        (float) (-231 * toReduceBy),
                                                        (float) (-299 * toReduceBy),
                                                        customProjectile.getBall().getCritChance(),
                                                        customProjectile.getBall().getCritMultiplier()
                                                );
                                            }
                                        }
                                    }
                                } else {
                                    if (Warlords.game.onSameTeam((Player) entity, customProjectile.getShooter())) {
                                        Warlords.getPlayer((Player) entity).addHealth(
                                                Warlords.getPlayer(customProjectile.getShooter()),
                                                customProjectile.getBall().getName(),
                                                (float) (customProjectile.getBall().getMinDamageHeal() * 1.15),
                                                (float) (customProjectile.getBall().getMaxDamageHeal() * 1.15),
                                                customProjectile.getBall().getCritChance(),
                                                customProjectile.getBall().getCritMultiplier()
                                        );
                                    } else {
                                        Warlords.getPlayer((Player) entity).addHealth(
                                                Warlords.getPlayer(customProjectile.getShooter()),
                                                customProjectile.getBall().getName(),
                                                (-231f * 1.15f),
                                                (-299f * 1.15f),
                                                customProjectile.getBall().getCritChance(),
                                                customProjectile.getBall().getCritMultiplier()
                                        );
                                    }
                                    List<Entity> near = victim.getNearbyEntities(3.5D, 3.5D, 3.5D);
                                    for (Entity nearEntity : near) {
                                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                            if (Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter())) {
                                                Warlords.getPlayer((Player) nearEntity).addHealth(
                                                        Warlords.getPlayer(customProjectile.getShooter()),
                                                        customProjectile.getBall().getName(),
                                                        customProjectile.getBall().getMinDamageHeal(),
                                                        customProjectile.getBall().getMaxDamageHeal(),
                                                        customProjectile.getBall().getCritChance(),
                                                        customProjectile.getBall().getCritMultiplier()
                                                );
                                            } else {
                                                Warlords.getPlayer((Player) nearEntity).addHealth(
                                                        Warlords.getPlayer(customProjectile.getShooter()),
                                                        customProjectile.getBall().getName(),
                                                        -231,
                                                        -299,
                                                        customProjectile.getBall().getCritChance(),
                                                        customProjectile.getBall().getCritMultiplier()
                                                );
                                            }
                                        }
                                    }
                                }
                                customProjectile.setRemove(true);
                                break;
                            }
                        }
                    }


                } else if (customProjectile.getBall().getName().contains("Flame")) {
                    location.add(customProjectile.getDirection().multiply(1.05));
                    location.add(0, 1.5, 0);

                    Matrix4d center = new Matrix4d(location);

                    for (float i = 0; i < 4; i++) {
                        double angle = Math.toRadians(i * 90) + animationTimer * 0.45;
                        double width = 0.24D;
                        ParticleEffect.FLAME.display(0, 0, 0, 0, 2,
                                center.translateVector(location.getWorld(), 0, Math.sin(angle) * width, Math.cos(angle) * width), 500);
                    }
                    animationTimer++;

                    List<Entity> entities = (List<Entity>) location.getWorld().getNearbyEntities(location, 5, 5, 5);
                    entities = Utils.filterOutTeammates(entities, customProjectile.getShooter());
                    for (Entity entity : entities) {
                        if (entity instanceof Player && ((Player) entity).getGameMode() != GameMode.SPECTATOR && entity != customProjectile.getShooter()) {
                            if (entity.getLocation().clone().add(0, 1, 0).distanceSquared(location) < hitBox * hitBox) {
                                hitPlayer = true;
                                ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.5F, 2, entity.getLocation().add(0, 1, 0), 500);
                                ParticleEffect.LAVA.display(0.5F, 0, 0.5F, 2F, 10, entity.getLocation().add(0, 1, 0), 500);
                                Player victim = (Player) entity;

                                for (Player player1 : player.getWorld().getPlayers()) {
                                    player1.playSound(entity.getLocation(), "mage.flameburst.impact", 2, 1);
                                }

                                Warlords.getPlayer(victim).addHealth(
                                        Warlords.getPlayer(customProjectile.getShooter()),
                                        customProjectile.getBall().getName(),
                                        customProjectile.getBall().getMinDamageHeal(),
                                        customProjectile.getBall().getMaxDamageHeal(),
                                        customProjectile.getBall().getCritChance() + (int) location.distance(customProjectile.getStartingLocation()),
                                        customProjectile.getBall().getCritMultiplier()
                                );
                                List<Entity> near = victim.getNearbyEntities(5D, 5D, 5D);
                                near = Utils.filterOutTeammates(near, customProjectile.getShooter());
                                for (Entity nearEntity : near) {
                                    if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                                        Warlords.getPlayer((Player) nearEntity).addHealth(
                                                Warlords.getPlayer(customProjectile.getShooter()),
                                                customProjectile.getBall().getName(),
                                                customProjectile.getBall().getMinDamageHeal(),
                                                customProjectile.getBall().getMaxDamageHeal(),
                                                customProjectile.getBall().getCritChance() + (int) Math.pow(location.distanceSquared(customProjectile.getStartingLocation()), 2),
                                                customProjectile.getBall().getCritMultiplier()
                                        );
                                    }
                                }

                                customProjectile.setRemove(true);
                                break;
                            }
                        }
                    }
                }

                //hit block or out of range
                if ((location.getWorld().getBlockAt(location).getType() != Material.AIR && location.getWorld().getBlockAt(location).getType() != Material.WATER) && !hitPlayer) {
                    if (customProjectile.getBall().getName().contains("Water")) {
                        ParticleEffect.HEART.display(1, 1, 1, 0.2F, 3, location, 500);
                        ParticleEffect.VILLAGER_HAPPY.display(1, 1, 1, 0.2F, 5, location, 500);

                        for (Player player1 : player.getWorld().getPlayers()) {
                            player1.playSound(location, "mage.waterbolt.impact", 2, 1);
                        }

                    } else if (customProjectile.getBall().getName().contains("Fire")) {
                        ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.0F, 1, location, 500);
                        ParticleEffect.LAVA.display(0.5F, 0, 0.5F, 2F, 10, location, 500);
                        ParticleEffect.CLOUD.display(0.3F, 0.3F, 0.3F, 1F, 3, location, 500);

                        for (Player player1 : player.getWorld().getPlayers()) {
                            player1.playSound(location, "mage.fireball.impact", 2, 1);
                        }

                    } else if (customProjectile.getBall().getName().contains("Frost")) {
                        ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.0F, 1, location, 500);
                        ParticleEffect.CLOUD.display(0.3F, 0.3F, 0.3F, 1F, 3, location, 500);

                        for (Player player1 : player.getWorld().getPlayers()) {
                            player1.playSound(location, "mage.frostbolt.impact", 2, 1);
                        }

                    } else if (customProjectile.getBall().getName().contains("Flame")) {
                        ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.0F, 1, location, 500);
                        ParticleEffect.LAVA.display(0.5F, 0.1F, 0.5F, 2F, 15, location, 500);

                        for (Player player1 : player.getWorld().getPlayers()) {
                            player1.playSound(location, "mage.flameburst.impact", 2, 1);
                        }
                    }

                    List<Entity> near = (List<Entity>) location.getWorld().getNearbyEntities(location, 4, 4, 4);
                    for (Entity nearEntity : near) {
                        if (nearEntity instanceof Player && ((Player) nearEntity).getGameMode() != GameMode.SPECTATOR) {
                            if (customProjectile.getBall().getName().contains("Flame") && !Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter()) && nearEntity != customProjectile.getShooter()) {
                                Warlords.getPlayer((Player) nearEntity).addHealth(
                                        Warlords.getPlayer(customProjectile.getShooter()),
                                        customProjectile.getBall().getName(),
                                        customProjectile.getBall().getMinDamageHeal(),
                                        customProjectile.getBall().getMaxDamageHeal(),
                                        customProjectile.getBall().getCritChance() + (int) Math.pow(location.distanceSquared(customProjectile.getStartingLocation()), 2),
                                        customProjectile.getBall().getCritMultiplier()
                                );
                            } else {
                                if (location.distanceSquared(customProjectile.getStartingLocation()) >= customProjectile.getMaxDistance() * customProjectile.getMaxDistance()) {
                                    double toReduceBy = (1 - ((location.distance(customProjectile.getStartingLocation()) - customProjectile.getMaxDistance()) / 100.0));
                                    if (toReduceBy < 0) toReduceBy = 0;
                                    if (customProjectile.getBall().getName().contains("Water")) {
                                        if (Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter())) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    (float) (customProjectile.getBall().getMinDamageHeal() * toReduceBy),
                                                    (float) (customProjectile.getBall().getMaxDamageHeal() * toReduceBy),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        } else {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    (float) (-231f * toReduceBy),
                                                    (float) (-299f * toReduceBy),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    } else {
                                        if (!Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter())) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    (float) (customProjectile.getBall().getMinDamageHeal() * toReduceBy),
                                                    (float) (customProjectile.getBall().getMaxDamageHeal() * toReduceBy),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    }
                                } else {

                                    if (customProjectile.getBall().getName().contains("Water")) {
                                        if (Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter())) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    customProjectile.getBall().getMinDamageHeal(),
                                                    customProjectile.getBall().getMaxDamageHeal(),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        } else {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    -231,
                                                    -299,
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    } else {
                                        if (!Warlords.game.onSameTeam((Player) nearEntity, customProjectile.getShooter())) {
                                            Warlords.getPlayer((Player) nearEntity).addHealth(
                                                    Warlords.getPlayer(customProjectile.getShooter()),
                                                    customProjectile.getBall().getName(),
                                                    customProjectile.getBall().getMinDamageHeal(),
                                                    customProjectile.getBall().getMaxDamageHeal(),
                                                    customProjectile.getBall().getCritChance(),
                                                    customProjectile.getBall().getCritMultiplier()
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    customProjectile.setRemove(true);
                } else if (location.distanceSquared(customProjectile.getStartingLocation()) >= 300 * 300) {
                    customProjectile.setRemove(true);
                }

                location.subtract(0, 1.5, 0);

                if (customProjectile.isRemove()) {
                    this.cancel();
                }

            }

        }.runTaskTimer(Warlords.getInstance(), 0, 0);

    }

    public static class CustomProjectile {

        private Player shooter;
        private Location startingLocation;
        private Location currentLocation;
        private Vector direction;
        private int maxDistance;
        private Projectile projectile;
        private boolean remove;

        public CustomProjectile(Player shooter, Location startingLocation, Location currentLocation, Vector direction, int maxDistance, Projectile projectile) {
            this.shooter = shooter;
            this.startingLocation = startingLocation;
            this.currentLocation = currentLocation;
            this.direction = direction;
            this.maxDistance = maxDistance;
            this.projectile = projectile;
            remove = false;
        }

        public Player getShooter() {
            return shooter;
        }

        public void setShooter(Player shooter) {
            this.shooter = shooter;
        }

        public Location getStartingLocation() {
            return startingLocation;
        }

        public void setStartingLocation(Location startingLocation) {
            this.startingLocation = startingLocation;
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(Location currentLocation) {
            this.currentLocation = currentLocation;
        }

        public Vector getDirection() {
            return direction;
        }

        public void setDirection(Vector direction) {
            this.direction = direction;
        }

        public int getMaxDistance() {
            return maxDistance;
        }

        public void setMaxDistance(int maxDistance) {
            this.maxDistance = maxDistance;
        }

        public Projectile getBall() {
            return projectile;
        }

        public void setBall(Projectile projectile) {
            this.projectile = projectile;
        }

        public boolean isRemove() {
            return remove;
        }

        public void setRemove(boolean remove) {
            this.remove = remove;
        }
    }
}