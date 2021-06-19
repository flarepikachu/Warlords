package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.WarlordsPlayer;
import com.ebicep.warlords.classes.AbstractAbility;
import org.bukkit.entity.Player;

public class Windfury extends AbstractAbility {

    public Windfury() {
        super("Windfury Weapon", 0, 0, 15.66f, 30, 25, 135);
    }

    @Override
    public void updateDescription() {
        description = "§7Imbue your weapon with the power\n" +
                "§7of the wind, causing each of your\n" +
                "§7melee attacks to have a §e35% §7chance\n" +
                "§7to hit §e2 §7additional times for §c135%\n" +
                "§7weapon damage. The first melee hit is\n" +
                "§7guaranteed to activate Windfury. Lasts §68\n" +
                "§7seconds.";
    }

    @Override
    public void onActivate(Player player) {
        WarlordsPlayer warlordsPlayer = Warlords.getPlayer(player);
        warlordsPlayer.subtractEnergy(energyCost);
        warlordsPlayer.setWindfuryDuration(8);
        warlordsPlayer.setFirstProc(true);

        for (Player player1 : player.getWorld().getPlayers()) {
            player1.playSound(player.getLocation(), "shaman.windfuryweapon.activation", 2, 1);
        }
    }
}