package com.ebicep.warlords.database.repositories.player.pojos.ctf;

import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGameBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerBase;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayerResult;
import com.ebicep.warlords.database.repositories.games.pojos.ctf.DatabaseGameCTF;
import com.ebicep.warlords.database.repositories.games.pojos.ctf.DatabaseGamePlayersCTF;
import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.game.GameMode;
import org.springframework.data.mongodb.core.mapping.Field;

public class CTFDatabaseStatInformation extends AbstractDatabaseStatInformation {

    @Field("flags_captured")
    private int flagsCaptured = 0;
    @Field("flags_returned")
    private int flagsReturned = 0;
    @Field("total_blocks_travelled")
    private long totalBlocksTravelled = 0;
    @Field("most_blocks_travelled")
    private long mostBlocksTravelled = 0;
    @Field("total_time_in_respawn")
    private long totalTimeInRespawn = 0;
    @Field("total_time_played")
    private long totalTimePlayed = 0;

    public CTFDatabaseStatInformation() {
    }

    @Override
    public void updateCustomStats(DatabaseGameBase databaseGame, GameMode gameMode, DatabaseGamePlayerBase gamePlayer, DatabaseGamePlayerResult result, boolean add) {
        assert databaseGame instanceof DatabaseGameCTF;
        assert gamePlayer instanceof DatabaseGamePlayersCTF.DatabaseGamePlayerCTF;

        this.flagsCaptured += ((DatabaseGamePlayersCTF.DatabaseGamePlayerCTF) gamePlayer).getFlagCaptures();
        this.flagsReturned += ((DatabaseGamePlayersCTF.DatabaseGamePlayerCTF) gamePlayer).getFlagReturns();
        this.totalBlocksTravelled += gamePlayer.getBlocksTravelled();
        if (this.mostBlocksTravelled < gamePlayer.getBlocksTravelled()) {
            this.mostBlocksTravelled = gamePlayer.getBlocksTravelled();
        }
        this.totalTimeInRespawn += ((DatabaseGamePlayersCTF.DatabaseGamePlayerCTF) gamePlayer).getSecondsInRespawn();
        this.totalTimePlayed += 900 - ((DatabaseGameCTF) databaseGame).getTimeLeft();

    }

    public int getFlagsCaptured() {
        return flagsCaptured;
    }

    public int getFlagsReturned() {
        return flagsReturned;
    }

    public long getTotalBlocksTravelled() {
        return totalBlocksTravelled;
    }

    public long getMostBlocksTravelled() {
        return mostBlocksTravelled;
    }

    public long getTotalTimeInRespawn() {
        return totalTimeInRespawn;
    }

    public long getTotalTimePlayed() {
        return totalTimePlayed;
    }
}