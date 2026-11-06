package com.midtermwork;

import org.junit.Test;
import static org.junit.Assert.*;

public final class GameWorldChecks {
    private void tick(GameWorld w,int frames,float move,boolean attack) { for(int i=0;i<frames;i++)w.step(1f/60f,move,false,attack); }
    private GameWorld quiet() { GameWorld w=new GameWorld();w.enemies.clear();if(w.boss!=null)w.boss.hp=0;return w; }
    @Test public void movementJumpAndPause() {
        GameWorld w=quiet();tick(w,30,1,false);assertEquals(218,w.x,.1f);
        w.step(1f/60f,0,true,false);assertTrue(w.y>112);float vy=w.vy;
        w.step(1f/60f,0,true,false);assertTrue(w.vy<vy);
        tick(w,100,0,false);assertTrue(w.grounded);assertEquals(112,w.y,.01f);
        w.paused=true;float time=w.elapsed;tick(w,90,1,true);assertEquals(time,w.elapsed,0);
    }
    @Test public void accelerationBrakingAndReversalAreGradual() {
        GameWorld w=quiet();w.step(1f/60f,1,false,false);assertTrue(w.vx>0&&w.vx<260);
        tick(w,12,1,false);assertEquals(260,w.vx,.01f);float before=w.x;
        w.step(1f/60f,0,false,false);assertTrue(w.vx>0&&w.vx<260);
        tick(w,10,0,false);assertEquals(0,w.vx,.01f);assertTrue(w.x-before<13);
        tick(w,15,1,false);w.step(1f/60f,-1,false,false);assertTrue(w.vx>0);
        tick(w,14,-1,false);assertEquals(-260,w.vx,.01f);
    }
    @Test public void hitStopFreezesBodiesAndPauseFreezesEffects() {
        GameWorld w=quiet();w.damage(1,500);float x=w.x;
        w.step(1f/60f,1,false,false);assertEquals(x,w.x,0);assertTrue(w.hurtTime>0);assertFalse(w.effects.isEmpty());
        w.paused=true;float age=w.effects.get(0).age;tick(w,30,1,true);assertEquals(age,w.effects.get(0).age,0);
    }
    @Test public void heartsDamageAndGracePeriod() {
        GameWorld w=quiet();assertEquals(6,w.hp);assertEquals(6,w.maxHp);
        w.damage(GameWorld.CONTACT_DAMAGE,300);assertEquals(5,w.hp);
        w.damage(GameWorld.SPIT_DAMAGE,300);assertEquals(5,w.hp);
        w.invulnerable=0;w.damage(GameWorld.TACKLE_DAMAGE,300);assertEquals(3,w.hp);
        w.invulnerable=0;w.damage(GameWorld.SPIT_DAMAGE,300);assertEquals(0,w.hp);
        float x=w.x;tick(w,60,1,true);assertEquals(x,w.x,0);
        w.restart();assertEquals(6,w.hp);assertEquals(1,w.level);
    }
    @Test public void evolutionAloneAddsHeartContainers() {
        GameWorld w=quiet();w.addExperience(150);assertEquals(3,w.level);assertEquals(50,w.xp);assertEquals(6,w.maxHp);
        while(w.level<16)w.addExperience(w.xpRequired()-w.xp);
        assertEquals(1,w.evolution);assertEquals(8,w.maxHp);assertEquals(8,w.hp);
        while(w.level<36)w.addExperience(w.xpRequired()-w.xp);
        assertEquals(2,w.evolution);assertEquals(10,w.maxHp);assertEquals("리자몽",w.species());
    }
    @Test public void attackTelegraphLocksDirectionAndLeavesOpening() {
        GameWorld w=new GameWorld();GameWorld.Enemy e=w.enemies.get(0);w.x=300;
        tick(w,1,0,false);assertEquals(GameWorld.Phase.PATROL,e.phase);
        tick(w,50,0,false);assertEquals(GameWorld.Phase.WINDUP,e.phase);int dir=e.dir;
        w.x=e.x+120;tick(w,10,0,false);assertEquals(dir,e.dir);
        boolean attack=false,recover=false;
        for(int i=0;i<130;i++){w.y=400;w.vy=0;w.step(1f/60f,0,false,false);attack|=e.phase==GameWorld.Phase.ATTACK;recover|=e.phase==GameWorld.Phase.RECOVER;}
        assertTrue(attack);assertTrue(recover);
    }
    @Test public void counterattackHasBonusAndAwardsExperienceOnce() {
        GameWorld w=new GameWorld();GameWorld.Enemy e=w.enemies.get(0);w.enemies.clear();w.enemies.add(e);
        w.x=390;e.x=450;e.phase=GameWorld.Phase.RECOVER;e.timer=10;
        w.step(1f/60f,0,false,true);tick(w,16,0,false);
        assertEquals(26,e.hp);tick(w,10,0,false);assertEquals(26,e.hp);
        tick(w,35,0,true);assertFalse(e.alive());assertEquals(25,w.xp);assertEquals(1,w.kills);
        tick(w,90,0,true);assertEquals(25,w.xp);assertFalse(w.cleared);
    }
    @Test public void projectileCanBeDodgedAndDealsOneAndHalfHearts() {
        GameWorld w=quiet();w.shots.add(new GameWorld.Shot(w.x+70,w.y+35,-330));
        tick(w,15,0,false);assertEquals(3,w.hp);assertTrue(w.shots.isEmpty());
        w=quiet();w.shots.add(new GameWorld.Shot(w.x+100,w.y+35,-330));
        w.step(1f/60f,0,true,false);tick(w,25,0,false);assertEquals(6,w.hp);
    }
    @Test public void trapsAndPitReturnToCheckpointWithoutFreeHealing() {
        GameWorld w=quiet();w.x=1405;w.step(1f/60f,0,false,false);assertEquals(4,w.hp);
        w=quiet();w.x=700;w.y=-165;w.step(1f/60f,0,false,false);assertEquals(4,w.hp);assertEquals(100,w.x,.1f);
        w.invulnerable=0;w.hitStop=0;w.x=1190;w.y=112;w.step(1f/60f,0,false,false);assertEquals(1180,w.checkpoint,.1f);
        w.x=1900;w.y=-165;w.step(1f/60f,0,false,false);assertEquals(2,w.hp);assertEquals(1180,w.x,.1f);
    }
    private void walk(GameWorld w,float target) {
        for(int i=0;i<1000&&Math.abs(w.x-target)>4;i++)w.step(1f/60f,w.x<target?1:-1,false,false);
        assertTrue("Walk target "+target,Math.abs(w.x-target)<=4);
    }
    private void jumpTo(GameWorld w,float target,float top) {
        w.step(1f/60f,w.x<target?1:-1,true,false);
        for(int i=0;i<100;i++) {
            float move=Math.abs(w.x-target)>4?(w.x<target?1:-1):0;
            w.step(1f/60f,move,false,false);
            if(w.grounded)break;
        }
        assertTrue("Landing at "+target+" top "+top+" actual "+w.x+","+w.y,w.grounded&&Math.abs(w.y-top)<1&&Math.abs(w.x-target)<8);
    }
    @Test public void steppingStonesAreRequiredAndTraversable() {
        GameWorld w=quiet();walk(w,600);jumpTo(w,670,200);walk(w,740);jumpTo(w,840,280);walk(w,895);jumpTo(w,1010,205);walk(w,1090);jumpTo(w,1185,112);
        // The remaining ravines use the same reachable step heights and spacing.
        walk(w,1350);jumpTo(w,1430,205);walk(w,1570);jumpTo(w,1630,112);
        walk(w,1690);jumpTo(w,1730,200);walk(w,1800);jumpTo(w,1910,280);walk(w,1960);jumpTo(w,2070,205);walk(w,2140);jumpTo(w,2240,112);
        walk(w,2400);jumpTo(w,2480,210);walk(w,2630);jumpTo(w,2690,112);
        walk(w,2800);jumpTo(w,2840,200);walk(w,2910);jumpTo(w,3020,280);walk(w,3070);jumpTo(w,3190,205);walk(w,3280);jumpTo(w,3410,112);
        walk(w,3690);jumpTo(w,3770,210);walk(w,3930);jumpTo(w,4010,112);walk(w,4370);tick(w,10,1,false);
        assertTrue(w.stageComplete);assertFalse(w.cleared);assertEquals(6,w.hp);
    }
}
