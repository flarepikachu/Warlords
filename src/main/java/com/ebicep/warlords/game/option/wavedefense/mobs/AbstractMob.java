package com.ebicep.warlords.game.option.wavedefense.mobs;

import com.ebicep.customentities.nms.pve.CustomEntity;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.wavedefense.WaveDefenseOption;
import com.ebicep.warlords.player.general.Specializations;
import com.ebicep.warlords.player.general.Weapons;
import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.WarlordsNPC;
import com.ebicep.warlords.pve.weapons.AbstractWeapon;
import com.ebicep.warlords.util.bukkit.TextComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

public abstract class AbstractMob<T extends CustomEntity<?>> implements Mob {

    protected final T entity;
    protected final EntityInsentient entityInsentient;
    protected final LivingEntity livingEntity;
    protected final Location spawnLocation;
    protected final String name;
    protected final MobTier mobTier;
    protected final EntityEquipment ee;
    protected final int maxHealth;
    protected final float walkSpeed;
    protected final int damageResistance;
    protected final float minMeleeDamage;
    protected final float maxMeleeDamage;

    protected WarlordsNPC warlordsNPC;

    public AbstractMob(T entity, Location spawnLocation, String name, MobTier mobTier, EntityEquipment ee, int maxHealth, float walkSpeed, int damageResistance, float minMeleeDamage, float maxMeleeDamage) {
        this.entity = entity;
        this.spawnLocation = spawnLocation;
        this.name = name;
        this.mobTier = mobTier;
        this.ee = ee;
        this.maxHealth = maxHealth;
        this.walkSpeed = walkSpeed;
        this.damageResistance = damageResistance;
        this.minMeleeDamage = minMeleeDamage;
        this.maxMeleeDamage = maxMeleeDamage;

        entity.spawn(spawnLocation);

        this.entityInsentient = entity.get();
        this.entityInsentient.persistent = true;

        this.livingEntity = (LivingEntity) entityInsentient.getBukkitEntity();
        if (ee != null) {
            livingEntity.getEquipment().setBoots(ee.getBoots());
            livingEntity.getEquipment().setLeggings(ee.getLeggings());
            livingEntity.getEquipment().setChestplate(ee.getChestplate());
            livingEntity.getEquipment().setHelmet(ee.getHelmet());
            livingEntity.getEquipment().setItemInHand(ee.getItemInHand());
        } else {
            livingEntity.getEquipment().setHelmet(new ItemStack(Material.BARRIER));
        }
    }

    public WarlordsNPC toNPC(Game game, Team team, UUID uuid) {
        this.warlordsNPC = new WarlordsNPC(
                uuid,
                name,
                mobTier,
                Weapons.ABBADON,
                livingEntity,
                game,
                team,
                Specializations.PYROMANCER,
                maxHealth,
                walkSpeed,
                damageResistance,
                minMeleeDamage,
                maxMeleeDamage
        );

        WaveDefenseOption waveDefenseOption = (WaveDefenseOption) game.getOptions()
                .stream()
                .filter(option -> option instanceof WaveDefenseOption)
                .findFirst()
                .get();

        onSpawn(waveDefenseOption);
        game.addNPC(warlordsNPC);

        // temp
        double scale = 600.0;
        long playerCount = game.warlordsPlayers().count();
        double modifiedScale = scale - (playerCount > 1 ? 50 * playerCount : 0);
        float health = (float) Math.pow(warlordsNPC.getMaxHealth(), waveDefenseOption.getWaveCounter() / modifiedScale + 1);
        if (playerCount > 1 && warlordsNPC.getMobTier() == MobTier.BOSS) {
            warlordsNPC.setMaxHealth(health * (1 + (0.25f * playerCount)));
            warlordsNPC.setHealth(health * (1 + (0.25f * playerCount)));
        } else {
            warlordsNPC.setMaxHealth(health);
            warlordsNPC.setHealth(health);
        }

        return warlordsNPC;
    }

    public AbstractMob<T> prependOperation(UnaryOperator<WarlordsNPC> mapper) {
        mapper.apply(this.warlordsNPC);
        return this;
    }

    public abstract void onSpawn(WaveDefenseOption option);

    public abstract void whileAlive(int ticksElapsed, WaveDefenseOption option);

    public abstract void onAttack(WarlordsEntity attacker, WarlordsEntity receiver, String ability);

    public abstract void onDamageTaken(WarlordsEntity mob, WarlordsEntity attacker);

    public void onDeath(WarlordsEntity killer, Location deathLocation, WaveDefenseOption option) {
        dropWeapon(killer);
    }

    public void dropWeapon(WarlordsEntity killer) {
        if (ThreadLocalRandom.current().nextInt(0, 100) < dropRate() && DatabaseManager.playerService != null) {
            UUID uuid = killer.getUuid();
            DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(uuid);
            AbstractWeapon weapon = generateWeapon(uuid);
            databasePlayer.getPveStats().getWeaponInventory().add(weapon);
            DatabaseManager.queueUpdatePlayerAsync(databasePlayer);

            killer.getGame().forEachOnlinePlayer((player, team) -> {
                player.spigot().sendMessage(
                        new TextComponent(ChatColor.AQUA + killer.getName() + ChatColor.GRAY + " got lucky and found "),
                        new TextComponentBuilder(weapon.getName())
                                .setHoverItem(weapon.generateItemStack())
                                .getTextComponent(),
                        new TextComponent(ChatColor.GRAY + "!")
                );
            });
        }
    }

    public WarlordsNPC getWarlordsNPC() {
        return warlordsNPC;
    }

    public MobTier getMobTier() {
        return mobTier;
    }
}