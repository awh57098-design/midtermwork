package com.midtermwork;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
/** Four unique skill keys, saved separately from the current run. */
public final class SkillBindings {
 private final Preferences prefs;private final int[] keys={Input.Keys.X,Input.Keys.C,Input.Keys.A,Input.Keys.S};
 public SkillBindings(Preferences prefs){this.prefs=prefs;int[] defaults=keys.clone();for(int i=0;i<4;i++)keys[i]=prefs.getInteger("skill"+i,defaults[i]);boolean valid=true;for(int i=0;i<4;i++){valid&=allowed(keys[i]);for(int j=0;j<i;j++)valid&=keys[j]!=keys[i];}if(!valid)System.arraycopy(defaults,0,keys,0,4);}
 public int key(int slot){return keys[slot];}
 public String label(int slot){return Input.Keys.toString(keys[slot]);}
 public boolean uses(int key){for(int value:keys)if(value==key)return true;return false;}
 public String compact(){return label(0)+" "+label(1)+" "+label(2)+" "+label(3);}
 public static boolean allowed(int key){if(key<=0||key>255)return false;if(key>=Input.Keys.NUM_0&&key<=Input.Keys.NUM_9)return false;if(key>=Input.Keys.NUMPAD_0&&key<=Input.Keys.NUMPAD_9)return false;switch(key){
 case Input.Keys.LEFT:case Input.Keys.RIGHT:case Input.Keys.UP:case Input.Keys.DOWN:case Input.Keys.SPACE:case Input.Keys.J:case Input.Keys.Z:case Input.Keys.T:case Input.Keys.K:case Input.Keys.ESCAPE:case Input.Keys.ENTER:case Input.Keys.F2:case Input.Keys.F8:case Input.Keys.CONTROL_LEFT:case Input.Keys.CONTROL_RIGHT:case Input.Keys.SHIFT_LEFT:case Input.Keys.SHIFT_RIGHT:case Input.Keys.ALT_LEFT:case Input.Keys.ALT_RIGHT:case Input.Keys.TAB:case Input.Keys.DEL:case Input.Keys.BACK:case Input.Keys.FORWARD_DEL:return false;
 default:return Input.Keys.toString(key)!=null;
 }}
 public boolean bind(int slot,int key){if(slot<0||slot>=4||!allowed(key))return false;int old=keys[slot];for(int i=0;i<4;i++)if(i!=slot&&keys[i]==key)keys[i]=old;keys[slot]=key;save();return true;}
 public void reset(){keys[0]=Input.Keys.X;keys[1]=Input.Keys.C;keys[2]=Input.Keys.A;keys[3]=Input.Keys.S;save();}
 private void save(){for(int i=0;i<4;i++)prefs.putInteger("skill"+i,keys[i]);prefs.flush();}
}
