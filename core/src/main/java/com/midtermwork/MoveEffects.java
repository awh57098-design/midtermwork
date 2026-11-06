package com.midtermwork;

/** Move-specific secondary effects adapted from PokeAPI metadata. Durations are real-time game rules. */
public final class MoveEffects {
    private MoveEffects(){}
    public static final class Profile {
        public final int ailment,chance,flinch,crit,recoil,statChance;public final int[] stats;
        Profile(int ailment,int chance,int flinch,int crit,int drain,int statChance,int[] stats){this.ailment=ailment;this.chance=chance;this.flinch=flinch;this.crit=crit;this.recoil=Math.max(0,-drain);this.statChance=statChance;this.stats=stats;}
    }
    private static final java.util.EnumMap<GameWorld.Skill,Profile> DATA=new java.util.EnumMap<>(GameWorld.Skill.class);
    static {
        put(GameWorld.Skill.SCRATCH,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.GROWL,0,0,0,0,0,0,new int[]{2,-1});
        put(GameWorld.Skill.EMBER,4,10,0,0,0,0,new int[]{});
        put(GameWorld.Skill.SMOKE,0,0,0,0,0,0,new int[]{7,-1});
        put(GameWorld.Skill.DRAGON,1,30,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FANG,4,10,10,0,0,0,new int[]{});
        put(GameWorld.Skill.SLASH,0,0,0,1,0,0,new int[]{});
        put(GameWorld.Skill.FLAMETHROWER,4,10,0,0,0,0,new int[]{});
        put(GameWorld.Skill.SCARY_FACE,0,0,0,0,0,0,new int[]{6,-2});
        put(GameWorld.Skill.FIRE_SPIN,8,100,0,0,0,0,new int[]{});
        put(GameWorld.Skill.INFERNO,4,100,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FLARE_BLITZ,4,10,0,0,-33,0,new int[]{});
        put(GameWorld.Skill.FIRE_PUNCH,4,10,0,0,0,0,new int[]{});
        put(GameWorld.Skill.THUNDER_PUNCH,1,10,0,0,0,0,new int[]{});
        put(GameWorld.Skill.SWORDS_DANCE,0,0,0,0,0,0,new int[]{2,2});
        put(GameWorld.Skill.BODY_SLAM,1,30,0,0,0,0,new int[]{});
        put(GameWorld.Skill.TAKE_DOWN,0,0,0,0,-25,0,new int[]{});
        put(GameWorld.Skill.ROAR,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.DIG,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FIRE_BLAST,4,10,0,0,0,0,new int[]{});
        put(GameWorld.Skill.SWIFT,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.REST,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.ROCK_SLIDE,0,0,30,0,0,0,new int[]{});
        put(GameWorld.Skill.SUBSTITUTE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.PROTECT,43,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.OUTRAGE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.ENDURE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FALSE_SWIPE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.SLEEP_TALK,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.METAL_CLAW,0,0,0,0,0,10,new int[]{2,1});
        put(GameWorld.Skill.SUNNY_DAY,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.CRUNCH,0,0,0,0,0,20,new int[]{3,-1});
        put(GameWorld.Skill.HEAT_WAVE,4,10,0,0,0,0,new int[]{});
        put(GameWorld.Skill.WILL_O_WISP,4,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FACADE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FOCUS_PUNCH,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.HELPING_HAND,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.BRICK_BREAK,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.WEATHER_BALL,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.OVERHEAT,0,0,0,0,0,100,new int[]{4,-2});
        put(GameWorld.Skill.ROCK_TOMB,0,0,0,0,0,100,new int[]{6,-1});
        put(GameWorld.Skill.DRAGON_CLAW,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.DRAGON_DANCE,0,0,0,0,0,0,new int[]{2,1,6,1});
        put(GameWorld.Skill.FLING,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.DRAGON_PULSE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.FOCUS_BLAST,0,0,0,0,0,10,new int[]{5,-1});
        put(GameWorld.Skill.SHADOW_CLAW,0,0,0,1,0,0,new int[]{});
        put(GameWorld.Skill.FLAME_CHARGE,0,0,0,0,0,100,new int[]{6,1});
        put(GameWorld.Skill.FIRE_PLEDGE,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.DRAGON_TAIL,0,0,0,0,0,0,new int[]{});
        put(GameWorld.Skill.BREAKING_SWIPE,0,0,0,0,0,100,new int[]{2,-1});
        put(GameWorld.Skill.TERA_BLAST,0,0,0,0,0,0,new int[]{2,-1,4,-1});
        put(GameWorld.Skill.TEMPER_FLARE,0,0,0,0,0,0,new int[]{});
    }
    private static void put(GameWorld.Skill skill,int ailment,int chance,int flinch,int crit,int drain,int statChance,int[] stats){DATA.put(skill,new Profile(ailment,chance,flinch,crit,drain,statChance,stats));}
    public static Profile profile(GameWorld.Skill skill){return DATA.get(skill);}
    public static boolean selfStat(GameWorld.Skill move,int change){return change>0||move==GameWorld.Skill.OVERHEAT;}
    public static String description(GameWorld.Skill move){
        switch(move){
        case FLAME_CHARGE:return "지상과 공중에서 사용 가능. 시전 준비와 돌진 중 무적. 앞으로 돌진합니다. 명중하면 이동 속도 1단계 상승. 10초간 단계당 15%, 최대 6단계. 빗나가면 상승하지 않습니다.";
        case FLARE_BLITZ:return "지상과 공중에서 사용 가능. 시전 준비와 돌진 중 무적. 반동은 적용됩니다. 불꽃을 두르고 돌진합니다. 10% 화상. 명중 피해를 적 체력 비율로 환산해 1/3 반동을 받습니다. 빗나가면 반동 없음. 반동으로 기절할 수 있습니다.";
        case TAKE_DOWN:return "지상과 공중에서 사용 가능. 시전 준비와 돌진 중 무적. 반동은 적용됩니다. 앞으로 돌진합니다. 명중 피해를 적 체력 비율로 환산해 1/4 반동을 받습니다. 빗나가면 반동이 없습니다.";
        case SMOKE:return "3초간 연막. 공격 준비를 방해하고 독 투사체를 차단합니다.";
        case GROWL:return "근처 적의 공격을 1단계 낮춥니다. 적 능력 변화는 6초간 지속됩니다.";
        case SCARY_FACE:return "근처 적의 이동 속도를 2단계 낮춥니다. 6초간 지속됩니다.";
        case SWORDS_DANCE:return "물리 공격을 2단계 올립니다. 10초간 지속, 최대 6단계.";
        case DRAGON_DANCE:return "물리 공격과 이동 속도를 각각 1단계 올립니다. 10초간 지속됩니다.";
        case SUNNY_DAY:return "10초 동안 불꽃 피해 50% 증가. 웨더볼이 불꽃 타입과 위력 100으로 바뀝니다.";
        case WEATHER_BALL:return "평소 노말 위력 50. 쾌청 중에는 불꽃 위력 100으로 바뀝니다.";
        case HELPING_HAND:return "8초 동안 직접 피해가 25% 증가합니다. 싱글 플레이용 강화 효과입니다.";
        case PROTECT:return "2초 동안 피해를 막습니다. 반동과 체력 소모는 막지 못합니다.";
        case ENDURE:return "4초 동안 적 공격과 함정의 치명적 피해를 하트 반 칸으로 버팁니다. 반동은 제외됩니다.";
        case SUBSTITUTE:return "하트 1칸을 사용해 3초 동안 피해를 막습니다.";
        case REST:return "체력을 모두 회복하고 2.5초 동안 잠듭니다.";
        case SLEEP_TALK:return "잠든 동안 장착한 공격 기술 하나를 사용합니다.";
        case WILL_O_WISP:return "앞쪽 적에게 6초 화상을 남깁니다. 불꽃 타입에는 화상이 걸리지 않습니다.";
        case OVERHEAT:return "명중하면 자신의 특수 공격이 2단계 내려갑니다. 10초간 지속됩니다.";
        case FIRE_SPIN:return "명중한 적을 4초 동안 구속합니다. 이동을 늦추고 초당 최대 체력의 4%를 고정 피해로 줍니다.";
        case FALSE_SWIPE:return "추가 타격을 포함해 적의 체력을 최소 1 남깁니다.";
        case DIG:return "전방을 타격하고 1.2초 동안 피해를 피합니다.";
        case DRAGON_TAIL:return "적을 밀어내고 공격 준비를 끊습니다. 보스는 밀리지 않습니다.";
        case FACADE:return "하트가 부족할 때 피해가 2배입니다. 상태 이상 대신 체력 조건을 사용하는 게임용 효과입니다.";
        case FOCUS_PUNCH:return "준비 중 피격되면 취소되는 강력한 전방 공격입니다.";
        case TEMPER_FLARE:return "이전 공격 기술이 빗나갔다면 이번 피해가 2배입니다.";
        default:break;
        }
        Profile p=profile(move);String lead;
        switch(SkillVisual.style(move)){
        case STREAM:lead="긴 줄기를 빠르게 쏘아 여러 적을 관통합니다.";break;
        case GLYPH:lead="큰 불꽃 글자를 천천히 날립니다. 넓은 명중 판정.";break;
        case WAVE:lead="넓은 파동이 전진하며 여러 적을 관통합니다.";break;
        case ERUPTION:lead="불기둥 형태의 불꽃을 전방으로 보냅니다.";break;
        case CLAW:lead="전방을 발톱 궤적으로 벱니다.";break;
        case JAW:lead="전방의 적을 엄니로 물어뜯습니다.";break;
        case FIST:lead="주먹을 뻗어 전방의 적을 타격합니다.";break;
        case TAIL:lead="꼬리를 크게 휘둘러 전방을 타격합니다.";break;
        case ROCK:lead="암석 파편을 전방으로 날립니다.";break;
        case STAR:lead="회전하는 별을 전방으로 날립니다.";break;
        case SPARK:lead="작은 불씨를 빠르게 날립니다.";break;
        default:lead=move.ranged()?"전방 투사체 공격.":"전방 근접 공격.";break;
        }
        StringBuilder text=new StringBuilder(lead);
        if(p.ailment==4)text.append(" ").append(p.chance).append("% 화상(").append(move==GameWorld.Skill.INFERNO?6:3).append("초).");
        if(p.ailment==1)text.append(" ").append(p.chance).append("% 마비(4초, 이동 둔화).");
        if(p.flinch>0)text.append(" ").append(p.flinch).append("% 풀죽음으로 공격 중단.");
        if(p.crit>0)text.append(" 급소에 맞기 쉬움(기본 12.5%).");
        for(int i=0;i<p.stats.length;i+=2){int stat=p.stats[i],change=p.stats[i+1];text.append(" ").append(p.statChance).append("% ").append(selfStat(move,change)?"자신 ":"적 ").append(stat==2?"공격":stat==3?"방어":stat==4?"특수공격":stat==5?"특수방어":"이동속도").append(change>0?" 상승 ":" 하락 ").append(Math.abs(change)).append("단계.");}
        return text.toString();
    }
}
