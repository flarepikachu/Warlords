package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.skilltree.AbstractTree;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public abstract class SingleUltTree extends AbstractTree {

    protected LinkedList<Upgrade> leftUpgrades = new LinkedList<>();
    protected LinkedList<Upgrade> rightUpgrades = new LinkedList<>();
    protected Upgrade lastUpgrade;


    public SingleUltTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);
    }

    @Override
    public boolean canUpgrade(Upgrade upgrade) {
        if (upgrade.isLocked()) return false;
        if (upgrade.getCounter() >= upgrade.getMaxCounter()) return false;
        if (upgrade == leftUpgrades.getFirst() || upgrade == rightUpgrades.getFirst()) {
            return true;
        } else {
            return ((upgrade == leftUpgrades.get(1) && leftUpgrades.getFirst().getCounter() != 0) || (upgrade == rightUpgrades.get(1) && rightUpgrades.getFirst().getCounter() != 0)) ||
                    ((upgrade == leftUpgrades.getLast() && leftUpgrades.get(1).getCounter() != 0) || (upgrade == rightUpgrades.getLast() && rightUpgrades.get(1).getCounter() != 0)) ||
                    ((upgrade == lastUpgrade && leftUpgrades.getLast().getCounter() != 0) || (upgrade == lastUpgrade && rightUpgrades.getLast().getCounter() != 0));
        }
    }

    @Override
    public void openTreeMenu(Player player) {
        Menu menu = new Menu(name, 9 * 6);
        menu.setItem(lastUpgrade.getX(), lastUpgrade.getY(),
                lastUpgrade.getItem(),
                (n, e) -> {
                    lastUpgrade.onClick();
                }
        );
        for (Upgrade leftUpgrade : leftUpgrades) {
            menu.setItem(leftUpgrade.getX(), leftUpgrade.getY(),
                    leftUpgrade.getItem(),
                    (n, e) -> {
                        leftUpgrade.onClick();
                    }
            );
        }
        for (Upgrade rightUpgrade : rightUpgrades) {
            menu.setItem(rightUpgrade.getX(), rightUpgrade.getY(),
                    rightUpgrade.getItem(),
                    (n, e) -> {
                        rightUpgrade.onClick();
                    }
            );
        }
        menu.openForPlayer(player);
    }

    public abstract void doFirstLeftUpgrade();

    public abstract void doSecondLeftUpgrade();

    public abstract void doThirdLeftUpgrade();

    public abstract void doFirstRightUpgrade();

    public abstract void doSecondRightUpgrade();

    public abstract void doThirdRightUpgrade();

    public abstract void doLastUpgrade();

    public LinkedList<Upgrade> getLeftUpgrades() {
        return leftUpgrades;
    }

    public LinkedList<Upgrade> getRightUpgrades() {
        return rightUpgrades;
    }

    public Upgrade getLastUpgrade() {
        return lastUpgrade;
    }
}
