package com.midtermwork;
import org.junit.Test;
import static org.junit.Assert.*;
public final class BossChecks {
 @Test public void bossCannotBeSnipedBeforeEnteringArena(){
  GameWorld w=stage(1);w.skillShots.add(new GameWorld.SkillShot(w.boss.x-20,w.boss.y+40,1,2,9999));
  w.step(1f/60f,0,false,false);assertEquals(w.boss.maxHp,w.boss.hp);assertFalse(w.bossActive);
 }
 private void tick(GameWorld w,int n){for(int i=0;i<n;i++)w.step(1f/60f,0,false,false);}
 private GameWorld area(int target,int room){GameWorld w=new GameWorld();while(w.stage<target||w.mapIndex<room){if(w.boss!=null)w.boss.hp=0;w.x=w.exitX()+2;w.step(1f/60f,0,false,false);assertTrue(w.advanceStage());}return w;}
 private GameWorld stage(int target){return area(target,5);}
 @Test public void bossLocksExitAndCanBeDefeatedWithNormalAttack(){
  GameWorld w=stage(1);w.x=w.exitX()+2;tick(w,1);assertTrue(w.bossActive);assertFalse(w.stageComplete);assertTrue(w.x<w.exitX());
  w.x=w.boss.x-90;w.boss.hp=1;w.boss.phase=GameWorld.Phase.RECOVER;w.boss.timer=10;
  w.step(1f/60f,0,false,true);tick(w,35);assertTrue(w.bossDefeated());assertEquals(1,w.totalKills);int lv=w.level,xp=w.xp;
  tick(w,100);assertEquals(lv,w.level);assertEquals(xp,w.xp);while(w.choosingSkill())w.skipLearning();w.x=w.exitX()+2;tick(w,5);assertTrue(w.stageComplete);
 }
 @Test public void eachBossHasReadableAttacksAndRecovery(){
  for(int level=1;level<=3;level++){
   GameWorld w=stage(level);w.enemies.removeIf(e->!e.boss);w.x=300;w.bossActive=true;w.boss.timer=0;
   int attacks=0;boolean recover=false,eruption=false,volley=false;GameWorld.Phase old=w.boss.phase;
   for(int i=0;i<500;i++){w.y=500;w.vy=0;w.step(1f/60f,0,false,false);if(old!=w.boss.phase&&w.boss.phase==GameWorld.Phase.ATTACK)attacks++;old=w.boss.phase;
    recover|=w.boss.phase==GameWorld.Phase.RECOVER;volley|=w.shots.size()>=3;eruption|=w.bossHazards.stream().anyMatch(h->h.type==GameWorld.BossHazardType.FIRE_PILLAR);}
   assertTrue(recover);assertTrue(attacks>=2);if(level>=2)assertTrue(volley);if(level==3)assertTrue(eruption);
  }
 }
 @Test public void retryRestoresStageEntryWithoutExperienceFarming(){
  GameWorld w=stage(2);int before=w.level,xp=w.xp;w.addExperience(150);w.hp=0;assertTrue(w.retryStage());assertEquals(2,w.stage);assertEquals(before,w.level);assertEquals(xp,w.xp);assertEquals(w.maxHp,w.hp);assertFalse(w.bossDefeated());assertTrue(w.eruptions.isEmpty());
 }
 @Test public void threeBossesLeadToEndingAndRestart(){
  GameWorld w=stage(3);assertEquals("불꽃 화산",w.stageName());w.boss.hp=0;w.x=w.exitX()+2;tick(w,1);assertTrue(w.cleared);assertFalse(w.stageComplete);assertFalse(w.advanceStage());w.restart();assertEquals(1,w.stage);assertEquals(1,w.level);assertFalse(w.cleared);assertFalse(w.bossActive);
 }
 private float move(GameWorld w,float target){float dx=target-w.x,stop=w.vx*w.vx/(2*(w.grounded?3200:1300));return Math.abs(dx)<=Math.max(3,stop)&&dx*w.vx>=0?0:dx>0?1:-1;}
 private void walk(GameWorld w,float target){for(int i=0;i<600;i++){if(Math.abs(w.x-target)<5&&Math.abs(w.vx)<1)break;w.step(1f/60f,move(w,target),false,false);}assertTrue("walk "+target,Math.abs(w.x-target)<8);}
 private void jump(GameWorld w,float target,float top){w.step(1f/60f,move(w,target),true,false);for(int i=0;i<120;i++){w.step(1f/60f,move(w,target),false,false);if(w.grounded)break;}assertTrue("jump "+target+" top "+top+" actual "+w.x+","+w.y,w.grounded&&Math.abs(w.y-top)<1&&Math.abs(w.x-target)<12);}
 @Test public void volcanoJumpRouteIsReachable(){
  GameWorld w=area(3,1);w.enemies.clear();w.rockSpawnTimer=9999;
  walk(w,600);jump(w,670,205);walk(w,740);jump(w,840,290);walk(w,895);jump(w,1010,205);walk(w,1090);jump(w,1185,112);
  walk(w,1350);jump(w,1430,215);walk(w,1570);jump(w,1630,112);walk(w,1690);jump(w,1730,210);walk(w,1800);jump(w,1910,305);walk(w,1970);jump(w,2080,210);walk(w,2140);jump(w,2240,112);
  walk(w,2390);jump(w,2480,225);walk(w,2630);jump(w,2690,112);walk(w,2800);jump(w,2840,205);walk(w,2910);jump(w,3020,300);walk(w,3080);jump(w,3190,205);walk(w,3280);jump(w,3410,112);walk(w,3680);jump(w,3780,215);walk(w,3930);jump(w,4100,112);walk(w,4370);for(int i=0;i<15;i++)w.step(1f/60f,1,false,false);
  assertTrue(w.stageComplete);assertEquals(6,w.hp);
 }
}
