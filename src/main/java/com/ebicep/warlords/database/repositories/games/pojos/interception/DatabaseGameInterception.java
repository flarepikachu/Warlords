package com.ebicep.warlords.database.repositories.games.pojos.interception;

import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerResult;
import com.ebicep.warlords.events.game.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.WinAfterTimeoutOption;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Document(collection = "Games_Information_Interception")
public class DatabaseGameInterception extends DatabaseGameBase {

    @Field("time_left")
    protected int timeLeft;
    protected Team winner;
    @Field("blue_points")
    protected int bluePoints;
    @Field("red_points")
    protected int redPoints;
    protected DatabaseGamePlayersInterception players;

    public DatabaseGameInterception() {
    }

    public DatabaseGameInterception(@Nonnull Game game, @Nullable WarlordsGameTriggerWinEvent gameWinEvent, boolean counted) {
        super(game, counted);
        this.timeLeft = WinAfterTimeoutOption.getTimeRemaining(game).orElse(-1);
        this.winner = gameWinEvent == null || gameWinEvent.isCancelled() ? null : gameWinEvent.getDeclaredWinner();
        this.bluePoints = game.getPoints(Team.BLUE);
        this.redPoints = game.getPoints(Team.RED);
        this.players = new DatabaseGamePlayersInterception(game);
    }

    @Override
    public void updatePlayerStatsFromGame(DatabaseGameBase databaseGame, int multiplier) {
        players.blue.forEach(gamePlayerInterception -> DatabaseGameBase.updatePlayerStatsFromTeam(databaseGame, gamePlayerInterception,
                multiplier
        ));
        players.red.forEach(gamePlayerInterception -> DatabaseGameBase.updatePlayerStatsFromTeam(databaseGame, gamePlayerInterception,
                multiplier
        ));
    }

    @Override
    public DatabaseGamePlayerResult getPlayerGameResult(DatabaseGamePlayerBase player) {
        assert player instanceof DatabaseGamePlayersInterception.DatabaseGamePlayerInterception;

        if (bluePoints > redPoints) {
            return players.blue.contains((DatabaseGamePlayersInterception.DatabaseGamePlayerInterception) player) ? DatabaseGamePlayerResult.WON : DatabaseGamePlayerResult.LOST;
        } else if (redPoints > bluePoints) {
            return players.red.contains((DatabaseGamePlayersInterception.DatabaseGamePlayerInterception) player) ? DatabaseGamePlayerResult.WON : DatabaseGamePlayerResult.LOST;
        } else {
            return DatabaseGamePlayerResult.DRAW;
        }
    }

    @Override
    public void createHolograms() {

    }

    @Override
    public String getGameLabel() {
        return "";
    }

    @Override
    public List<String> getExtraLore() {
        return Arrays.asList(
                ChatColor.GRAY + "Time Left: " + ChatColor.GREEN + Utils.formatTimeLeft(timeLeft),
                ChatColor.GRAY + "Winner: " + winner.teamColor + winner.name,
                ChatColor.GRAY + "Blue Points: " + ChatColor.BLUE + bluePoints,
                ChatColor.GRAY + "Red Points: " + ChatColor.RED + redPoints,
                ChatColor.GRAY + "Players: " + ChatColor.YELLOW + (players.getBlue().size() + players.getRed().size())
        );
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public Team getWinner() {
        return winner;
    }

    public void setWinner(Team winner) {
        this.winner = winner;
    }

    public int getBluePoints() {
        return bluePoints;
    }

    public void setBluePoints(int bluePoints) {
        this.bluePoints = bluePoints;
    }

    public int getRedPoints() {
        return redPoints;
    }

    public void setRedPoints(int redPoints) {
        this.redPoints = redPoints;
    }
}
