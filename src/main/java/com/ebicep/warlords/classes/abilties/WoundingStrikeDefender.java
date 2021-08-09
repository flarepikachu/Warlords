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

public class WoundingStrikeDefender extends AbstractStrikeBase {

    private WoundingStrikeDefenderTree woundingStrikeDefenderTree;
    private boolean bleed = false;
    private boolean toxicBlade = false;

    public WoundingStrikeDefender() {
        super("Wounding Strike", -415.8f, -556.5f, 0, 100, 20, 200);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7Strike the targeted enemy player,\n" +
                "§7causing §c" + Math.floor(-minDamageHeal) + " §7- §c" + Math.floor(-maxDamageHeal) + " §7damage\n" +
                "§7and §cwounding §7them for §63 §7seconds.\n" +
                "§7A wounded player receives §c25% §7less\n" +
                "§7healing for the duration of the effect.";
    }

    @Override
    protected void onHit(@Nonnull WarlordsPlayer wp, @Nonnull Player player, @Nonnull WarlordsPlayer nearPlayer) {
        nearPlayer.addHealth(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier);
        if (!(nearPlayer.getCooldownManager().hasCooldown(WoundingStrikeBerserker.class) || nearPlayer.getCooldownManager().hasCooldown(WoundingStrikeDefender.class))) {
            nearPlayer.sendMessage(ChatColor.GRAY + "You are " + ChatColor.RED + "wounded" + ChatColor.GRAY + ".");
        }
        if (!nearPlayer.getCooldownManager().hasCooldown(WoundingStrikeBerserker.class)) {
            nearPlayer.getCooldownManager().removeCooldown(WoundingStrikeDefender.class);
            nearPlayer.getCooldownManager().addCooldown(name, this.getClass(), new WoundingStrikeDefender(), "WND", 3, wp, CooldownTypes.DEBUFF);
        }
        if (bleed) {
            int time = 3;
            time += woundingStrikeDefenderTree.getRightUpgrades().getLast().getCounter() * 2;
            nearPlayer.getCooldownManager().addCooldown("Bleed", this.getClass(), new WoundingStrikeBerserker(), "BLEED", time, wp, CooldownTypes.DEBUFF);
        }
        if (toxicBlade) {
            nearPlayer.getCooldownManager().addCooldown("Toxic", this.getClass(), new WoundingStrikeBerserker(), "TOXIC", 60, wp, CooldownTypes.DEBUFF);
        }
    }

    @Override
    public void createSkillTreeAbility(WarlordsPlayer warlordsPlayer, SkillTree skillTree) {
        woundingStrikeDefenderTree = new WoundingStrikeDefenderTree(skillTree, this, name, new ItemStack(warlordsPlayer.getWeapon().item));
        setSkillTree(woundingStrikeDefenderTree);
    }

    public void setBleed(boolean bleed) {
        this.bleed = bleed;
    }

    public void setToxicBlade(boolean toxicBlade) {
        this.toxicBlade = toxicBlade;
    }
}

class WoundingStrikeDefenderTree extends SingleUltTree {

    public WoundingStrikeDefenderTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);

        leftUpgrades.add(new Upgrade(this, 3, 4, 0, 1, "Damage Increase", "Increases damage"));
        leftUpgrades.add(new Upgrade(this, 3, 3, 0, 1, "Deep Wounding", "Wounded targets receive even less healing"));
        leftUpgrades.add(new Upgrade(this, 3, 2, 0, 1, "Combo Attack", "Striking a Wounded target does bonus damage"));

        rightUpgrades.add(new Upgrade(this, 5, 4, 0, 1, "Crit Chance Increase", "Increases crit chance"));
        rightUpgrades.add(new Upgrade(this, 5, 3, 0, 1, "Bleeding Attack", "Wounded targets lose a certain \namount of health per second"));
        rightUpgrades.add(new Upgrade(this, 5, 2, 0, 1, "Heavy Bleeding", "Bleeding now lasts longer and prevents mounts"));

        lastUpgrade = new Upgrade(this, 4, 1, 0, 1, "Toxic Blade", "The next single healing received \nwill be reduced by 100%");
    }

    @Override
    public void doFirstLeftUpgrade() {
        ability.addDamageHeal(-25);
    }

    @Override
    public void doSecondLeftUpgrade() {
        //15% more wound
    }

    @Override
    public void doThirdLeftUpgrade() {
        //10% more dmg
    }

    @Override
    public void doFirstRightUpgrade() {
        ability.addCritChance(5);
    }

    @Override
    public void doSecondRightUpgrade() {
        //BLEED = same as burn
        ((WoundingStrikeDefender) ability).setBleed(true);
    }

    @Override
    public void doThirdRightUpgrade() {

    }

    @Override
    public void doLastUpgrade() {
        ((WoundingStrikeDefender) ability).setToxicBlade(true);
    }
}