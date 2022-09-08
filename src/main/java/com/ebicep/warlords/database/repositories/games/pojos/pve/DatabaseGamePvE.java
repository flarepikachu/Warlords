package com.ebicep.warlords.database.repositories.games.pojos.pve;

import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerResult;
import com.ebicep.warlords.events.game.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.option.Option;
import com.ebicep.warlords.game.option.RecordTimeElapsedOption;
import com.ebicep.warlords.game.option.wavedefense.WaveDefenseOption;
import com.ebicep.warlords.pve.DifficultyIndex;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document(collection = "Games_Information_PvE")
public class DatabaseGamePvE extends DatabaseGameBase {

    private DifficultyIndex difficulty;
    @Field("waves_cleared")
    private int wavesCleared;
    @Field("time_elapsed")
    private int timeElapsed;
    @Field("total_mobs_killed")
    private int totalMobsKilled;
    private List<DatabaseGamePlayerPvE> players = new ArrayList<>();

    public DatabaseGamePvE() {

    }

    public DatabaseGamePvE(@Nonnull Game game, @Nullable WarlordsGameTriggerWinEvent gameWinEvent, boolean counted) {
        super(game, counted);
        //this.difficulty =
        for (Option option : game.getOptions()) {
            if (option instanceof WaveDefenseOption) {
                WaveDefenseOption waveDefenseOption = (WaveDefenseOption) option;
                this.wavesCleared = waveDefenseOption.getWavesCleared();
                game.warlordsPlayers().forEach(warlordsPlayer -> players.add(new DatabaseGamePlayerPvE(warlordsPlayer, waveDefenseOption)));
            }
        }
        this.timeElapsed = RecordTimeElapsedOption.getTicksElapsed(game);
        this.totalMobsKilled = players.stream().mapToInt(DatabaseGamePlayerBase::getTotalKills).sum();
    }

    @Override
    public void updatePlayerStatsFromGame(DatabaseGameBase databaseGame, int multiplier) {
        players.forEach(databaseGamePlayerPvE -> DatabaseGameBase.updatePlayerStatsFromTeam(databaseGame,
                databaseGamePlayerPvE,
                multiplier
        ));
    }

    @Override
    public DatabaseGamePlayerResult getPlayerGameResult(DatabaseGamePlayerBase player) {
        return DatabaseGamePlayerResult.NONE;
    }

    @Override
    public void createHolograms() {

    }

    @Override
    public String getGameLabel() {
        return null;
    }

    @Override
    public List<String> getExtraLore() {
        return Arrays.asList(
                ChatColor.GRAY + "Time Elapsed: " + ChatColor.YELLOW + Utils.formatTimeLeft(timeElapsed),
                ChatColor.GRAY + "Waves Cleared: " + ChatColor.YELLOW + wavesCleared,
                ChatColor.GRAY + "Total Mobs Killed: " + ChatColor.YELLOW + totalMobsKilled,
                ChatColor.GRAY + "Players: " + ChatColor.YELLOW + players.size()
        );
    }

    public DifficultyIndex getDifficulty() {
        return difficulty;
    }

    public int getWavesCleared() {
        return wavesCleared;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public int getTotalMobsKilled() {
        return totalMobsKilled;
    }

    public List<DatabaseGamePlayerPvE> getPlayers() {
        return players;
    }
}
