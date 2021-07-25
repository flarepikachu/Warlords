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
//        skillTrees[4] = wpClass.getRed() instanceof ISingleUlt ?
//                new SingleUltTree(this,wpClass.getRed(), wpClass.getRed().getName(), new ItemStack(Material.INK_SACK, 1, (byte)1)) :
//                new DoubleUltTree(this,wpClass.getRed(), wpClass.getRed().getName(), new ItemStack(Material.INK_SACK, 1, (byte)1));
//        skillTrees[5] = wpClass.getPurple() instanceof ISingleUlt ?
//                new SingleUltTree(this,wpClass.getPurple(), wpClass.getPurple().getName(), new ItemStack(Material.GLOWSTONE_DUST)) :
//                new DoubleUltTree(this,wpClass.getPurple(), wpClass.getPurple().getName(), new ItemStack(Material.GLOWSTONE_DUST));
//        skillTrees[6] = wpClass.getBlue() instanceof ISingleUlt ?
//                new SingleUltTree(this,wpClass.getBlue(), wpClass.getBlue().getName(), new ItemStack(Material.INK_SACK, 1, (byte)10)) :
//                new DoubleUltTree(this,wpClass.getBlue(), wpClass.getBlue().getName(), new ItemStack(Material.INK_SACK, 1, (byte)10));
//        skillTrees[7] = wpClass.getOrange() instanceof ISingleUlt ?
//                new SingleUltTree(this,wpClass.getOrange(), wpClass.getOrange().getName(), new ItemStack(Material.INK_SACK, 1, (byte)14)) :
//                new DoubleUltTree(this,wpClass.getOrange(), wpClass.getOrange().getName(), new ItemStack(Material.INK_SACK, 1, (byte)14));
    }

    public void openSkillTreeMenu() {
        Menu menu = new Menu("Skill Tree", 9 * 6);
        for (int i = 0; i < skillTrees.length / 2; i++) {
            AbstractTree tree = skillTrees[i];
            menu.setItem((i * 2) + 1,
                    1,
                    tree.itemStack,
                    (n, e) -> {
                        tree.openTreeMenu((Player) warlordsPlayer.getEntity());
                    }
            );
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
