package com.midtermwork;
import org.junit.Test;
import static org.junit.Assert.*;
public final class CampaignChecks {
 private void tick(GameWorld w,int count){for(int i=0;i<count;i++)w.step(1f/60f,0,false,false);}
 private void complete(GameWorld w){if(w.boss!=null)w.boss.hp=0;w.x=w.exitX()+2;tick(w,1);}
 private GameWorld area(int biome,int room){GameWorld w=new GameWorld();while(w.stage<biome||w.mapIndex<room){complete(w);assertTrue(w.advanceStage());}return w;}
 @Test public void campaignVisitsTwelveFullMapsAndThreeSeparateArenas(){
  GameWorld w=new GameWorld();int maps=0,arenas=0;
  for(int index=1;index<=15;index++){
   assertEquals(index,w.areaNumber());assertEquals((index-1)/5+1,w.stage);assertEquals((index-1)%5+1,w.mapIndex);
   if(w.isBossRoom()){arenas++;assertEquals(1280,w.worldWidth(),0);assertEquals(1,w.enemies.size());assertTrue(w.enemies.get(0).boss);}
   else{maps++;assertEquals(4500,w.worldWidth(),0);assertNull(w.boss);assertTrue(w.enemies.stream().noneMatch(e->e.boss));}
   complete(w);if(index<15){assertTrue(w.stageComplete);assertFalse(w.cleared);assertTrue(w.advanceStage());}else{assertTrue(w.cleared);assertFalse(w.advanceStage());}
  }
  assertEquals(12,maps);assertEquals(3,arenas);
 }
 @Test public void layoutsDifferAndCheckpointsAreSafe(){
  for(int biome=1;biome<=3;biome++){
   java.util.Set<String> layouts=new java.util.HashSet<>();
   for(int room=1;room<=4;room++){GameWorld w=area(biome,room);StringBuilder signature=new StringBuilder();
    for(GameWorld.Block b:w.blocks){signature.append(b.x).append(':').append(b.y).append(':').append(b.w).append(':').append(b.h).append(';');assertTrue(b.x>=0&&b.x+b.w<=w.worldWidth());}
    layouts.add(signature.toString());
    for(float marker:w.checkpointPositions()){assertTrue(w.blocks.stream().anyMatch(b->!b.oneWay&&b.y+b.h==112&&marker>=b.x&&marker+44<=b.x+b.w));assertTrue(w.spikes.stream().noneMatch(s->marker+44>s.x&&marker<s.x+s.w));}
   }
   assertEquals(4,layouts.size());
  }
 }
 @Test public void roomProgressAndRetryPreserveTheCorrectGrowthSnapshot(){
  GameWorld w=new GameWorld();w.addExperience(150);while(w.choosingSkill())w.skipLearning();int level=w.level,xp=w.xp;w.hp=3;complete(w);assertTrue(w.advanceStage());
  assertEquals(1,w.stage);assertEquals(2,w.mapIndex);assertEquals(level,w.level);assertEquals(xp,w.xp);assertEquals(w.maxHp,w.hp);
  w.addExperience(10);w.hp=0;assertTrue(w.retryStage());assertEquals(2,w.mapIndex);assertEquals(level,w.level);assertEquals(xp,w.xp);assertEquals(100,w.x,0);
  w.restart();assertEquals(1,w.areaNumber());assertEquals(1,w.level);
 }
 private float move(GameWorld w,float target){float dx=target-w.x,stop=w.vx*w.vx/(2*(w.grounded?3200:1300));return Math.abs(dx)<=Math.max(3,stop)&&dx*w.vx>=0?0:dx>0?1:-1;}
 private void walk(GameWorld w,float target){for(int i=0;i<700;i++){if(Math.abs(w.x-target)<5&&Math.abs(w.vx)<1)break;w.step(1f/60f,move(w,target),false,false);}assertTrue(w.stageLabel()+" walk "+target+" actual "+w.x,Math.abs(w.x-target)<8);}
 private void jump(GameWorld w,float target,float top){w.step(1f/60f,move(w,target),true,false);for(int i=0;i<120;i++){w.step(1f/60f,move(w,target),false,false);if(w.grounded)break;}assertTrue(w.stageLabel()+" jump "+target+" top "+top+" actual "+w.x+","+w.y,w.grounded&&Math.abs(w.y-top)<1&&Math.abs(w.x-target)<12);}
 private void highRoute(GameWorld w){
  walk(w,640);jump(w,690,200);walk(w,775);jump(w,875,290);walk(w,935);jump(w,1030,210);walk(w,1120);jump(w,1240,210);walk(w,1310);jump(w,1420,300);walk(w,1490);jump(w,1600,390);walk(w,1700);jump(w,1810,300);walk(w,1890);jump(w,2010,210);walk(w,2180);jump(w,2300,112);
  walk(w,2390);jump(w,2480,215);walk(w,2630);jump(w,2720,112);walk(w,2940);jump(w,3010,200);walk(w,3080);jump(w,3190,285);walk(w,3240);jump(w,3350,205);walk(w,3440);jump(w,3540,112);walk(w,3720);jump(w,3820,215);walk(w,3970);jump(w,4100,112);
 }
 private void ravines(GameWorld w){
  for(int i=0;i<4;i++){float o=i*1000;walk(w,o+550);jump(w,o+610,200);walk(w,o+675);jump(w,o+780,280);walk(w,o+845);jump(w,o+950,205);walk(w,o+1010);
   if(i<3){jump(w,o+1140,210);walk(w,o+1320);jump(w,o+1430,112);}else jump(w,4140,112);
  }
 }
 private void gauntlet(GameWorld w){
  walk(w,520);jump(w,620,210);walk(w,880);jump(w,1040,200);walk(w,1440);jump(w,1540,112);walk(w,1600);jump(w,1700,210);walk(w,1920);jump(w,2040,112);walk(w,2160);jump(w,2235,200);walk(w,2260);
  walk(w,2300);jump(w,2400,295);walk(w,2580);jump(w,2670,200);jump(w,2790,112);walk(w,3040);jump(w,3140,210);walk(w,3360);jump(w,3435,200);walk(w,3450);walk(w,3860);jump(w,3990,112);
 }
 @Test public void allNineNewFullMapRoutesAreTraversableWithoutTrapDamage(){
  for(int biome=1;biome<=3;biome++)for(int room=2;room<=4;room++){
   GameWorld w=area(biome,room);w.enemies.clear();w.rockSpawnTimer=9999;
   if(room==2)highRoute(w);else if(room==3)ravines(w);else gauntlet(w);
   walk(w,4370);for(int i=0;i<15;i++)w.step(1f/60f,1,false,false);assertTrue(w.stageComplete);assertEquals(w.stageLabel(),6,w.hp);
  }
 }
}
