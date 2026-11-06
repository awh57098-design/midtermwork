package com.midtermwork;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Bitmap scenery; all solid tops and hazard footprints come from GameWorld. */
public final class ForestEnvironment {
    private final Texture background;
    private final Texture atlas;
    private final boolean cave,volcano;
    private final TextureRegion[][] cells=new TextureRegion[4][4];
    public ForestEnvironment(){this("forest");}
    public ForestEnvironment(String theme) {
        cave=theme.equals("cave");volcano=theme.equals("volcano");background=new Texture("environment/"+theme+"-background.png");atlas=new Texture("environment/"+theme+"-atlas.png");
        background.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        atlas.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        for(int row=0;row<4;row++)for(int col=0;col<4;col++) {
            int x=Math.round(col*atlas.getWidth()/4f),y=Math.round(row*atlas.getHeight()/4f);
            int endX=Math.round((col+1)*atlas.getWidth()/4f),endY=Math.round((row+1)*atlas.getHeight()/4f);
            // Inset one texel to exclude grid-boundary artifacts in the source atlas.
            int inset=cave?7:1,topInset=cave&&row==1?25:inset;
            cells[row][col]=new TextureRegion(atlas,x+inset,y+topInset,endX-x-inset*2,endY-y-topInset-inset);
        }
    }
    public void draw(SpriteBatch batch,GameWorld world,float cameraLeft) {
        batch.setColor(Color.WHITE);
        float skyWidth=720f*background.getWidth()/background.getHeight();
        float shift=(cameraLeft*.18f)%skyWidth;
        for(int i=-1;i<3;i++)batch.draw(background,cameraLeft+i*skyWidth-shift,0,skyWidth,720);
        // The lower ravine layer is intentionally darker than safe, grass-covered ledges.
        if(volcano)batch.setColor(1,.26f,.09f,1);else if(cave)batch.setColor(.26f,.27f,.43f,1);else batch.setColor(.22f,.37f,.35f,1);
        for(int i=0;i<(int)Math.ceil(world.worldWidth()/64);i++)drawCell(batch,cells[3][0],i*64,-32,64,144);
        batch.setColor(Color.WHITE);
        for(GameWorld.Block block:world.blocks) {
            float top=block.y+block.h;
            if(block.oneWay) {
                tiledStrip(batch,block.x,top-48,block.w,48,1);
            } else {
                for(float yy=block.y;yy<top-64;yy+=64)
                    tiledFill(batch,block.x,yy,block.w,Math.min(64,top-64-yy),cells[3][0]);
                tiledStrip(batch,block.x,top-64,block.w,64,0);
            }
        }
        // Small props sit only on safe ground; they never masquerade as collision geometry.
        for(GameWorld.Block block:world.blocks) if(!block.oneWay) {
            for(float xx=block.x+45;xx<block.x+block.w-70;xx+=180) {
                if(nearHazard(world,xx))continue;
                int variant=((int)xx/180)%4;
                TextureRegion prop=variant==0?cells[2][0]:variant==1?cells[2][1]:variant==2?cells[3][2]:cells[3][3];
                float size=variant==0?88:variant==1?53:variant==2?46:55;
                drawCell(batch,prop,xx,top(block),size,size);
            }
        }
        for(GameWorld.Spike spike:world.spikes) {
            int count=(int)Math.ceil(spike.w/48);float width=spike.w/count;
            for(int i=0;i<count;i++)drawCell(batch,cells[1][3],spike.x+i*width,spike.y,width,23);
        }
        for(float checkpoint:world.checkpointPositions()) {
            if(world.checkpoint<checkpoint)batch.setColor(.74f,.80f,.75f,1);
            drawCell(batch,cells[2][2],checkpoint-24,112,105,105);
            batch.setColor(Color.WHITE);
        }
        drawCell(batch,cells[2][3],world.exitX()-5,112,world.isBossRoom()?100:150,150);
        if(cave)for(GameWorld.RockFall rock:world.rockFalls)if(rock.warning<=0)drawCell(batch,cells[3][1],rock.x-22,rock.y-22,44,44);
    }
    private float top(GameWorld.Block b){return b.y+b.h;}
    private boolean nearHazard(GameWorld w,float x) {
        for(GameWorld.Spike s:w.spikes)if(x+90>s.x-25&&x<s.x+s.w+25)return true;
        return false;
    }
    private void tiledStrip(SpriteBatch batch,float x,float y,float width,float height,int row) {
        int count=Math.max(1,(int)Math.ceil(width/64));
        for(int i=0;i<count;i++) {
            int col=i==0?0:i==count-1?2:1;
            float segment=Math.min(64,width-i*64);
            TextureRegion source=cells[row][col];
            if(segment<64)source=new TextureRegion(source,0,0,Math.max(1,Math.round(source.getRegionWidth()*segment/64)),source.getRegionHeight());
            drawCell(batch,source,x+i*64,y,segment,height);
        }
    }
    private void tiledFill(SpriteBatch batch,float x,float y,float width,float height,TextureRegion tile) {
        for(float offset=0;offset<width;offset+=64) {
            float segment=Math.min(64,width-offset);
            TextureRegion source=new TextureRegion(tile,0,0,Math.max(1,Math.round(tile.getRegionWidth()*segment/64)),Math.max(1,Math.round(tile.getRegionHeight()*height/64)));
            drawCell(batch,source,x+offset,y,segment,height);
        }
    }
    private void drawCell(SpriteBatch batch,TextureRegion region,float x,float y,float w,float h){batch.draw(region,x,y,w,h);}
    public void dispose(){background.dispose();atlas.dispose();}
}
