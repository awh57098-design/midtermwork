package com.midtermwork;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader;
import java.util.HashMap;
import java.util.Map;

/** Reads native SpriteCollab cells and variable frame durations; keeps a fixed foot pivot. */
public final class CreatureAnimation {
    private static final String[] ACTIONS={"Idle","Walk","Attack","Hurt","Hop","Charge","Shoot"};
    private final Map<String,Clip> clips=new HashMap<>();
    private final float scale,footOffset;
    private static final class Clip {
        Texture texture;
        int w,h,frames;
        float[] duration;
        int[][] bottom;
        float length;
        Clip(String folder,XmlReader.Element node) {
            w=node.getInt("FrameWidth");h=node.getInt("FrameHeight");
            XmlReader.Element times=node.getChildByName("Durations");frames=times.getChildCount();duration=new float[frames];
            for(int i=0;i<frames;i++){duration[i]=Float.parseFloat(times.getChild(i).getText())/60f;length+=duration[i];}
            texture=new Texture(folder+"/"+node.get("Name")+"-Anim.png");
            texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
            if(texture.getWidth()<w*frames||texture.getHeight()<h*8)throw new IllegalStateException("Invalid animation sheet: "+folder);
            texture.getTextureData().prepare();Pixmap pixels=texture.getTextureData().consumePixmap();
            bottom=new int[8][frames];
            for(int row=0;row<8;row++)for(int frame=0;frame<frames;frame++)
                for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++)
                    if((pixels.getPixel(frame*w+xx,row*h+yy)&255)!=0)bottom[row][frame]=Math.max(bottom[row][frame],yy);
            pixels.dispose();
        }
        int frame(float time,boolean loop) {
            if(loop)time=time%length;else time=Math.min(time,length-.0001f);
            for(int i=0;i<frames;i++){time-=duration[i];if(time<0)return i;}
            return frames-1;
        }
    }
    public CreatureAnimation(String id,float visibleHeight) {
        String folder="animations/"+id;
        XmlReader.Element root=new XmlReader().parse(Gdx.files.internal(folder+"/AnimData.xml"));
        XmlReader.Element entries=root.getChildByName("Anims");
        for(String name:ACTIONS) {
            XmlReader.Element entry=find(entries,name);
            while(entry!=null&&entry.hasChild("CopyOf"))entry=find(entries,entry.get("CopyOf"));
            if(entry!=null) {
                String resolved=entry.get("Name");Clip existing=clips.get(resolved);
                if(existing==null){existing=new Clip(folder,entry);clips.put(resolved,existing);}
                clips.put(name,existing);
            }
        }
        Clip idle=clips.get("Idle");
        idle.texture.getTextureData().prepare();Pixmap p=idle.texture.getTextureData().consumePixmap();
        int low=idle.h,high=0;
        for(int yy=0;yy<idle.h;yy++)for(int xx=0;xx<idle.w;xx++)if((p.getPixel(xx,6*idle.h+yy)&255)!=0){low=Math.min(low,yy);high=Math.max(high,yy);}
        p.dispose();scale=visibleHeight/(high-low+1);footOffset=high+1-idle.h/2f;
    }
    private static XmlReader.Element find(XmlReader.Element entries,String name) {
        for(int i=0;i<entries.getChildCount();i++)if(name.equals(entries.getChild(i).get("Name")))return entries.getChild(i);
        return null;
    }
    public void draw(SpriteBatch batch,String action,float clock,boolean progress,boolean loop,int facing,
                     float centerX,float feetY,float stretchX,float stretchY,float rotation) {
        Clip c=clips.get(action);if(c==null)c=clips.get("Idle");
        int index=c.frame(progress?Math.max(0,Math.min(1,clock))*c.length:Math.max(0,clock),loop);
        int row=facing>0?2:6;
        TextureRegion region=new TextureRegion(c.texture,index*c.w,row*c.h,c.w,c.h);
        float w=c.w*scale,h=c.h*scale,originX=w/2,originY=(c.h/2f-footOffset)*scale;
        if(action.equals("Idle")||action.equals("Walk")||action.equals("Charge"))originY=(c.h-1-c.bottom[row][index])*scale;
        batch.draw(region,centerX-originX,feetY-originY,originX,originY,w,h,stretchX,stretchY,rotation);
    }
    public void dispose() { java.util.HashSet<Texture> textures=new java.util.HashSet<>();for(Clip c:clips.values())textures.add(c.texture);for(Texture t:textures)t.dispose(); }
}
