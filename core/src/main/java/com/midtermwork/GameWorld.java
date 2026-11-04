package com.midtermwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Simulation rules. HP is measured in half-heart units (6 = three hearts). */
public final class GameWorld {
    public static final float WIDTH=4500, FLOOR=112, PLAYER_W=44, PLAYER_H=72;
    public static final int CONTACT_DAMAGE=1, TACKLE_DAMAGE=2, SPIT_DAMAGE=3, TRAP_DAMAGE=2;
    public static final float ATTACK_DURATION=.42f;
    public float walkClock,hurtTime,landingTime,hitStop,shake,coyote;
    private float dustClock;
    public final List<Effect> effects=new ArrayList<>();
    public static final class Effect {
        public float x,y,age;public final float life;public final int kind,dir;
        public Effect(float x,float y,int kind,int dir){this.x=x;this.y=y;this.kind=kind;this.dir=dir;life=kind==2?.55f:.32f;}
    }
    public enum Skill {
        EMBER("X","EMBER",1,1.4f), SMOKE("C","SMOKESCREEN",2,6f),
        DRAGON("A","DRAGON BREATH",3,3.8f), FANG("S","FIRE FANG",4,4.8f);
        public final String key,name;public final int unlockLevel;public final float cooldown;
        Skill(String key,String name,int level,float cooldown){this.key=key;this.name=name;unlockLevel=level;this.cooldown=cooldown;}
    }
    public final float[] skillCooldowns=new float[4];
    public final List<SkillShot> skillShots=new ArrayList<>();
    public final List<Smoke> smokeClouds=new ArrayList<>();
    public int activeSkill=-1;
    public float skillTime;
    private boolean skillReleased;
    private int skillDirection;
    public static final float CAST_DURATION=.45f;
    public static final class SkillShot {
        public float x,y,life;public final float vx;public final int type,power;
        public final java.util.Set<Enemy> hit=new java.util.HashSet<>();
        public SkillShot(float x,float y,int dir,int type,int power){this.x=x;this.y=y;this.type=type;this.power=power;vx=dir*(type==0?460:600);life=type==0?1f:1.05f;}
    }
    public static final class Smoke {
        public float x,y,life=3f;
        public Smoke(float x,float y){this.x=x;this.y=y;}
    }
    public enum Phase { PATROL, WINDUP, ATTACK, RECOVER }
    public final List<Block> blocks=new ArrayList<>();
    public final List<Spike> spikes=new ArrayList<>();
    public final List<Enemy> enemies=new ArrayList<>();
    public final List<Shot> shots=new ArrayList<>();
    public float x,y,vx,vy,attackTime,attackCooldown,invulnerable,elapsed,checkpoint=100;
    public int facing,hp,maxHp,level,xp,kills,evolution;
    public boolean grounded,paused,cleared;
    public String message;
    public float messageTime;
    private int attackSerial;

    public static final class Block {
        public final float x,y,w,h;
        public final boolean oneWay;
        public Block(float x,float y,float w,float h) { this(x,y,w,h,false); }
        public Block(float x,float y,float w,float h,boolean oneWay) { this.x=x;this.y=y;this.w=w;this.h=h;this.oneWay=oneWay; }
    }
    public static final class Spike {
        public final float x,y,w;
        public Spike(float x,float y,float w) { this.x=x;this.y=y;this.w=w; }
    }
    public static final class Enemy {
        public float x,y,vx,vy,hurtTime,phaseTime,walkClock,timer=.8f,burnTime,burnTick,slowTime,smoked;
        public final float home,homeY;
        public final boolean spitter;
        public int hp=65,lastHit=-1,dir=-1;
        public Phase phase=Phase.PATROL;
        public Enemy(float x,float y,boolean spitter) { this.x=home=x;this.y=homeY=y;this.spitter=spitter; }
        public boolean alive() { return hp>0; }
    }
    public static final class Shot {
        public float x,y,vx,life=3;
        public Shot(float x,float y,float vx) { this.x=x;this.y=y;this.vx=vx; }
    }
    public GameWorld() {
        ground(0,650); ground(1100,620); ground(2150,680); ground(3320,1180);
        bridge(620,200,150); bridge(800,280,130); bridge(970,205,160);
        bridge(1360,205,260);
        bridge(1680,200,150); bridge(1870,280,130); bridge(2040,205,150);
        bridge(2410,210,260);
        bridge(2790,200,150); bridge(2970,280,140); bridge(3150,205,190);
        bridge(3700,210,270);
        spikes.add(new Spike(1400,FLOOR,155));
        spikes.add(new Spike(2460,FLOOR,155));
        spikes.add(new Spike(3750,FLOOR,150));
        restart();
    }
    private void ground(float x,float width) { blocks.add(new Block(x,0,width,FLOOR)); }
    private void bridge(float x,float top,float width) { blocks.add(new Block(x,top-24,width,24,true)); }
    public void restart() {
        x=checkpoint=100;y=FLOOR;vx=vy=0;facing=1;grounded=true;
        level=1;xp=kills=attackSerial=evolution=0;maxHp=hp=6;
        attackTime=attackCooldown=invulnerable=elapsed=0;paused=cleared=false;
        skillShots.clear();smokeClouds.clear();java.util.Arrays.fill(skillCooldowns,0);activeSkill=-1;skillTime=0;skillReleased=false;
        enemies.clear(); shots.clear();effects.clear();walkClock=hurtTime=landingTime=hitStop=shake=dustClock=0;coyote=.1f;
        enemies.add(new Enemy(470,FLOOR,false));
        enemies.add(new Enemy(835,280,true));
        enemies.add(new Enemy(1250,FLOOR,false));
        enemies.add(new Enemy(1620,FLOOR,false));
        enemies.add(new Enemy(1900,280,true));
        enemies.add(new Enemy(2290,FLOOR,false));
        enemies.add(new Enemy(2730,FLOOR,false));
        enemies.add(new Enemy(3010,280,true));
        enemies.add(new Enemy(3510,FLOOR,false));
        enemies.add(new Enemy(4110,FLOOR,true));
        message="Use the stepping stones! Yellow = prepare, red = attack, blue = opening.";messageTime=7;
    }
    public int xpRequired() { return 40+(level-1)*20; }
    public int attackDamage() { return 23+(level-1)*2; }
    public String species() { return evolution==0?"CHARMANDER":evolution==1?"CHARMELEON":"CHARIZARD"; }
    public void addExperience(int amount) {
        xp+=amount;
        while(xp>=xpRequired()) {
            xp-=xpRequired();level++;effects.add(new Effect(x+22,y+45,2,1));
            message="LEVEL UP! Lv."+level+" / Attack increased";messageTime=3;
            for(Skill skill:Skill.values())if(skill.unlockLevel==level){message="NEW SKILL! ["+skill.key+"] "+skill.name;messageTime=4;}
        }
        int next=level>=36?2:level>=16?1:0;
        if(next>evolution) {
            evolution=next;maxHp=6+evolution*2;hp=maxHp;
            message="EVOLVED! "+species()+" / "+(maxHp/2)+" hearts";messageTime=5;
        }
    }
    public float attackX() { return facing>0?x+PLAYER_W-8:x-80; }
    public boolean attacking() { return attackTime>.10f&&attackTime<.20f; }
    public void damage(int halfHearts,float sourceX) {
        if(invulnerable>0||hp<=0||cleared) return;
        hp=Math.max(0,hp-halfHearts);invulnerable=1.1f;attackTime=0;skillTime=0;activeSkill=-1;hurtTime=.32f;hitStop=.055f;shake=.2f;
        effects.add(new Effect(x+22,y+38,1,x<sourceX?-1:1));
        vx=x<sourceX?-170:170;vy=250;grounded=false;
        message="Hit! -"+(halfHearts/2f)+" heart(s)";messageTime=1.8f;
        if(hp==0) message="Fainted! Press R to restart.";
    }
    public void step(float dt,float move,boolean jump,boolean attack) {
        if(paused||hp<=0||cleared) return;
        dt=Math.min(Math.max(dt,0),1f/30f);if(dt==0)return;
        for(Iterator<Effect> it=effects.iterator();it.hasNext();){Effect fx=it.next();fx.age+=dt;if(fx.age>=fx.life)it.remove();}
        shake=Math.max(0,shake-dt);
        if(hitStop>0){hitStop=Math.max(0,hitStop-dt);return;}
        hurtTime=Math.max(0,hurtTime-dt);landingTime=Math.max(0,landingTime-dt);
        if(grounded)coyote=.09f;else coyote=Math.max(0,coyote-dt);
        updateSkills(dt);
        elapsed+=dt;messageTime=Math.max(0,messageTime-dt);
        attackTime=Math.max(0,attackTime-dt);attackCooldown=Math.max(0,attackCooldown-dt);invulnerable=Math.max(0,invulnerable-dt);
        if(invulnerable<.87f) {
            float target=Math.max(-1,Math.min(1,move))*260;
            float accel=move==0?(grounded?3200:1300):(grounded?2400:1700);
            if(vx*target<0)accel=3400;
            vx=approach(vx,target,accel*dt);
        }
        walkClock+=Math.abs(vx)/200f*dt;
        dustClock+=dt;
        if(grounded&&Math.abs(vx)>80&&dustClock>.12f){effects.add(new Effect(x+22,y,0,-facing));dustClock=0;}
        if(move!=0&&attackTime<=0&&hurtTime<=0&&skillTime<=0) facing=move>0?1:-1;
        if(jump&&coyote>0) { vy=650;grounded=false;coyote=0;effects.add(new Effect(x+22,y,0,0)); }
        if(attack&&attackCooldown<=0&&skillTime<=0&&hurtTime<=0) { attackTime=ATTACK_DURATION;attackCooldown=.55f;attackSerial++; }
        x=Math.max(0,Math.min(WIDTH-PLAYER_W,x+vx*dt));
        for(Block b:blocks) if(!b.oneWay&&overlap(x,y,PLAYER_W,PLAYER_H,b.x,b.y,b.w,b.h)) {
            if(vx>0)x=b.x-PLAYER_W;else if(vx<0)x=b.x+b.w;
        }
        boolean wasGrounded=grounded;float fallSpeed=vy;float oldY=y;vy=Math.max(-900,vy-1700*dt);float dy=vy*dt;y+=dy;grounded=false;
        for(Block b:blocks) if(overlap(x,y,PLAYER_W,PLAYER_H,b.x,b.y,b.w,b.h)) {
            if(b.oneWay&&(dy>=0||oldY<b.y+b.h-.5f))continue;
            if(dy<0) { y=b.y+b.h;grounded=true;vy=0; }
            else if(dy>0&&!b.oneWay) { y=b.y-PLAYER_H;vy=0; }
        }
        if(grounded&&!wasGrounded&&fallSpeed< -100){landingTime=.16f;effects.add(new Effect(x+22,y,0,0));}
        if(y< -160) {
            // A fall always costs a heart, even if a previous hit's grace period is active.
            invulnerable=0;damage(TRAP_DAMAGE,x+1);x=checkpoint;y=FLOOR+8;vx=vy=0;
            message=hp>0?"Fell into the ravine! Back to the last checkpoint.":"Fainted! Press R to restart.";messageTime=3;
        }
        for(Spike s:spikes) if(overlap(x+5,y,PLAYER_W-10,PLAYER_H,s.x,s.y,s.w,19))damage(TRAP_DAMAGE,s.x+s.w/2);
        if(hp<=0)return;
        if(grounded&&Math.abs(y-FLOOR)<1) {
            if(x>=3400)checkpoint=3400;else if(x>=2230)checkpoint=2230;else if(x>=1180)checkpoint=1180;
        }
        for(Enemy e:enemies) if(e.alive()) updateEnemy(e,dt);
        for(Iterator<Shot> it=shots.iterator();it.hasNext();) {
            Shot s=it.next();float previous=s.x;s.x+=s.vx*dt;s.life-=dt;
            boolean solid=false;
            for(Block b:blocks)if(!b.oneWay&&overlap(s.x-9,s.y-9,18,18,b.x,b.y,b.w,b.h))solid=true;
            for(Smoke cloud:smokeClouds)if(overlap(s.x-9,s.y-9,18,18,cloud.x-135,cloud.y-70,270,150))solid=true;
            if(s.life<=0||solid) { it.remove();continue; }
            // Swept horizontal range also catches a shot crossing the body in one frame.
            if(overlap(Math.min(previous,s.x)-9,s.y-9,Math.abs(s.x-previous)+18,18,x,y,PLAYER_W,PLAYER_H)) {
                damage(SPIT_DAMAGE,s.x);it.remove();
            }
        }
        updateSkillShots(dt);
        if(x>=4380) { cleared=true;message="Trail cleared!"; }
    }
    private void updateEnemy(Enemy e,float dt) {
        e.hurtTime=Math.max(0,e.hurtTime-dt);
        e.slowTime=Math.max(0,e.slowTime-dt);e.smoked=Math.max(0,e.smoked-dt);
        if(e.burnTime>0){e.burnTime=Math.max(0,e.burnTime-dt);e.burnTick-=dt;if(e.burnTick<=0){e.burnTick=.75f;hitEnemy(e,3,facing,false);if(!e.alive())return;}}
        for(Smoke cloud:smokeClouds)if(overlap(e.x,e.y,48,64,cloud.x-135,cloud.y-70,270,150))e.smoked=.15f;
        if(e.smoked>0&&e.phase==Phase.WINDUP){e.phase=Phase.RECOVER;e.timer=.65f;e.phaseTime=0;}
        e.timer-=dt;e.phaseTime+=dt;Phase oldPhase=e.phase;
        if(attacking()&&e.lastHit!=attackSerial&&overlap(attackX(),y+4,88,66,e.x,e.y,48,64)) {
            e.lastHit=attackSerial;
            int hit=attackDamage();
            if(e.phase==Phase.RECOVER)hit=Math.round(hit*1.7f);
            else if(e.phase==Phase.WINDUP||e.phase==Phase.ATTACK)hit=Math.max(1,hit/3);
            hitEnemy(e,hit,facing,true);
            if(!e.alive())return;
        }
        float distance=x-e.x;
        switch(e.phase) {
        case PATROL:
            e.vx=approach(e.vx,e.spitter?0:e.dir*38,500*dt);
            if(e.x<e.home-65)e.dir=1;else if(e.x>e.home+65)e.dir=-1;
            if(e.timer<=0&&e.smoked<=0&&Math.abs(distance)<(e.spitter?430:250)&&Math.abs(y-e.y)<110) {
                e.phase=Phase.WINDUP;e.timer=e.spitter?.8f:.7f;e.dir=distance>=0?1:-1;e.vx=0;
            }
            break;
        case WINDUP:
            e.vx=0;
            if(e.timer<=0) {
                e.phase=Phase.ATTACK;e.timer=e.spitter?.12f:.46f;
                if(e.spitter)shots.add(new Shot(e.x+24+e.dir*32,e.y+35,e.dir*330));
            }
            break;
        case ATTACK:
            e.vx=approach(e.vx,e.spitter?0:e.dir*400,3400*dt);
            if(e.timer<=0) { e.phase=Phase.RECOVER;e.timer=1.15f;e.vx=0; }
            break;
        case RECOVER:
            e.vx=0;
            if(e.timer<=0) { e.phase=Phase.PATROL;e.timer=.6f; }
            break;
        default:break;
        }
        if(oldPhase!=e.phase)e.phaseTime=0;
        e.walkClock+=Math.abs(e.vx)/90f*dt;
        if(e.slowTime>0)e.vx*=.55f;if(e.smoked>0)e.vx*=.5f;
        float next=e.x+e.vx*dt;
        // Stay on the current ledge. A tackle stops at its edge and leaves an opening.
        float foot=e.vx>=0?next+45:next+3;
        boolean support=false;
        for(Block b:blocks) if(foot>=b.x&&foot<=b.x+b.w&&Math.abs(e.y-(b.y+b.h))<4)support=true;
        if(support) e.x=next;
        else if(e.vx!=0) {
            e.dir=-e.dir;e.vx=0;
            if(e.phase==Phase.ATTACK) { e.phase=Phase.RECOVER;e.timer=1.15f; }
        }
        int contact=e.phase==Phase.ATTACK&&!e.spitter?TACKLE_DAMAGE:CONTACT_DAMAGE;
        if(overlap(x,y,PLAYER_W,PLAYER_H,e.x,e.y,48,64))damage(contact,e.x+24);
    }
    public boolean skillUnlocked(int index){return index>=0&&index<4&&level>=Skill.values()[index].unlockLevel;}
    public boolean trySkill(int index) {
        if(index<0||index>=4||paused||hp<=0||cleared||hitStop>0||hurtTime>0||skillTime>0)return false;
        Skill skill=Skill.values()[index];
        if(!skillUnlocked(index)){message="["+skill.key+"] "+skill.name+" unlocks at Lv."+skill.unlockLevel;messageTime=2;return false;}
        if(skillCooldowns[index]>0)return false;
        attackTime=0;
        skillCooldowns[index]=skill.cooldown;activeSkill=index;skillTime=CAST_DURATION;skillReleased=false;skillDirection=facing;
        attackCooldown=Math.max(attackCooldown,CAST_DURATION);
        message=skill.name+"!";messageTime=1.4f;return true;
    }
    private void updateSkills(float dt) {
        for(int i=0;i<4;i++)skillCooldowns[i]=Math.max(0,skillCooldowns[i]-dt);
        for(Iterator<Smoke> it=smokeClouds.iterator();it.hasNext();){Smoke cloud=it.next();cloud.life-=dt;if(cloud.life<=0)it.remove();}
        if(skillTime>0){
            skillTime=Math.max(0,skillTime-dt);
            if(skillTime<=.27f&&!skillReleased){skillReleased=true;releaseSkill();}
            if(skillTime<=0)activeSkill=-1;
        }
    }
    private void releaseSkill() {
        if(activeSkill==0||activeSkill==2) {
            int power=activeSkill==0?30+level*2:25+level*2;
            skillShots.add(new SkillShot(x+22+skillDirection*35,y+38,skillDirection,activeSkill,power));
        } else if(activeSkill==1) {
            smokeClouds.add(new Smoke(x+22+skillDirection*80,y+35));
        } else if(activeSkill==3) {
            effects.add(new Effect(x+22+skillDirection*85,y+40,3,skillDirection));
            float start=skillDirection>0?x+24:x-135;
            for(Enemy e:enemies)if(e.alive()&&overlap(start,y,155,90,e.x,e.y,48,64)) {
                int power=52+level*3;if(e.phase==Phase.RECOVER)power=Math.round(power*1.7f);
                hitEnemy(e,power,skillDirection,true);if(e.alive()){e.burnTime=1.5f;e.burnTick=.75f;}
            }
        }
    }
    private void updateSkillShots(float dt) {
        for(Iterator<SkillShot> it=skillShots.iterator();it.hasNext();) {
            SkillShot shot=it.next();float old=shot.x;shot.x+=shot.vx*dt;shot.life-=dt;
            boolean remove=shot.life<=0;
            for(Block b:blocks)if(!b.oneWay&&overlap(shot.x-12,shot.y-10,24,20,b.x,b.y,b.w,b.h))remove=true;
            if(!remove)for(Enemy e:enemies)if(e.alive()&&!shot.hit.contains(e)&&overlap(Math.min(old,shot.x)-15,shot.y-13,Math.abs(shot.x-old)+30,26,e.x,e.y,48,64)) {
                shot.hit.add(e);hitEnemy(e,shot.power,shot.vx>0?1:-1,true);
                if(e.alive()){if(shot.type==0){e.burnTime=3;e.burnTick=.75f;}else e.slowTime=2.5f;}
                if(shot.type==0){remove=true;break;}
            }
            if(remove)it.remove();
        }
    }
    private void hitEnemy(Enemy e,int power,int direction,boolean impact) {
        if(!e.alive())return;
        e.hp=Math.max(0,e.hp-power);
        if(impact){e.hurtTime=.24f;hitStop=.035f;shake=.1f;effects.add(new Effect(e.x+24,e.y+35,1,direction));}
        if(!e.alive()) {
            effects.add(new Effect(e.x+24,e.y+35,2,direction));kills++;message="+25 EXP / Enemy defeated!";messageTime=2;addExperience(25);
        }
    }
    private static float approach(float value,float target,float delta){return value<target?Math.min(target,value+delta):Math.max(target,value-delta);}
    private static boolean overlap(float x,float y,float w,float h,float a,float b,float c,float d) {
        return x<a+c&&x+w>a&&y<b+d&&y+h>b;
    }
}
