package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.effects.circle.CircumferenceEffect;
import com.ebicep.warlords.events.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.EffectUtils;
import com.ebicep.warlords.util.ParticleEffect;
import com.ebicep.warlords.util.PlayerFilter;
import com.ebicep.warlords.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class Vindicate extends AbstractAbility {

    private final int radius = 8;
    private final int vindicateDuration = 12;
    private final int vindicateSelfDuration = 8;
    private float vindicateDamageReduction = 25;

    public Vindicate() {
        super("Vindicate", 0, 0, 55, 25, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Allies within an §e" + radius + " §7block radius gain the\n" +
                "§7status §6VIND §7which clears all de-buffs. In\n" +
                "§7addition, the status §6VIND §7prevents allies from being\n" +
                "§7affected by de-buffs and grants §625% §7knockback\n" +
                "§7resistance for §6" + vindicateDuration + " §7seconds." +
                "\n\n" +
                "§7You gain §e" + format(vindicateDamageReduction) + "% §7damage reduction for §6" + vindicateSelfDuration + " §7seconds instead.\n";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        wp.subtractEnergy(energyCost);

        Utils.playGlobalSound(player.getLocation(), "rogue.vindicate.activation", 2, 0.7f);
        Utils.playGlobalSound(player.getLocation(), "shaman.capacitortotem.pulse", 2, 0.7f);

        Vindicate tempVindicate = new Vindicate();

        for (WarlordsPlayer vindicateTarget : PlayerFilter
                .entitiesAround(wp, radius, radius, radius)
                .aliveTeammatesOfExcludingSelf(wp)
                .closestFirst(wp)
        ) {
            wp.sendMessage(
                WarlordsPlayer.RECEIVE_ARROW +
                ChatColor.GRAY + " Your Vindicate is now protecting " +
                ChatColor.YELLOW + vindicateTarget.getName() +
                ChatColor.GRAY + "!"
            );

            vindicateTarget.sendMessage(
                WarlordsPlayer.RECEIVE_ARROW + " " +
                ChatColor.GRAY + wp.getName() + "'s" +
                ChatColor.YELLOW + " Vindicate" +
                ChatColor.GRAY + " is now protecting you from de-buffs for " +
                ChatColor.GOLD + vindicateDuration +
                ChatColor.GRAY + " seconds!"
            );

            // Vindicate Immunity
            vindicateTarget.getSpeed().removeSlownessModifiers();
            vindicateTarget.getCooldownManager().removeDebuffCooldowns();
            vindicateTarget.getCooldownManager().removeCooldownByName("Vindicate Debuff Immunity");
            vindicateTarget.getCooldownManager().addRegularCooldown(
                    "Vindicate Debuff Immunity",
                    "VIND",
                    Vindicate.class,
                    tempVindicate,
                    wp,
                    CooldownTypes.BUFF,
                    cooldownManager -> {},
                    vindicateDuration * 20
            );
        }

        wp.getCooldownManager().addCooldown(new RegularCooldown<Vindicate>(
                "Vindicate Resistance",
                "VIND RES",
                Vindicate.class,
                tempVindicate,
                wp,
                CooldownTypes.BUFF,
                cooldownManager -> {},
                vindicateSelfDuration * 20
        ) {
            @Override
            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                return currentDamageValue * getVindicateDamageReduction();
            }
        });

        CircleEffect circle = new CircleEffect(wp.getGame(), wp.getTeam(), player.getLocation(), radius);
        circle.addEffect(new CircumferenceEffect(ParticleEffect.SPELL, ParticleEffect.REDSTONE).particlesPerCircumference(2));
        circle.playEffects();

        EffectUtils.playHelixAnimation(player, radius, 230, 130, 5);

        return true;
    }

    public float getVindicateDamageReduction() {
        return (100 - vindicateDamageReduction) / 100f;
    }

    public void setVindicateDamageReduction(int vindicateDamageReduction) {
        this.vindicateDamageReduction = vindicateDamageReduction;
    }
}
