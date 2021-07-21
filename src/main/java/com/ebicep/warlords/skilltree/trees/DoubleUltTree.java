package com.ebicep.warlords.skilltree.trees;

import com.ebicep.warlords.classes.AbstractAbility;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.skilltree.AbstractTree;
import com.ebicep.warlords.skilltree.SkillTree;
import com.ebicep.warlords.skilltree.Upgrade;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class DoubleUltTree extends AbstractTree {

    protected Upgrade firstUpgrade;
    protected LinkedList<Upgrade> leftUpgrades = new LinkedList<>();
    protected LinkedList<Upgrade> rightUpgrades = new LinkedList<>();

    public DoubleUltTree(SkillTree skillTree, AbstractAbility ability, String name, ItemStack itemStack) {
        super(skillTree, ability, name, itemStack);
    }

    @Override
    public boolean canUpgrade(Upgrade upgrade) {
        if (upgrade.isLocked()) return false;
        if (upgrade.getCounter() >= upgrade.getMaxCounter()) return false;
        if (upgrade == firstUpgrade) {
            return true;
        } else {
            return ((upgrade == leftUpgrades.getFirst() || upgrade == rightUpgrades.getFirst()) && firstUpgrade.getCounter() != 0) ||
                    ((upgrade == leftUpgrades.getLast() && leftUpgrades.get(1).getCounter() != 0) || (upgrade == rightUpgrades.getLast() && rightUpgrades.get(1).getCounter() != 0)) ||
                    ((upgrade == leftUpgrades.get(1) && leftUpgrades.getFirst().getCounter() != 0) || (upgrade == rightUpgrades.get(1) && rightUpgrades.getFirst().getCounter() != 0));
        }
    }

    @Override
    public void openTreeMenu(Player player) {
        Menu menu = new Menu(name, 9 * 6);
        menu.setItem(firstUpgrade.getX(), firstUpgrade.getY(),
                firstUpgrade.getItem(),
                (n, e) -> {
                    firstUpgrade.onClick();
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

    public Upgrade getFirstUpgrade() {
        return firstUpgrade;
    }

    public LinkedList<Upgrade> getLeftUpgrades() {
        return leftUpgrades;
    }

    public LinkedList<Upgrade> getRightUpgrades() {
        return rightUpgrades;
    }
}
