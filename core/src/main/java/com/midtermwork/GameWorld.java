package com.midtermwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Simulation rules. HP is measured in half-heart units (6 = three hearts). */
public final class GameWorld {
    public static final float WIDTH=4500, FLOOR=112, PLAYER_W=44, PLAYER_H=72;
    public static final int CONTACT_DAMAGE=1, TACKLE_DAMAGE=2, SPIT_DAMAGE=3, TRAP_DAMAGE=2;
    public static final float ATTACK_DURATION=.42f;
    public int stage=1,mapIndex=1,totalKills;
    private float[] roomCheckpoints;
    public boolean isBossRoom(){return mapIndex==5;}
    public float worldWidth(){return isBossRoom()?1280:WIDTH;}
    public float exitX(){return worldWidth()-120;}
    public int areaNumber(){return (stage-1)*5+mapIndex;}
    public String mapTitle(){return MapLayout.title(mapIndex);}
    public Enemy boss;public boolean bossActive;
    private int entryLevel,entryXp,entryEvolution,entryMaxHp,entryKills;
    public final List<Eruption> eruptions=new ArrayList<>();
    public static final class Eruption {public final float x,y;public float warning=.95f,life=.75f;public Eruption(float x,float y){this.x=x;this.y=y;}}
    public boolean stageComplete;
    public float transitionTime,rockSpawnTimer;
    public final List<RockFall> rockFalls=new ArrayList<>();
    public static final class RockFall {
        public final float x;public float y=565,vy,warning=1f;
        public RockFall(float x){this.x=x;}
    }
    public String stageName(){return stage==1?"초록빛 숲":stage==2?"수정 동굴":"불꽃 화산";}
    public String stageLabel(){return String.format(java.util.Locale.ROOT,"%d-%d / %s",stage,mapIndex,stageName());}
    public float[] checkpointPositions(){return roomCheckpoints.clone();}
    private float dropThroughTime,dropThroughTop;
    public static final float FOOT_INSET=15;
    public float walkClock,hurtTime,landingTime,hitStop,shake,coyote;
    private float dustClock;
    public final List<Effect> effects=new ArrayList<>();
    public static final class Effect {
        public float x,y,age;public final float life;public final int kind,dir;
        public Effect(float x,float y,int kind,int dir){this.x=x;this.y=y;this.kind=kind;this.dir=dir;life=kind==2?.55f:.32f;}
    }
    public enum Item {
        CANDY("이상한사탕","rare-candy",40), BERRY("자뭉열매","sitrus-berry",10),
        MULTI("멀티렌즈","zoom-lens",4), WIDE("광각렌즈","wide-lens",4),
        CHARCOAL("목탄","charcoal",5), FANG("용의이빨","dragon-fang",5),
        SASH("기합의띠","focus-sash",4), LEFTOVERS("먹다남은음식","leftovers",3),
        MUSCLE("힘의머리띠","muscle-band",4), GLASSES("박식안경","wise-glasses",4),
        SHELL("조개껍질방울","shell-bell",3), SCOPE("초점렌즈","scope-lens",3),
        CLAW("선제공격손톱","quick-claw",3), SCARF("실크스카프","silk-scarf",3),
        EGG("행복의알","lucky-egg",3), BELT("달인의띠","expert-belt",2);
        public final String title,sprite;public final int weight;
        Item(String title,String sprite,int weight){this.title=title;this.sprite=sprite;this.weight=weight;}
        public boolean held(){return this!=CANDY&&this!=BERRY;}
    }
    public static final class Drop {
        public final Item item;public float x,y,vy=150,age;
        public Drop(Item item,float x,float y){this.item=item;this.x=x;this.y=y;}
    }
    private static final class EchoHit {
        final Enemy enemy;final int power,dir;float delay=.14f;
        EchoHit(Enemy enemy,int power,int dir){this.enemy=enemy;this.power=power;this.dir=dir;}
    }
    public final List<Drop> drops=new ArrayList<>();
    private final List<EchoHit> echoes=new ArrayList<>();
    private final int[] inventory=new int[Item.values().length],entryInventory=new int[Item.values().length];
    private final java.util.Random dropRandom=new java.util.Random();
    private float healClock,drainCharge;
    private final java.util.Random combatRandom=new java.util.Random();
    public String itemNotice="";public float itemNoticeTime;
    public int itemCount(Item item){return inventory[item.ordinal()];}
    public void setDropSeed(long seed){dropRandom.setSeed(seed);combatRandom.setSeed(seed);}
    public static Item rollItem(int roll){
        if(roll<0||roll>=100)throw new IllegalArgumentException("roll must be 0..99");
        for(Item item:Item.values()){roll-=item.weight;if(roll<0)return item;}throw new AssertionError();
    }
    public void collectItem(Item item){
        String effect;
        if(item==Item.CANDY){addExperience(xpRequired()-xp);hp=Math.min(maxHp,hp+2);effect="레벨 상승 + 하트 1칸 회복";}
        else if(item==Item.BERRY){hp=Math.min(maxHp,hp+2);effect="하트 1칸 회복";}
        else {inventory[item.ordinal()]++;effect="x"+itemCount(item)+" / "+itemSummary(item);}
        itemNotice=item.title+" / "+effect;itemNoticeTime=4;
        effects.add(new Effect(x+22,y+40,2,1));
    }
    public String itemSummary(Item item){
        int n=itemCount(item);
        switch(item){
        case CANDY:return "레벨이 1 오르고 하트 1칸을 회복합니다.";
        case BERRY:return "즉시 하트 1칸을 회복합니다.";
        case MULTI:return "추가 타격 피해 +"+(n*50)+"%.";
        case WIDE:return "유도 범위 "+(450+Math.max(0,n-1)*100)+" / 회전 속도 강화";
        case CHARCOAL:return "불꽃 기술 피해 +"+(n*20)+"%.";
        case FANG:return "용 기술 피해 +"+(n*20)+"%.";
        case SASH:return "치명적 피해 "+n+"회 생존 / 남은 하트 반 칸";
        case LEFTOVERS:return "하트 "+(n*.5f)+"칸을 12초마다 회복";
        case MUSCLE:return "기본 공격과 불꽃엄니 피해 +"+(n*15)+"%.";
        case GLASSES:return "불꽃세례와 용의숨결 피해 +"+(n*15)+"%.";
        case SHELL:return "타격 시 회복 충전 +"+n+" / 6 충전마다 하트 반 칸 회복";
        case SCOPE:return String.format(java.util.Locale.ROOT,"치명타 확률 %.1f%% / 피해 1.5배",100*(1-Math.pow(.9,n)));
        case CLAW:return String.format(java.util.Locale.ROOT,"기본 공격과 기술 재사용 시간 %.0f%% 감소",100*(1-1/(1+.12f*n)));
        case SCARF:return "기본 공격 피해 +"+(n*20)+"%.";
        case EGG:return "적 처치 경험치 +"+(n*25)+"%.";
        case BELT:return "보스에게 주는 피해 +"+(n*20)+"%.";
        default:return "";
        }
    }
    public String itemDescription(Item item){
        switch(item){
        case MULTI:return "잠시 뒤 한 번 더 타격합니다. 도구 1개마다 원래 피해의 50%를 더합니다. 화상에는 발동하지 않습니다.";
        case WIDE:return "투사체가 가까운 적을 따라갑니다. 추가 도구마다 유도 범위가 100 늘고 더 빠르게 방향을 바꿉니다.";
        case SASH:return "쓰러질 피해를 받으면 1개를 소모해 생존합니다. 함정 피해에도 적용됩니다.";
        case SHELL:return "직접 타격하면 회복을 충전합니다. 많이 모을수록 빨리 회복합니다. 화상과 추가 타격은 제외됩니다.";
        case CLAW:return "1개마다 재사용 회복 속도가 12% 증가합니다. 중첩할수록 추가 감소 폭은 작아집니다.";
        default:return item.held()?"보유 중 자동 적용되며 같은 도구도 중첩됩니다. 다음 맵에 유지되며 재도전 시 입장 당시 수량으로 돌아갑니다.":"획득 즉시 사용됩니다.";
        }
    }
    public float cooldownScale(){return 1/(1+.12f*itemCount(Item.CLAW));}
    private int elementalDamage(int power,boolean fire){return Math.round(power*(1+.2f*itemCount(fire?Item.CHARCOAL:Item.FANG)));}
    private int categoryDamage(int power,boolean physical){return Math.round(power*(1+.15f*itemCount(physical?Item.MUSCLE:Item.GLASSES)));}
    private void playerHit(Enemy enemy,int power,int dir){
        if(enemy.boss)power=Math.round(power*(1+.2f*itemCount(Item.BELT)));
        if(itemCount(Item.SCOPE)>0&&combatRandom.nextFloat()<1-Math.pow(.9,itemCount(Item.SCOPE)))power=Math.round(power*1.5f);
        hitEnemy(enemy,power,dir,true);
        if(itemCount(Item.SHELL)>0){drainCharge+=itemCount(Item.SHELL);if(drainCharge>=6){int heal=(int)(drainCharge/6);drainCharge%=6;hp=Math.min(maxHp,hp+heal);}}
        if(enemy.alive()&&itemCount(Item.MULTI)>0)echoes.add(new EchoHit(enemy,Math.max(1,Math.round(power*.5f*itemCount(Item.MULTI))),dir));
    }
    private void updateItems(float dt){
        itemNoticeTime=Math.max(0,itemNoticeTime-dt);
        if(itemCount(Item.LEFTOVERS)>0){healClock+=dt;if(healClock>=12){healClock-=12;if(hp<maxHp){hp=Math.min(maxHp,hp+itemCount(Item.LEFTOVERS));itemNotice="먹다남은음식 / 하트 회복";itemNoticeTime=3;}}}
        for(Iterator<EchoHit> it=echoes.iterator();it.hasNext();){EchoHit echo=it.next();echo.delay-=dt;if(echo.delay<=0){hitEnemy(echo.enemy,echo.power,echo.dir,true);it.remove();}}
        for(Iterator<Drop> it=drops.iterator();it.hasNext();){
            Drop drop=it.next();drop.age+=dt;float old=drop.y;drop.vy=Math.max(-400,drop.vy-650*dt);drop.y+=drop.vy*dt;
            for(Block block:blocks)if(drop.vy<=0&&drop.x+12>block.x&&drop.x-12<block.x+block.w&&old-12>=block.y+block.h-.5f&&drop.y-12<=block.y+block.h){drop.y=block.y+block.h+12;drop.vy=0;}
            if(drop.age>.4f&&overlap(x-20,y-10,PLAYER_W+40,PLAYER_H+20,drop.x-14,drop.y-14,28,28)){collectItem(drop.item);it.remove();}
            else if(drop.y< -180)it.remove();
        }
    }
    public enum Skill {
        EMBER("X","불꽃세례",1,1.4f), SMOKE("C","연막",2,6f),
        DRAGON("A","용의숨결",3,3.8f), FANG("S","불꽃엄니",4,4.8f);
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
        public float x,y,life,vx,vy;public final int type,power;
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
        public int hp=65,maxHp=65,reward=25,lastHit=-1,dir=-1;
        public boolean boss;public int cycle,bursts;public float width(){return boss?82:48;}public float height(){return boss?105:64;}
        public Phase phase=Phase.PATROL;
        public Enemy(float x,float y,boolean spitter) { this.x=home=x;this.y=homeY=y;this.spitter=spitter; }
        public boolean alive() { return hp>0; }
    }
    public static final class Shot {
        public float x,y,vx,vy,life=3;public int damage=SPIT_DAMAGE;
        public Shot(float x,float y,float vx) { this.x=x;this.y=y;this.vx=vx; }
    }
    public GameWorld() { restart(); }
    private void ground(float x,float width) { blocks.add(new Block(x,0,width,FLOOR)); }
    private void bridge(float x,float top,float width) { blocks.add(new Block(x,top-24,width,24,true)); }
    public void restart() {
        stage=1;mapIndex=1;totalKills=0;level=1;xp=evolution=0;maxHp=hp=6;elapsed=0;
        java.util.Arrays.fill(inventory,0);
        loadStage();
    }
    private void loadStage() {
        System.arraycopy(inventory,0,entryInventory,0,inventory.length);drops.clear();echoes.clear();healClock=drainCharge=dropThroughTime=0;itemNoticeTime=0;
        entryLevel=level;entryXp=xp;entryEvolution=evolution;entryMaxHp=maxHp;entryKills=totalKills;
        bossActive=false;boss=null;eruptions.clear();
        x=checkpoint=100;y=FLOOR;vx=vy=0;facing=1;grounded=true;
        kills=attackSerial=0;attackTime=attackCooldown=0;invulnerable=1;
        paused=cleared=stageComplete=false;transitionTime=0;rockSpawnTimer=3;
        skillShots.clear();smokeClouds.clear();rockFalls.clear();java.util.Arrays.fill(skillCooldowns,0);activeSkill=-1;skillTime=0;skillReleased=false;
        enemies.clear();shots.clear();effects.clear();walkClock=hurtTime=landingTime=hitStop=shake=dustClock=0;coyote=.1f;
        blocks.clear();spikes.clear();
        MapLayout layout=MapLayout.create(stage,mapIndex);blocks.addAll(layout.blocks);spikes.addAll(layout.spikes);roomCheckpoints=layout.checkpoints;
        if(!isBossRoom()) {
            if(stage==1&&mapIndex==1)invulnerable=0;
            for(MapLayout.Spawn spawn:layout.enemies)spawn(spawn.x,spawn.y,spawn.spitter);
            message=stageName()+" / "+mapTitle()+": 출구까지 이동하세요!";
        }else{
            boss=new Enemy(920,FLOOR,stage!=1);boss.boss=true;boss.hp=boss.maxHp=stage==1?260:stage==2?340:440;
            boss.reward=stage==1?600:stage==2?1000:1400;enemies.add(boss);
            message="보스의 방 / "+bossName()+": 보스를 쓰러뜨리면 출구가 열립니다.";
        }
        messageTime=6;
    }
    private void spawn(float x,float y,boolean spitter) {
        Enemy e=new Enemy(x,y,spitter);e.maxHp=e.hp=stage==1?65:stage==2?90:115;e.reward=stage==1?25:stage==2?40:55;enemies.add(e);
    }
    public boolean advanceStage() {
        if(!stageComplete||(stage==3&&isBossRoom())||paused||hp<=0)return false;
        if(mapIndex<5)mapIndex++;else{stage++;mapIndex=1;}hp=maxHp;loadStage();return true;
    }
    public String nextAreaLabel(){return mapIndex<5?stage+"-"+(mapIndex+1)+" / "+MapLayout.title(mapIndex+1):(stage+1)+"-1 / "+(stage==1?"수정 동굴":"불꽃 화산");}
    public String bossName(){return stage==1?"레트라":stage==2?"아보크":"마그마";}
    public boolean bossDefeated(){return !isBossRoom()||(boss!=null&&!boss.alive());}
    public boolean retryStage(){if(hp>0||cleared)return false;level=entryLevel;xp=entryXp;evolution=entryEvolution;maxHp=entryMaxHp;hp=maxHp;totalKills=entryKills;System.arraycopy(entryInventory,0,inventory,0,inventory.length);loadStage();return true;}
    public int xpRequired() { return 40+(level-1)*20; }
    public int attackDamage() { return categoryDamage(Math.round((23+(level-1)*2)*(1+.2f*itemCount(Item.SCARF))),true); }
    public String species() { return evolution==0?"파이리":evolution==1?"리자드":"리자몽"; }
    public void addExperience(int amount) {
        xp+=amount;
        while(xp>=xpRequired()) {
            xp-=xpRequired();level++;effects.add(new Effect(x+22,y+45,2,1));
            message="레벨 상승! Lv."+level+" / 공격력 증가";messageTime=3;
            for(Skill skill:Skill.values())if(skill.unlockLevel==level){message="새 기술! ["+skill.key+"] "+skill.name;messageTime=4;}
        }
        int next=level>=36?2:level>=16?1:0;
        if(next>evolution) {
            evolution=next;maxHp=6+evolution*2;hp=maxHp;
            message="진화! "+species()+" / "+(maxHp/2)+"칸 하트";messageTime=5;
        }
    }
    public float attackX() { return facing>0?x+PLAYER_W-8:x-80; }
    public boolean attacking() { return attackTime>.10f&&attackTime<.20f; }
    public void damage(int halfHearts,float sourceX) {
        if(invulnerable>0||hp<=0||cleared||stageComplete) return;
        boolean saved=halfHearts>=hp&&itemCount(Item.SASH)>0;
        if(saved){inventory[Item.SASH.ordinal()]--;hp=1;itemNotice="기합의띠 발동! 하트 반 칸으로 생존";itemNoticeTime=4;}else hp=Math.max(0,hp-halfHearts);invulnerable=1.1f;attackTime=0;skillTime=0;activeSkill=-1;hurtTime=.32f;hitStop=.055f;shake=.2f;
        effects.add(new Effect(x+22,y+38,1,x<sourceX?-1:1));
        vx=x<sourceX?-170:170;vy=250;grounded=false;
        message="피격! 하트 -"+(halfHearts/2f)+"칸";messageTime=1.8f;
        if(hp==0) message="쓰러졌습니다! Enter: 재도전 / R: 처음부터";
    }
    public boolean dropThrough(){
        if(!grounded||paused||hp<=0||cleared||stageComplete)return false;
        for(Block b:blocks)if(b.oneWay&&Math.abs(y-(b.y+b.h))<1&&x+PLAYER_W-FOOT_INSET>b.x&&x+FOOT_INSET<b.x+b.w){
            dropThroughTop=y;dropThroughTime=.3f;y-=2;vy=-100;grounded=false;coyote=0;return true;
        }return false;
    }
    public void step(float dt,float move,boolean jump,boolean attack) {
        if(paused||hp<=0||cleared) return;
        dt=Math.min(Math.max(dt,0),1f/30f);if(dt==0)return;
        if(stageComplete){transitionTime=Math.max(0,transitionTime-dt);if(transitionTime<=0)advanceStage();return;}
        for(Iterator<Effect> it=effects.iterator();it.hasNext();){Effect fx=it.next();fx.age+=dt;if(fx.age>=fx.life)it.remove();}
        shake=Math.max(0,shake-dt);
        if(hitStop>0){hitStop=Math.max(0,hitStop-dt);return;}
        hurtTime=Math.max(0,hurtTime-dt);landingTime=Math.max(0,landingTime-dt);
        if(grounded)coyote=.09f;else coyote=Math.max(0,coyote-dt);
        dropThroughTime=Math.max(0,dropThroughTime-dt);
        updateItems(dt);
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
        if(attack&&attackCooldown<=0&&skillTime<=0&&hurtTime<=0) { attackTime=ATTACK_DURATION;attackCooldown=.55f*cooldownScale();attackSerial++; }
        float previousX=x,desiredX=Math.max(0,Math.min(worldWidth()-PLAYER_W,x+vx*dt));
        x=desiredX;
        for(Block b:blocks)if(!b.oneWay&&y<b.y+b.h-.01f&&y+PLAYER_H>b.y+.01f){
            if(desiredX>previousX&&previousX+PLAYER_W<=b.x+.01f&&desiredX+PLAYER_W>b.x)x=Math.min(x,b.x-PLAYER_W);
            else if(desiredX<previousX&&previousX>=b.x+b.w-.01f&&desiredX<b.x+b.w)x=Math.max(x,b.x+b.w);
            else if(overlap(previousX,y,PLAYER_W,PLAYER_H,b.x,b.y,b.w,b.h)){
                // A body can still overlap a ledge after its feet leave it. Never eject it to the far end.
                float leftDistance=previousX+PLAYER_W-b.x,rightDistance=b.x+b.w-previousX;
                if(desiredX>previousX&&leftDistance<rightDistance)x=Math.min(x,previousX);
                if(desiredX<previousX&&rightDistance<=leftDistance)x=Math.max(x,previousX);
            }
        }
        if(x!=desiredX)vx=0;
        boolean wasGrounded=grounded;float fallSpeed=vy,oldY=y;
        vy=Math.max(-900,vy-1700*dt);float dy=vy*dt,targetY=y+dy;y=targetY;grounded=false;
        for(Block b:blocks){
            float top=b.y+b.h;
            if(dy<0&&x+PLAYER_W-FOOT_INSET>b.x&&x+FOOT_INSET<b.x+b.w&&oldY>=top-.01f&&targetY<=top){
                if(b.oneWay&&dropThroughTime>0&&top>=dropThroughTop-.5f)continue;
                if(!grounded||top>y){y=top;grounded=true;vy=0;}
            }else if(dy>0&&!b.oneWay&&x+PLAYER_W>b.x&&x<b.x+b.w&&oldY+PLAYER_H<=b.y+.01f&&targetY+PLAYER_H>=b.y){
                y=Math.min(y,b.y-PLAYER_H);vy=0;
            }
        }
        if(grounded&&!wasGrounded&&fallSpeed< -100){landingTime=.16f;effects.add(new Effect(x+22,y,0,0));}
        if(y< -160) {
            // A fall always costs a heart, even if a previous hit's grace period is active.
            invulnerable=0;damage(TRAP_DAMAGE,x+1);x=checkpoint;y=FLOOR+8;vx=vy=0;
            message=hp>0?"협곡에 떨어졌습니다! 최근 체크포인트로 돌아갑니다.":"쓰러졌습니다! Enter: 재도전 / R: 처음부터";messageTime=3;
        }
        for(Spike s:spikes) if(overlap(x+5,y,PLAYER_W-10,PLAYER_H,s.x,s.y,s.w,19))damage(TRAP_DAMAGE,s.x+s.w/2);
        if(hp<=0)return;
        if(grounded&&Math.abs(y-FLOOR)<1) {
            for(float marker:checkpointPositions())if(x>=marker)checkpoint=Math.max(checkpoint,marker);
        }
        if(isBossRoom()&&x>=170&&!bossActive&&!bossDefeated()) {bossActive=true;boss.timer=1.1f;message="보스: "+bossName()+"! 공격을 피하고 빈틈을 노리세요.";messageTime=4;}
        if(bossActive&&!bossDefeated())x=Math.max(64,Math.min(exitX()-45,x));
        for(Enemy e:enemies) if(e.alive()&&(!e.boss||bossActive)) updateEnemy(e,dt);
        for(Iterator<Shot> it=shots.iterator();it.hasNext();) {
            Shot s=it.next();float previous=s.x;s.x+=s.vx*dt;s.y+=s.vy*dt;s.life-=dt;
            boolean solid=false;
            for(Block b:blocks)if(!b.oneWay&&overlap(s.x-9,s.y-9,18,18,b.x,b.y,b.w,b.h))solid=true;
            for(Smoke cloud:smokeClouds)if(overlap(s.x-9,s.y-9,18,18,cloud.x-135,cloud.y-70,270,150))solid=true;
            if(s.life<=0||solid) { it.remove();continue; }
            // Swept horizontal range also catches a shot crossing the body in one frame.
            if(overlap(Math.min(previous,s.x)-9,s.y-9,Math.abs(s.x-previous)+18,18,x,y,PLAYER_W,PLAYER_H)) {
                damage(s.damage,s.x);it.remove();
            }
        }
        updateSkillShots(dt);
        updateRockFalls(dt);
        updateEruptions(dt);
        if(x>=exitX()&&hp>0&&bossDefeated()) {
            if(stage<3||!isBossRoom()){stageComplete=true;transitionTime=1.4f;message=mapTitle()+" 완료!";}
            else {cleared=true;message="모험 완료! 세 보스를 모두 쓰러뜨렸습니다!";}
        }
    }
    private void updateEnemy(Enemy e,float dt) {
        e.hurtTime=Math.max(0,e.hurtTime-dt);
        e.slowTime=Math.max(0,e.slowTime-dt);e.smoked=Math.max(0,e.smoked-dt);
        if(e.burnTime>0){e.burnTime=Math.max(0,e.burnTime-dt);e.burnTick-=dt;if(e.burnTick<=0){e.burnTick=.75f;hitEnemy(e,3,facing,false);if(!e.alive())return;}}
        for(Smoke cloud:smokeClouds)if(overlap(e.x,e.y,e.width(),e.height(),cloud.x-135,cloud.y-70,270,150))e.smoked=.15f;
        if(!e.boss&&e.smoked>0&&e.phase==Phase.WINDUP){e.phase=Phase.RECOVER;e.timer=.65f;e.phaseTime=0;}
        e.timer-=dt;e.phaseTime+=dt;Phase oldPhase=e.phase;
        if(attacking()&&e.lastHit!=attackSerial&&overlap(attackX(),y+4,88,66,e.x,e.y,e.width(),e.height())) {
            e.lastHit=attackSerial;
            int hit=attackDamage();
            if(e.phase==Phase.RECOVER)hit=Math.round(hit*1.7f);
            else if(e.phase==Phase.WINDUP||e.phase==Phase.ATTACK)hit=Math.max(1,hit/3);
            playerHit(e,hit,facing);
            if(!e.alive())return;
        }
        if(e.boss){updateBoss(e,dt);return;}
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
            e.vx=approach(e.vx,e.spitter?0:e.dir*(stage==1?400:440),3400*dt);
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
        if(overlap(x,y,PLAYER_W,PLAYER_H,e.x,e.y,e.width(),e.height()))damage(contact,e.x+24);
    }
    public boolean skillUnlocked(int index){return index>=0&&index<4&&level>=Skill.values()[index].unlockLevel;}
    public boolean trySkill(int index) {
        if(index<0||index>=4||paused||hp<=0||cleared||stageComplete||hitStop>0||hurtTime>0||skillTime>0)return false;
        Skill skill=Skill.values()[index];
        if(!skillUnlocked(index)){message="["+skill.key+"] "+skill.name+" 해금 레벨: "+skill.unlockLevel;messageTime=2;return false;}
        if(skillCooldowns[index]>0)return false;
        attackTime=0;
        skillCooldowns[index]=skill.cooldown*cooldownScale();activeSkill=index;skillTime=CAST_DURATION;skillReleased=false;skillDirection=facing;
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
            int power=categoryDamage(elementalDamage(activeSkill==0?30+level*2:25+level*2,activeSkill==0),false);
            skillShots.add(new SkillShot(x+22+skillDirection*35,y+38,skillDirection,activeSkill,power));
        } else if(activeSkill==1) {
            smokeClouds.add(new Smoke(x+22+skillDirection*80,y+35));
        } else if(activeSkill==3) {
            effects.add(new Effect(x+22+skillDirection*85,y+40,3,skillDirection));
            float start=skillDirection>0?x+24:x-135;
            for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&overlap(start,y,155,90,e.x,e.y,e.width(),e.height())) {
                int power=categoryDamage(elementalDamage(52+level*3,true),true);if(e.phase==Phase.RECOVER)power=Math.round(power*1.7f);
                playerHit(e,power,skillDirection);if(e.alive()){e.burnTime=1.5f;e.burnTick=.75f;}
            }
        }
    }
    private void updateSkillShots(float dt) {
        for(Iterator<SkillShot> it=skillShots.iterator();it.hasNext();) {
            SkillShot shot=it.next();float old=shot.x,oldY=shot.y;
            if(itemCount(Item.WIDE)>0){
                Enemy target=null;float range=450+Math.max(0,itemCount(Item.WIDE)-1)*100;float nearest=range*range;
                for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&!shot.hit.contains(e)){
                    float dx=e.x+e.width()/2-shot.x,dy=e.y+e.height()/2-shot.y,d=dx*dx+dy*dy;
                    if(d<nearest&&dx*shot.vx+dy*shot.vy>0){target=e;nearest=d;}
                }
                if(target!=null){float dx=target.x+target.width()/2-shot.x,dy=target.y+target.height()/2-shot.y,len=(float)Math.sqrt(dx*dx+dy*dy),speed=shot.type==0?460:600;
                    if(len>1){shot.vx+=(dx/len*speed-shot.vx)*Math.min(1,dt*(10+3*(itemCount(Item.WIDE)-1)));shot.vy+=(dy/len*speed-shot.vy)*Math.min(1,dt*(10+3*(itemCount(Item.WIDE)-1)));}}
            }
            shot.x+=shot.vx*dt;shot.y+=shot.vy*dt;shot.life-=dt;
            boolean remove=shot.life<=0;
            for(Block b:blocks)if(!b.oneWay&&overlap(shot.x-12,shot.y-10,24,20,b.x,b.y,b.w,b.h))remove=true;
            if(!remove)for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&!shot.hit.contains(e)&&overlap(Math.min(old,shot.x)-15,Math.min(oldY,shot.y)-13,Math.abs(shot.x-old)+30,Math.abs(shot.y-oldY)+26,e.x,e.y,e.width(),e.height())) {
                shot.hit.add(e);playerHit(e,shot.power,shot.vx>0?1:-1);
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
            if(e.boss||dropRandom.nextInt(100)<65)drops.add(new Drop(rollItem(dropRandom.nextInt(100)),e.x+e.width()/2,e.y+30));
            effects.add(new Effect(e.x+24,e.y+35,2,direction));kills++;totalKills++;int reward=Math.round(e.reward*(1+.25f*itemCount(Item.EGG)));message="+"+reward+" 경험치 / 적 처치!";messageTime=2;addExperience(reward);
        }
    }
    private void updateRockFalls(float dt) {
        if(stage!=2||isBossRoom())return;
        rockSpawnTimer-=dt;
        if(rockSpawnTimer<=0){
            if(x>700&&x<4100)rockFalls.add(new RockFall(x+PLAYER_W/2));
            rockSpawnTimer=3.4f;
        }
        for(Iterator<RockFall> it=rockFalls.iterator();it.hasNext();) {
            RockFall rock=it.next();
            if(rock.warning>0){rock.warning=Math.max(0,rock.warning-dt);continue;}
            rock.vy=Math.max(-650,rock.vy-1000*dt);rock.y+=rock.vy*dt;
            if(overlap(rock.x-13,rock.y-13,26,26,x,y,PLAYER_W,PLAYER_H)){damage(TRAP_DAMAGE,rock.x);it.remove();continue;}
            boolean landed=rock.y< -100;
            for(Block b:blocks)if(overlap(rock.x-13,rock.y-13,26,26,b.x,b.y,b.w,b.h))landed=true;
            if(landed){effects.add(new Effect(rock.x,rock.y,0,0));it.remove();}
        }
    }
    private void bossVolley(Enemy e) {
        int count=stage==3?5:3;
        for(int i=0;i<count;i++){Shot shot=new Shot(e.x+e.width()/2+e.dir*45,e.y+44,e.dir*(stage==3?300:270));shot.vy=(i-(count-1)/2f)*70;shot.damage=2;shots.add(shot);}
    }
    private void updateBoss(Enemy e,float dt) {
        Phase previous=e.phase;
        switch(e.phase) {
        case PATROL:
            e.vx=0;
            if(e.timer<=0){e.dir=x<e.x?-1:1;e.phase=Phase.WINDUP;e.timer=stage==1?.7f:.9f;e.bursts=0;}
            break;
        case WINDUP:
            e.vx=0;
            if(e.timer<=0){e.phase=Phase.ATTACK;e.timer=stage==1?.46f:.7f;
                if(stage==2||(stage==3&&e.cycle%2==0)){bossVolley(e);e.bursts=1;}
                else if(stage==3){for(float dx:new float[]{-90,0,90})eruptions.add(new Eruption(Math.max(82,Math.min(1140,x+22+dx)),FLOOR));}
            }
            break;
        case ATTACK:
            if(stage==1){e.vx=approach(e.vx,e.dir*530,3400*dt);e.x=Math.max(85,Math.min(1200-e.width(),e.x+e.vx*dt));}
            else if(e.timer<.35f&&e.bursts==1){bossVolley(e);e.bursts=2;}
            if(e.timer<=0){
                if(stage==1&&e.bursts==0){e.bursts=1;e.phase=Phase.WINDUP;e.timer=.5f;e.dir=x<e.x?-1:1;}
                else{e.phase=Phase.RECOVER;e.timer=1.4f;e.vx=0;e.cycle++;}
            }
            break;
        case RECOVER:
            e.vx=0;if(e.timer<=0){e.phase=Phase.PATROL;e.timer=.45f;}break;
        default:break;
        }
        if(e.phase!=previous)e.phaseTime=0;
        e.walkClock+=Math.abs(e.vx)/100f*dt;
        if(overlap(x,y,PLAYER_W,PLAYER_H,e.x,e.y,e.width(),e.height()))damage(e.phase==Phase.ATTACK?2:1,e.x+e.width()/2);
    }
    private float surfaceBelow(float px,float py){float top=FLOOR;for(Block b:blocks)if(px>=b.x&&px<=b.x+b.w&&b.y+b.h<=py)top=Math.max(top,b.y+b.h);return top;}
    private void updateEruptions(float dt) {
        if(stage==3&&!isBossRoom()){rockSpawnTimer-=dt;if(rockSpawnTimer<=0){if(x>1100&&x<3600)eruptions.add(new Eruption(x+22,surfaceBelow(x+22,y+4)));rockSpawnTimer=3.6f;}}
        for(Iterator<Eruption> it=eruptions.iterator();it.hasNext();){Eruption eruption=it.next();
            if(eruption.warning>0){eruption.warning=Math.max(0,eruption.warning-dt);continue;}
            eruption.life-=dt;if(eruption.life<=0){it.remove();continue;}
            if(overlap(eruption.x-24,eruption.y,48,150,x,y,PLAYER_W,PLAYER_H))damage(2,eruption.x);
        }
    }
    private static float approach(float value,float target,float delta){return value<target?Math.min(target,value+delta):Math.max(target,value-delta);}
    private static boolean overlap(float x,float y,float w,float h,float a,float b,float c,float d) {
        return x<a+c&&x+w>a&&y<b+d&&y+h>b;
    }
}
