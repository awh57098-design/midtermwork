import com.midtermwork.*;import com.badlogic.gdx.*;import com.badlogic.gdx.graphics.*;import com.badlogic.gdx.graphics.g2d.*;import com.badlogic.gdx.backends.lwjgl3.*;
public class FramebufferHoverCheck {
 static float[] mouse={-100,-100};
 public static void main(String[] a){int width=Integer.parseInt(a[0]),height=Integer.parseInt(a[1]);Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();c.setWindowedMode(width,height);c.setTitle("midtermwork framebuffer hover verification");c.disableAudio(true);c.setForegroundFPS(60);
 new Lwjgl3Application(new Main(pos->{pos[0]=mouse[0];pos[1]=mouse[1];}){int phase;Pixmap visible;Texture blank;TextureRegion original;int chosenX,chosenY,bottom;TextureRegion[] sprites;
 Object field(String name){try{java.lang.reflect.Field f=Main.class.getDeclaredField(name);f.setAccessible(true);return f.get(this);}catch(Exception e){throw new RuntimeException(e);}}
 Pixmap capture(){return Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());}
 void pointPixel(int x,int topY){mouse[0]=(x+.5f)*Gdx.graphics.getWidth()/Gdx.graphics.getBackBufferWidth();mouse[1]=(topY+.5f)*Gdx.graphics.getHeight()/Gdx.graphics.getBackBufferHeight();}
 public void create(){super.create();GameWorld w=(GameWorld)field("world");w.collectItem(GameWorld.Item.MULTI);w.itemNoticeTime=w.messageTime=0;w.paused=true;sprites=(TextureRegion[])field("itemSprites");original=sprites[GameWorld.Item.MULTI.ordinal()];Pixmap pixel=new Pixmap(1,1,Pixmap.Format.RGBA8888);blank=new Texture(pixel);pixel.dispose();}
 public void render(){super.render();if(phase==0){visible=capture();sprites[GameWorld.Item.MULTI.ordinal()]=new TextureRegion(blank);phase++;return;}
 if(phase==1){Pixmap absent=capture();int minX=99999,minY=99999,maxX=-1,maxY=-1,count=0;
  for(int y=0;y<visible.getHeight();y++)for(int x=0;x<visible.getWidth();x++)if(visible.getPixel(x,y)!=absent.getPixel(x,y)){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);count++;}
  if(count<10||maxX-minX>80||maxY-minY>80)throw new AssertionError("Unexpected icon pixel difference: "+count);
  float best=Float.MAX_VALUE;for(int y=minY;y<=maxY;y++)for(int x=minX;x<=maxX;x++)if(visible.getPixel(x,y)!=absent.getPixel(x,y)){float d=(x-(minX+maxX)/2f)*(x-(minX+maxX)/2f)+(y-(minY+maxY)/2f)*(y-(minY+maxY)/2f);if(d<best){best=d;chosenX=x;chosenY=visible.getHeight()-1-y;}}
  bottom=visible.getHeight()-1-minY;
  System.out.println("Measured actual icon pixels: x="+minX+".."+maxX+", screenY="+(visible.getHeight()-1-maxY)+".."+bottom+", count="+count);
  absent.dispose();visible.dispose();sprites[GameWorld.Item.MULTI.ordinal()]=original;pointPixel(chosenX,chosenY);phase++;return;
 }
 if(phase==2){if(field("hoveredTool")!=GameWorld.Item.MULTI)throw new AssertionError("A visibly painted framebuffer pixel did not hover at "+chosenX+","+chosenY);
  Pixmap image=capture();if(image.getPixel(chosenX,image.getHeight()-1-chosenY)!=0xff0000ff)throw new AssertionError("Visible crosshair center does not match the hovered pixel");if(!(Boolean)field("cursorHidden"))throw new AssertionError("System cursor was not hidden");PixmapIO.writePNG(Gdx.files.local(System.getProperty("midtermwork.hoverOutput","../build/hover-check")+"/software-cursor-"+width+"x"+height+".png"),image,-1,true);image.dispose();pointPixel(chosenX,bottom+9);phase++;return;
 }
 if(phase==3){if(field("hoveredTool")!=null)throw new AssertionError("Below the visibly painted icon triggered a tooltip");GameWorld w=(GameWorld)field("world");w.x=2500;pointPixel(chosenX,chosenY);phase++;return;}if(phase==4){if(field("hoveredTool")!=GameWorld.Item.MULTI)throw new AssertionError("Map camera movement changed hover");mouse[0]=-20;mouse[1]=-20;phase++;return;}if(phase==5){if((Boolean)field("cursorHidden"))throw new AssertionError("System cursor not restored outside window");System.out.println("PASS: visible crosshair center AND tooltip on measured rendered pixel "+chosenX+","+chosenY+"; no tooltip below image; size "+width+"x"+height);blank.dispose();Gdx.app.exit();phase++;}
 }
 },c);}
}