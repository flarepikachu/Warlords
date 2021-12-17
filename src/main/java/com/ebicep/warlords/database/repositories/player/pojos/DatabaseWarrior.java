package com.ebicep.warlords.database.repositories.player.pojos;


import com.ebicep.warlords.player.ArmorManager;

public class DatabaseWarrior extends DatabaseWarlordsClass {

    private DatabaseSpecialization berserker = new DatabaseSpecialization();
    private DatabaseSpecialization defender = new DatabaseSpecialization();
    private DatabaseSpecialization revenant = new DatabaseSpecialization();
    protected ArmorManager.Helmets helmet = ArmorManager.Helmets.SIMPLE_WARRIOR_HELMET;
    protected ArmorManager.ArmorSets armor = ArmorManager.ArmorSets.SIMPLE_CHESTPLATE_WARRIOR;

    public DatabaseWarrior() {
        super();
    }

    public DatabaseSpecialization[] getSpecs() {
        return new DatabaseSpecialization[]{berserker, defender, revenant};
    }

    public DatabaseSpecialization getBerserker() {
        return berserker;
    }

    public DatabaseSpecialization getDefender() {
        return defender;
    }

    public DatabaseSpecialization getRevenant() {
        return revenant;
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