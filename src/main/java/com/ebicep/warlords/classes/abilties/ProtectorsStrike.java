package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.internal.AbstractStrikeBase;
import com.ebicep.warlords.player.WarlordsPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ProtectorsStrike extends AbstractStrikeBase {

    private int convertPercent = 100;
    private int selfConvertPercent = 50;

    public ProtectorsStrike() {
        super("Protector's Strike", 261, 352, 0, 90, 20, 175);
    }

    @Override
    public void updateDescription(Player player) {
        int boost = convertPercent == 100 ? 100 : 120;
        int selfBoost = selfConvertPercent == 50 ? 50 : 60;
        description = "§7Strike the targeted enemy player,\n" +
                "§7causing §c261 §7- §c352 §7damage\n" +
                "§7and healing two nearby allies for\n" +
                "§a" + boost + "% §7of the damage done. Also\n" +
                "§7heals yourself by §a" + selfBoost + "% §7of the\n" +
                "§7damage done.";
    }

    @Override
    protected void onHit(@Nonnull WarlordsPlayer wp, @Nonnull Player player, @Nonnull WarlordsPlayer nearPlayer) {
        if (standingOnConsecrate(player, nearPlayer)) {
            nearPlayer.addDamageInstance(wp, name, minDamageHeal * 1.15f, maxDamageHeal * 1.15f, critChance, critMultiplier, false);
        } else {
            nearPlayer.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
        }
    }

    public void setConvertPercent(int convertPercent) {
        this.convertPercent = convertPercent;
    }

    public void setSelfConvertPercent(int selfConvertPercent) {
        this.selfConvertPercent = selfConvertPercent;
    }
}