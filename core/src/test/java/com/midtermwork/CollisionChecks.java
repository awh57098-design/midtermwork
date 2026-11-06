package com.midtermwork;
import org.junit.Test;
import static org.junit.Assert.*;
public class CollisionChecks {
 private GameWorld world(){GameWorld w=new GameWorld();w.enemies.clear();w.spikes.clear();w.blocks.clear();return w;}
 @Test public void walkingOffRightEdgeNeverEjectsToFarLeft(){GameWorld w=world();w.blocks.add(new GameWorld.Block(0,0,650,112));w.x=636;w.y=112;w.grounded=true;for(int n=0;n<22;n++){float old=w.x;w.step(1f/60,1,false,false);assertTrue("backward ejection "+old+" to "+w.x,w.x>=old-.01f);assertTrue(w.x-old<5);}assertTrue(w.x>650);assertTrue(w.y<112);}
 @Test public void walkingOffLeftEdgeNeverEjectsToFarRight(){GameWorld w=world();w.blocks.add(new GameWorld.Block(1000,0,1000,112));w.x=971;w.y=112;w.grounded=true;for(int n=0;n<22;n++){float old=w.x;w.step(1f/60,-1,false,false);assertTrue("forward ejection "+old+" to "+w.x,w.x<=old+.01f);assertTrue(old-w.x<5);}assertTrue(w.x<970);assertTrue(w.y<112);}
 @Test public void raisedSolidStepStopsWalkingAtItsNearFace(){GameWorld w=world();w.blocks.add(new GameWorld.Block(0,0,1000,112));w.blocks.add(new GameWorld.Block(1000,0,480,200));w.x=930;w.y=112;w.grounded=true;for(int n=0;n<60;n++){float old=w.x;w.step(1f/60,1,false,false);assertTrue(Math.abs(w.x-old)<5);}assertEquals(956,w.x,.01);assertEquals(112,w.y,.01);w.step(1f/60,1,true,false);for(int n=0;n<45;n++)w.step(1f/60,1,false,false);assertTrue(w.x>1000);assertTrue(w.y>=200);}
 @Test public void movingBackIntoLedgeWhileAlreadyOverlappingDoesNotTeleport(){GameWorld w=world();w.blocks.add(new GameWorld.Block(0,0,650,112));w.x=636;w.y=111;w.grounded=false;w.step(1f/60,-1,false,false);assertTrue(w.x>630);assertTrue(w.y<111);}
 @Test public void fastFallLandsOnThinPlatformUsingCrossedSurface(){GameWorld w=world();w.blocks.add(new GameWorld.Block(80,200,160,3,true));w.x=100;w.y=210;w.vy=-900;w.grounded=false;w.step(1f/30,0,false,false);assertEquals(203,w.y,.01);assertTrue(w.grounded);}
}