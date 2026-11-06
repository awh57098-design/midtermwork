package com.midtermwork;

/** World-pixel reach shared by simulation, move silhouettes and tooltips. */
public final class MoveGeometry {
    private MoveGeometry() {}
    public static float reach(GameWorld.Skill m){switch(m){
        case SCRATCH:return 105;case FANG:return 135;
        case METAL_CLAW:return 190;case FALSE_SWIPE:return 205;case SLASH:return 220;
        case DRAGON_CLAW:return 260;case SHADOW_CLAW:return 280;
        case CRUNCH:return 180;case FIRE_PUNCH:return 180;case THUNDER_PUNCH:return 200;
        case FOCUS_PUNCH:return 320;case BRICK_BREAK:return 240;
        case DRAGON_TAIL:return 300;case BREAKING_SWIPE:return 310;
        case OUTRAGE:return 230;case BODY_SLAM:return 135;case TEMPER_FLARE:return 190;
        case DIG:return 340;default:return 170;
    }}
    public static float height(GameWorld.Skill m){switch(m){
        case SCRATCH:return 84;case FANG:return 105;case FALSE_SWIPE:return 100;
        case SLASH:return 160;case METAL_CLAW:return 128;
        case DRAGON_CLAW:return 180;case SHADOW_CLAW:return 200;
        case CRUNCH:return 130;case FIRE_PUNCH:return 145;case THUNDER_PUNCH:return 160;
        case FOCUS_PUNCH:return 190;case BRICK_BREAK:return 180;
        case DRAGON_TAIL:return 200;case BREAKING_SWIPE:return 220;
        case OUTRAGE:return 280;case BODY_SLAM:return 200;case TEMPER_FLARE:return 230;
        case DIG:return 110;default:return 120;
    }}
    public static boolean radial(GameWorld.Skill m){return m==GameWorld.Skill.OUTRAGE||m==GameWorld.Skill.BODY_SLAM||m==GameWorld.Skill.TEMPER_FLARE;}
    public static boolean hits(GameWorld.Skill m,float x,float y,int dir,GameWorld.Enemy e){
        float cx=x+22,cy=y+38,r=reach(m),h=height(m);
        if(radial(m)){float nx=Math.max(e.x,Math.min(cx,e.x+e.width())),ny=Math.max(e.y,Math.min(cy,e.y+e.height()));float dx=(nx-cx)/r,dy=(ny-cy)/(h/2);return dx*dx+dy*dy<1;}
        float left=dir>0?cx:cx-r;
        return left<e.x+e.width()&&left+r>e.x&&cy-h/2<e.y+e.height()&&cy+h/2>e.y;
    }
    public static float projectileRange(GameWorld.Skill m){switch(m){
        case EMBER:return 300;case DRAGON:return 500;case FLAMETHROWER:return 480;
        case FIRE_BLAST:return 320;case FIRE_SPIN:return 260;case INFERNO:return 240;
        case HEAT_WAVE:return 280;case DRAGON_PULSE:return 550;case FOCUS_BLAST:return 500;
        case OVERHEAT:return 220;case ROCK_SLIDE:return 420;case ROCK_TOMB:return 360;
        case FLING:return 400;case SWIFT:return 440;case FIRE_PLEDGE:return 300;
        case WEATHER_BALL:return 380;case TERA_BLAST:return 480;default:return 380;
    }}
    public static float projectileRadius(GameWorld.Skill m){switch(m){
        case EMBER:return 12;case DRAGON:return 10;case FLAMETHROWER:return 14;
        case FIRE_BLAST:return 58;case FIRE_SPIN:return 65;case INFERNO:return 85;
        case HEAT_WAVE:return 90;case DRAGON_PULSE:return 12;case FOCUS_BLAST:return 18;
        case OVERHEAT:return 100;case ROCK_SLIDE:return 35;case ROCK_TOMB:return 28;
        case FLING:return 16;case SWIFT:return 16;case FIRE_PLEDGE:return 70;
        case WEATHER_BALL:return 26;case TERA_BLAST:return 20;default:return 16;
    }}
    public static boolean areaProjectile(GameWorld.Skill m){return m.ranged()&&projectileRadius(m)>=50;}
    public static float dashSpeed(GameWorld.Skill m){return m==GameWorld.Skill.FLARE_BLITZ?850:m==GameWorld.Skill.TAKE_DOWN?740:650;}
    public static float dashDuration(GameWorld.Skill m){return m==GameWorld.Skill.FLARE_BLITZ?.38f:m==GameWorld.Skill.TAKE_DOWN?.34f:.28f;}
    public static boolean dash(GameWorld.Skill m){return m==GameWorld.Skill.FLARE_BLITZ||m==GameWorld.Skill.TAKE_DOWN||m==GameWorld.Skill.FLAME_CHARGE;}
    public static String summary(GameWorld.Skill m){
        if(m.category==GameWorld.Category.STATUS)return "변화 기술 / 기술별 효과 범위";
        if(dash(m))return "돌진 거리 약 "+Math.round(dashSpeed(m)*dashDuration(m))+" / 몸 주변 타격";
        if(m.ranged())return "사거리 "+Math.round(projectileRange(m))+" / 범위 폭 "+Math.round(projectileRadius(m)*2)+(areaProjectile(m)?" / 다수 타격":"");
        return (radial(m)?"주변 범위 / 반경 ":"전방 사거리 ")+Math.round(reach(m))+" / 높이 "+Math.round(height(m));
    }
}
