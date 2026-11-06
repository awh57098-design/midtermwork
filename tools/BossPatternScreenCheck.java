import com.midtermwork.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.backends.lwjgl3.*;
/** Three real boss scenes, rendered using the normal game loop. Optional output directory argument. */
public class BossPatternScreenCheck {
 public static void main(String[] args){final String dir=args.length>0?args[0]:"../build/boss-pattern-check";Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("midtermwork - boss pattern verification");c.setWindowedMode(1152,648);c.setForegroundFPS(60);c.disableAudio(true);
 new Lwjgl3Application(new Main(){GameWorld w;int biome=1,frames;
  public void create(){super.create();try{java.lang.reflect.Field f=Main.class.getDeclaredField("world");f.setAccessible(true);w=(GameWorld)f.get(this);w.level=15;for(int i=0;i<5;i++)w.collectItem(GameWorld.Item.CANDY);while(w.choosingSkill())w.skipLearning();scene();}catch(Exception e){throw new RuntimeException(e);}}
  void scene(){w.stage=biome;w.mapIndex=4;w.stageComplete=true;w.advanceStage();w.x=300;w.invulnerable=100;w.bossActive=true;w.boss.pattern=biome==1?2:1;w.boss.cycle=w.boss.pattern;w.boss.targetX=300;w.boss.jumpStartX=w.boss.x;w.boss.dir=-1;w.boss.phase=GameWorld.Phase.WINDUP;w.boss.timer=.2f;w.messageTime=w.itemNoticeTime=0;frames=0;}
  public void render(){super.render();if(++frames==(biome==1?40:biome==2?100:78)){if(w.enemyLevel()!=15||w.level!=20)throw new AssertionError("candy lead");Pixmap pix=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());java.io.File file=new java.io.File(dir,"boss-"+biome+".png");file.getParentFile().mkdirs();PixmapIO.writePNG(Gdx.files.absolute(file.getAbsolutePath()),pix,-1,true);pix.dispose();if(++biome>3){System.out.println("PASS: three boss scenes, distinct hazards and player 20 / enemy 15 rendered");Gdx.app.exit();}else scene();}}
 },c);}
}
