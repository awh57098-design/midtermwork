package com.midtermwork;
/** Shared HUD geometry in the fixed 1280x720 logical viewport. */
public final class ItemHud {
    private ItemHud(){}
    public static float pointerX(float screenX,int viewportX,int viewportWidth){return viewportWidth<=0?-1:(screenX-viewportX)*1280f/viewportWidth;}
    public static float pointerY(float screenY,int windowHeight,int viewportY,int viewportHeight){return viewportHeight<=0?-1:(windowHeight-screenY-viewportY)*720f/viewportHeight;}
    public static float x(int slot){return 22+(slot%7)*66;}
    public static float y(int slot){return 645-(slot/7)*39;}
    public static final float IMAGE_OFFSET=2,IMAGE_SIZE=32;
    public static final class Mask {
        private final int width,height;private final boolean[] pixels;
        public Mask(int width,int height,boolean[] pixels){
            if(width<=0||height<=0||pixels.length!=width*height)throw new IllegalArgumentException("Invalid icon mask");
            this.width=width;this.height=height;this.pixels=pixels.clone();
        }
        public boolean contains(float u,float v){
            if(u<0||v<0||u>=1||v>=1)return false;
            int px=Math.min(width-1,(int)(u*width)),py=Math.min(height-1,(int)((1-v)*height));
            return pixels[py*width+px];
        }
    }
    /** Actual framebuffer rectangle projected by the renderer, origin at the top left. */
    public static final class RenderedIcon {
        public final GameWorld.Item item;public final float left,top,width,height;private final Mask mask;
        public RenderedIcon(GameWorld.Item item,float left,float top,float width,float height,Mask mask){
            this.item=item;this.left=left;this.top=top;this.width=width;this.height=height;this.mask=mask;
        }
        public boolean contains(float screenX,float screenY){
            if(width<=0||height<=0||screenX<left||screenX>=left+width||screenY<top||screenY>=top+height)return false;
            return mask.contains((screenX-left)/width,1-(screenY-top)/height);
        }
    }
    public static GameWorld.Item at(GameWorld world,float x,float y){return at(world,x,y,null);}
    public static GameWorld.Item at(GameWorld world,float x,float y,Mask[] masks){
        int slot=0;for(GameWorld.Item item:GameWorld.Item.values())if(item.held()&&world.itemCount(item)>0){
            float u=(x-x(slot)-IMAGE_OFFSET)/IMAGE_SIZE,v=(y-y(slot)-IMAGE_OFFSET)/IMAGE_SIZE;
            if(u>=0&&u<1&&v>=0&&v<1&&(masks==null||masks[item.ordinal()]!=null&&masks[item.ordinal()].contains(u,v)))return item;
            slot++;
        }return null;
    }
}
