package com.midtermwork;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.EnumMap;
import java.util.EnumSet;
/** Short original PCM effects; bounded voices and cue cooldowns prevent crowd-hit clipping. */
public final class GameAudio {
 private final EnumMap<GameWorld.SoundCue,Sound> sounds=new EnumMap<>(GameWorld.SoundCue.class);
 private final EnumMap<GameWorld.SoundCue,Long> last=new EnumMap<>(GameWorld.SoundCue.class);
 private boolean muted,available;
 public GameAudio(){try{for(GameWorld.SoundCue cue:GameWorld.SoundCue.values())sounds.put(cue,Gdx.audio.newSound(Gdx.files.internal("audio/"+cue.name().toLowerCase(java.util.Locale.ROOT)+".wav")));available=true;}catch(Exception ex){dispose();if(Gdx.app!=null)Gdx.app.error("midtermwork","Sound unavailable",ex);}}
 public boolean available(){return available;}
 public boolean muted(){return muted;}
 public void toggle(){muted=!muted;if(muted)for(Sound sound:sounds.values())sound.stop();}
 public void update(GameWorld world){java.util.List<GameWorld.SoundCue> cues=world.drainSoundCues();if(!available||muted||world.paused)return;EnumSet<GameWorld.SoundCue> pending=EnumSet.noneOf(GameWorld.SoundCue.class);pending.addAll(cues);long now=TimeUtils.nanoTime();int voices=0;
  GameWorld.SoundCue[] priority={GameWorld.SoundCue.HURT,GameWorld.SoundCue.BOSS_WARNING,GameWorld.SoundCue.CRITICAL,GameWorld.SoundCue.COUNTER,GameWorld.SoundCue.GUARDED,GameWorld.SoundCue.HIT,GameWorld.SoundCue.IMMUNE};
  for(GameWorld.SoundCue cue:priority)if(pending.contains(cue)&&voices<4){long interval=cue==GameWorld.SoundCue.BOSS_WARNING?200000000L:65000000L;if(!last.containsKey(cue)||now-last.get(cue)>=interval){sounds.get(cue).play(cue==GameWorld.SoundCue.HIT?.3f:cue==GameWorld.SoundCue.GUARDED?.28f:.4f);last.put(cue,now);voices++;}}
 }
 public void dispose(){for(Sound sound:sounds.values())sound.dispose();sounds.clear();available=false;}
}
