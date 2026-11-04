package com.midtermwork;

import org.junit.Test;
import static org.junit.Assert.*;

public final class SkillChecks {
    @Test public void skillCanTakePriorityOverHeldBasicAttack(){
        GameWorld w=training(1);w.step(1f/60f,0,false,true);assertTrue(w.attackTime>0);
        assertTrue(w.trySkill(0));assertEquals(0,w.attackTime,0);
        w.step(1f/60f,0,false,true);assertEquals(0,w.attackTime,0);assertTrue(w.skillTime>0);
    }
    private void tick(GameWorld w,int frames){for(int i=0;i<frames;i++)w.step(1f/60f,0,false,false);}
    private GameWorld training(int level){GameWorld w=new GameWorld();w.enemies.clear();while(w.level<level)w.addExperience(w.xpRequired()-w.xp);return w;}
    private GameWorld.Enemy target(GameWorld w,float x){GameWorld.Enemy e=new GameWorld.Enemy(x,112,true);e.hp=200;e.timer=100;w.enemies.add(e);return e;}
    @Test public void unlocksInOrderAndCooldownPreventsSpam(){
        GameWorld w=training(1);assertTrue(w.skillUnlocked(0));assertFalse(w.skillUnlocked(1));assertFalse(w.trySkill(1));assertEquals(0,w.skillCooldowns[1],0);
        assertTrue(w.trySkill(0));assertFalse(w.trySkill(0));tick(w,35);assertFalse(w.trySkill(0));tick(w,70);assertTrue(w.trySkill(0));
        for(int level=2;level<=4;level++){w=training(level);for(int i=0;i<4;i++)assertEquals(i<level,w.skillUnlocked(i));}
    }
    @Test public void emberHitsOnceThenBurns(){
        GameWorld w=training(1);GameWorld.Enemy e=target(w,250);assertTrue(w.trySkill(0));tick(w,60);
        assertEquals(168,e.hp);assertTrue(e.burnTime>0);assertTrue(w.skillShots.isEmpty());
        tick(w,100);assertTrue(e.hp<168);assertEquals(0,w.kills);
    }
    @Test public void dragonBreathPiercesAndSlowsEachTargetOnlyOnce(){
        GameWorld w=training(3);GameWorld.Enemy first=target(w,250),second=target(w,400);
        assertTrue(w.trySkill(2));tick(w,70);
        assertEquals(169,first.hp);assertEquals(169,second.hp);assertTrue(first.slowTime>0);assertTrue(second.slowTime>0);
        tick(w,15);assertEquals(169,first.hp);assertEquals(169,second.hp);
    }
    @Test public void smokeInterruptsWindupAndBlocksPoison(){
        GameWorld w=training(2);GameWorld.Enemy e=target(w,250);e.phase=GameWorld.Phase.WINDUP;e.timer=.7f;
        assertTrue(w.trySkill(1));tick(w,25);assertEquals(1,w.smokeClouds.size());assertTrue(e.smoked>0);assertEquals(GameWorld.Phase.RECOVER,e.phase);
        w.shots.add(new GameWorld.Shot(210,150,-330));tick(w,1);assertTrue(w.shots.isEmpty());assertEquals(6,w.hp);
    }
    @Test public void fireFangHitsNearbyTargetsOnceWithCounterBonus(){
        GameWorld w=training(4);GameWorld.Enemy e=target(w,220);e.phase=GameWorld.Phase.RECOVER;e.timer=10;
        GameWorld.Enemy far=target(w,400);assertTrue(w.trySkill(3));tick(w,30);
        assertEquals(91,e.hp);assertTrue(e.burnTime>0);assertEquals(200,far.hp);tick(w,10);assertEquals(91,e.hp);
    }
    @Test public void pauseDeathAndHitCancelOrBlockCasting(){
        GameWorld w=training(4);w.paused=true;assertFalse(w.trySkill(0));w.paused=false;
        assertTrue(w.trySkill(0));w.paused=true;float cooldown=w.skillCooldowns[0];tick(w,90);assertEquals(cooldown,w.skillCooldowns[0],0);w.paused=false;
        w.damage(1,300);tick(w,60);assertTrue(w.skillShots.isEmpty());assertEquals(-1,w.activeSkill);
        w.hp=0;assertFalse(w.trySkill(1));w.restart();assertEquals(0,w.skillCooldowns[0],0);assertTrue(w.smokeClouds.isEmpty());
    }
    @Test public void burnKillGivesExperienceOnlyOnce(){
        GameWorld w=training(1);GameWorld.Enemy e=target(w,250);e.hp=1;e.burnTime=2;e.burnTick=.01f;
        tick(w,120);assertFalse(e.alive());assertEquals(1,w.kills);assertEquals(25,w.xp);
    }
}
