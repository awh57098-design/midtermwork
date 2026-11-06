package com.midtermwork;

import java.util.ArrayList;
import java.util.List;

/** Authored room catalog: four full-length routes and a separate arena per biome. */
public final class MapLayout {
    public final List<GameWorld.Block> blocks=new ArrayList<>();
    public final List<GameWorld.Spike> spikes=new ArrayList<>();
    public final List<Spawn> enemies=new ArrayList<>();
    public float[] checkpoints;
    public static final class Spawn {
        public final float x,y;public final boolean spitter;
        Spawn(float x,float y,boolean spitter){this.x=x;this.y=y;this.spitter=spitter;}
    }
    private void ground(float x,float w){blocks.add(new GameWorld.Block(x,0,w,112));}
    private void plateau(float x,float w){blocks.add(new GameWorld.Block(x,0,w,200));}
    private void ledge(float x,float top,float w){blocks.add(new GameWorld.Block(x,top-24,w,24,true));}
    private void spike(float x,float y,float w){spikes.add(new GameWorld.Spike(x,y,w));}
    private void enemy(float x,float y,boolean spitter){enemies.add(new Spawn(x,y,spitter));}
    public static String title(int index){return index==1?"숲길":index==2?"높은 산길":index==3?"협곡":index==4?"시련의 길":"보스의 방";}
    public static MapLayout create(int biome,int index){
        if(biome<1||biome>3||index<1||index>5)throw new IllegalArgumentException("Invalid campaign room");
        MapLayout m=new MapLayout();
        if(index==5){m.ground(0,1280);m.ledge(220,230,190);m.ledge(800,230,190);m.checkpoints=new float[0];return m;}
        if(index==1)m.trail(biome);else if(index==2)m.highRoute();else if(index==3)m.ravines();else m.gauntlet();
        return m;
    }
    private void trail(int biome){
        if(biome==2){
            ground(0,600);ground(1000,600);ground(2080,800);ground(3300,1200);
            ledge(560,200,160);ledge(740,280,140);ledge(900,210,180);
            ledge(1140,210,170);ledge(1320,300,160);ledge(1500,385,170);ledge(1700,300,160);ledge(1890,205,220);
            ledge(2410,215,260);ledge(2840,200,160);ledge(3020,290,150);ledge(3200,200,150);ledge(3820,215,270);
            spike(1200,112,130);spike(2450,112,155);spike(3870,112,155);
            enemy(430,112,false);enemy(785,280,true);enemy(1090,112,false);enemy(1360,300,true);enemy(1550,385,false);
            enemy(1950,205,true);enemy(2280,112,false);enemy(2770,112,true);enemy(3060,290,true);enemy(3510,112,false);enemy(4160,112,true);
            checkpoints=new float[]{1080,2200,3380};
        }else{
            float first=biome==3?205:200,second=biome==3?290:280,middle=biome==3?210:200,high=biome==3?305:280;
            ground(0,650);ground(1100,620);ground(2150,680);ground(3320,1180);
            ledge(620,first,150);ledge(800,second,130);ledge(970,205,160);ledge(1360,biome==3?215:205,260);
            ledge(1680,middle,150);ledge(1870,high,140);ledge(2040,biome==3?210:205,150);ledge(2410,biome==3?225:210,260);
            ledge(2790,first,150);ledge(2970,biome==3?300:280,150);ledge(3150,205,190);ledge(3700,biome==3?215:210,270);
            spike(1400,112,155);spike(2460,112,155);spike(3750,112,150);
            enemy(470,112,false);enemy(835,second,true);enemy(1250,112,false);enemy(1620,112,false);enemy(1900,high,true);
            enemy(2290,112,false);enemy(2730,112,false);enemy(3010,biome==3?300:280,true);enemy(3510,112,false);enemy(4110,112,true);
            checkpoints=new float[]{1180,2230,3400};
        }
    }
    private void highRoute(){
        ground(0,700);ground(1080,670);ground(2200,800);ground(3420,1080);
        ledge(650,200,160);ledge(830,290,140);ledge(1000,210,160);
        ledge(1190,210,160);ledge(1370,300,160);ledge(1550,390,190);ledge(1770,300,160);ledge(1950,210,280);
        ledge(2410,215,260);ledge(2960,200,160);ledge(3140,285,140);ledge(3310,205,180);ledge(3740,215,270);
        spike(1240,112,110);spike(2450,112,150);spike(3780,112,145);
        enemy(480,112,false);enemy(865,290,true);enemy(1140,112,false);enemy(1410,300,true);enemy(1620,390,false);
        enemy(2010,210,true);enemy(2300,112,false);enemy(2860,112,false);enemy(3180,285,true);enemy(3610,112,false);enemy(4200,112,true);
        checkpoints=new float[]{1160,2280,3500};
    }
    private void ravines(){
        ground(0,600);ground(1000,600);ground(2000,600);ground(3000,600);ground(4000,500);
        for(int i=0;i<4;i++){float offset=i*1000;
            ledge(offset+560,200,150);ledge(offset+740,280,140);ledge(offset+900,205,150);
            enemy(offset+775,280,i%2==0);
            if(i<3){ledge(offset+1100,210,260);spike(offset+1150,112,150);enemy(offset+1430,112,true);}
        }
        enemy(420,112,false);enemy(4150,112,true);checkpoints=new float[]{1080,2080,3080};
    }
    private void gauntlet(){
        ground(0,1000);plateau(1000,480);ground(1480,720);plateau(2200,500);ground(2700,700);plateau(3400,500);ground(3900,600);
        ledge(540,210,380);ledge(1620,210,340);ledge(2320,295,300);ledge(3060,210,340);
        spike(620,112,240);spike(1680,112,220);spike(2380,200,150);spike(3120,112,220);
        enemy(400,112,false);enemy(720,210,true);enemy(1150,200,false);enemy(1390,200,true);enemy(1550,112,false);enemy(1910,112,true);
        enemy(2260,200,false);enemy(2520,295,true);enemy(2850,112,false);enemy(3500,200,true);enemy(3800,200,false);enemy(4210,112,true);
        checkpoints=new float[]{1550,2800,3980};
    }
}
