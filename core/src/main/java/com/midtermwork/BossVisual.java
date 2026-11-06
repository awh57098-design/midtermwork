package com.midtermwork;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
/** Boss telegraphs and attacks use the simulation's actual hazard rectangles. */
public final class BossVisual {
 private BossVisual(){}
 public static void shot(ShapeRenderer s,GameWorld.Shot b,float t){float d=b.vx>=0?1:-1;
  if(b.element==GameWorld.Element.FIRE){s.setColor(1,.22f,.03f,.85f);s.triangle(b.x-d*32,b.y-7,b.x,b.y+12,b.x,b.y-12);s.circle(b.x,b.y,11,16);s.setColor(1,.9f,.35f,1);s.circle(b.x+d*3,b.y+2,6,12);}
  else{s.setColor(.45f,.08f,.66f,.9f);s.triangle(b.x-d*25,b.y,b.x,b.y+10,b.x,b.y-10);s.circle(b.x,b.y,11,12);s.setColor(.65f,1,.3f,.9f);s.circle(b.x+d*3,b.y+3,4,10);}
 }
 public static void draw(ShapeRenderer s,GameWorld w,float t){GameWorld.Enemy e=w.boss;if(e==null||!e.alive()||!w.bossActive)return;
  if(e.phase==GameWorld.Phase.WINDUP){
   if(w.stage==1){s.setColor(1,.9f,.65f,.65f);if(e.pattern==2){s.ellipse(e.targetX-18,GameWorld.FLOOR+2,120,13,24);for(int i=0;i<4;i++)s.rect(e.targetX+35,GameWorld.FLOOR+30+i*28,4,12);}else {float start=e.dir>0?e.x+e.width():e.x-320;s.rect(start,GameWorld.FLOOR+2,320,5);for(int i=0;i<4;i++){float x=start+i*80;s.triangle(x,GameWorld.FLOOR+12,x+e.dir*20,GameWorld.FLOOR+20,x,GameWorld.FLOOR+28);}}}
   else if(w.stage==2){s.setColor(.8f,.3f,1,.6f);for(int i=-1;i<=1;i++)s.rectLine(e.x+40,e.y+45,e.x+40+e.dir*210,e.y+45+i*65,2);s.circle(e.x+40,e.y+45,20+MathUtils.sin(t*18)*3,20);}
   else{s.setColor(1,.35f,.1f,.55f);for(int i=0;i<16;i++){float a=t*4+i*MathUtils.PI2/16;s.circle(e.x+40+MathUtils.cos(a)*45,e.y+48+MathUtils.sin(a)*45,4,8);}if(e.pattern==2){s.rect(85,GameWorld.FLOOR+2,1110,3);s.rect(85,GameWorld.FLOOR+135,1110,3);}}
  }
  if(w.stage==1&&e.phase==GameWorld.Phase.ATTACK&&e.pattern==0){s.setColor(.95f,.9f,.7f,.6f);for(int i=0;i<7;i++)s.rectLine(e.x-e.dir*(10+i*13),e.y+18+i*8,e.x-e.dir*(28+i*13),e.y+18+i*8,3);}
  if(w.stage==2&&e.phase==GameWorld.Phase.ATTACK&&e.pattern==2){s.setColor(.65f,.15f,.9f,.5f);for(int i=0;i<5;i++)s.circle(e.x+40-e.dir*i*19,e.y+25,14-i,12);}
  for(GameWorld.BossHazard h:w.bossHazards){
   boolean poison=h.type==GameWorld.BossHazardType.POISON_POOL,physical=h.type==GameWorld.BossHazardType.SHOCKWAVE;
   float r=poison?.65f:physical?.95f:1,g=poison?.15f:physical?.85f:.32f,b=poison?.85f:physical?.6f:.04f;
   if(h.warning>0){s.setColor(r,g,b,.16f+.12f*MathUtils.sin(t*18));if(h.vx==0)s.rect(h.x,h.y,h.w,h.h);else s.rect(85,h.y,1110,h.h);s.setColor(r,g,b,.9f);s.rect(h.vx==0?h.x:85,h.y,h.vx==0?h.w:1110,4);float cx=h.vx==0?h.x+h.w/2:640;s.triangle(cx-7,h.y+12,cx,h.y+27,cx+7,h.y+12);continue;}
   s.setColor(r,g,b,.8f);
   switch(h.type){
   case POISON_POOL:s.ellipse(h.x,h.y,h.w,h.h,24);s.setColor(.65f,1,.25f,.65f);for(int i=0;i<6;i++){float x=h.x+10+i*(h.w-20)/5,y=h.y+9+MathUtils.sin(t*4+i)*5;s.circle(x,y,3+MathUtils.sin(t*7+i)*1.5f,10);}break;
   case FIRE_PILLAR:s.rect(h.x,h.y,h.w,h.h*.78f);for(int i=0;i<5;i++){float x=h.x+i*h.w/5;s.triangle(x,h.y+h.h*.65f,x+h.w/5,h.y+h.h*.65f,x+h.w/10,h.y+h.h*(.88f+.1f*MathUtils.sin(t*18+i)));}s.setColor(1,.95f,.4f,.95f);s.rect(h.x+h.w*.3f,h.y,h.w*.4f,h.h*.7f);break;
   case SHOCKWAVE:for(int i=0;i<4;i++){float x=h.x+i*h.w/4;s.triangle(x,h.y,x+h.w/4,h.y,x+h.w/8,h.y+h.h*(.7f+.25f*MathUtils.sin(t*20+i)));}break;
   case HEAT_WAVE:for(int i=0;i<9;i++){float yy=h.y+i*h.h/9,xx=h.x+MathUtils.sin(i*.5f)*h.w*.5f;s.circle(xx,yy,7,10);}s.setColor(1,.9f,.3f,.9f);for(int i=0;i<6;i++)s.circle(h.x+15+MathUtils.sin(t*10+i)*5,h.y+i*h.h/6,4,8);break;
   }
  }
 }
}
