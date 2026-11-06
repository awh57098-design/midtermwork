package com.midtermwork;

/** Base stats: PokeAPI. Species and level only; levels clamped 1..100. */
public final class PokemonStats {
    private PokemonStats() {}
    public enum Species {
        CHARMANDER(39,52,43,60,50,65),
        CHARMELEON(58,64,58,80,65,80),
        CHARIZARD(78,84,78,109,85,100),
        RATTATA(30,56,35,25,35,72),
        RATICATE(55,81,60,50,70,97),
        EKANS(35,60,44,40,54,55),
        ARBOK(60,95,69,65,79,80),
        MAGMAR(65,95,57,100,85,93);
        public final int hp,attack,defense,specialAttack,specialDefense,speed;
        Species(int h,int a,int d,int s,int r,int v){hp=h;attack=a;defense=d;specialAttack=s;specialDefense=r;speed=v;}
    }
    public static final class Values {
        public final int hp,attack,defense,specialAttack,specialDefense,speed;
        Values(Species s,int lv){lv=Math.max(1,Math.min(100,lv));hp=(2*s.hp)*lv/100+lv+10;attack=stat(s.attack,lv);defense=stat(s.defense,lv);specialAttack=stat(s.specialAttack,lv);specialDefense=stat(s.specialDefense,lv);speed=stat(s.speed,lv);}
    }
    private static int stat(int base,int lv){return (2*base)*lv/100+5;}
    public static Values at(Species s,int level){return new Values(s,level);}
    public static int damage(int level,int power,float attack,float defense){return Math.max(1,(int)(((2*Math.max(1,Math.min(100,level))/5f+2)*power*attack/Math.max(1,defense))/50+2));}
}
