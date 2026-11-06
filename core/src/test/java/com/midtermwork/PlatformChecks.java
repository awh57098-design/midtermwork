package com.midtermwork;
import org.junit.Test;
import static org.junit.Assert.*;
public class PlatformChecks {
 private GameWorld world(){GameWorld w=new GameWorld();w.enemies.clear();w.spikes.clear();w.blocks.clear();w.blocks.add(new GameWorld.Block(0,0,900,112));return w;}
 private void tick(GameWorld w,int frames){for(int n=0;n<frames;n++)w.step(1f/60,0,false,false);}
 @Test public void narrowVisibleGapNoLongerSupportsEntireBody(){GameWorld w=world();w.blocks.add(new GameWorld.Block(1000,186,160,24,true));w.blocks.add(new GameWorld.Block(1190,186,160,24,true));w.x=1153;w.y=210;w.grounded=true;tick(w,12);assertFalse(w.grounded);assertTrue(w.y<190);}
 @Test public void downJumpPassesOnlyCurrentPlatformAndLandsBelow(){GameWorld w=world();w.blocks.add(new GameWorld.Block(80,276,160,24,true));w.blocks.add(new GameWorld.Block(80,186,160,24,true));w.x=100;w.y=300;w.grounded=true;assertTrue(w.dropThrough());tick(w,45);assertTrue(w.grounded);assertEquals(210,w.y,.1);assertEquals(6,w.hp);}
 @Test public void dropOntoSolidGroundNeverFallsThroughIt(){GameWorld w=world();w.blocks.add(new GameWorld.Block(80,186,160,24,true));w.x=100;w.y=210;w.grounded=true;assertTrue(w.dropThrough());tick(w,60);assertEquals(112,w.y,.1);assertTrue(w.grounded);assertFalse(w.dropThrough());}
 @Test public void pauseDeathAndAirCannotStartDrop(){GameWorld w=world();w.blocks.add(new GameWorld.Block(80,186,160,24,true));w.x=100;w.y=210;w.grounded=true;w.paused=true;assertFalse(w.dropThrough());w.paused=false;w.hp=0;assertFalse(w.dropThrough());w.hp=6;w.grounded=false;assertFalse(w.dropThrough());}
 @Test public void normalJumpStillPassesUpThroughOneWayPlatform(){GameWorld w=world();w.blocks.add(new GameWorld.Block(80,186,160,24,true));w.x=100;w.y=112;w.grounded=true;w.step(1f/60,0,true,false);tick(w,75);assertEquals(210,w.y,.1);assertTrue(w.grounded);}
}