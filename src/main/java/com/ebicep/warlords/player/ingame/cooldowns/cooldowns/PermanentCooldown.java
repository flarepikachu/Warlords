package com.ebicep.warlords.player.ingame.cooldowns.cooldowns;

import com.ebicep.warlords.player.ingame.WarlordsEntity;
import com.ebicep.warlords.player.ingame.cooldowns.AbstractCooldown;
import com.ebicep.warlords.player.ingame.cooldowns.CooldownTypes;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;


public class PermanentCooldown<T> extends AbstractCooldown<T> {

    /**
     * <p>cooldown = this
     * <p>ticksLeft = ticksLeft of cooldown
     * <p>counter = counter incrementing every tick, separate from ticksLeft
     */
    protected final List<BiConsumer<PermanentCooldown<T>, Integer>> consumers;
    protected final boolean removeOnDeath;
    protected int ticksElapsed;

    public PermanentCooldown(String name, String nameAbbreviation, Class<T> cooldownClass, T cooldownObject, WarlordsEntity from, CooldownTypes cooldownType, boolean removeOnDeath) {
        super(name, nameAbbreviation, cooldownClass, cooldownObject, from, cooldownType, cooldownManager -> {
        });
        this.removeOnDeath = removeOnDeath;
        this.consumers = new ArrayList<>();
    }

    @SafeVarargs
    public PermanentCooldown(String name, String nameAbbreviation, Class<T> cooldownClass, T cooldownObject, WarlordsEntity from, CooldownTypes cooldownType, boolean removeOnDeath, BiConsumer<PermanentCooldown<T>, Integer>... biConsumers) {
        super(name, nameAbbreviation, cooldownClass, cooldownObject, from, cooldownType, cooldownManager -> {
        });
        this.removeOnDeath = removeOnDeath;
        this.consumers = new ArrayList<>(Arrays.asList(biConsumers));
    }

    @Override
    public String getNameAbbreviation() {
        if (nameAbbreviation == null) {
            return null;
        }

        return ChatColor.GREEN + nameAbbreviation + ChatColor.GRAY + ":" + ChatColor.GOLD + "INF";
    }

    @Override
    public void onTick() {
        consumers.forEach(integerConsumer -> integerConsumer.accept(this, ticksElapsed));
        this.ticksElapsed++;
    }

    @Override
    public boolean removeCheck() {
        return false;
    }

    public List<BiConsumer<PermanentCooldown<T>, Integer>> getConsumers() {
        return consumers;
    }

    public boolean isRemoveOnDeath() {
        return removeOnDeath;
    }

    public int getTicksElapsed() {
        return ticksElapsed;
    }
}