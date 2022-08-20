package com.ebicep.warlords.game.option.wavedefense.mobs.bosses;

import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.events.player.WarlordsDeathEvent;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.wavedefense.WaveDefenseOption;
import com.ebicep.warlords.game.option.wavedefense.mobs.MobTier;
import com.ebicep.warlords.game.option.wavedefense.mobs.bosses.bossminions.NarmerAcolyte;
import com.ebicep.warlords.game.option.wavedefense.mobs.mobtypes.BossMob;
import com.ebicep.warlords.game.option.wavedefense.mobs.zombie.AbstractZombie;
import com.ebicep.warlords.game.option.wavedefense.mobs.zombie.BasicZombie;
import com.ebicep.warlords.player.general.ArmorManager;
import com.ebicep.warlords.player.general.Weapons;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.pve.SkullID;
import com.ebicep.warlords.util.pve.SkullUtils;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class Narmer extends AbstractZombie implements BossMob {

    private int acolytesAlive = 0;
    private int timeUntilNewAcolyte = 0; // ticks
    private int acolyteDeathWindow = 0; // ticks

    public Narmer(Location spawnLocation) {
        super(spawnLocation,
                "Narmer",
                MobTier.BOSS,
                new Utils.SimpleEntityEquipment(
                        SkullUtils.getSkullFrom(SkullID.BURNING_WITHER_SKELETON),
                        Utils.applyColorTo(Material.LEATHER_CHESTPLATE, 255, 160, 160),
                        ArmorManager.ArmorSets.GREATER_LEGGINGS.itemRed,
                        Utils.applyColorTo(Material.LEATHER_BOOTS, 255, 160, 160),
                        Weapons.WALKING_STICK.getItem()
                ),
                16000,
                0.16f,
                20,
                1600,
                2000
        );
    }

    @Override
    public void onSpawn(WaveDefenseOption option) {
        for (WarlordsEntity we : PlayerFilter.playingGame(getWarlordsNPC().getGame())) {
            if (we.getEntity() instanceof Player) {
                PacketUtils.sendTitle(
                        (Player) we.getEntity(),
                        ChatColor.GOLD + getWarlordsNPC().getName(),
                        ChatColor.YELLOW + "Unifier of Worlds",
                        20, 30, 20
                );
            }
        }

        for (int i = 0; i < option.getGame().warlordsPlayers().count(); i++) {
            NarmerAcolyte acolyte = new NarmerAcolyte(warlordsNPC.getLocation());
            acolyte.toNPC(warlordsNPC.getGame(), Team.RED, UUID.randomUUID());
            warlordsNPC.getGame().addNPC(acolyte.getWarlordsNPC());
            option.getMobs().add(acolyte);
            acolyte.getWarlordsNPC().teleport(warlordsNPC.getLocation());
            acolytesAlive++;
        }

        for (int i = 0; i < 12; i++) {
            BasicZombie basicZombie = new BasicZombie(warlordsNPC.getLocation());
            basicZombie.toNPC(warlordsNPC.getGame(), Team.RED, UUID.randomUUID());
            warlordsNPC.getGame().addNPC(basicZombie.getWarlordsNPC());
            option.getMobs().add(basicZombie);
        }


        option.getGame().registerEvents(new Listener() {
            @EventHandler
            private void onAllyDeath(WarlordsDeathEvent event) {
                float currentHealth = warlordsNPC.getHealth() * 0.15f;

                if (event.getPlayer().isTeammate(warlordsNPC)) {
                    warlordsNPC.setHealth(warlordsNPC.getHealth() + currentHealth);
                    //Bukkit.broadcastMessage("healed 15% current hp");
                }

                if (event.getPlayer().getName().equals("Acolyte of Narmer")) {
                    acolytesAlive--;

                    //Bukkit.broadcastMessage("acolyte died");

                    Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.ENDERDRAGON_GROWL, 2, 0.4f);
                    EffectUtils.playHelixAnimation(
                            warlordsNPC.getLocation().add(0, 0.15, 0),
                            12,
                            ParticleEffect.SPELL,
                            3,
                            60
                    );

                    if (acolyteDeathWindow > 0) {
                        //Bukkit.broadcastMessage("mega execute");
                        Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.WITHER_DEATH, 500, 0.2f);
                        Utils.playGlobalSound(warlordsNPC.getLocation(), Sound.WITHER_DEATH, 500, 0.2f);
                        EffectUtils.strikeLightning(warlordsNPC.getLocation(), false, 12);
                        for (WarlordsEntity enemy : PlayerFilter
                                .entitiesAround(warlordsNPC, 40, 40, 40)
                                .aliveEnemiesOf(warlordsNPC)
                        ) {
                            enemy.addDamageInstance(
                                    warlordsNPC,
                                    "Acolyte Revenge",
                                    965 * 8,
                                    1138 * 8,
                                    -1,
                                    100,
                                    false
                            );
                        }
                    } else {
                        for (WarlordsEntity enemy : PlayerFilter
                                .entitiesAround(warlordsNPC, 12, 12, 12)
                                .aliveEnemiesOf(warlordsNPC)
                        ) {
                            enemy.addDamageInstance(
                                    warlordsNPC,
                                    "Acolyte Revenge",
                                    965,
                                    1138,
                                    -1,
                                    100,
                                    false
                            );
                        }
                    }

                    if (acolyteDeathWindow <= 0) {
                        acolyteDeathWindow = 20;
                    }

                    timeUntilNewAcolyte = 300;
                }
            }
        });
    }

    @Override
    public void whileAlive(int ticksElapsed, WaveDefenseOption option) {
        Location loc = warlordsNPC.getLocation();
        if (ticksElapsed % 200 == 0) {
            //Bukkit.broadcastMessage("earthquake");
            Utils.playGlobalSound(loc, Sound.ENDERDRAGON_GROWL, 2, 0.4f);
            EffectUtils.strikeLightning(loc, false);
            EffectUtils.playSphereAnimation(loc, 12, ParticleEffect.SPELL_WITCH, 2);
            EffectUtils.playHelixAnimation(loc, 12, ParticleEffect.FIREWORKS_SPARK, 2, 40);
            for (WarlordsEntity enemy : PlayerFilter
                    .entitiesAround(warlordsNPC, 12, 12, 12)
                    .aliveEnemiesOf(warlordsNPC)
            ) {
                enemy.addDamageInstance(
                        warlordsNPC,
                        "Ground Shred",
                        600,
                        900,
                        -1,
                        100,
                        false
                );
            }
        }

        if (ticksElapsed % 300 == 0) {
            //Bukkit.broadcastMessage("projectile");
            warlordsNPC.getSpec().getRed().onActivate(warlordsNPC, null);
        }

        float executeHealth = warlordsNPC.getMaxHealth() * 0.4f;
        for (WarlordsEntity acolyte : PlayerFilter
                .playingGame(option.getGame())
                .filter(we -> we.getName().equals("Acolyte of Narmer"))
        ) {
            if (ticksElapsed % 20 == 0) {
                EffectUtils.playParticleLinkAnimation(loc, acolyte.getLocation(), ParticleEffect.DRIP_LAVA);
            }
            if (warlordsNPC.getHealth() < executeHealth && acolyte.isAlive()) {
                warlordsNPC.setHealth(warlordsNPC.getHealth());
                option.getGame().forEachOnlineWarlordsEntity(we -> {
                    Utils.playGlobalSound(loc, Sound.BLAZE_HIT, 2, 0.2f);
                    Utils.playGlobalSound(loc, "mage.arcaneshield.activation", 0.8f, 0.5f);
                    we.sendMessage(ChatColor.RED + "Narmer is invincible while his acolytes are still alive!");
                });
            }
        }

        if (acolytesAlive < option.getGame().warlordsPlayers().count() && timeUntilNewAcolyte <= 0) {
            //Bukkit.broadcastMessage("spawned new acolyte");
            NarmerAcolyte newAcolyte = new NarmerAcolyte(loc);
            newAcolyte.toNPC(warlordsNPC.getGame(), Team.RED, UUID.randomUUID());
            warlordsNPC.getGame().addNPC(newAcolyte.getWarlordsNPC());
            option.getMobs().add(newAcolyte);
            acolytesAlive++;
            timeUntilNewAcolyte = 300;
        }

        //Bukkit.broadcastMessage("ticks until new acolyte: " + timeUntilNewAcolyte);
        if (timeUntilNewAcolyte > 0) {
            timeUntilNewAcolyte--;
        }

        //Bukkit.broadcastMessage("ticks: " + acolyteDeathWindow);
        if (acolyteDeathWindow > 0) {
            acolyteDeathWindow--;
        }
    }

    @Override
    public void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, String ability) {
        Utils.addKnockback(attacker.getLocation(), receiver, -2.5, 0.25f);
    }

    @Override
    public void onDamageTaken(WarlordsEntity mob, WarlordsEntity attacker) {
        EffectUtils.playRandomHitEffect(mob.getLocation(), 255, 255, 255, 7);
    }

    @Override
    public void onDeath(WarlordsEntity killer, Location deathLocation, WaveDefenseOption option) {
        EffectUtils.playHelixAnimation(warlordsNPC.getLocation(), 6, ParticleEffect.FIREWORKS_SPARK, 3, 20);
        FireWorkEffectPlayer.playFirework(deathLocation, FireworkEffect.builder()
                .withColor(Color.WHITE)
                .with(FireworkEffect.Type.STAR)
                .withTrail()
                .build());
    }
}