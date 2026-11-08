package com.midtermwork;
/** Combat fixtures deliberately separate equipped moves from progression tests. */
final class TestWorlds {
 static void equip(GameWorld w,GameWorld.Skill... moves){
  while(w.choosingSkill())w.skipLearning();
  try{java.lang.reflect.Field f=GameWorld.class.getDeclaredField("equipped");f.setAccessible(true);GameWorld.Skill[] slots=(GameWorld.Skill[])f.get(w);java.util.Arrays.fill(slots,null);System.arraycopy(moves,0,slots,0,Math.min(4,moves.length));java.lang.reflect.Method reset=GameWorld.class.getDeclaredMethod("resetDashCharges");reset.setAccessible(true);reset.invoke(w);}catch(Exception e){throw new AssertionError(e);}
 }
 static void claimRewards(GameWorld w){while(w.choosingReward()){if(w.choosingBuildReward())w.chooseBuildReward(GameWorld.BuildPath.FIRE);else w.chooseItemReward(0);}}
 static void legacy(GameWorld w){equip(w,GameWorld.Skill.EMBER,GameWorld.Skill.SMOKE,GameWorld.Skill.DRAGON,GameWorld.Skill.FANG);}
}
