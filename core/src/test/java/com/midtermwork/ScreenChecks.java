package com.midtermwork;
import org.junit.Test;
import static org.junit.Assert.*;
public class ScreenChecks {
 @Test public void imageAndHoverAgreeInWideTallSmallAndHighDensityLayouts(){
  int[][] layouts={{1280,720,0,0,1280,720},{1280,800,0,40,1280,720},{800,600,0,75,800,450},{1600,720,160,0,1280,720},{1920,1080,0,0,1920,1080},{1024,768,0,96,1024,576}};
  GameWorld w=new GameWorld();for(GameWorld.Item item:GameWorld.Item.values())if(item.held())w.collectItem(item);
  for(int[] v:layouts){int slot=0;for(GameWorld.Item item:GameWorld.Item.values())if(item.held()){
   for(float[] offset:new float[][]{{3,3},{18,18},{33,33}}){
    float targetX=ItemHud.x(slot)+offset[0],targetY=ItemHud.y(slot)+offset[1];
    float mouseX=v[2]+targetX*v[4]/1280f,mouseY=v[1]-(v[3]+targetY*v[5]/720f);
    float x=ItemHud.pointerX(mouseX,v[2],v[4]),y=ItemHud.pointerY(mouseY,v[1],v[3],v[5]);
    assertEquals(targetX,x,.01);assertEquals(targetY,y,.01);assertEquals(item,ItemHud.at(w,x,y));
   }slot++;
  }}
 }
 @Test public void letterboxAndSpaceBelowInventoryDoNotActivateTools(){GameWorld w=new GameWorld();w.collectItem(GameWorld.Item.MULTI);assertNull(ItemHud.at(w,ItemHud.pointerX(40,0,1280),ItemHud.pointerY(10,800,40,720)));assertNull(ItemHud.at(w,40,590));assertTrue(ItemHud.pointerX(10,160,1280)<0);}

 @Test public void quantityTextAndMarginsNeverTriggerTooltip(){GameWorld w=new GameWorld();w.collectItem(GameWorld.Item.MULTI);
  assertNull(ItemHud.at(w,ItemHud.x(0)+48,ItemHud.y(0)+14));
  assertNull(ItemHud.at(w,ItemHud.x(0)+18,ItemHud.y(0)+1));
  assertNull(ItemHud.at(w,ItemHud.x(0)+18,ItemHud.y(0)-8));
  assertNull(ItemHud.at(w,ItemHud.x(0)+18,ItemHud.y(0)+35));
 }
 @Test public void transparentPixelsAreExcludedAndTextureYIsFlipped(){GameWorld w=new GameWorld();w.collectItem(GameWorld.Item.MULTI);
  ItemHud.Mask[] masks=new ItemHud.Mask[GameWorld.Item.values().length];
  masks[GameWorld.Item.MULTI.ordinal()]=new ItemHud.Mask(2,2,new boolean[]{true,false,false,true});
  float x=ItemHud.x(0)+2,y=ItemHud.y(0)+2;
  assertEquals(GameWorld.Item.MULTI,ItemHud.at(w,x+8,y+24,masks));
  assertEquals(GameWorld.Item.MULTI,ItemHud.at(w,x+24,y+8,masks));
  assertNull(ItemHud.at(w,x+24,y+24,masks));assertNull(ItemHud.at(w,x+8,y+8,masks));
 }

 @Test public void renderedRectangleIsAuthoritativeEvenWhenItMovesAwayFromLogicalSlot(){
  ItemHud.Mask mask=new ItemHud.Mask(2,2,new boolean[]{true,false,false,true});
  ItemHud.RenderedIcon icon=new ItemHud.RenderedIcon(GameWorld.Item.MULTI,64,121,48,48,mask);
  assertTrue(icon.contains(76,133));assertTrue(icon.contains(100,157));
  assertFalse(icon.contains(100,133));assertFalse(icon.contains(76,157));
  assertFalse(icon.contains(76,170));assertFalse(icon.contains(40,57));
 }
}