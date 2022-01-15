package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.player.cooldowns.Cooldown;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.PlayerFilter;
import com.ebicep.warlords.util.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FallenSouls extends AbstractAbility {

    private static final float fallenSoulHitBox = 1.25f;
    private static final float fallenSoulSpeed = 1.95f;

    public FallenSouls() {
        super("Fallen Souls", 164f, 212f, 0, 55, 20, 180);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Summon a wave of fallen souls, dealing\n" +
                "§c" + format(minDamageHeal) + " §7- §c" + format(maxDamageHeal) + " §7damage to all enemies they\n" +
                "§7pass through. Each target hit reduces the\n" +
                "§7cooldown of Spirit Link by §62 §7seconds.";
    }

    @Override
    public void onActivate(WarlordsPlayer wp, Player player) {
        Location location = player.getLocation();
        ArmorStand fallenSoulLeft = player.getWorld().spawn(location.clone().subtract(0, .5, 0).add(Utils.getLeftDirection(location).multiply(.5)), ArmorStand.class);
        Location locationLeft = player.getLocation().add(player.getLocation().getDirection().multiply(.2));
        locationLeft.setYaw(location.getYaw() - 13);// - (int)(location.getPitch()/-10f * 1.6));
        ArmorStand fallenSoulMiddle = player.getWorld().spawn(location.clone().subtract(0, .5, 0), ArmorStand.class);
        Location locationMiddle = player.getLocation().add(player.getLocation().getDirection().multiply(.2));
        locationMiddle.setYaw(location.getYaw() - 0);
        ArmorStand fallenSoulRight = player.getWorld().spawn(location.clone().subtract(0, .5, 0).add(Utils.getRightDirection(location).multiply(.5)), ArmorStand.class);
        Location locationRight = player.getLocation().add(player.getLocation().getDirection().multiply(.2));
        locationRight.setYaw(location.getYaw() + 13);// + (int)(location.getPitch()/-10f * 1.6));

        FallenSoul fallenSoul = new FallenSoul(Warlords.getPlayer(player), fallenSoulLeft, fallenSoulMiddle, fallenSoulRight, player.getLocation(), player.getLocation(), player.getLocation(), locationLeft.getDirection(), locationMiddle.getDirection(), locationRight.getDirection(), this);

        wp.subtractEnergy(energyCost);

        for (Player player1 : player.getWorld().getPlayers()) {
            player1.playSound(player.getLocation(), "shaman.lightningbolt.impact", 2, 1.5f);
        }
        wp.getGame().getGameTasks().put(
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (!wp.getGame().isGameFreeze()) {

                            if ((fallenSoul.isLeftRemoved() && fallenSoul.isMiddleRemoved() && fallenSoul.isRightRemoved())) {
                                this.cancel();
                            }

                            ArmorStand leftSoul = fallenSoul.getFallenSoulLeft();
                            ArmorStand middleSoul = fallenSoul.getFallenSoulMiddle();
                            ArmorStand rightSoul = fallenSoul.getFallenSoulRight();

                            leftSoul.teleport(leftSoul.getLocation().add(fallenSoul.getDirectionLeft().clone().multiply(fallenSoulSpeed)));
                            middleSoul.teleport(middleSoul.getLocation().add(fallenSoul.getDirectionMiddle().clone().multiply(fallenSoulSpeed)));
                            rightSoul.teleport(rightSoul.getLocation().add(fallenSoul.getDirectionRight().clone().multiply(fallenSoulSpeed)));

                            List<Entity> nearLeft = (List<Entity>) leftSoul.getWorld().getNearbyEntities(leftSoul.getLocation().clone().add(0, 2, 0), fallenSoulHitBox, 1, fallenSoulHitBox);
                            List<Entity> nearMiddle = (List<Entity>) middleSoul.getWorld().getNearbyEntities(middleSoul.getLocation().clone().add(0, 2, 0), fallenSoulHitBox, 1, fallenSoulHitBox);
                            List<Entity> nearRight = (List<Entity>) rightSoul.getWorld().getNearbyEntities(rightSoul.getLocation().clone().add(0, 2, 0), fallenSoulHitBox, 1, fallenSoulHitBox);

                            damageNearByPlayers(nearLeft, wp, player, fallenSoul);
                            damageNearByPlayers(nearMiddle, wp, player, fallenSoul);
                            damageNearByPlayers(nearRight, wp, player, fallenSoul);

                            ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1, leftSoul.getLocation().add(0, 1.5, 0), 500);
                            ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1, middleSoul.getLocation().add(0, 1.5, 0), 500);
                            ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1, rightSoul.getLocation().add(0, 1.5, 0), 500);

                            if (!fallenSoul.isLeftRemoved() && leftSoul.getLocation().getWorld().getBlockAt(leftSoul.getLocation().clone().add(0, 2, 0)).getType() != Material.AIR || fallenSoul.getFallenSoulLeft().getTicksLived() > 25 / fallenSoulSpeed * 1.2) {
                                ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.7F, 1, leftSoul.getLocation().add(0, 1, 0), 500);
                                fallenSoul.getFallenSoulLeft().remove();
                                fallenSoul.setLeftRemoved(true);
                            }

                            if (!fallenSoul.isMiddleRemoved() && middleSoul.getLocation().getWorld().getBlockAt(middleSoul.getLocation().clone().add(0, 2, 0)).getType() != Material.AIR || fallenSoul.getFallenSoulMiddle().getTicksLived() > 25 / fallenSoulSpeed * 1.2) {
                                ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.7F, 1, middleSoul.getLocation().add(0, 1, 0), 500);
                                fallenSoul.getFallenSoulMiddle().remove();
                                fallenSoul.setMiddleRemoved(true);
                            }

                            if (!fallenSoul.isRightRemoved() && rightSoul.getLocation().getWorld().getBlockAt(rightSoul.getLocation().clone().add(0, 2, 0)).getType() != Material.AIR || fallenSoul.getFallenSoulRight().getTicksLived() > 25 / fallenSoulSpeed * 1.2) {
                                ParticleEffect.EXPLOSION_LARGE.display(0, 0, 0, 0.7F, 1, rightSoul.getLocation().add(0, 1, 0), 500);
                                fallenSoul.getFallenSoulRight().remove();
                                fallenSoul.setRightRemoved(true);
                            }
                        }

                    }

                }.runTaskTimer(Warlords.getInstance(), 0, 0),
                System.currentTimeMillis()
        );
    }

    public void damageNearByPlayers(List<Entity> near, WarlordsPlayer wp, Player player, FallenSoul fallenSoul) {
        PlayerFilter.entities(near)
                .filter(p -> !fallenSoul.getPlayersHit().contains(p))
                .aliveEnemiesOf(wp)
                .forEach((warlordsPlayer) -> {
                    warlordsPlayer.addDamageInstance(fallenSoul.getShooter(), fallenSoul.getFallenSouls().getName(), fallenSoul.getFallenSouls().getMinDamageHeal(), fallenSoul.getFallenSouls().getMaxDamageHeal(), fallenSoul.getFallenSouls().getCritChance(), fallenSoul.getFallenSouls().getCritMultiplier(), false);
                    fallenSoul.getPlayersHit().add(warlordsPlayer);
                    fallenSoul.getShooter().getSpec().getRed().subtractCooldown(2);
                    fallenSoul.getShooter().updateRedItem(player);
                    fallenSoul.getShooter().getCooldownManager().getCooldown(Soulbinding.class).stream()
                            .map(Cooldown::getCooldownObject)
                            .map(Soulbinding.class::cast)
                            .filter(soulbinding -> soulbinding.hasBoundPlayerSoul(warlordsPlayer))
                            .forEach(sb -> {
                                fallenSoul.getShooter().getSpec().getRed().subtractCooldown(1.5F);
                                fallenSoul.getShooter().getSpec().getPurple().subtractCooldown(1.5F);
                                fallenSoul.getShooter().getSpec().getBlue().subtractCooldown(1.5F);
                                fallenSoul.getShooter().getSpec().getOrange().subtractCooldown(1.5F);

                                fallenSoul.getShooter().updateRedItem(player);
                                fallenSoul.getShooter().updatePurpleItem(player);
                                fallenSoul.getShooter().updateBlueItem(player);
                                fallenSoul.getShooter().updateOrangeItem(player);

                                PlayerFilter.entitiesAround(player, 8, 8, 8)
                                        .aliveTeammatesOfExcludingSelf(wp)
                                        .closestFirst(player)
                                        .limit(2)
                                        .forEach((warlordsPlayer1) -> {

                                            warlordsPlayer1.getSpec().getRed().subtractCooldown(1);
                                            warlordsPlayer1.getSpec().getPurple().subtractCooldown(1);
                                            warlordsPlayer1.getSpec().getBlue().subtractCooldown(1);
                                            warlordsPlayer1.getSpec().getOrange().subtractCooldown(1);

                                            if (warlordsPlayer1.getEntity() instanceof Player) {
                                                Player p = (Player) warlordsPlayer1.getEntity();
                                                warlordsPlayer1.updateRedItem(p);
                                                warlordsPlayer1.updatePurpleItem(p);
                                                warlordsPlayer1.updateBlueItem(p);
                                                warlordsPlayer1.updateOrangeItem(p);
                                            }
                                        });
                            });
                });
    }


    public static class FallenSoul {

        private WarlordsPlayer shooter;
        private ArmorStand fallenSoulLeft;
        private ArmorStand fallenSoulMiddle;
        private ArmorStand fallenSoulRight;
        private Location locationLeft;
        private Location locationMiddle;
        private Location locationRight;
        private Vector directionLeft;
        private Vector directionMiddle;
        private Vector directionRight;
        private boolean leftRemoved;
        private boolean middleRemoved;
        private boolean rightRemoved;
        private FallenSouls fallenSouls;
        private List<WarlordsPlayer> playersHit;

        public FallenSoul(WarlordsPlayer shooter, ArmorStand fallenSoulLeft, ArmorStand fallenSoulMiddle, ArmorStand fallenSoulRight, Location locationLeft, Location locationMiddle, Location locationRight, Vector directionLeft, Vector directionMiddle, Vector directionRight, FallenSouls fallenSouls) {
            this.shooter = shooter;
            this.fallenSoulLeft = fallenSoulLeft;
            fallenSoulLeft.setHelmet(new ItemStack(Material.ACACIA_FENCE_GATE));
            fallenSoulLeft.setGravity(false);
            fallenSoulLeft.setVisible(false);
            fallenSoulLeft.setMarker(true);
            fallenSoulLeft.setHeadPose(new EulerAngle(directionLeft.getY() * -fallenSoulSpeed, -.2, 0));
            this.fallenSoulMiddle = fallenSoulMiddle;
            fallenSoulMiddle.setHelmet(new ItemStack(Material.ACACIA_FENCE_GATE));
            fallenSoulMiddle.setGravity(false);
            fallenSoulMiddle.setVisible(false);
            fallenSoulMiddle.setMarker(true);
            fallenSoulMiddle.setHeadPose(new EulerAngle(directionMiddle.getY() * -fallenSoulSpeed, 0, 0));
            this.fallenSoulRight = fallenSoulRight;
            fallenSoulRight.setHelmet(new ItemStack(Material.ACACIA_FENCE_GATE));
            fallenSoulRight.setGravity(false);
            fallenSoulRight.setVisible(false);
            fallenSoulRight.setMarker(true);
            fallenSoulRight.setHeadPose(new EulerAngle(directionRight.getY() * -fallenSoulSpeed, .2, 0));
            this.locationLeft = locationLeft;
            this.locationMiddle = locationMiddle;
            this.locationRight = locationRight;
            this.directionLeft = directionLeft;
            this.directionMiddle = directionMiddle;
            this.directionRight = directionRight;
            this.fallenSouls = fallenSouls;
            leftRemoved = false;
            middleRemoved = false;
            rightRemoved = false;
            playersHit = new ArrayList<>();
            playersHit.add(shooter);
        }

        public WarlordsPlayer getShooter() {
            return shooter;
        }

        public void setShooter(WarlordsPlayer shooter) {
            this.shooter = shooter;
        }

        public ArmorStand getFallenSoulLeft() {
            return fallenSoulLeft;
        }

        public void setFallenSoulLeft(ArmorStand fallenSoulLeft) {
            this.fallenSoulLeft = fallenSoulLeft;
        }

        public ArmorStand getFallenSoulMiddle() {
            return fallenSoulMiddle;
        }

        public void setFallenSoulMiddle(ArmorStand fallenSoulMiddle) {
            this.fallenSoulMiddle = fallenSoulMiddle;
        }

        public ArmorStand getFallenSoulRight() {
            return fallenSoulRight;
        }

        public void setFallenSoulRight(ArmorStand fallenSoulRight) {
            this.fallenSoulRight = fallenSoulRight;
        }

        public Location getLocationLeft() {
            return locationLeft;
        }

        public void setLocationLeft(Location locationLeft) {
            this.locationLeft = locationLeft;
        }

        public Location getLocationMiddle() {
            return locationMiddle;
        }

        public void setLocationMiddle(Location locationMiddle) {
            this.locationMiddle = locationMiddle;
        }

        public Location getLocationRight() {
            return locationRight;
        }

        public void setLocationRight(Location locationRight) {
            this.locationRight = locationRight;
        }

        public Vector getDirectionLeft() {
            return directionLeft;
        }

        public void setDirectionLeft(Vector directionLeft) {
            this.directionLeft = directionLeft;
        }

        public Vector getDirectionMiddle() {
            return directionMiddle;
        }

        public void setDirectionMiddle(Vector directionMiddle) {
            this.directionMiddle = directionMiddle;
        }

        public Vector getDirectionRight() {
            return directionRight;
        }

        public void setDirectionRight(Vector directionRight) {
            this.directionRight = directionRight;
        }

        public FallenSouls getFallenSouls() {
            return fallenSouls;
        }

        public boolean isLeftRemoved() {
            return leftRemoved;
        }

        public void setLeftRemoved(boolean leftRemoved) {
            this.leftRemoved = leftRemoved;
        }

        public boolean isMiddleRemoved() {
            return middleRemoved;
        }

        public void setMiddleRemoved(boolean middleRemoved) {
            this.middleRemoved = middleRemoved;
        }

        public boolean isRightRemoved() {
            return rightRemoved;
        }

        public void setRightRemoved(boolean rightRemoved) {
            this.rightRemoved = rightRemoved;
        }

        public void setFallenSouls(FallenSouls fallenSouls) {
            this.fallenSouls = fallenSouls;
        }

        public List<WarlordsPlayer> getPlayersHit() {
            return playersHit;
        }

        public void setPlayersHit(List<WarlordsPlayer> playersHit) {
            this.playersHit = playersHit;
        }

    }
}
