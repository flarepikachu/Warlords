package com.ebicep.warlords.skilltree;

import com.ebicep.warlords.classes.AbstractPlayerClass;
import com.ebicep.warlords.menu.Menu;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.skilltree.trees.FlagTree;
import com.ebicep.warlords.skilltree.trees.MiscellaneousTree;
import com.ebicep.warlords.skilltree.trees.MountTree;
import org.bukkit.entity.Player;

public class SkillTree {
    //8 trees
    //each tree with upgrades
    private WarlordsPlayer warlordsPlayer;
    private AbstractTree[] skillTrees = new AbstractTree[8];

    public SkillTree(WarlordsPlayer warlordsPlayer) {
        this.warlordsPlayer = warlordsPlayer;
        skillTrees[0] = new MiscellaneousTree(this);
        skillTrees[1] = new FlagTree(this);
        skillTrees[2] = new MountTree(this);
        AbstractPlayerClass wpClass = warlordsPlayer.getSpec();
        wpClass.getWeapon().createSkillTreeAbility(warlordsPlayer, this);
        wpClass.getRed().createSkillTreeAbility(warlordsPlayer, this);
        wpClass.getPurple().createSkillTreeAbility(warlordsPlayer, this);
        wpClass.getBlue().createSkillTreeAbility(warlordsPlayer, this);
        wpClass.getOrange().createSkillTreeAbility(warlordsPlayer, this);
        skillTrees[3] = wpClass.getWeapon().getSkillTree();

    }

    public void openSkillTreeMenu() {
        Menu menu = new Menu("Skill Tree", 9 * 6);
        for (int i = 0; i < skillTrees.length / 2; i++) {
            AbstractTree tree = skillTrees[i];
            if (tree != null) {
                menu.setItem((i * 2) + 1,
                        1,
                        tree.getTreeItemStack(),
                        (n, e) -> {
                            tree.openTreeMenu((Player) warlordsPlayer.getEntity());
                        }
                );
            }
        }
//        for (int i = skillTrees.length / 2; i < skillTrees.length; i++) {
//            AbstractTree tree = skillTrees[i];
//            menu.setItem(((i - skillTrees.length / 2) * 2) + 1,
//                    3,
//                    tree.itemStack,
//                    (n, e) -> {
//                    }
//            );
//        }
        menu.openForPlayer((Player) warlordsPlayer.getEntity());
    }

    public WarlordsPlayer getWarlordsPlayer() {
        return warlordsPlayer;
    }

    public AbstractTree[] getSkillTrees() {
        return skillTrees;
    }
}