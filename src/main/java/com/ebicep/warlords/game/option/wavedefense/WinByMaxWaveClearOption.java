package com.ebicep.warlords.game.option.wavedefense;

import com.ebicep.warlords.events.game.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.events.game.pve.WarlordsGameWaveClearEvent;
import com.ebicep.warlords.events.game.pve.WarlordsGameWaveEditEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.Option;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WinByMaxWaveClearOption implements Option {

    @Override
    public void register(Game game) {
        for (Option option : game.getOptions()) {
            if (option instanceof WaveDefenseOption) {
                game.registerEvents(new Listener() {

                    @EventHandler
                    public void onWaveClear(WarlordsGameWaveClearEvent event) {
                        if (event.getWaveCleared() >= ((WaveDefenseOption) option).getMaxWave()) {
                            Bukkit.getPluginManager().callEvent(new WarlordsGameTriggerWinEvent(game, WinByMaxWaveClearOption.this, Team.BLUE));
                        }
                    }

                    @EventHandler
                    public void onWaveEdit(WarlordsGameWaveEditEvent event) {
                        if (event.getWaveCleared() >= ((WaveDefenseOption) option).getMaxWave()) {
                            Bukkit.getPluginManager().callEvent(new WarlordsGameTriggerWinEvent(game, WinByMaxWaveClearOption.this, Team.BLUE));
                        }
                    }

                });
                return;
            }
        }

    }

}