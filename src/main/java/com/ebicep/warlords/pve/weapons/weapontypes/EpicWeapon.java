package com.ebicep.warlords.pve.weapons.weapontypes;

import com.ebicep.warlords.pve.weapons.AbstractBetterWeapon;
import com.ebicep.warlords.util.java.Utils;
import org.bukkit.ChatColor;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ebicep.warlords.pve.weapons.weapontypes.WeaponScore.getAverageValue;

public class EpicWeapon extends AbstractBetterWeapon implements Salvageable, WeaponScore, StatsRerollable {

    public static final int MELEE_DAMAGE_MIN = 120;
    @Transient
    public static final int MELEE_DAMAGE_MAX = 180;
    @Transient
    public static final int CRIT_CHANCE_MIN = 12;
    @Transient
    public static final int CRIT_CHANCE_MAX = 20;
    @Transient
    public static final int CRIT_MULTIPLIER_MIN = 150;
    @Transient
    public static final int CRIT_MULTIPLIER_MAX = 200;
    @Transient
    public static final int HEALTH_BONUS_MIN = 200;
    @Transient
    public static final int HEALTH_BONUS_MAX = 500;
    @Transient
    public static final int SPEED_BONUS_MIN = 2;
    @Transient
    public static final int SPEED_BONUS_MAX = 8;

    public EpicWeapon() {
    }

    public EpicWeapon(UUID uuid) {
        super(uuid);
    }

    @Override
    public ChatColor getChatColor() {
        return ChatColor.DARK_PURPLE;
    }

    @Override
    public List<String> getLore() {
        List<String> lore = new ArrayList<>(super.getLore());
        lore.add("");
        lore.add(getWeaponScoreString());
        return lore;
    }

    @Override
    public void generateStats() {
        this.meleeDamage = Utils.generateRandomValueBetweenInclusive(MELEE_DAMAGE_MIN, MELEE_DAMAGE_MAX);
        this.critChance = Utils.generateRandomValueBetweenInclusive(CRIT_CHANCE_MIN, CRIT_CHANCE_MAX);
        this.critMultiplier = Utils.generateRandomValueBetweenInclusive(CRIT_MULTIPLIER_MIN, CRIT_MULTIPLIER_MAX);
        this.healthBonus = Utils.generateRandomValueBetweenInclusive(HEALTH_BONUS_MIN, HEALTH_BONUS_MAX);

        this.speedBonus = Utils.generateRandomValueBetweenInclusive(SPEED_BONUS_MIN, SPEED_BONUS_MAX);
    }

    @Override
    public List<Double> getWeaponScoreAverageValues() {
        return Arrays.asList(
                getAverageValue(MELEE_DAMAGE_MIN, MELEE_DAMAGE_MAX, meleeDamage),
                getAverageValue(CRIT_CHANCE_MIN, CRIT_CHANCE_MAX, critChance),
                getAverageValue(CRIT_MULTIPLIER_MIN, CRIT_MULTIPLIER_MAX, critMultiplier),
                getAverageValue(HEALTH_BONUS_MIN, HEALTH_BONUS_MAX, healthBonus),
                getAverageValue(SPEED_BONUS_MIN, SPEED_BONUS_MAX, speedBonus)
        );
    }

    @Override
    public int getMinSalvageAmount() {
        return 12;
    }

    @Override
    public int getMaxSalvageAmount() {
        return 18;
    }

    @Override
    public int getRerollCost() {
        return 500;
    }

    @Override
    public void reroll() {
        generateStats();
    }
}
