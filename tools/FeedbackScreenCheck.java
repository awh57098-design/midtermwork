import com.midtermwork.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.backends.lwjgl3.*;
/** Desktop audio is enabled: validates all WAV loads, plays every cue and captures combat labels. */
public class FeedbackScreenCheck {
 public static void main(String[] args){final String out=args.length>0?args[0]:"../build/combat-feedback.png";Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("midtermwork - combat feedback and sound check");c.setWindowedMode(1152,648);c.setForegroundFPS(60);
 new Lwjgl3Application(new Main(){GameWorld w;GameAudio sound;int frame;
 Object field(String name){try{java.lang.reflect.Field f=Main.class.getDeclaredField(name);f.setAccessible(true);return f.get(this);}catch(Exception e){throw new RuntimeException(e);}}
 public void create(){super.create();w=(GameWorld)field("world");sound=(GameAudio)field("audio");if(!sound.available())throw new AssertionError("desktop audio unavailable");w.enemies.clear();w.spikes.clear();w.setDropSeed(0);w.messageTime=0;}
 void hit(float x,GameWorld.Phase phase,int power,boolean crit){try{GameWorld.Enemy e=new GameWorld.Enemy(x,112,true);e.hp=e.maxHp=2000;e.timer=1000;e.phase=phase;w.enemies.add(e);if(crit)for(int i=0;i<100;i++)w.collectItem(GameWorld.Item.SCOPE);java.lang.reflect.Method m=GameWorld.class.getDeclaredMethod("playerHit",GameWorld.Enemy.class,int.class,int.class,GameWorld.Skill.class);m.setAccessible(true);m.invoke(w,e,power,1,null);w.itemNoticeTime=0;}catch(Exception e){throw new RuntimeException(e);}}
 void cue(GameWorld.SoundCue cue){try{java.lang.reflect.Method m=GameWorld.class.getDeclaredMethod("feedback",float.class,float.class,String.class,GameWorld.CombatKind.class,GameWorld.SoundCue.class);m.setAccessible(true);m.invoke(w,400f,360f,cue.name(),GameWorld.CombatKind.NORMAL,cue);}catch(Exception e){throw new RuntimeException(e);}}
 public void render(){frame++;if(frame==10){hit(310,GameWorld.Phase.WINDUP,6,false);hit(620,GameWorld.Phase.RECOVER,34,false);hit(930,GameWorld.Phase.PATROL,20,true);}if(frame>=45&&frame<=225&&(frame-45)%30==0)cue(GameWorld.SoundCue.values()[(frame-45)/30]);if(frame==230)sound.toggle();if(frame==231)cue(GameWorld.SoundCue.HIT);super.render();if(frame==25){Pixmap p=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());java.io.File f=new java.io.File(out);f.getParentFile().mkdirs();PixmapIO.writePNG(Gdx.files.absolute(f.getAbsolutePath()),p,-1,true);p.dispose();}if(frame==240){if(!sound.muted()||!w.drainSoundCues().isEmpty())throw new AssertionError("mute / consumed cues");System.out.println("PASS: desktop audio loaded, seven cues played, mute drained cues, combat labels captured");Gdx.app.exit();}}
 },c);}
}
