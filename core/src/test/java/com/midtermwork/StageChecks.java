package com.midtermwork;

import org.junit.Test;
import static org.junit.Assert.*;

public final class StageChecks {
    private void tick(GameWorld w,int frames){for(int i=0;i<frames;i++)w.step(1f/60f,0,false,false);}
    private GameWorld cave(){GameWorld w=new GameWorld();while(w.stage<2){if(w.boss!=null)w.boss.hp=0;w.x=w.exitX()+2;w.step(1f/60f,0,false,false);assertTrue(w.advanceStage());}return w;}
    @Test public void forestTransitionsOnceAndPreservesProgress(){
        GameWorld w=new GameWorld();w.addExperience(180);GameWorld.Enemy first=w.enemies.get(0);first.hp=1;first.burnTime=1;first.burnTick=0;tick(w,1);
        int level=w.level,xp=w.xp,total=w.totalKills;w.hp=3;w.x=4382;w.step(1f/60f,0,false,false);
        assertTrue(w.stageComplete);assertFalse(w.cleared);assertFalse(w.trySkill(0));
        w.paused=true;float timer=w.transitionTime;tick(w,180);assertEquals(timer,w.transitionTime,0);assertEquals(1,w.stage);
        w.paused=false;tick(w,115);assertEquals(1,w.stage);assertEquals(2,w.mapIndex);assertFalse(w.stageComplete);assertFalse(w.cleared);
        assertEquals(level,w.level);assertEquals(xp,w.xp);assertEquals(total,w.totalKills);assertTrue(w.skillUnlocked(3));assertEquals(w.maxHp,w.hp);assertEquals(100,w.x,.01f);assertEquals(0,w.kills);
        assertFalse(w.advanceStage());
    }
    @Test public void caveHasDifferentLayoutAndStrongerEnemies(){
        GameWorld w=cave();assertEquals("수정 동굴",w.stageName());assertEquals(11,w.enemies.size());
        assertTrue(w.blocks.stream().anyMatch(b->b.y+b.h==385));assertEquals(90,w.enemies.get(0).maxHp);assertEquals(40,w.enemies.get(0).reward);
        for(float marker:w.checkpointPositions())assertTrue(w.blocks.stream().anyMatch(b->!b.oneWay&&marker>=b.x&&marker+44<=b.x+b.w));
    }
    @Test public void fallingRockWarnsThenDamagesAndPauses(){
        GameWorld w=cave();w.enemies.clear();w.invulnerable=0;w.x=2250;w.rockSpawnTimer=0;tick(w,20);
        assertEquals(1,w.rockFalls.size());assertTrue(w.rockFalls.get(0).warning>0);assertEquals(6,w.hp);
        w.paused=true;float y=w.rockFalls.get(0).y,warning=w.rockFalls.get(0).warning;tick(w,120);assertEquals(y,w.rockFalls.get(0).y,0);assertEquals(warning,w.rockFalls.get(0).warning,0);
        w.paused=false;tick(w,120);assertEquals(4,w.hp);
    }
    @Test public void restartFromCaveStartsFreshForest(){
        GameWorld w=cave();w.addExperience(150);w.rockFalls.add(new GameWorld.RockFall(100));w.restart();
        assertEquals(1,w.stage);assertEquals(1,w.level);assertEquals(0,w.xp);assertEquals(0,w.totalKills);assertEquals(6,w.hp);assertTrue(w.rockFalls.isEmpty());assertEquals(10,w.enemies.size());assertEquals(65,w.enemies.get(0).maxHp);
    }
    private float steering(GameWorld w,float target){
        float dx=target-w.x;float stopping=w.vx*w.vx/(2*(w.grounded?3200:1300));
        if(Math.abs(dx)<=Math.max(3,stopping)&&dx*w.vx>=0)return 0;return dx>0?1:-1;
    }
    private void walk(GameWorld w,float target){
        for(int i=0;i<600;i++){if(Math.abs(w.x-target)<5&&Math.abs(w.vx)<1)break;w.step(1f/60f,steering(w,target),false,false);}
        assertTrue("walk "+target+" actual "+w.x,Math.abs(w.x-target)<8);
    }
    private void jump(GameWorld w,float target,float top){
        w.step(1f/60f,steering(w,target),true,false);
        for(int i=0;i<120;i++){w.step(1f/60f,steering(w,target),false,false);if(w.grounded)break;}
        assertTrue("jump "+target+" top "+top+" actual "+w.x+","+w.y,w.grounded&&Math.abs(w.y-top)<1&&Math.abs(w.x-target)<12);
    }
    @Test public void entireCaveRouteCanBeTraversedWithoutTrapDamage(){
        GameWorld w=cave();w.enemies.clear();w.rockSpawnTimer=9999;
        walk(w,550);jump(w,610,200);walk(w,685);jump(w,780,280);walk(w,845);jump(w,950,210);walk(w,1030);jump(w,1085,112);
        walk(w,1120);jump(w,1200,210);walk(w,1270);jump(w,1370,300);walk(w,1440);jump(w,1540,385);walk(w,1630);jump(w,1750,300);walk(w,1820);jump(w,1940,205);walk(w,2060);jump(w,2200,112);
        walk(w,2390);jump(w,2490,215);walk(w,2630);jump(w,2720,112);walk(w,2820);jump(w,2900,200);walk(w,2960);jump(w,3070,290);walk(w,3130);jump(w,3240,200);walk(w,3300);jump(w,3400,112);
        walk(w,3800);jump(w,3910,215);walk(w,4050);jump(w,4160,112);walk(w,4370);for(int i=0;i<15;i++)w.step(1f/60f,1,false,false);
        assertTrue(w.stageComplete);assertFalse(w.cleared);assertEquals(6,w.hp);assertTrue(w.advanceStage());assertEquals(2,w.stage);assertEquals(2,w.mapIndex);
    }
}
