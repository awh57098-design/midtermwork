package com.midtermwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Simulation rules. HP is measured in half-heart units (6 = three hearts). */
public final class GameWorld {
    public static final float WIDTH=4500, FLOOR=112, PLAYER_W=44, PLAYER_H=72;
    public static final int CONTACT_DAMAGE=1, TACKLE_DAMAGE=2, SPIT_DAMAGE=3, TRAP_DAMAGE=2;
    public static final float ATTACK_DURATION=.42f;
    public static final int BOSS_HP_MULTIPLIER=10,BOSS_DEFENSE_MULTIPLIER=3;
    private static int enemyMaxHp(Enemy e){return e.stats().hp*(e.boss?BOSS_HP_MULTIPLIER:1);}
    public void syncEnemyLevels(){
        for(Enemy e:enemies)if(e.alive()&&e.level!=enemyLevel()){float fraction=e.hp/(float)e.maxHp;e.level=enemyLevel();e.maxHp=enemyMaxHp(e);e.hp=Math.min(e.maxHp,Math.max(1,(int)Math.ceil(e.maxHp*fraction)));}
    }
    private void periodicHit(Enemy e){
        if(!e.boss){hitEnemy(e,Math.max(1,Math.round(e.maxHp*.04f)),facing,false);return;}
        e.dotRemainder+=e.maxHp*.0025f;int damage=(int)e.dotRemainder;e.dotRemainder-=damage;
        if(damage>0)hitEnemy(e,damage,facing,false);
    }
    public int stage=1,mapIndex=1,totalKills;
    private float[] roomCheckpoints;
    public boolean isBossRoom(){return mapIndex==5;}
    public float worldWidth(){return isBossRoom()?1280:WIDTH;}
    public float exitX(){return worldWidth()-120;}
    public int areaNumber(){return (stage-1)*5+mapIndex;}
    public String mapTitle(){return MapLayout.title(mapIndex);}
    public Enemy boss;public boolean bossActive;
    private int entryLevel,entryXp,entryEvolution,entryMaxHp,entryKills,entryCandyLevels;
    private int candyLevels;
    public int candyLevelBonus(){return candyLevels;}
    public int enemyLevel(){return Math.max(1,level-candyLevels);}
    public final List<BossHazard> bossHazards=new ArrayList<>();
    public enum BossHazardType { SHOCKWAVE, POISON_POOL, FIRE_PILLAR, HEAT_WAVE }
    public static final class BossHazard {
        public final BossHazardType type;public float x,y,w,h,warning,life,vx,age;public final int damage;
        public BossHazard(BossHazardType t,float x,float y,float w,float h,float warning,float life,float vx,int damage){type=t;this.x=x;this.y=y;this.w=w;this.h=h;this.warning=warning;this.life=life;this.vx=vx;this.damage=damage;}
    }
    public String bossMoveName(){if(boss==null)return "";String[][] names={{"연속 몸통박치기","충격파","점프 내려찍기"},{"독침 부채꼴","맹독 장판","독니 돌진"},{"화염탄 연사","추적 불기둥","상하 열파"}};return names[stage-1][boss.pattern];}
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
        CANDY("이상한사탕","rare-candy",900,5), BERRY("자뭉열매","sitrus-berry",3600,5),
        MULTI("멀티렌즈","zoom-lens",180,2), WIDE("광각렌즈","wide-lens",100,1),
        CHARCOAL("목탄","charcoal",625,4), FANG("용의이빨","dragon-fang",625,4),
        SASH("기합의띠","focus-sash",180,2), LEFTOVERS("먹다남은음식","leftovers",180,2),
        MUSCLE("힘의머리띠","muscle-band",400,3), GLASSES("박식안경","wise-glasses",400,3),
        SHELL("조개껍질방울","shell-bell",400,3), SCOPE("초점렌즈","scope-lens",180,2),
        CLAW("선제공격손톱","quick-claw",400,3), SCARF("실크스카프","silk-scarf",625,4),
        EGG("행복의알","lucky-egg",400,3), BELT("달인의띠","expert-belt",625,4), TM("기술머신","tm-normal",180,2);
        public final String title,sprite;public final int weight,tier;
        Item(String title,String sprite,int weight,int tier){this.title=title;this.sprite=sprite;this.weight=weight;this.tier=tier;}
        public boolean held(){return this!=CANDY&&this!=BERRY&&this!=TM;}
    }
    public static final class Drop {
        public final Item item;public float x,y,vy=150,age;
        public Drop(Item item,float x,float y){this.item=item;this.x=x;this.y=y;}
    }
    private static final class EchoHit {
        final Enemy enemy;final int power,dir,minHp;float delay=.14f;
        EchoHit(Enemy enemy,int power,int dir,int minHp){this.enemy=enemy;this.power=power;this.dir=dir;this.minHp=minHp;}
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
    public static final int DROP_WEIGHT_TOTAL=10000,NORMAL_DROP_PERCENT=20,BOSS_DROP_COUNT=3,BOSS_DROP_WEIGHT_TOTAL=3000;
    public static boolean normalDropOccurs(int roll){if(roll<0||roll>=100)throw new IllegalArgumentException("drop roll outside 0..99");return roll<NORMAL_DROP_PERCENT;}
    public static Item rollBossItem(int roll){
        if(roll<0||roll>=BOSS_DROP_WEIGHT_TOTAL)throw new IllegalArgumentException("boss roll outside weight range");
        for(Item item:Item.values())if(item.tier<=3){roll-=item.weight;if(roll<0)return item;}throw new AssertionError();
    }
    public static Item rollItem(int roll){
        if(roll<0||roll>=DROP_WEIGHT_TOTAL)throw new IllegalArgumentException("roll outside drop weight range");
        for(Item item:Item.values()){roll-=item.weight;if(roll<0)return item;}throw new AssertionError();
    }
    public void collectItem(Item item){
        String effect;
        if(item==Item.CANDY){int previousHp=hp;level++;candyLevels++;xp=0;offerLevelSkill(level);finishGrowth();hp=previousHp;effect="레벨 상승 / 적 레벨 유지";}
        else if(item==Item.BERRY){hp=Math.min(maxHp,hp+2);effect="하트 1칸 회복";}
        else if(item==Item.TM){inventory[item.ordinal()]++;effect="T키로 배울 기술 선택";if(!choosingSkill()&&!paused&&hp>0)tmSelecting=true;}
        else {inventory[item.ordinal()]++;effect="x"+itemCount(item)+" / "+itemSummary(item);}
        itemNotice=item.tier+"티어 "+item.title+" / "+effect;itemNoticeTime=4;
        effects.add(new Effect(x+22,y+40,2,1));
    }
    public String itemSummary(Item item){
        int n=itemCount(item);
        switch(item){
        case TM:return "파이리의 기술머신 기술을 골라 배웁니다. T키로 사용.";
        case CANDY:return "레벨이 1 오릅니다. 적 레벨은 오르지 않으며 하트는 회복하지 않습니다.";
        case BERRY:return "즉시 하트 1칸을 회복합니다.";
        case MULTI:return "추가 타격 피해 +"+(n*50)+"%.";
        case WIDE:return "유도 범위 "+(450+Math.max(0,n-1)*100)+" / 회전 속도 강화";
        case CHARCOAL:return "불꽃 기술 피해 +"+(n*20)+"%.";
        case FANG:return "용 기술 피해 +"+(n*20)+"%.";
        case SASH:return "치명적 피해 "+n+"회 생존 / 남은 하트 반 칸";
        case LEFTOVERS:return "하트 "+(n*.5f)+"칸을 12초마다 회복";
        case MUSCLE:return "모든 물리 공격 피해 +"+(n*15)+"%.";
        case GLASSES:return "모든 특수 공격 피해 +"+(n*15)+"%.";
        case SHELL:return "타격 시 회복 충전 +"+n+" / 6 충전마다 하트 반 칸 회복";
        case SCOPE:return String.format(java.util.Locale.ROOT,"치명타 확률 %.1f%% / 피해 1.5배",100*(1-Math.pow(.9,n)));
        case CLAW:return String.format(java.util.Locale.ROOT,"기본 공격과 기술 재사용 시간 %.0f%% 감소",100*(1-1/(1+.12f*n)));
        case SCARF:return "노말 타입 공격 피해 +"+(n*20)+"%.";
        case EGG:return "적 처치 경험치 +"+(n*25)+"%.";
        case BELT:return "보스에게 주는 피해 +"+(n*20)+"%.";
        default:return "";
        }
    }
    public String itemDescription(Item item){
        switch(item){
        case TM:return "배울 기술과 교체할 슬롯을 선택하면 1개 소모합니다. 취소하면 보관됩니다.";
        case MULTI:return "잠시 뒤 한 번 더 타격합니다. 도구 1개마다 원래 피해의 50%를 더합니다. 화상에는 발동하지 않습니다.";
        case WIDE:return "투사체가 가까운 적을 따라갑니다. 추가 도구마다 유도 범위가 100 늘고 더 빠르게 방향을 바꿉니다.";
        case SASH:return "쓰러질 피해를 받으면 1개를 소모해 생존합니다. 함정 피해에도 적용됩니다.";
        case SHELL:return "직접 타격하면 회복을 충전합니다. 많이 모을수록 빨리 회복합니다. 화상과 추가 타격은 제외됩니다.";
        case CLAW:return "1개마다 재사용 회복 속도가 12% 증가합니다. 중첩할수록 추가 감소 폭은 작아집니다.";
        default:return item.held()?"보유 중 자동 적용되며 같은 도구도 중첩됩니다. 다음 맵에 유지되며 재도전 시 입장 당시 수량으로 돌아갑니다.":"획득 즉시 사용됩니다.";
        }
    }
    public float cooldownScale(){return 1/(1+.12f*itemCount(Item.CLAW));}
    private int categoryDamage(int power,boolean physical){return Math.round(power*(1+.15f*itemCount(physical?Item.MUSCLE:Item.GLASSES))*statMultiplier(physical?attackStage:specialAttackStage)*(helpBuff>0?1.25f:1));}
    public float effectiveness(Skill move,Enemy enemy){return TypeChart.multiplier(move==null?Element.NORMAL:skillElement(move),enemy.primaryType,enemy.secondaryType);}
    public int matchupDamage(int power,Skill move,Enemy enemy){float factor=effectiveness(move,enemy);return factor==0||power<=0?0:Math.max(1,Math.round(power*factor));}
    private void playerHit(Enemy enemy,int power,int dir){playerHit(enemy,power,dir,null);}
    private int playerHit(Enemy enemy,int power,int dir,Skill move){return playerHit(enemy,power,dir,move,move==null?Element.NORMAL:skillElement(move));}
    private int playerHit(Enemy enemy,int power,int dir,Skill move,Element attackType){
        if(!enemy.alive())return 0;
        int oldHp=enemy.hp;
        float factor=TypeChart.multiplier(attackType,enemy.primaryType,enemy.secondaryType);enemy.lastEffectiveness=factor;enemy.matchupTime=.9f;
        if(factor==0){enemy.lastDamage=0;enemy.combatNote="효과 없음";return 0;}
        if(enemy.boss)power=Math.round(power*(1+.2f*itemCount(Item.BELT)));
        boolean physical=move==null||move.category==Category.PHYSICAL;
        power=Math.max(1,Math.round(power*referenceDefense()/enemy.defense(physical)));
        float natural=move!=null&&MoveEffects.profile(move).crit>0?.125f:0;
        float critical=1-(1-natural)*(float)Math.pow(.9,itemCount(Item.SCOPE));
        if(critical>0&&combatRandom.nextFloat()<critical)power=Math.round(power*1.5f);
        power=Math.max(1,Math.round(power*factor));
        if(move==Skill.FALSE_SWIPE)power=Math.min(power,enemy.hp-1);
        if(power<=0)return 0;
        if(move!=null)skillVisuals.add(new SkillVisual.Burst(move,enemy.x+enemy.width()/2,enemy.y+enemy.height()/2,dir,true));
        enemy.lastDamage=power;enemy.combatNote="-"+power+" / "+TypeChart.label(factor);
        hitEnemy(enemy,power,dir,true);
        if(itemCount(Item.SHELL)>0){drainCharge+=itemCount(Item.SHELL);if(drainCharge>=6){int heal=(int)(drainCharge/6);drainCharge%=6;hp=Math.min(maxHp,hp+heal);}}
        // Echoes store already adjusted damage and must not apply type effectiveness a second time.
        if(enemy.alive()&&itemCount(Item.MULTI)>0)echoes.add(new EchoHit(enemy,Math.max(1,Math.round(power*.5f*itemCount(Item.MULTI))),dir,move==Skill.FALSE_SWIPE?1:0));
        return oldHp-enemy.hp;
    }
    private void updateItems(float dt){
        itemNoticeTime=Math.max(0,itemNoticeTime-dt);
        if(itemCount(Item.LEFTOVERS)>0){healClock+=dt;if(healClock>=12){healClock-=12;if(hp<maxHp){hp=Math.min(maxHp,hp+itemCount(Item.LEFTOVERS));itemNotice="먹다남은음식 / 하트 회복";itemNoticeTime=3;}}}
        for(Iterator<EchoHit> it=echoes.iterator();it.hasNext();){EchoHit echo=it.next();echo.delay-=dt;if(echo.delay<=0){hitEnemy(echo.enemy,Math.max(0,Math.min(echo.power,echo.enemy.hp-echo.minHp)),echo.dir,true);it.remove();}}
        for(Iterator<Drop> it=drops.iterator();it.hasNext();){
            Drop drop=it.next();drop.age+=dt;float old=drop.y;drop.vy=Math.max(-400,drop.vy-650*dt);drop.y+=drop.vy*dt;
            for(Block block:blocks)if(drop.vy<=0&&drop.x+12>block.x&&drop.x-12<block.x+block.w&&old-12>=block.y+block.h-.5f&&drop.y-12<=block.y+block.h){drop.y=block.y+block.h+12;drop.vy=0;}
            if(drop.age>.4f&&overlap(x-20,y-10,PLAYER_W+40,PLAYER_H+20,drop.x-14,drop.y-14,28,28)){collectItem(drop.item);it.remove();}
            else if(drop.y< -180)it.remove();
        }
    }
    public enum Element {
        NORMAL("노말",1,0xA8A77A),FIRE("불꽃",10,0xEE8130),DRAGON("드래곤",16,0x6F35FC),
        FIGHTING("격투",2,0xC22E28),GROUND("땅",5,0xE2BF65),ROCK("바위",6,0xB6A136),
        GHOST("고스트",8,0x735797),STEEL("강철",9,0xB7B7CE),ELECTRIC("전기",13,0xF7D02C),
        PSYCHIC("에스퍼",14,0xF95587),DARK("악",17,0x705746),POISON("독",4,0xA33EA1),
        FLYING("비행",3,0xA98FF3),BUG("벌레",7,0xA6B91A),WATER("물",11,0x6390F0),
        GRASS("풀",12,0x7AC74C),ICE("얼음",15,0x96D9D6),FAIRY("페어리",18,0xD685AD);
        public final String title;public final int typeId,rgb;
        Element(String title,int typeId,int rgb){this.title=title;this.typeId=typeId;this.rgb=rgb;}
    }
    public enum Category {STATUS("변화"),PHYSICAL("물리"),SPECIAL("특수");public final String title;Category(String title){this.title=title;}}
    public static final String[] SLOT_KEYS={"X","C","A","S"};
    public enum Skill {
        EMBER("불꽃세례",4,1.50f,40,Element.FIRE,Category.SPECIAL,false),
        SMOKE("연막",8,6.00f,0,Element.NORMAL,Category.STATUS,false),
        DRAGON("용의숨결",12,2.60f,60,Element.DRAGON,Category.SPECIAL,false),
        FANG("불꽃엄니",17,2.42f,65,Element.FIRE,Category.PHYSICAL,true),
        SCRATCH("할퀴기",1,1.50f,40,Element.NORMAL,Category.PHYSICAL,false),
        GROWL("울음소리",1,4.00f,0,Element.NORMAL,Category.STATUS,false),
        SLASH("베어가르기",20,2.75f,70,Element.NORMAL,Category.PHYSICAL,false),
        FLAMETHROWER("화염방사",24,3.35f,90,Element.FIRE,Category.SPECIAL,true),
        SCARY_FACE("겁나는얼굴",28,6.00f,0,Element.NORMAL,Category.STATUS,true),
        FIRE_SPIN("회오리불꽃",32,1.38f,35,Element.FIRE,Category.SPECIAL,true),
        INFERNO("연옥",36,3.00f,100,Element.FIRE,Category.SPECIAL,false),
        FLARE_BLITZ("플레어드라이브",40,3.10f,120,Element.FIRE,Category.PHYSICAL,true),
        FIRE_PUNCH("불꽃펀치",0,2.38f,75,Element.FIRE,Category.PHYSICAL,true),
        THUNDER_PUNCH("번개펀치",0,2.38f,75,Element.ELECTRIC,Category.PHYSICAL,true),
        SWORDS_DANCE("칼춤",0,9.00f,0,Element.NORMAL,Category.STATUS,true),
        BODY_SLAM("누르기",0,2.62f,85,Element.NORMAL,Category.PHYSICAL,true),
        TAKE_DOWN("돌진",0,2.35f,90,Element.NORMAL,Category.PHYSICAL,true),
        ROAR("울부짖기",0,6.00f,0,Element.NORMAL,Category.STATUS,true),
        DIG("구멍파기",0,3.00f,80,Element.GROUND,Category.PHYSICAL,true),
        FIRE_BLAST("불대문자",0,3.25f,110,Element.FIRE,Category.SPECIAL,true),
        SWIFT("스피드스타",0,2.00f,60,Element.NORMAL,Category.SPECIAL,true),
        REST("잠자기",0,12.00f,0,Element.PSYCHIC,Category.STATUS,true),
        ROCK_SLIDE("스톤샤워",0,2.38f,75,Element.ROCK,Category.PHYSICAL,true),
        SUBSTITUTE("대타출동",0,10.00f,0,Element.NORMAL,Category.STATUS,true),
        PROTECT("방어",0,9.00f,0,Element.NORMAL,Category.STATUS,true),
        OUTRAGE("역린",0,3.50f,120,Element.DRAGON,Category.PHYSICAL,true),
        ENDURE("버티기",0,9.00f,0,Element.NORMAL,Category.STATUS,true),
        FALSE_SWIPE("칼등치기",0,1.50f,40,Element.NORMAL,Category.PHYSICAL,true),
        SLEEP_TALK("잠꼬대",0,5.00f,0,Element.NORMAL,Category.STATUS,true),
        METAL_CLAW("메탈클로",0,1.75f,50,Element.STEEL,Category.PHYSICAL,true),
        SUNNY_DAY("쾌청",0,10.00f,0,Element.FIRE,Category.STATUS,true),
        CRUNCH("깨물어부수기",0,2.50f,80,Element.DARK,Category.PHYSICAL,true),
        HEAT_WAVE("열풍",0,3.48f,95,Element.FIRE,Category.SPECIAL,true),
        WILL_O_WISP("도깨비불",0,6.00f,0,Element.FIRE,Category.STATUS,true),
        FACADE("객기",0,2.25f,70,Element.NORMAL,Category.PHYSICAL,true),
        FOCUS_PUNCH("힘껏펀치",0,5.25f,150,Element.FIGHTING,Category.PHYSICAL,true),
        HELPING_HAND("도우미",0,8.00f,0,Element.NORMAL,Category.STATUS,true),
        BRICK_BREAK("깨뜨리다",0,2.38f,75,Element.FIGHTING,Category.PHYSICAL,true),
        WEATHER_BALL("웨더볼",0,1.75f,50,Element.NORMAL,Category.SPECIAL,true),
        OVERHEAT("오버히트",0,3.75f,130,Element.FIRE,Category.SPECIAL,true),
        ROCK_TOMB("암석봉인",0,2.00f,60,Element.ROCK,Category.PHYSICAL,true),
        DRAGON_CLAW("드래곤클로",0,2.50f,80,Element.DRAGON,Category.PHYSICAL,true),
        DRAGON_DANCE("용의춤",0,10.00f,0,Element.DRAGON,Category.STATUS,true),
        FLING("내던지기",0,1.50f,0,Element.DARK,Category.PHYSICAL,true),
        DRAGON_PULSE("용의파동",0,3.23f,85,Element.DRAGON,Category.SPECIAL,true),
        FOCUS_BLAST("기합구슬",0,4.10f,120,Element.FIGHTING,Category.SPECIAL,true),
        SHADOW_CLAW("섀도클로",0,2.75f,70,Element.GHOST,Category.PHYSICAL,true),
        FLAME_CHARGE("니트로차지",0,2.95f,50,Element.FIRE,Category.PHYSICAL,true),
        FIRE_PLEDGE("불꽃의맹세",0,2.50f,80,Element.FIRE,Category.SPECIAL,true),
        DRAGON_TAIL("드래곤테일",0,2.00f,60,Element.DRAGON,Category.PHYSICAL,true),
        BREAKING_SWIPE("와이드브레이커",0,2.00f,60,Element.DRAGON,Category.PHYSICAL,true),
        TERA_BLAST("테라버스트",0,2.50f,80,Element.NORMAL,Category.SPECIAL,true),
        TEMPER_FLARE("열불내기",0,2.38f,75,Element.FIRE,Category.PHYSICAL,true);
        public final String name,key;public final int unlockLevel,power;public final float cooldown;public final Element element;public final Category category;public final boolean tm;
        Skill(String name,int level,float cooldown,int power,Element element,Category category,boolean tm){this.name=name;this.unlockLevel=level;this.cooldown=cooldown;this.power=power;this.element=element;this.category=category;this.tm=tm;key=ordinal()<4?SLOT_KEYS[ordinal()]:"";}
        public boolean ranged(){return category==Category.SPECIAL||this==ROCK_SLIDE||this==ROCK_TOMB||this==FLING;}
        public boolean piercing(){return this==DRAGON||this==FLAMETHROWER||this==HEAT_WAVE||this==DRAGON_PULSE||this==FOCUS_BLAST;}
        public String description(){return MoveEffects.description(this);}
    }
    private final Skill[] equipped=new Skill[4],entrySkills=new Skill[4];
    private final java.util.ArrayDeque<Skill> learnQueue=new java.util.ArrayDeque<>();
    private Skill tmCandidate,castingSkill;
    public boolean tmSelecting;
    public int attackStage,specialAttackStage,speedStage;
    public float statTime;
    private CastRecord lastAttack;
    private Dash dash;
    private static final class CastRecord {boolean hit,finished;}
    private static final class Dash {Skill move;Element element;int power,dir,maxHpAtStart;float time=.28f,speed=650,recoil;boolean userEffect;CastRecord record;final java.util.Set<Enemy> hit=new java.util.HashSet<>();}
    public boolean dashing(){return dash!=null;}
    public boolean dashInvulnerable(){return dash!=null||(skillTime>0&&!skillReleased&&castingSkill!=null&&MoveGeometry.dash(castingSkill));}
    public Skill dashSkill(){return dash==null?null:dash.move;}
    public static float statMultiplier(int stage){stage=Math.max(-6,Math.min(6,stage));return stage>=0?(2+stage)/2f:2f/(2-stage);}
    public PokemonStats.Values stats(){return PokemonStats.at(evolution==0?PokemonStats.Species.CHARMANDER:evolution==1?PokemonStats.Species.CHARMELEON:PokemonStats.Species.CHARIZARD,level);}
    public int referenceDefense(){return PokemonStats.at(PokemonStats.Species.RATTATA,level).defense;}
    public float movementSpeed(){return Math.min(360,230+stats().speed*1.4f)*(1+.15f*speedStage);}
    public int enemyDamage(Enemy enemy,boolean special,int power){
        PokemonStats.Values a=enemy.stats();
        float offense=(special?a.specialAttack:a.attack)*statMultiplier(special?0:enemy.attackStage);
        // Heart damage depends on enemy offense, never on player HP/defense stats.
        return Math.max(1,(int)Math.ceil(power*offense/1400f));
    }
    public int damageAgainst(Skill move,Enemy enemy){int n=move==null?attackDamage():skillDamage(move);if(enemy.boss)n=Math.round(n*(1+.2f*itemCount(Item.BELT)));boolean physical=move==null||move.category==Category.PHYSICAL;return matchupDamage(Math.max(1,Math.round(n*referenceDefense()/enemy.defense(physical))),move,enemy);}

    public Element skillElement(Skill move){return move==Skill.WEATHER_BALL&&fireBuff>0?Element.FIRE:move.element;}
    private boolean roll(int chance){return chance>=100||chance>0&&combatRandom.nextInt(100)<chance;}
    private void userStat(int stat,int change){
        if(stat==2)attackStage=Math.max(-6,Math.min(6,attackStage+change));
        if(stat==4)specialAttackStage=Math.max(-6,Math.min(6,specialAttackStage+change));
        if(stat==6)speedStage=Math.max(-6,Math.min(6,speedStage+change));
        statTime=10;effects.add(new Effect(x+22,y+45,2,1));
    }
    private void enemyStat(Enemy e,int stat,int change){
        if(stat==2){e.attackStage=Math.max(-6,Math.min(6,e.attackStage+change));e.weakened=6;}
        if(stat==3)e.defenseStage=Math.max(-6,Math.min(6,e.defenseStage+change));
        if(stat==5)e.specialDefenseStage=Math.max(-6,Math.min(6,e.specialDefenseStage+change));
        if(stat==6)e.speedStage=Math.max(-6,Math.min(6,e.speedStage+change));
        e.statTime=6;effects.add(new Effect(e.x+24,e.y+40,2,1));
    }
    public float physicalBuff,speedBuff,fireBuff,helpBuff,specialPenalty,sleepTime,endureTime;
    public Skill skillAt(int slot){return slot>=0&&slot<4?equipped[slot]:null;}
    public Skill castingSkill(){return castingSkill;}
    public boolean knows(Skill skill){for(Skill move:equipped)if(move==skill)return true;return false;}
    public Skill pendingSkill(){return tmCandidate!=null?tmCandidate:learnQueue.peek();}
    public boolean choosingSkill(){return hp>0&&!cleared&&(tmSelecting||pendingSkill()!=null);}
    public java.util.List<Skill> tmOptions(){java.util.List<Skill> list=new ArrayList<>();for(Skill skill:Skill.values())if(skill.tm&&!knows(skill))list.add(skill);return list;}
    public boolean openTm(){if(paused||hp<=0||cleared||stageComplete||choosingSkill()||itemCount(Item.TM)<=0)return false;tmSelecting=true;return true;}
    public boolean chooseTm(Skill skill){if(hp<=0||!tmSelecting||skill==null||!skill.tm||knows(skill)||itemCount(Item.TM)<=0)return false;tmSelecting=false;tmCandidate=skill;return true;}
    public boolean learnInto(int slot){
        Skill move=pendingSkill();if(!choosingSkill()||move==null||slot<0||slot>=4||knows(move))return false;
        equipped[slot]=move;skillCooldowns[slot]=0;
        if(tmCandidate!=null){inventory[Item.TM.ordinal()]--;tmCandidate=null;}else learnQueue.remove();
        message="기술 습득! ["+SLOT_KEYS[slot]+"] "+move.name;messageTime=4;return true;
    }
    public void skipLearning(){if(tmSelecting){tmSelecting=false;return;}if(tmCandidate!=null){tmCandidate=null;return;}if(!learnQueue.isEmpty())learnQueue.remove();}
    private void offerLevelSkill(int reached){for(Skill move:Skill.values())if(move.unlockLevel==reached&&reached>1&&!knows(move)&&!learnQueue.contains(move))learnQueue.add(move);}
    public int skillDamage(Skill move){
        if(move==null||move.category==Category.STATUS)return 0;
        int power=move==Skill.WEATHER_BALL&&fireBuff>0?100:move.power==0?40:move.power;
        PokemonStats.Values values=stats();
        int base=PokemonStats.damage(level,power,move.category==Category.PHYSICAL?values.attack:values.specialAttack,referenceDefense());
        Element element=skillElement(move);
        if(element==Element.FIRE)base=Math.round(base*(1+.2f*itemCount(Item.CHARCOAL))*(fireBuff>0?1.5f:1));
        if(element==Element.DRAGON)base=Math.round(base*(1+.2f*itemCount(Item.FANG)));
        if(element==Element.NORMAL)base=Math.round(base*(1+.2f*itemCount(Item.SCARF)));
        base=categoryDamage(base,move.category==Category.PHYSICAL);
        return Math.max(1,base);
    }
    public final float[] skillCooldowns=new float[4];
    public final List<SkillShot> skillShots=new ArrayList<>();
    public final List<Smoke> smokeClouds=new ArrayList<>();
    public final List<SkillVisual.Burst> skillVisuals=new ArrayList<>();
    public int activeSkill=-1;
    public float skillTime;
    private boolean skillReleased;
    private int skillDirection;
    public static final float CAST_DURATION=.45f;
    public float castDuration(){return castingSkill==Skill.FOCUS_PUNCH?.9f:CAST_DURATION;}
    public static final class SkillShot {
        public float x,y,life,vx,vy;public final int type,power;public final Skill move;public final Element element;public boolean userEffect;private CastRecord record;
        public final java.util.Set<Enemy> hit=new java.util.HashSet<>();
        public SkillShot(float x,float y,int dir,int type,int power){this(x,y,dir,type==0?Skill.EMBER:Skill.DRAGON,power);}
        public SkillShot(float x,float y,int dir,Skill move,int power){this(x,y,dir,move,power,move.element);}
        public SkillShot(float x,float y,int dir,Skill move,int power,Element element){this.x=x;this.y=y;this.move=move;this.element=element;this.type=element==Element.FIRE?0:2;this.power=power;vx=dir*(type==0?460:600);
            switch(SkillVisual.style(move)){
            case STREAM:vx=dir*620;break;
            case GLYPH:vx=dir*320;break;
            case VORTEX:vx=dir*260;break;
            case WAVE:vx=dir*380;break;
            case ERUPTION:vx=dir*420;break;
            case ROCK:vx=dir*390;break;
            default:break;
            }
            life=MoveGeometry.projectileRange(move)/Math.abs(vx);}
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
        public float x,y,vx,vy,hurtTime,phaseTime,walkClock,timer=.8f,burnTime,burnTick,slowTime,smoked,weakened;
        public final float home,homeY;
        public final boolean spitter;
        public int hp=65,maxHp=65,reward=25,lastHit=-1,dir=-1;
        public int level=2;
        public float dotRemainder;
        public PokemonStats.Species species;
        public PokemonStats.Values stats(){return PokemonStats.at(species,level);}
        public float defense(boolean physical){PokemonStats.Values d=stats();return (physical?d.defense:d.specialDefense)*(boss?BOSS_DEFENSE_MULTIPLIER:1)*statMultiplier(physical?defenseStage:specialDefenseStage);}
        public Element primaryType,secondaryType;
        public int attackStage,defenseStage,specialDefenseStage,speedStage;
        public float statTime,paralysisTime,flinchTime,trapTime,trapTick;
        public float matchupTime,lastEffectiveness=1;public int lastDamage;public String combatNote="";
        public String typeLabel(){return primaryType.title+(secondaryType==null||secondaryType==primaryType?"":" / "+secondaryType.title);}
        public String weaknesses(){StringBuilder result=new StringBuilder();for(Element element:Element.values()){float factor=TypeChart.multiplier(element,primaryType,secondaryType);if(factor>1){if(result.length()>0)result.append(" / ");result.append(element.title);if(factor==4)result.append(" 4배");}}return result.toString();}
        public boolean boss;public int cycle,bursts,pattern;public float targetX,jumpStartX;public float width(){return boss?82:48;}public float height(){return boss?105:64;}
        public Phase phase=Phase.PATROL;
        public Enemy(float x,float y,boolean spitter) { this.x=home=x;this.y=homeY=y;this.spitter=spitter;species=spitter?PokemonStats.Species.EKANS:PokemonStats.Species.RATTATA;primaryType=spitter?Element.POISON:Element.NORMAL; }
        public boolean alive() { return hp>0; }
    }
    public static final class Shot {
        public float x,y,vx,vy,life=3;public int damage=SPIT_DAMAGE;public Element element=Element.POISON;
        public Shot(float x,float y,float vx) { this.x=x;this.y=y;this.vx=vx; }
    }
    public GameWorld() { restart(); }
    private void ground(float x,float width) { blocks.add(new Block(x,0,width,FLOOR)); }
    private void bridge(float x,float top,float width) { blocks.add(new Block(x,top-24,width,24,true)); }
    public void restart() {
        stage=1;mapIndex=1;totalKills=0;level=1;candyLevels=0;xp=evolution=0;maxHp=hp=6;elapsed=0;
        java.util.Arrays.fill(inventory,0);java.util.Arrays.fill(equipped,null);equipped[0]=Skill.SCRATCH;equipped[1]=Skill.GROWL;learnQueue.clear();tmCandidate=null;tmSelecting=false;
        loadStage();
    }
    private void loadStage() {
        dash=null;lastAttack=null;attackStage=specialAttackStage=speedStage=0;statTime=0;
        System.arraycopy(equipped,0,entrySkills,0,4);physicalBuff=speedBuff=fireBuff=helpBuff=specialPenalty=sleepTime=endureTime=0;castingSkill=null;
        System.arraycopy(inventory,0,entryInventory,0,inventory.length);drops.clear();echoes.clear();healClock=drainCharge=dropThroughTime=0;itemNoticeTime=0;
        entryLevel=level;entryCandyLevels=candyLevels;entryXp=xp;entryEvolution=evolution;entryMaxHp=maxHp;entryKills=totalKills;
        bossActive=false;boss=null;eruptions.clear();bossHazards.clear();
        x=checkpoint=100;y=FLOOR;vx=vy=0;facing=1;grounded=true;
        kills=attackSerial=0;attackTime=attackCooldown=0;invulnerable=1;
        paused=cleared=stageComplete=false;transitionTime=0;rockSpawnTimer=3;
        skillShots.clear();skillVisuals.clear();smokeClouds.clear();rockFalls.clear();java.util.Arrays.fill(skillCooldowns,0);activeSkill=-1;skillTime=0;skillReleased=false;
        enemies.clear();shots.clear();effects.clear();walkClock=hurtTime=landingTime=hitStop=shake=dustClock=0;coyote=.1f;
        blocks.clear();spikes.clear();
        MapLayout layout=MapLayout.create(stage,mapIndex);blocks.addAll(layout.blocks);spikes.addAll(layout.spikes);roomCheckpoints=layout.checkpoints;
        if(!isBossRoom()) {
            if(stage==1&&mapIndex==1)invulnerable=0;
            for(MapLayout.Spawn spawn:layout.enemies)spawn(spawn.x,spawn.y,spawn.spitter);
            message=stageName()+" / "+mapTitle()+": 출구까지 이동하세요!";
        }else{
            boss=new Enemy(920,FLOOR,stage!=1);boss.boss=true;boss.primaryType=stage==1?Element.NORMAL:stage==2?Element.POISON:Element.FIRE;boss.species=stage==1?PokemonStats.Species.RATICATE:stage==2?PokemonStats.Species.ARBOK:PokemonStats.Species.MAGMAR;boss.level=enemyLevel();boss.hp=boss.maxHp=enemyMaxHp(boss);
            boss.reward=stage==1?600:stage==2?1000:1400;enemies.add(boss);
            message="보스의 방 / "+bossName()+": 보스를 쓰러뜨리면 출구가 열립니다.";
        }
        messageTime=6;
    }
    private void spawn(float x,float y,boolean spitter) {
        Enemy e=new Enemy(x,y,spitter);e.level=enemyLevel();e.maxHp=e.hp=enemyMaxHp(e);e.reward=stage==1?25:stage==2?40:55;enemies.add(e);
    }
    public boolean advanceStage() {
        if(choosingSkill()||!stageComplete||(stage==3&&isBossRoom())||paused||hp<=0)return false;
        if(mapIndex<5)mapIndex++;else{stage++;mapIndex=1;}hp=maxHp;loadStage();return true;
    }
    public String nextAreaLabel(){return mapIndex<5?stage+"-"+(mapIndex+1)+" / "+MapLayout.title(mapIndex+1):(stage+1)+"-1 / "+(stage==1?"수정 동굴":"불꽃 화산");}
    public String bossName(){return stage==1?"레트라":stage==2?"아보크":"마그마";}
    public boolean bossDefeated(){return !isBossRoom()||(boss!=null&&!boss.alive());}
    public boolean retryStage(){if(hp>0||cleared)return false;level=entryLevel;candyLevels=entryCandyLevels;xp=entryXp;evolution=entryEvolution;maxHp=entryMaxHp;hp=maxHp;totalKills=entryKills;System.arraycopy(entryInventory,0,inventory,0,inventory.length);System.arraycopy(entrySkills,0,equipped,0,4);learnQueue.clear();tmCandidate=null;tmSelecting=false;loadStage();return true;}
    public int xpRequired() { return 40+(enemyLevel()-1)*10; }
    public int attackDamage() { return categoryDamage(Math.round(PokemonStats.damage(level,35,stats().attack,referenceDefense())*(1+.2f*itemCount(Item.SCARF))),true); }
    public String species() { return evolution==0?"파이리":evolution==1?"리자드":"리자몽"; }
    public void addExperience(int amount) {
        xp+=amount;
        while(xp>=xpRequired()) {
            xp-=xpRequired();level++;effects.add(new Effect(x+22,y+45,2,1));
            message="레벨 상승! Lv."+level+" / 공격력 증가";messageTime=3;
            offerLevelSkill(level);
        }
        finishGrowth();
    }
    private void finishGrowth(){
        int next=level>=36?2:level>=16?1:0;
        if(next>evolution) {
            evolution=next;maxHp=6+evolution*2;hp=maxHp;
            message="진화! "+species()+" / "+(maxHp/2)+"칸 하트";messageTime=5;
        }
        syncEnemyLevels();
    }
    public float attackX() { return facing>0?x+PLAYER_W-8:x-80; }
    public boolean attacking() { return attackTime>.10f&&attackTime<.20f; }
    public void damage(int halfHearts,float sourceX) {
        if(invulnerable>0||dashInvulnerable()||hp<=0||cleared||stageComplete||choosingSkill()) return;
        if(halfHearts>=hp&&endureTime>0)halfHearts=hp-1;
        boolean saved=halfHearts>=hp&&itemCount(Item.SASH)>0;
        if(saved){inventory[Item.SASH.ordinal()]--;hp=1;itemNotice="기합의띠 발동! 하트 반 칸으로 생존";itemNoticeTime=4;}else hp=Math.max(0,hp-halfHearts);invulnerable=1.1f;attackTime=0;skillTime=0;activeSkill=-1;hurtTime=.32f;hitStop=.055f;shake=.2f;
        effects.add(new Effect(x+22,y+38,1,x<sourceX?-1:1));
        vx=x<sourceX?-170:170;vy=250;grounded=false;
        message="피격! 하트 -"+(halfHearts/2f)+"칸";messageTime=1.8f;
        if(hp==0) message="쓰러졌습니다! Enter: 재도전 / R: 처음부터";
    }
    public boolean dropThrough(){
        if(!grounded||paused||hp<=0||cleared||stageComplete||choosingSkill())return false;
        for(Block b:blocks)if(b.oneWay&&Math.abs(y-(b.y+b.h))<1&&x+PLAYER_W-FOOT_INSET>b.x&&x+FOOT_INSET<b.x+b.w){
            dropThroughTop=y;dropThroughTime=.3f;y-=2;vy=-100;grounded=false;coyote=0;return true;
        }return false;
    }
    public void step(float dt,float move,boolean jump,boolean attack) {
        if(paused||hp<=0||cleared||choosingSkill()) return;
        dt=Math.min(Math.max(dt,0),1f/30f);if(dt==0)return;
        for(Iterator<SkillVisual.Burst> it=skillVisuals.iterator();it.hasNext();){SkillVisual.Burst b=it.next();b.age+=dt;if(b.age>=b.life)it.remove();}
        if(stageComplete){transitionTime=Math.max(0,transitionTime-dt);if(transitionTime<=0)advanceStage();return;}
        for(Iterator<Effect> it=effects.iterator();it.hasNext();){Effect fx=it.next();fx.age+=dt;if(fx.age>=fx.life)it.remove();}
        shake=Math.max(0,shake-dt);
        if(hitStop>0){hitStop=Math.max(0,hitStop-dt);return;}
        hurtTime=Math.max(0,hurtTime-dt);landingTime=Math.max(0,landingTime-dt);
        if(grounded)coyote=.09f;else coyote=Math.max(0,coyote-dt);
        dropThroughTime=Math.max(0,dropThroughTime-dt);
        updateItems(dt);
        if(choosingSkill())return;
        physicalBuff=Math.max(0,physicalBuff-dt);speedBuff=Math.max(0,speedBuff-dt);fireBuff=Math.max(0,fireBuff-dt);helpBuff=Math.max(0,helpBuff-dt);specialPenalty=Math.max(0,specialPenalty-dt);sleepTime=Math.max(0,sleepTime-dt);endureTime=Math.max(0,endureTime-dt);
        statTime=Math.max(0,statTime-dt);if(statTime<=0)attackStage=specialAttackStage=speedStage=0;
        if(sleepTime>0){move=0;jump=false;attack=false;}
        updateSkills(dt);
        elapsed+=dt;messageTime=Math.max(0,messageTime-dt);
        attackTime=Math.max(0,attackTime-dt);attackCooldown=Math.max(0,attackCooldown-dt);invulnerable=Math.max(0,invulnerable-dt);
        if(hurtTime<.09f) {
            float target=Math.max(-1,Math.min(1,move))*movementSpeed();
            float accel=move==0?(grounded?3200:1300):(grounded?2400:1700);
            if(vx*target<0)accel=3400;
            vx=approach(vx,target,accel*dt);
        }
        if(dash!=null){vx=dash.dir*dash.speed;move=0;jump=false;attack=false;}
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
            finishDash();skillTime=0;activeSkill=-1;castingSkill=null;skillReleased=true;
            // A fall always costs a heart, even if a previous hit's grace period is active.
            invulnerable=0;damage(TRAP_DAMAGE,x+1);x=checkpoint;y=FLOOR+8;vx=vy=0;
            message=hp>0?"협곡에 떨어졌습니다! 최근 체크포인트로 돌아갑니다.":"쓰러졌습니다! Enter: 재도전 / R: 처음부터";messageTime=3;
        }
        for(Spike s:spikes) if(overlap(x+5,y,PLAYER_W-10,PLAYER_H,s.x,s.y,s.w,19))damage(TRAP_DAMAGE,s.x+s.w/2);
        if(hp<=0)return;
        updateDash(dt,previousX);if(hp<=0||choosingSkill())return;
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
        updateBossHazards(dt);
        if(choosingSkill())return;
        if(x>=exitX()&&hp>0&&bossDefeated()) {
            finishDash();if(hp<=0)return;
            if(stage<3||!isBossRoom()){stageComplete=true;transitionTime=1.4f;message=mapTitle()+" 완료!";}
            else {cleared=true;message="모험 완료! 세 보스를 모두 쓰러뜨렸습니다!";}
        }
    }
    private void updateEnemy(Enemy e,float dt) {
        e.hurtTime=Math.max(0,e.hurtTime-dt);e.matchupTime=Math.max(0,e.matchupTime-dt);e.weakened=Math.max(0,e.weakened-dt);
        e.statTime=Math.max(0,e.statTime-dt);if(e.statTime<=0)e.attackStage=e.defenseStage=e.specialDefenseStage=e.speedStage=0;
        e.paralysisTime=Math.max(0,e.paralysisTime-dt);e.flinchTime=Math.max(0,e.flinchTime-dt);
        if(e.trapTime>0){e.trapTime=Math.max(0,e.trapTime-dt);e.trapTick-=dt;if(e.trapTick<=0){e.trapTick=1;periodicHit(e);if(!e.alive())return;}}
        e.slowTime=Math.max(0,e.slowTime-dt);e.smoked=Math.max(0,e.smoked-dt);
        if(e.burnTime>0){e.burnTime=Math.max(0,e.burnTime-dt);e.burnTick-=dt;if(e.burnTick<=0){e.burnTick=.75f;periodicHit(e);if(!e.alive())return;}}
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
        if(e.flinchTime>0){e.vx=0;return;}
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
                if(e.spitter){Shot shot=new Shot(e.x+24+e.dir*32,e.y+35,e.dir*330);shot.damage=enemyDamage(e,true,55);shots.add(shot);}
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
        e.vx*=Math.max(.4f,1+.15f*e.speedStage);if(e.slowTime>0||e.paralysisTime>0)e.vx*=.55f;if(e.trapTime>0)e.vx*=.6f;if(e.smoked>0)e.vx*=.5f;
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
        if(overlap(x,y,PLAYER_W,PLAYER_H,e.x,e.y,e.width(),e.height()))damage(enemyDamage(e,false,contact==TACKLE_DAMAGE?65:30),e.x+24);
    }
    public boolean skillUnlocked(int index){return skillAt(index)!=null;}
    public boolean trySkill(int index) {
        Skill skill=skillAt(index);
        if(skill==null||paused||hp<=0||cleared||stageComplete||choosingSkill()||hitStop>0||hurtTime>0||skillTime>0||dash!=null||sleepTime>0&&skill!=Skill.SLEEP_TALK)return false;
        if(skill==Skill.SLEEP_TALK&&sleepTime<=0){message="잠꼬대는 잠든 동안 사용할 수 있습니다.";messageTime=2;return false;}
        if(skill==Skill.SUBSTITUTE&&hp<=2){message="대타출동에 필요한 하트가 부족합니다.";messageTime=2;return false;}
        if(skillCooldowns[index]>0)return false;
        attackTime=0;skillCooldowns[index]=skill.cooldown*cooldownScale();activeSkill=index;castingSkill=skill;
        skillTime=castDuration();skillReleased=false;skillDirection=facing;attackCooldown=Math.max(attackCooldown,castDuration());
        message=skill.name+"!";messageTime=1.4f;return true;
    }
    private void updateSkills(float dt) {
        for(int i=0;i<4;i++)skillCooldowns[i]=Math.max(0,skillCooldowns[i]-dt);
        for(Iterator<Smoke> it=smokeClouds.iterator();it.hasNext();){Smoke cloud=it.next();cloud.life-=dt;if(cloud.life<=0)it.remove();}
        if(skillTime>0){skillTime=Math.max(0,skillTime-dt);if(skillTime<=castDuration()*.6f&&!skillReleased){skillReleased=true;releaseSkill(castingSkill);}if(skillTime<=0){activeSkill=-1;castingSkill=null;}}
    }
    private void releaseSkill(Skill move){
        if(move==null)return;
        skillVisuals.add(new SkillVisual.Burst(move,x+22,y+38,skillDirection,false));
        if(move.category==Category.STATUS){
            switch(move){
            case SMOKE:smokeClouds.add(new Smoke(x+22+skillDirection*80,y+35));break;
            case SWORDS_DANCE:userStat(2,2);break;
            case DRAGON_DANCE:userStat(2,1);userStat(6,1);break;
            case SUNNY_DAY:fireBuff=10;break;
            case HELPING_HAND:helpBuff=8;break;
            case PROTECT:invulnerable=Math.max(invulnerable,2);break;
            case SUBSTITUTE:hp-=2;invulnerable=Math.max(invulnerable,3);break;
            case ENDURE:endureTime=4;break;
            case REST:hp=maxHp;sleepTime=2.5f;break;
            case SLEEP_TALK:
                java.util.List<Skill> attacks=new ArrayList<>();for(Skill skill:equipped)if(skill!=null&&skill.category!=Category.STATUS)attacks.add(skill);
                if(!attacks.isEmpty())releaseSkill(attacks.get(combatRandom.nextInt(attacks.size())));break;
            default:
                for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&Math.abs(e.x-x)<450&&Math.abs(e.y-y)<160){
                    if(move==Skill.GROWL)enemyStat(e,2,-1);
                    else if(move==Skill.WILL_O_WISP){if(e.primaryType!=Element.FIRE&&e.secondaryType!=Element.FIRE){e.burnTime=6;e.burnTick=.75f;}}
                    else if(move==Skill.SCARY_FACE)enemyStat(e,6,-2);else e.slowTime=6;
                    if(move==Skill.ROAR&&!e.boss){e.phase=Phase.RECOVER;e.timer=1.2f;e.x=Math.max(e.home-65,Math.min(e.home+65,e.x+skillDirection*45));}
                    effects.add(new Effect(e.x+24,e.y+35,2,skillDirection));
                }break;
            }
            effects.add(new Effect(x+22,y+40,2,skillDirection));return;
        }
        int power=skillDamage(move);
        if(move==Skill.FACADE&&hp<maxHp)power*=2;
        if(move==Skill.TEMPER_FLARE&&lastAttack!=null&&lastAttack.finished&&!lastAttack.hit)power*=2;
        CastRecord record=new CastRecord();lastAttack=record;Element element=skillElement(move);
        if(move==Skill.FLARE_BLITZ||move==Skill.FLAME_CHARGE||move==Skill.TAKE_DOWN){
            dash=new Dash();dash.move=move;dash.element=element;dash.power=power;dash.dir=skillDirection;dash.maxHpAtStart=maxHp;dash.record=record;dash.time=MoveGeometry.dashDuration(move);dash.speed=MoveGeometry.dashSpeed(move);return;
        }
        if(move.ranged()){SkillShot shot=new SkillShot(x+22+skillDirection*35,y+38,skillDirection,move,power,element);shot.record=record;skillShots.add(shot);}
        else{
            boolean userEffect=false;
            // Skill-specific silhouette is emitted on release, with a separate hit burst.
            for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&MoveGeometry.hits(move,x,y,skillDirection,e)){
                int hit=e.phase==Phase.RECOVER?Math.round(power*1.7f):power;
                playerHit(e,hit,skillDirection,move,element);
                if(TypeChart.multiplier(element,e.primaryType,e.secondaryType)>0){record.hit=true;userEffect=applySkillStatus(e,move,userEffect);}
            }
            record.finished=true;
        }
        if(move==Skill.DIG)invulnerable=Math.max(invulnerable,1.2f);
    }
    private boolean applySkillStatus(Enemy e,Skill move,boolean userEffect){
        MoveEffects.Profile profile=MoveEffects.profile(move);
        if(!userEffect){
            for(int i=0;i<profile.stats.length;i+=2)if(MoveEffects.selfStat(move,profile.stats[i+1])&&roll(profile.statChance))userStat(profile.stats[i],profile.stats[i+1]);
            userEffect=true;
        }
        if(!e.alive())return userEffect;
        if(profile.ailment==4&&e.primaryType!=Element.FIRE&&e.secondaryType!=Element.FIRE&&roll(profile.chance)){e.burnTime=move==Skill.INFERNO?6:3;e.burnTick=.75f;}
        if(profile.ailment==1&&e.primaryType!=Element.ELECTRIC&&e.secondaryType!=Element.ELECTRIC&&roll(profile.chance)){e.paralysisTime=4;e.slowTime=4;}
        if(profile.ailment==8){e.trapTime=4;e.trapTick=1;}
        if(roll(profile.flinch)){e.flinchTime=e.boss?.15f:.35f;e.vx=0;if(e.phase==Phase.WINDUP||e.phase==Phase.ATTACK){e.phase=Phase.RECOVER;e.timer=e.boss?.25f:.6f;}}
        for(int i=0;i<profile.stats.length;i+=2)if(!MoveEffects.selfStat(move,profile.stats[i+1])&&roll(profile.statChance))enemyStat(e,profile.stats[i],profile.stats[i+1]);
        if(move==Skill.DRAGON_TAIL&&!e.boss){e.x=Math.max(e.home-65,Math.min(e.home+65,e.x+skillDirection*45));e.phase=Phase.RECOVER;e.timer=.8f;}
        return userEffect;
    }
    private void updateDash(float dt,float oldX){
        if(dash==null)return;Dash current=dash;
        for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&!current.hit.contains(e)&&overlap(Math.min(oldX,x)-16,y+4,Math.abs(x-oldX)+PLAYER_W+32,PLAYER_H,e.x,e.y,e.width(),e.height())){
            current.hit.add(e);int damage=playerHit(e,current.power,current.dir,current.move,current.element);
            if(TypeChart.multiplier(current.element,e.primaryType,e.secondaryType)>0){current.record.hit=true;current.userEffect=applySkillStatus(e,current.move,current.userEffect);}
            float rate=current.move==Skill.FLARE_BLITZ?1f/3:current.move==Skill.TAKE_DOWN?.25f:0;
            current.recoil+=damage/(float)e.maxHp*current.maxHpAtStart*rate;
        }
        current.time-=dt;if(current.time<=0||vx==0)finishDash();
    }
    private void finishDash(){
        if(dash==null)return;Dash current=dash;dash=null;current.record.finished=true;vx=Math.max(-movementSpeed(),Math.min(movementSpeed(),vx));
        if(current.recoil>0){float rate=current.move==Skill.FLARE_BLITZ?1f/3:.25f;int cap=Math.max(1,(int)Math.floor(current.maxHpAtStart*rate));int cost=Math.min(cap,(int)Math.ceil(current.recoil));hp=Math.max(0,hp-cost);itemNotice="반동! 하트 "+(cost/2f)+"칸 감소";itemNoticeTime=2;if(hp==0){skillTime=0;activeSkill=-1;message="반동으로 쓰러졌습니다. Enter로 재도전";messageTime=4;}}
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
                if(target!=null){float dx=target.x+target.width()/2-shot.x,dy=target.y+target.height()/2-shot.y,len=(float)Math.sqrt(dx*dx+dy*dy),speed=(float)Math.sqrt(shot.vx*shot.vx+shot.vy*shot.vy);
                    if(len>1){shot.vx+=(dx/len*speed-shot.vx)*Math.min(1,dt*(10+3*(itemCount(Item.WIDE)-1)));shot.vy+=(dy/len*speed-shot.vy)*Math.min(1,dt*(10+3*(itemCount(Item.WIDE)-1)));}}
            }
            shot.x+=shot.vx*dt;shot.y+=shot.vy*dt;shot.life-=dt;
            boolean remove=shot.life<=0;
            for(Block b:blocks)if(!b.oneWay&&overlap(shot.x-12,shot.y-10,24,20,b.x,b.y,b.w,b.h))remove=true;
            if(!remove)for(Enemy e:enemies)if(e.alive()&&(!e.boss||bossActive)&&!shot.hit.contains(e)&&overlap(Math.min(old,shot.x)-SkillVisual.radius(shot.move),Math.min(oldY,shot.y)-SkillVisual.radius(shot.move),Math.abs(shot.x-old)+2*SkillVisual.radius(shot.move),Math.abs(shot.y-oldY)+2*SkillVisual.radius(shot.move),e.x,e.y,e.width(),e.height())) {
                shot.hit.add(e);playerHit(e,shot.power,shot.vx>0?1:-1,shot.move,shot.element);
                if(TypeChart.multiplier(shot.element,e.primaryType,e.secondaryType)>0){if(shot.record!=null)shot.record.hit=true;shot.userEffect=applySkillStatus(e,shot.move,shot.userEffect);}
                if(!shot.move.piercing()){remove=true;if(!MoveGeometry.areaProjectile(shot.move))break;}
            }
            if(remove){if(shot.record!=null)shot.record.finished=true;it.remove();}
        }
    }
    private void hitEnemy(Enemy e,int power,int direction,boolean impact) {
        if(!e.alive())return;
        e.hp=Math.max(0,e.hp-power);
        if(impact){e.hurtTime=.24f;hitStop=.035f;shake=.1f;effects.add(new Effect(e.x+24,e.y+35,1,direction));}
        if(!e.alive()) {
            if(e.boss){bossHazards.clear();shots.clear();eruptions.clear();}
            if(e.boss){for(int i=0;i<BOSS_DROP_COUNT;i++)drops.add(new Drop(rollBossItem(dropRandom.nextInt(BOSS_DROP_WEIGHT_TOTAL)),e.x+e.width()/2+(i-1)*40,e.y+30));}
            else if(normalDropOccurs(dropRandom.nextInt(100)))drops.add(new Drop(rollItem(dropRandom.nextInt(DROP_WEIGHT_TOTAL)),e.x+e.width()/2,e.y+30));
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
        int count=stage==3?5:3;if(e.hp<e.maxHp/2)count+=2;
        for(int i=0;i<count;i++){Shot shot=new Shot(e.x+e.width()/2+e.dir*45,e.y+44,e.dir*(stage==3?300:270));shot.vy=(i-(count-1)/2f)*(stage==3?65:85);shot.damage=enemyDamage(e,true,stage==3?75:60);shot.element=stage==3?Element.FIRE:Element.POISON;shots.add(shot);}
    }
    private void bossWave(Enemy e,BossHazardType type,float y,int dir,float warning){float h=type==BossHazardType.SHOCKWAVE?28:48;bossHazards.add(new BossHazard(type,e.x+e.width()/2,y,55,h,warning,3,dir*(type==BossHazardType.SHOCKWAVE?360:300),enemyDamage(e,type!=BossHazardType.SHOCKWAVE,65)));}
    private void bossTargets(Enemy e){
        float[] offsets=e.hp<e.maxHp/2?new float[]{-180,-90,0,90,180}:new float[]{-110,0,110};
        for(float offset:offsets){float target=Math.max(85,Math.min(1130,e.targetX+offset));
            if(stage==2)bossHazards.add(new BossHazard(BossHazardType.POISON_POOL,target,FLOOR,90,24,.85f,2.6f,0,enemyDamage(e,true,55)));
            else {bossHazards.add(new BossHazard(BossHazardType.FIRE_PILLAR,target,FLOOR,56,205,.8f+Math.abs(offset)/500, .7f,0,enemyDamage(e,true,85)));}
        }
    }
    private void updateBoss(Enemy e,float dt) {
        Phase previous=e.phase;boolean enraged=e.hp<e.maxHp/2;
        switch(e.phase) {
        case PATROL:
            e.vx=0;
            if(e.timer<=0){e.pattern=e.cycle%3;e.dir=x<e.x?-1:1;e.targetX=Math.max(85,Math.min(1110,x));e.jumpStartX=e.x;e.phase=Phase.WINDUP;e.timer=(stage==1?.8f:.95f)-(enraged?.15f:0);e.bursts=0;}
            break;
        case WINDUP:
            e.vx=0;
            if(e.timer<=0){e.phase=Phase.ATTACK;
                e.timer=stage==1?(e.pattern==2?.85f:.6f):e.pattern==2?.8f:1.05f;
                if(stage==1&&e.pattern==1){bossWave(e,BossHazardType.SHOCKWAVE,FLOOR,1,.15f);bossWave(e,BossHazardType.SHOCKWAVE,FLOOR,-1,.15f);e.bursts=1;}
                if(stage==2||stage==3){if(e.pattern==0){bossVolley(e);e.bursts=1;}else if(e.pattern==1)bossTargets(e);else if(stage==3){bossWave(e,BossHazardType.HEAT_WAVE,FLOOR,e.dir,.25f);e.bursts=1;}}
            }
            break;
        case ATTACK:
            if(stage==1){
                if(e.pattern==0){e.vx=approach(e.vx,e.dir*(enraged?610:530),3400*dt);e.x=Math.max(85,Math.min(1200-e.width(),e.x+e.vx*dt));}
                else if(e.pattern==2){float p=Math.max(0,Math.min(1,1-e.timer/.85f));e.x=e.jumpStartX+(e.targetX-e.jumpStartX)*p;e.y=FLOOR+(float)Math.sin(p*Math.PI)*175;if(e.timer<=0){e.y=FLOOR;bossWave(e,BossHazardType.SHOCKWAVE,FLOOR,1,.15f);bossWave(e,BossHazardType.SHOCKWAVE,FLOOR,-1,.15f);shake=.18f;}}
            }else if(e.pattern==0&&e.timer<.55f&&e.bursts==1){bossVolley(e);e.bursts=2;}
            else if(stage==2&&e.pattern==2){e.vx=approach(e.vx,e.dir*470,3000*dt);e.x=Math.max(85,Math.min(1200-e.width(),e.x+e.vx*dt));if(e.timer<=0)bossHazards.add(new BossHazard(BossHazardType.POISON_POOL,e.x,FLOOR,110,24,.45f,2.2f,0,enemyDamage(e,true,50)));}
            else if(stage==3&&e.pattern==2&&e.timer<.4f&&e.bursts==1){bossWave(e,BossHazardType.HEAT_WAVE,FLOOR+135,e.dir,.35f);e.bursts=2;}
            if(e.timer<=0){
                if(stage==1&&e.pattern==0&&e.bursts==0){e.bursts=1;e.phase=Phase.WINDUP;e.timer=.55f;e.dir=x<e.x?-1:1;}
                else{e.y=FLOOR;e.phase=Phase.RECOVER;e.timer=enraged?1.1f:1.5f;e.vx=0;e.cycle++;}
            }
            break;
        case RECOVER:
            e.y=FLOOR;e.vx=0;if(e.timer<=0){e.phase=Phase.PATROL;e.timer=.45f;}break;
        default:break;
        }
        if(e.phase!=previous)e.phaseTime=0;
        e.walkClock+=Math.abs(e.vx)/100f*dt;
        if(overlap(x,y,PLAYER_W,PLAYER_H,e.x,e.y,e.width(),e.height()))damage(enemyDamage(e,false,e.phase==Phase.ATTACK?80:35),e.x+e.width()/2);
    }
    private void updateBossHazards(float dt){
        if(boss==null||!boss.alive()){bossHazards.clear();return;}
        for(Iterator<BossHazard> it=bossHazards.iterator();it.hasNext();){BossHazard h=it.next();h.age+=dt;
            if(h.warning>0){h.warning=Math.max(0,h.warning-dt);continue;}
            float old=h.x;h.x+=h.vx*dt;h.life-=dt;if(h.life<=0||h.x< -120||h.x>1400){it.remove();continue;}
            if(overlap(Math.min(old,h.x),h.y,Math.abs(h.x-old)+h.w,h.h,x,y,PLAYER_W,PLAYER_H))damage(h.damage,h.x+h.w/2);
        }
    }
    private float surfaceBelow(float px,float py){float top=FLOOR;for(Block b:blocks)if(px>=b.x&&px<=b.x+b.w&&b.y+b.h<=py)top=Math.max(top,b.y+b.h);return top;}
    private void updateEruptions(float dt) {
        if(stage==3&&!isBossRoom()){rockSpawnTimer-=dt;if(rockSpawnTimer<=0){if(x>1100&&x<3600)eruptions.add(new Eruption(x+22,surfaceBelow(x+22,y+4)));rockSpawnTimer=3.6f;}}
        for(Iterator<Eruption> it=eruptions.iterator();it.hasNext();){Eruption eruption=it.next();
            if(eruption.warning>0){eruption.warning=Math.max(0,eruption.warning-dt);continue;}
            eruption.life-=dt;if(eruption.life<=0){it.remove();continue;}
            if(!isBossRoom()&&overlap(eruption.x-24,eruption.y,48,150,x,y,PLAYER_W,PLAYER_H))damage(2,eruption.x);
        }
    }
    private static float approach(float value,float target,float delta){return value<target?Math.min(target,value+delta):Math.max(target,value-delta);}
    private static boolean overlap(float x,float y,float w,float h,float a,float b,float c,float d) {
        return x<a+c&&x+w>a&&y<b+d&&y+h>b;
    }
}
