package com.ebicep.warlords.classes.abilties;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.classes.internal.AbstractStrikeBase;
import com.ebicep.warlords.player.CooldownTypes;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import com.ebicep.warlords.skilltree.trees.SingleUltTree;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class CripplingStrike extends AbstractStrikeBase {

    private CripplingStrikeTree cripplingStrikeTree;
    private boolean speedBoost = false;
    private boolean reduceCooldownOrbs = false;
    private boolean lifeLeach = false;

    public CripplingStrike() {
        super("Crippling Strike", -362.25f, -498, 0, 100, 15, 200);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Strike the targeted enemy player,\n" +
                "§7causing §c" + Math.floor(-minDamageHeal) + " §7- §c" + -maxDamageHeal + " §7damage\n" +
                "§7and §ccrippling §7them for §63 §7seconds.\n" +
                "§7A §ccrippled §7player deals §c12.5% §7less\n" +
                "§7damage for the duration of the effect.";
    }

    @Override
    protected void onHit(@Nonnull WarlordsPlayer wp, @Nonnull Player player, @Nonnull WarlordsPlayer nearPlayer) {
        nearPlayer.addHealth(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
        if (!(nearPlayer.getCooldownManager().hasCooldown(CripplingStrike.class))) {
            nearPlayer.sendMessage(ChatColor.GRAY + "You are " + ChatColor.RED + "crippled" + ChatColor.GRAY + ".");
        }
        nearPlayer.getCooldownManager().removeCooldown(CripplingStrike.class);
        nearPlayer.getCooldownManager().addCooldown(name, this.getClass(), new CripplingStrike(), "CRIP", 3, wp, CooldownTypes.DEBUFF);
        if (speedBoost) {
            wp.getSpeed().addSpeedModifier("Crippling", 5, 2 * 20);
        }
        if (reduceCooldownOrbs) {
            wp.getSpec().getBlue().subtractCooldown(1);
            wp.updateBlueItem();
        }
    }

    @Override
    public void createSkillTreeAbility(WarlordsPlayer warlordsPlayer, SkillTree skillTree) {
        cripplingStrikeTree = new CripplingStrikeTree(skillTree, this, name, new ItemStack(warlordsPlayer.getWeapon().item));
        setSkillTree(cripplingStrikeTree);
    }

    public void setSpeedBoost(boolean speedBoost) {
        this.speedBoost = speedBoost;
    }

    public void setReduceCooldownOrbs(boolean reduceCooldownOrbs) {
        this.reduceCooldownOrbs = reduceCooldownOrbs;
    }

    public void setLifeLeach(boolean lifeLeach) {
        this.lifeLeach = lifeLeach;
    }

    public boolean isLifeLeach() {
        return lifeLeach;
    }
}

class CripplingStrikeTree extends SingleUltTree {

    public CripplingStrikeTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);

        leftUpgrades.add(new Upgrade(this, 3, 4, 0, 1, "Energy Crippling", "Crippled targets receive less energy per second"));
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Reduced Cost", "Reduce energy cost of Crippling Strike"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Speed Boost", "Gain a small speed boost for a short \nduration for each Crippling Strike"));

        rightUpgrades.add(new Upgrade(this, 5, 4, 0, 1, "Heavy Crippling", "Increases damage reduction effect of Crippling"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Cooldown Reduction", "Reduce cooldown of Orbs of Life by a certain \namount of time per Crippling Strike"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Life Leech", "Restores a certain percentage of \ndamage dealt by Crippling Strike"));

        lastUpgrade = new Upgrade(this, 4, 1, 0, 1, "Mount Crippling", "Crippled targets cannot mount");
    }

    @Override
    public void doFirstLeftUpgrade() {
        //-10 energy per second
    }

    @Override
    public void doSecondLeftUpgrade() {
        ability.setEnergyCost(ability.getEnergyCost() - 10);
    }

    @Override
    public void doThirdLeftUpgrade() {
        //5% boost
        ((CripplingStrike) ability).setSpeedBoost(true);
    }

    @Override
    public void doFirstRightUpgrade() {
        //15% more reduction
    }

    @Override
    public void doSecondRightUpgrade() {
        ((CripplingStrike) ability).setReduceCooldownOrbs(true);

    }

    @Override
    public void doThirdRightUpgrade() {
        ((CripplingStrike) ability).setLifeLeach(true);

    }

    @Override
    public void doLastUpgrade() {
    }
}