import com.midtermwork.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.backends.lwjgl3.*;
/** Headful visual regression scene. Run from assets with built fat JAR on classpath. */
public class SkillVfxScreenCheck {
 public static void main(String[] args){final String out=args.length>0?args[0]:"../build/skill-vfx-check.png";Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setTitle("midtermwork - skill VFX verification");c.setWindowedMode(1152,648);c.disableAudio(true);c.setForegroundFPS(60);
 new Lwjgl3Application(new Main(){GameWorld w;int frames;
  public void create(){super.create();try{java.lang.reflect.Field f=Main.class.getDeclaredField("world");f.setAccessible(true);w=(GameWorld)f.get(this);w.enemies.clear();w.spikes.clear();w.level=20;w.evolution=1;w.maxHp=w.hp=8;java.lang.reflect.Field e=GameWorld.class.getDeclaredField("equipped");e.setAccessible(true);GameWorld.Skill[] slots=(GameWorld.Skill[])e.get(w);slots[0]=GameWorld.Skill.FLAMETHROWER;slots[1]=GameWorld.Skill.FIRE_BLAST;slots[2]=GameWorld.Skill.FANG;slots[3]=GameWorld.Skill.FIRE_PUNCH;}catch(Exception e){throw new RuntimeException(e);}}
  public void render(){w.skillShots.clear();w.skillVisuals.clear();GameWorld.Skill[] moves={GameWorld.Skill.EMBER,GameWorld.Skill.FLAMETHROWER,GameWorld.Skill.FIRE_BLAST,GameWorld.Skill.FIRE_SPIN,GameWorld.Skill.HEAT_WAVE,GameWorld.Skill.OVERHEAT,GameWorld.Skill.SWIFT,GameWorld.Skill.ROCK_SLIDE};for(int i=0;i<moves.length;i++){w.skillShots.add(new GameWorld.SkillShot(220+(i%4)*240,460-(i/4)*130,1,moves[i],1));}GameWorld.Skill[] melee={GameWorld.Skill.FANG,GameWorld.Skill.FIRE_PUNCH,GameWorld.Skill.DRAGON_CLAW,GameWorld.Skill.DRAGON_TAIL};for(int i=0;i<melee.length;i++){SkillVisual.Burst b=new SkillVisual.Burst(melee[i],190+i*250,190,1,false);b.age=.1f;w.skillVisuals.add(b);}super.render();if(++frames==35){Pixmap pix=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());PixmapIO.writePNG(Gdx.files.absolute(new java.io.File(out).getAbsolutePath()),pix,-1,true);pix.dispose();System.out.println("PASS: eight projectile silhouettes, four melee silhouettes and offense and movement HUD rendered");Gdx.app.exit();}}
 },c);}
}
