package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.WarlordsPlayer;
import com.ebicep.warlords.classes.AbstractAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class IceBarrier extends AbstractAbility {

    public IceBarrier() {
        super("Ice Barrier", 0, 0, 47, 0, 0, 0, "ice barrier description");
    }

    @Override
    public void onActivate(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        WarlordsPlayer warlordsPlayer = Warlords.getPlayer(player);
        warlordsPlayer.setIceBarrier(6);
        //TODO add slowness
    }
}