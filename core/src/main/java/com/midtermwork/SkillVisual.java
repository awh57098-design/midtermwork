package com.midtermwork;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/** Procedural, type-tinted move silhouettes. No external sprite ownership required. */
public final class SkillVisual {
    private SkillVisual() {}
    public enum Style { SPARK, STREAM, GLYPH, VORTEX, WAVE, ERUPTION, CLAW, JAW, FIST, TAIL, ROCK, STAR, AURA, ORB, SLAM }
    public static Style style(GameWorld.Skill m){
        switch(m){
        case EMBER:return Style.SPARK;
        case FLAMETHROWER:case DRAGON:return Style.STREAM;
        case FIRE_BLAST:return Style.GLYPH;
        case FIRE_SPIN:return Style.VORTEX;
        case HEAT_WAVE:case DRAGON_PULSE:return Style.WAVE;
        case INFERNO:case OVERHEAT:case FIRE_PLEDGE:return Style.ERUPTION;
        case SCRATCH:case SLASH:case FALSE_SWIPE:case METAL_CLAW:case DRAGON_CLAW:case SHADOW_CLAW:case BREAKING_SWIPE:return Style.CLAW;
        case FANG:case CRUNCH:return Style.JAW;
        case FIRE_PUNCH:case THUNDER_PUNCH:case FOCUS_PUNCH:case BRICK_BREAK:return Style.FIST;
        case DRAGON_TAIL:case OUTRAGE:return Style.TAIL;
        case BODY_SLAM:case TEMPER_FLARE:return Style.SLAM;
        case ROCK_SLIDE:case ROCK_TOMB:case DIG:return Style.ROCK;
        case SWIFT:return Style.STAR;
        default:return m.category==GameWorld.Category.STATUS?Style.AURA:Style.ORB;
        }
    }
    public static float radius(GameWorld.Skill m){return MoveGeometry.projectileRadius(m);}
    public static final class Burst {
        public final GameWorld.Skill move;public final float x,y,life;public final int dir;public final boolean impact;public float age;
        public Burst(GameWorld.Skill m,float x,float y,int d,boolean hit){move=m;this.x=x;this.y=y;dir=d;impact=hit;life=hit?.42f:m.category==GameWorld.Category.STATUS?.8f:.5f;}
    }
    private static void tint(ShapeRenderer s,GameWorld.Element type,float alpha){int c=type.rgb;s.setColor(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f,alpha);}
    public static void projectile(ShapeRenderer s,GameWorld.SkillShot shot,float time){
        float x=shot.x,y=shot.y,d=shot.vx>=0?1:-1,radius=radius(shot.move);Style style=style(shot.move);
        tint(s,shot.element,.14f);s.circle(x,y,radius(shot.move)+9,24);
        for(int i=0;i<5;i++){float a=time*6+i*1.7f,r=radius(shot.move)+4; tint(s,shot.element,.7f);s.circle(x-d*(i*10)+MathUtils.cos(a)*r*.3f,y+MathUtils.sin(a)*r,2,8);}
        tint(s,shot.element,.9f);
        switch(style){
        case STREAM:
            for(int i=0;i<13;i++){float k=i/12f;float xx=x-d*i*8,yy=y+MathUtils.sin(time*27-i*.7f)*6*k;s.circle(xx,yy,radius*(.35f+.65f*k),12);}
            s.setColor(1,.95f,.7f,.9f);s.rectLine(x-d*90,y,x,y,Math.max(3,radius*.35f));break;
        case GLYPH:
            float k=radius/40;
            s.rectLine(x-22*d*k,y+28*k,x+22*d*k,y+28*k,7*k);s.rectLine(x,y+40*k,x,y+2*k,7*k);
            s.rectLine(x,y+12*k,x-30*d*k,y-29*k,8*k);s.rectLine(x,y+12*k,x+30*d*k,y-29*k,8*k);
            s.rectLine(x-28*d*k,y-2*k,x+28*d*k,y-2*k,7*k);
            s.setColor(1,.95f,.6f,1);s.rectLine(x,y+32*k,x,y+3*k,3*k);s.rectLine(x,y+9*k,x-28*d*k,y-27*k,3*k);s.rectLine(x,y+9*k,x+28*d*k,y-27*k,3*k);s.rectLine(x-26*d*k,y-2*k,x+26*d*k,y-2*k,3*k);break;
        case VORTEX:
            for(int i=0;i<38;i++){float a=time*9+i*.57f,r=8+i/37f*(radius-12);s.circle(x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,3+i*.08f,8);}break;
        case WAVE:
            for(int j=0;j<3;j++)for(int i=-7;i<=7;i++){float a=i*.2f;s.circle(x-d*j*18+d*MathUtils.cos(a)*radius*.35f,y+MathUtils.sin(a)*radius,Math.max(3,radius*.07f),8);}break;
        case ERUPTION:
            for(int i=-4;i<=4;i++){float dx=i*radius*.2f,yy=MathUtils.sin(time*18+i)*radius*.1f;s.triangle(x+dx-radius*.14f,y-radius*.7f,x+dx+radius*.14f,y-radius*.7f,x+dx,y+radius*(.8f-Math.abs(i)*.14f)+yy);}break;
        case ROCK:
            for(int i=0;i<3;i++){float xx=x-d*i*17,yy=y+(i-1)*12;s.triangle(xx-12,yy-9,xx+11,yy-9,xx+5,yy+15);s.rect(xx-9,yy-9,17,14);}break;
        case STAR:
            for(int i=0;i<3;i++)star(s,x-d*i*24,y+MathUtils.sin(time*13+i)*10,12-i*2,time*3);break;
        case SPARK:
            for(int i=0;i<5;i++){float xx=x-d*i*9,yy=y+MathUtils.sin(time*24+i)*8;s.triangle(xx-6,yy-5,xx+7,yy-5,xx+d*3,yy+10);}
            s.setColor(1,1,.65f,1);s.circle(x,y,5,10);break;
        default:
            s.circle(x,y,radius,20);s.setColor(1,1,1,.85f);s.circle(x+d*4,y+3,7,12);
            for(int i=0;i<4;i++){tint(s,shot.element,.6f-i*.12f);s.circle(x-d*(20+i*12),y+MathUtils.sin(time*20-i)*4,8-i,10);}break;
        }
    }
    private static void star(ShapeRenderer s,float x,float y,float r,float rotation){for(int i=0;i<5;i++){float a=rotation+i*MathUtils.PI2/5,b=a+MathUtils.PI2/5;s.triangle(x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,x+MathUtils.cos(b)*r,y+MathUtils.sin(b)*r,x-MathUtils.cos(a+b)*r*.35f,y-MathUtils.sin(a+b)*r*.35f);}}
    public static void burst(ShapeRenderer s,Burst b){
        float p=b.age/b.life,x=b.x,y=b.y,d=b.dir;Style st=style(b.move);tint(s,b.move.element,1-p);
        float reach=b.impact?80:MoveGeometry.reach(b.move),height=b.impact?90:MoveGeometry.height(b.move);
        if(!b.impact&&MoveGeometry.radial(b.move)){
            for(int j=0;j<3;j++)for(int i=0;i<36;i++){float a=i*MathUtils.PI2/36,r=Math.max(.1f,Math.min(1,p*2-j*.2f));tint(s,b.move.element,(1-p)*(1-j*.22f));s.rectLine(x+MathUtils.cos(a)*reach*r,y+MathUtils.sin(a)*height*.5f*r,x+MathUtils.cos(a+.18f)*reach*r,y+MathUtils.sin(a+.18f)*height*.5f*r,4-j);}
        }
        if(b.impact){
            if(MoveGeometry.areaProjectile(b.move))for(int i=0;i<32;i++){float a=i*MathUtils.PI2/32,r=radius(b.move)*(.4f+.6f*p);s.rectLine(x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,x+MathUtils.cos(a+.22f)*r,y+MathUtils.sin(a+.22f)*r,5*(1-p)+1);}
            for(int i=0;i<9;i++){float a=i*MathUtils.PI2/9,r=12+p*42;s.rectLine(x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,x+MathUtils.cos(a)*(r+12),y+MathUtils.sin(a)*(r+12),3*(1-p)+1);}
        }
        switch(st){
        case CLAW:
            if(!b.impact&&(b.move==GameWorld.Skill.SLASH||b.move==GameWorld.Skill.SHADOW_CLAW||b.move==GameWorld.Skill.BREAKING_SWIPE)){
                for(int j=0;j<3;j++)for(int i=0;i<20;i++){float a=-1.15f+i*.115f,r=reach*(.8f+j*.09f);float ax=x+d*MathUtils.cos(a)*r,ay=y+MathUtils.sin(a)*height*.48f;float bx=x+d*MathUtils.cos(a+.13f)*r,by=y+MathUtils.sin(a+.13f)*height*.48f;s.rectLine(ax,ay,bx,by,5-j);}
                break;
            }
            if(b.move==GameWorld.Skill.DRAGON_CLAW){s.rectLine(x+d*reach*.2f,y+height*.4f,x+d*reach,y-height*.4f,6);}
            for(int i=0;i<3;i++){float yy=y+(i-1)*height*.16f;float start=x+d*reach*.12f,end=x+d*reach*(.72f+.28f*Math.min(1,p*3));s.rectLine(start,yy-height*.22f,end,yy+height*.22f,6*(1-p)+1);s.setColor(1,1,1,(1-p)*.8f);s.rectLine(start,yy-height*.22f,end,yy+height*.22f,2);tint(s,b.move.element,1-p);}break;
        case JAW:
            x+=b.impact?0:d*reach*.65f;for(int i=-1;i<=1;i++){float xx=x+i*reach*.14f,open=(1-p)*height*.42f;s.triangle(xx-9,y+open+14,xx+9,y+open+14,xx,y+open-8);s.triangle(xx-9,y-open-14,xx+9,y-open-14,xx,y-open+8);}break;
        case FIST:
            x+=b.impact?0:d*reach*(.4f+Math.min(1,p*3)*.5f);s.rect(x-17,y-13,34,30);for(int i=0;i<4;i++)s.circle(x-12+i*8,y+17,5,10);
            for(int i=0;i<5;i++)s.rectLine(x-d*28,y-20+i*10,x-d*(65+p*20),y-20+i*10,2);
            if(b.move==GameWorld.Skill.THUNDER_PUNCH){s.setColor(1,1,.7f,1-p);for(int i=0;i<6;i++){float yy=y-42+i*15;s.rectLine(x+(i%2==0?-23:23),yy,x+(i%2==0?23:-23),yy+15,3);}}
            if(b.move==GameWorld.Skill.FOCUS_PUNCH||b.move==GameWorld.Skill.BRICK_BREAK)for(int i=0;i<20;i++){float a=i*MathUtils.PI2/20;s.rectLine(x+MathUtils.cos(a)*35,y+MathUtils.sin(a)*height*.38f,x+MathUtils.cos(a+.2f)*35,y+MathUtils.sin(a+.2f)*height*.38f,3);}
            break;
        case TAIL:
            for(int i=0;i<18;i++){float a=-1.25f+i*.147f,r=reach*(.6f+.4f*Math.min(1,p*3));s.circle(x+d*MathUtils.cos(a)*r,y+MathUtils.sin(a)*height*.45f,4+i*.3f,10);}break;
        case ROCK:
            for(int i=0;i<9;i++){float xx=x+d*reach*(i+1)/9,yy=y-height*.35f+p*20;s.triangle(xx-8,yy,xx+9,yy,xx,yy+18+Math.abs(i)*5);}break;
        case SLAM:
            for(int i=0;i<12;i++){float a=i*MathUtils.PI2/12,r=reach*Math.min(1,p*2);float xx=x+MathUtils.cos(a)*r,yy=y+MathUtils.sin(a)*height*.4f;s.triangle(xx-5,yy,xx+5,yy,xx,yy+13);}
            break;
        case AURA:
            for(int i=0;i<20;i++){float a=i*MathUtils.PI2/20+p*3,r=20+p*50;s.circle(x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r*.6f,3,8);}
            if(b.move==GameWorld.Skill.PROTECT||b.move==GameWorld.Skill.SUBSTITUTE)for(int i=0;i<26;i++){float a=i*MathUtils.PI2/26;s.rectLine(x+MathUtils.cos(a)*33,y+MathUtils.sin(a)*43,x+MathUtils.cos(a+.24f)*33,y+MathUtils.sin(a+.24f)*43,3);}break;
        default:
            if(!b.impact)for(int i=0;i<8;i++){float a=i*MathUtils.PI2/8;s.circle(x+MathUtils.cos(a)*(35-p*20),y+MathUtils.sin(a)*(35-p*20),3+p*3,10);}break;
        }
    }
}
