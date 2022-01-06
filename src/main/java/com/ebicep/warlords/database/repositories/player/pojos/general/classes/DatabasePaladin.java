package com.ebicep.warlords.database.repositories.player.pojos.general.classes;

import com.ebicep.warlords.database.repositories.games.GameMode;
import com.ebicep.warlords.database.repositories.games.pojos.DatabaseGamePlayers;
import com.ebicep.warlords.database.repositories.player.pojos.AbstractDatabaseStatInformation;
import com.ebicep.warlords.database.repositories.player.pojos.DatabaseWarlordsClass;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabaseSpecialization;
import com.ebicep.warlords.player.ArmorManager;
import com.ebicep.warlords.player.ClassesSkillBoosts;

public class DatabasePaladin extends AbstractDatabaseStatInformation implements DatabaseWarlordsClass {

    private DatabaseSpecialization avenger = new DatabaseSpecialization(ClassesSkillBoosts.AVENGER_STRIKE);
    private DatabaseSpecialization crusader = new DatabaseSpecialization(ClassesSkillBoosts.CRUSADER_STRIKE);
    private DatabaseSpecialization protector = new DatabaseSpecialization(ClassesSkillBoosts.PROTECTOR_STRIKE);
    protected ArmorManager.Helmets helmet = ArmorManager.Helmets.SIMPLE_PALADIN_HELMET;
    protected ArmorManager.ArmorSets armor = ArmorManager.ArmorSets.SIMPLE_CHESTPLATE_PALADIN;

    public DatabasePaladin() {
        super();
    }

    @Override
    public void updateCustomStats(GameMode gameMode, boolean isCompGame, DatabaseGamePlayers.GamePlayer gamePlayer, boolean won, boolean add) {
        //UPDATE SPEC EXPERIENCE
        this.experience += add ? gamePlayer.getExperienceEarnedSpec() : -gamePlayer.getExperienceEarnedSpec();
    }

    @Override
    public DatabaseSpecialization[] getSpecs() {
        return new DatabaseSpecialization[]{avenger, crusader, protector};
    }

    public DatabaseSpecialization getAvenger() {
        return avenger;
    }

    public DatabaseSpecialization getCrusader() {
        return crusader;
    }

    public DatabaseSpecialization getProtector() {
        return protector;
    }

    public ArmorManager.Helmets getHelmet() {
        return helmet;
    }

    public ArmorManager.ArmorSets getArmor() {
        return armor;
    }

    public void setHelmet(ArmorManager.Helmets helmet) {
        this.helmet = helmet;
    }

    public void setArmor(ArmorManager.ArmorSets armor) {
        this.armor = armor;
    }
}