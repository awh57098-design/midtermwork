package com.midtermwork;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** First playable: movement, melee combat and experience progression. */
public class Main extends ApplicationAdapter {
    public interface PointerReader {void read(float[] position);}
    private final PointerReader pointerReader;
    private final float[] pointerPosition=new float[2];
    public Main(){this(null);}
    public Main(PointerReader pointerReader){this.pointerReader=pointerReader;}
    private static final float VIEW_W=1280, VIEW_H=720;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private Texture[] itemTextures;
    private ItemHud.Mask[] itemMasks;
    private com.badlogic.gdx.graphics.g2d.TextureRegion[] itemSprites;
    private Texture charmander,charmeleon,charizard,rattata,ekans;
    private com.badlogic.gdx.graphics.g2d.TextureRegion[] playerSprites;
    private com.badlogic.gdx.graphics.g2d.TextureRegion ratSprite,ekansSprite;
    private OrthographicCamera camera,hud;
    private FitViewport viewport,hudViewport;
    private GameWorld world;
    private float time,pointerHudX,pointerHudY;
    private GameWorld.Item hoveredTool;
    private final java.util.List<ItemHud.RenderedIcon> renderedIcons=new java.util.ArrayList<>();
    private final java.nio.IntBuffer actualViewport=com.badlogic.gdx.utils.BufferUtils.newIntBuffer(4);
    private final com.badlogic.gdx.math.Vector3 iconBottom=new com.badlogic.gdx.math.Vector3(),iconTop=new com.badlogic.gdx.math.Vector3();
    private boolean captured,cursorHidden,cursorFocused=true;
    private float pointerPixelX,pointerPixelY;
    private final com.badlogic.gdx.math.Matrix4 cursorProjection=new com.badlogic.gdx.math.Matrix4();
    private CreatureAnimation heroAnimation,ratAnimation,snakeAnimation;
    private CreatureAnimation[] bossAnimations;
    private ForestEnvironment environment,caveEnvironment,volcanoEnvironment;
    private int displayedStage=1;
    private float cameraX=VIEW_W/2;

    @Override public void create() {
        batch=new SpriteBatch(); shapes=new ShapeRenderer(); font=new BitmapFont(Gdx.files.internal("fonts/korean.fnt"));
        charmander=new Texture("pokemon/charmander.png"); rattata=new Texture("pokemon/rattata.png");
        charmeleon=new Texture("pokemon/charmeleon.png"); charizard=new Texture("pokemon/charizard.png"); ekans=new Texture("pokemon/ekans.png");
        playerSprites=new com.badlogic.gdx.graphics.g2d.TextureRegion[]{trim(charmander),trim(charmeleon),trim(charizard)};
        ratSprite=trim(rattata); ekansSprite=trim(ekans);
        for(Texture texture:new Texture[]{charmander,charmeleon,charizard,rattata,ekans})texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        rattata.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
        itemTextures=new Texture[GameWorld.Item.values().length];
        for(GameWorld.Item item:GameWorld.Item.values()){Texture texture=new Texture("items/"+item.sprite+".png");texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);itemTextures[item.ordinal()]=texture;}
        itemSprites=new com.badlogic.gdx.graphics.g2d.TextureRegion[itemTextures.length];
        itemMasks=new ItemHud.Mask[itemTextures.length];
        for(int i=0;i<itemTextures.length;i++){
            itemSprites[i]=trim(itemTextures[i]);
            com.badlogic.gdx.graphics.g2d.TextureRegion region=itemSprites[i];
            Texture texture=itemTextures[i];texture.getTextureData().prepare();Pixmap pixels=texture.getTextureData().consumePixmap();
            int w=region.getRegionWidth(),h=region.getRegionHeight();boolean[] opaque=new boolean[w*h];
            for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++)opaque[yy*w+xx]=(pixels.getPixel(region.getRegionX()+xx,region.getRegionY()+yy)&255)>=32;
            itemMasks[i]=new ItemHud.Mask(w,h,opaque);pixels.dispose();
        }
        camera=new OrthographicCamera(); viewport=new FitViewport(VIEW_W,VIEW_H,camera);
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),false);
        hud=new OrthographicCamera(VIEW_W,VIEW_H); hud.position.set(VIEW_W/2,VIEW_H/2,0); hud.update();hudViewport=new FitViewport(VIEW_W,VIEW_H,hud);hudViewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
        world=new GameWorld();environment=new ForestEnvironment();caveEnvironment=new ForestEnvironment("cave");volcanoEnvironment=new ForestEnvironment("volcano");
        heroAnimation=new CreatureAnimation("0004",78);ratAnimation=new CreatureAnimation("0019",65);snakeAnimation=new CreatureAnimation("0023",80);
        bossAnimations=new CreatureAnimation[]{new CreatureAnimation("0020",110),new CreatureAnimation("0024",130),new CreatureAnimation("0126",125)};
    }
    @Override public void render() {
        float dt=Math.min(Gdx.graphics.getDeltaTime(),1f/30f);
        if(!world.paused&&world.hp>0&&!world.cleared)time+=dt;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {world.restart();cameraX=VIEW_W/2;}
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){if(world.hp<=0)world.retryStage();else world.advanceStage();}
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) world.paused=!world.paused;
        float move=(Gdx.input.isKeyPressed(Input.Keys.RIGHT)?1:0)-(Gdx.input.isKeyPressed(Input.Keys.LEFT)?1:0);
        int[] skillKeys={Input.Keys.X,Input.Keys.C,Input.Keys.A,Input.Keys.S};
        for(int i=0;i<skillKeys.length;i++)if(Gdx.input.isKeyJustPressed(skillKeys[i]))world.trySkill(i);
        boolean jump=Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean dropped=jump&&Gdx.input.isKeyPressed(Input.Keys.DOWN)&&world.dropThrough();
        world.step(dt,move,jump&&!dropped,held(Input.Keys.J,Input.Keys.Z));
        if(displayedStage!=world.areaNumber()){cameraX=VIEW_W/2;displayedStage=world.areaNumber();}
        float target=MathUtils.clamp(world.x+190,VIEW_W/2,world.worldWidth()-VIEW_W/2);
        if(Math.abs(cameraX-target)>650)cameraX=target;else if(!world.paused)cameraX=MathUtils.lerp(cameraX,target,1-(float)Math.exp(-7*dt));
        float shake=world.paused?0:world.shake*22;
        camera.position.set(cameraX+MathUtils.sin(time*117)*shake,VIEW_H/2+MathUtils.cos(time*97)*shake*.55f,0);
        camera.update(); viewport.apply(false);
        ScreenUtils.clear(.025f,.045f,.055f,1);
        drawWorld();drawHud();updatePointer();drawItemTooltip();drawPointer();
        // Optional framebuffer capture used for local desktop verification.
        String capture=System.getProperty("midtermwork.capture");
        if(capture!=null&&!captured&&time>2) {
            captured=true;
            Pixmap pixels=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.absolute(capture),pixels,-1,true); pixels.dispose();
            Gdx.app.log("midtermwork","Desktop render verified; capture saved."); Gdx.app.exit();
        }
    }
    private boolean held(int a,int b) { return Gdx.input.isKeyPressed(a)||Gdx.input.isKeyPressed(b); }
    private void drawWorld() {
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(camera.combined);batch.begin();
        (world.stage==1?environment:world.stage==2?caveEnvironment:volcanoEnvironment).draw(batch,world,camera.position.x-VIEW_W/2);batch.end();
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.setProjectionMatrix(camera.combined);shapes.begin(ShapeRenderer.ShapeType.Filled);
        for(GameWorld.Shot shot:world.shots){shapes.setColor(.65f,.24f,.86f,1);shapes.circle(shot.x,shot.y,11,16);shapes.setColor(.90f,.59f,1,1);shapes.circle(shot.x-3,shot.y+3,4,12);}
        for(GameWorld.RockFall rock:world.rockFalls)if(rock.warning>0) {
            float surface=112;for(GameWorld.Block b:world.blocks)if(rock.x>=b.x&&rock.x<=b.x+b.w)surface=Math.max(surface,b.y+b.h);
            shapes.setColor(1,.34f,.16f,.65f);
            for(float ry=surface+45;ry<555;ry+=27)shapes.rect(rock.x-2,ry,4,13);
            shapes.rectLine(rock.x-15,surface+5,rock.x+15,surface+25,4);shapes.rectLine(rock.x+15,surface+5,rock.x-15,surface+25,4);
        }
        for(GameWorld.Eruption eruption:world.eruptions){
            if(eruption.warning>0){shapes.setColor(1,.48f,.12f,.8f);shapes.rect(eruption.x-25,eruption.y+2,50,5);shapes.triangle(eruption.x-8,eruption.y+30,eruption.x,eruption.y+45,eruption.x+8,eruption.y+30);}
            else{shapes.setColor(1,.25f,.04f,.85f);shapes.rect(eruption.x-22,eruption.y,44,132);shapes.triangle(eruption.x-22,eruption.y+132,eruption.x,eruption.y+165,eruption.x+22,eruption.y+132);shapes.setColor(1,.85f,.2f,.95f);shapes.rect(eruption.x-9,eruption.y,18,110);}
        }
        drawSkills();
        if(world.attacking()) {
            shapes.setColor(1,.74f,.24f,1);
            float progress=1-world.attackTime/GameWorld.ATTACK_DURATION;
            float ax=world.x+22+world.facing*(45+progress*35);
            shapes.arc(ax-12,world.y+42,32,-70+(world.facing<0?180:0),140,16);
            shapes.setColor(1,.94f,.72f,1);shapes.arc(ax-8,world.y+42,23,-65+(world.facing<0?180:0),130,16);
        }
        for(GameWorld.Enemy e:world.enemies) if(e.alive()&&(!e.boss||world.bossActive)) {
            shapes.setColor(.15f,.25f,.22f,1); shapes.ellipse(e.x-7,e.y-3,62,10,16);
            if(e.phase==GameWorld.Phase.WINDUP) {
                shapes.setColor(1,.76f,.16f,1);shapes.rect(e.dir>0?e.x+48:e.x-140,e.y+3,140,5);
            }
            shapes.setColor(e.phase==GameWorld.Phase.WINDUP?Color.GOLD:e.phase==GameWorld.Phase.ATTACK?Color.SCARLET:e.phase==GameWorld.Phase.RECOVER?Color.SKY:Color.DARK_GRAY);
            shapes.rect(e.x-9,e.y-4,66,5);
            if(!e.boss){shapes.setColor(.17f,.21f,.24f,1); shapes.rect(e.x-3,e.y+95,54,6);
            shapes.setColor(.88f,.38f,.40f,1); shapes.rect(e.x-2,e.y+96,52f*e.hp/e.maxHp,4);}
        }
        shapes.setColor(.15f,.25f,.22f,1); shapes.ellipse(world.x-9,world.y-3,65,11,16);
        drawEffects();
        shapes.end(); batch.setProjectionMatrix(camera.combined); batch.begin();
        for(GameWorld.Drop drop:world.drops){
            float bob=MathUtils.sin(time*4+drop.x)*3;
            batch.setColor(Color.WHITE);batch.draw(itemTextures[drop.item.ordinal()],drop.x-24,drop.y-24+bob,48,48);
        }
        for(GameWorld.Enemy e:world.enemies) if(e.alive()&&(!e.boss||world.bossActive)) {
            batch.setColor(e.hurtTime>0?Color.PINK:e.burnTime>0?new Color(1,.73f,.55f,1):e.slowTime>0?Color.CYAN:Color.WHITE);
            String action;float clock;boolean progress=false,loop=false;float sx=1,sy=1,rotation=0;
            if(e.hurtTime>0){action="Hurt";clock=1-e.hurtTime/.24f;progress=true;rotation=-e.dir*7;}
            else if(e.phase==GameWorld.Phase.WINDUP){action="Charge";clock=e.phaseTime;loop=true;sy=.9f;sx=1.08f;}
            else if(e.phase==GameWorld.Phase.ATTACK){action=e.spitter?"Shoot":"Attack";clock=e.phaseTime/(e.boss&&e.spitter?.7f:e.spitter?.12f:.46f);progress=true;sx=e.spitter?1:1.1f;sy=e.spitter?1:.92f;}
            else if(e.phase==GameWorld.Phase.RECOVER){action="Idle";clock=e.phaseTime;loop=true;sy=.94f;}
            else {action=Math.abs(e.vx)>4?"Walk":"Idle";clock=action.equals("Walk")?e.walkClock:world.elapsed;loop=true;}
            (e.boss?bossAnimations[world.stage-1]:e.spitter?snakeAnimation:ratAnimation).draw(batch,action,clock,progress,loop,e.dir,e.x+e.width()/2,e.y,sx,sy,rotation);
            String state=e.smoked>0?"연막":e.slowTime>0?"둔화":e.burnTime>0?"화상":e.phase==GameWorld.Phase.WINDUP?"!":e.phase==GameWorld.Phase.ATTACK?(e.spitter?"독 발사":"돌진"):e.phase==GameWorld.Phase.RECOVER?"빈틈":"";
            if(!state.isEmpty())text(state,e.x-3,e.y+(e.boss?155:126),1.0f,e.phase==GameWorld.Phase.RECOVER?Color.SKY:Color.GOLD);
        }
        float alpha=world.invulnerable>0&&(int)(time*16)%2==0?.48f:1;
        batch.setColor(1,1,1,alpha);
        if(world.evolution==0) {
            String action;float clock;boolean progress=false,loop=false;float sx=1,sy=1,rotation=0;
            if(world.hurtTime>0){action="Hurt";clock=1-world.hurtTime/.32f;progress=true;rotation=-world.facing*9;}
            else if(world.skillTime>0){action=world.activeSkill==1?"Charge":world.activeSkill==3?"Attack":"Shoot";clock=1-world.skillTime/GameWorld.CAST_DURATION;progress=true;}
            else if(world.attackTime>0){action="Attack";clock=1-world.attackTime/GameWorld.ATTACK_DURATION;progress=true;}
            else if(!world.grounded){action="Hop";clock=world.vy>0?.25f:.72f;progress=true;sx=world.vy>0?.94f:1.03f;sy=world.vy>0?1.05f:.97f;}
            else if(Math.abs(world.vx)>8){action="Walk";clock=world.walkClock;loop=true;rotation=-world.vx/260f*3;}
            else {action="Idle";clock=world.elapsed;loop=true;}
            if(world.landingTime>0){float squash=MathUtils.sin(world.landingTime/.16f*MathUtils.PI)*.12f;sx+=squash;sy-=squash;}
            heroAnimation.draw(batch,action,clock,progress,loop,world.facing,world.x+22,world.y,sx,sy,rotation);
        } else drawSprite(playerSprites[world.evolution],world.x,world.y,GameWorld.PLAYER_W,world.facing>0,0);
        batch.setColor(Color.WHITE); batch.end();
    }
    private void drawSkills() {
        for(GameWorld.Smoke cloud:world.smokeClouds) {
            float opacity=Math.min(1,cloud.life)*.32f;
            shapes.setColor(.24f,.25f,.32f,opacity);
            for(int i=0;i<9;i++){float xx=cloud.x-105+i*27;float yy=cloud.y+MathUtils.sin(time*1.3f+i)*15;shapes.circle(xx,yy,40,18);}
            shapes.setColor(.57f,.57f,.65f,opacity*.6f);
            for(int i=0;i<5;i++)shapes.circle(cloud.x-85+i*42,cloud.y+18+MathUtils.sin(time+i)*9,31,16);
        }
        for(GameWorld.SkillShot shot:world.skillShots) {
            float dir=shot.vx>0?1:-1;
            if(shot.type==0) {
                shapes.setColor(1,.28f,.06f,1);shapes.triangle(shot.x-dir*39,shot.y+7,shot.x,shot.y+15,shot.x,shot.y-14);
                shapes.circle(shot.x,shot.y,15,16);shapes.setColor(1,.79f,.2f,1);shapes.circle(shot.x+dir*3,shot.y+2,10,14);
                shapes.setColor(1,.97f,.65f,1);shapes.circle(shot.x+dir*5,shot.y+3,5,12);
            } else {
                for(int i=6;i>=0;i--){shapes.setColor(.24f,.82f,1,1-i*.11f);shapes.circle(shot.x-dir*i*12,shot.y+MathUtils.sin(time*25-i)*5,14-i,14);}
                shapes.setColor(.82f,1,1,1);shapes.circle(shot.x+dir*2,shot.y,8,12);
            }
        }
        for(GameWorld.Enemy e:world.enemies)if(e.alive()&&e.burnTime>0) {
            shapes.setColor(1,.4f,.08f,.8f);for(int i=0;i<3;i++)shapes.triangle(e.x+i*15,e.y+8,e.x+i*15+6,e.y+28+MathUtils.sin(time*16+i)*7,e.x+i*15+12,e.y+8);
        }
    }
    private void drawEffects() {
        for(GameWorld.Effect fx:world.effects) {
            float p=fx.age/fx.life;
            if(fx.kind==3) {
                shapes.setColor(1,.26f,.08f,1-p);shapes.ellipse(fx.x-42,fx.y-32,84,64,20);
                shapes.setColor(1,.92f,.65f,1-p);
                for(int i=0;i<3;i++){float tx=fx.x-30+i*22;float opening=(1-p)*20;shapes.triangle(tx,fx.y+opening+15,tx+9,fx.y+opening-9,tx+16,fx.y+opening+15);shapes.triangle(tx,fx.y-opening-15,tx+9,fx.y-opening+9,tx+16,fx.y-opening-15);}
            } else if(fx.kind==0) {
                shapes.setColor(.76f,.79f,.61f,1-p);
                for(int i=0;i<4;i++)shapes.circle(fx.x+(i-1.5f)*(5+p*18)+fx.dir*p*15,fx.y+4+p*(12+i*2),Math.max(1,5*(1-p)),10);
            } else if(fx.kind==1) {
                shapes.setColor(1,.93f,.65f,1-p);
                for(int i=0;i<7;i++){float a=i*MathUtils.PI2/7;float r=8+p*44;float cx=fx.x+MathUtils.cos(a)*r,cy=fx.y+MathUtils.sin(a)*r;
                    shapes.rectLine(cx,cy,cx+MathUtils.cos(a)*(10-p*7),cy+MathUtils.sin(a)*(10-p*7),3*(1-p)+1);}
                if(p<.22f)shapes.circle(fx.x,fx.y,14*(1-p),16);
            } else {
                shapes.setColor(.99f,.77f,.36f,1-p);
                for(int i=0;i<8;i++){float a=i*MathUtils.PI2/8;shapes.circle(fx.x+MathUtils.cos(a)*p*45,fx.y+MathUtils.sin(a)*p*35+p*30,4*(1-p)+1,10);}
            }
        }
    }
    private com.badlogic.gdx.graphics.g2d.TextureRegion trim(Texture texture) {
        texture.getTextureData().prepare(); Pixmap p=texture.getTextureData().consumePixmap();
        int minX=p.getWidth(),minY=p.getHeight(),maxX=0,maxY=0;
        for(int yy=0;yy<p.getHeight();yy++)for(int xx=0;xx<p.getWidth();xx++)if((p.getPixel(xx,yy)&255)!=0){minX=Math.min(minX,xx);minY=Math.min(minY,yy);maxX=Math.max(maxX,xx);maxY=Math.max(maxY,yy);}
        p.dispose();
        return new com.badlogic.gdx.graphics.g2d.TextureRegion(texture,minX,minY,maxX-minX+1,maxY-minY+1);
    }
    private void drawSprite(com.badlogic.gdx.graphics.g2d.TextureRegion region,float x,float y,float width,boolean flip,float bob) {
        float scale=region==playerSprites[2]?1.6f:2;
        float w=region.getRegionWidth()*scale,h=region.getRegionHeight()*scale;
        Texture tex=region.getTexture();
        batch.draw(tex,x+width/2-w/2,y+bob,w,h,region.getRegionX(),region.getRegionY(),region.getRegionWidth(),region.getRegionHeight(),flip,false);
    }
    private void drawSkillSlots() {
        for(int i=0;i<4;i++) {
            GameWorld.Skill skill=GameWorld.Skill.values()[i];float sx=30+i*306;
            shapes.setColor(.15f,.25f,.24f,1);shapes.rect(sx,9,288,49);
            boolean unlocked=world.skillUnlocked(i);
            shapes.setColor(unlocked?i==0?new Color(.93f,.38f,.13f,1):i==1?new Color(.46f,.46f,.55f,1):i==2?new Color(.22f,.70f,.82f,1):new Color(.84f,.24f,.17f,1):Color.DARK_GRAY);
            shapes.rect(sx,9,35,49);
            shapes.setColor(.98f,.74f,.3f,1);
            float ready=unlocked?1-world.skillCooldowns[i]/(skill.cooldown*world.cooldownScale()):0;
            shapes.rect(sx+40,12,241*MathUtils.clamp(ready,0,1),3);
        }
    }
    private void heart(float x,float y,int halves,Color color) {
        shapes.setColor(color);
        if(halves>=1){shapes.circle(x+8,y+18,8,16);shapes.triangle(x,y+18,x+16,y+18,x+16,y);}
        if(halves>=2){shapes.circle(x+24,y+18,8,16);shapes.triangle(x+16,y,x+16,y+18,x+32,y+18);}
    }
    private void drawHud() {
        hudViewport.apply();
        shapes.setProjectionMatrix(hud.combined); shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.08f,.17f,.15f,1); shapes.rect(0,600,VIEW_W,120); shapes.rect(0,0,VIEW_W,88);
        shapes.setColor(.98f,.61f,.25f,1); shapes.rect(0,600,5,120);
        shapes.setColor(.17f,.24f,.27f,1);  shapes.rect(760,610,300,12);
        for(int i=0;i<world.maxHp/2;i++){heart(520+i*40,620,2,new Color(.24f,.29f,.33f,1));heart(520+i*40,620,Math.max(0,Math.min(2,world.hp-i*2)),new Color(.97f,.32f,.39f,1));}
        shapes.setColor(.99f,.73f,.28f,1); shapes.rect(760,610,300f*world.xp/world.xpRequired(),12);
        if(world.bossActive&&!world.bossDefeated()){
            shapes.setColor(.16f,.12f,.18f,1);shapes.rect(330,545,620,45);
            shapes.setColor(.77f,.20f,.27f,1);shapes.rect(342,551,596f*world.boss.hp/world.boss.maxHp,11);
        }
        drawSkillSlots();
        shapes.end(); batch.setProjectionMatrix(hud.combined); batch.begin();
        if(world.bossActive&&!world.bossDefeated())text("보스 / "+world.bossName()+"   "+world.boss.hp+" / "+world.boss.maxHp,342,582,1.15f,Color.WHITE);
        text("보유 도구 / 마우스를 올려 효과 확인",26,705,.9f,Color.GOLD);
        renderedIcons.clear();
        Gdx.gl.glGetIntegerv(com.badlogic.gdx.graphics.GL20.GL_VIEWPORT,actualViewport);
        int itemSlot=0;
        for(GameWorld.Item item:GameWorld.Item.values())if(item.held()&&world.itemCount(item)>0){
            float ix=ItemHud.x(itemSlot),iy=ItemHud.y(itemSlot);
            float drawX=ix+ItemHud.IMAGE_OFFSET,drawY=iy+ItemHud.IMAGE_OFFSET;
            batch.setColor(Color.WHITE);batch.draw(itemSprites[item.ordinal()],drawX,drawY,ItemHud.IMAGE_SIZE,ItemHud.IMAGE_SIZE);
            // Use the exact draw transform and current GL viewport, not a second copy of the layout.
            iconBottom.set(drawX,drawY,0).mul(batch.getTransformMatrix()).prj(batch.getProjectionMatrix());
            iconTop.set(drawX+ItemHud.IMAGE_SIZE,drawY+ItemHud.IMAGE_SIZE,0).mul(batch.getTransformMatrix()).prj(batch.getProjectionMatrix());
            float left=actualViewport.get(0)+(iconBottom.x+1)*actualViewport.get(2)/2f;
            float top=Gdx.graphics.getBackBufferHeight()-(actualViewport.get(1)+(iconTop.y+1)*actualViewport.get(3)/2f);
            float width=(iconTop.x-iconBottom.x)*actualViewport.get(2)/2f,height=(iconTop.y-iconBottom.y)*actualViewport.get(3)/2f;
            renderedIcons.add(new ItemHud.RenderedIcon(item,left,top,width,height,itemMasks[item.ordinal()]));
            text("x"+world.itemCount(item),ix+32,iy+14,.8f,Color.GOLD);itemSlot++;
        }
        if(itemSlot==0)text("적을 처치하면 도구가 나옵니다!",28,663,.95f,Color.LIGHT_GRAY);
        if(world.itemNoticeTime>0)text(world.itemNotice,35,530,1.1f,Color.GOLD);
        text(world.species(),520,693,1.65f,Color.WHITE);
        text("하트  "+(world.hp/2f)+" / "+(world.maxHp/2),520,661,1.05f,Color.WHITE);
        text("Lv."+world.level+"   경험치 "+world.xp+" / "+world.xpRequired(),760,642,1.15f,Color.WHITE);
        text("공격력 "+world.attackDamage(),1100,640,.95f,Color.GOLD);
        text(world.stageLabel(),870,689,1.35f,Color.WHITE);
        text(world.mapTitle()+"  |  구역 "+world.areaNumber()+" / 15",870,662,.9f,new Color(.7f,.85f,.77f,1));
        text("방향키 이동   Space 점프   ↓+Space 발판 내려가기   J/Z 공격   X C A S 기술   Esc 일시정지   R 처음부터",30,79,.95f,Color.WHITE);
        for(int i=0;i<4;i++){GameWorld.Skill skill=GameWorld.Skill.values()[i];float sx=30+i*306;
            Color tint=world.skillUnlocked(i)?Color.WHITE:Color.GRAY;
            text(skill.key,sx+10,45,1.5f,tint);text(skill.name,sx+45,49,1.0f,tint);
            String status=!world.skillUnlocked(i)?"잠김 / Lv."+skill.unlockLevel:world.skillCooldowns[i]>.05f?String.format(java.util.Locale.ROOT,"%.1f초",world.skillCooldowns[i]):"사용 가능";
            text(status,sx+45,28,.9f,world.skillUnlocked(i)&&world.skillCooldowns[i]<=.05f?Color.GOLD:tint);
        }
        if(world.messageTime>0&&!world.cleared&&!world.stageComplete&&!world.bossActive&&world.hp>0) text(world.message,35,568,1.1f,world.stage>=2?Color.WHITE:new Color(.08f,.22f,.23f,1));
        batch.end();
        if(world.paused||world.hp<=0||world.cleared||world.stageComplete) {
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapes.begin(ShapeRenderer.ShapeType.Filled); shapes.setColor(.03f,.10f,.13f,.88f); shapes.rect(250,245,780,230); shapes.end();
            Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND); batch.begin();
            text(world.hp<=0?"쓰러짐":world.paused?"일시정지":world.stageComplete?"구역 완료!":"모험 완료!",330,407,2.2f,Color.WHITE);
            text(world.paused?"Esc: 계속하기 / R: 처음부터":world.hp<=0?"Enter: 이 맵 재도전 / R: 새 모험":world.stageComplete?("다음: "+world.nextAreaLabel()+" / Enter로 이동"):"보스 3마리 격파! R: 처음부터",330,347,1.25f,Color.WHITE);
            text("Lv."+world.level+"    |    처치한 적 "+world.totalKills+"마리",360,300,1.2f,Color.GOLD); batch.end();
        }
    }
    private void updatePointer(){
        if(pointerReader!=null)pointerReader.read(pointerPosition);
        else {pointerPosition[0]=Gdx.input.getX();pointerPosition[1]=Gdx.input.getY();}
        pointerHudX=ItemHud.pointerX(pointerPosition[0],hudViewport.getScreenX(),hudViewport.getScreenWidth());
        pointerHudY=ItemHud.pointerY(pointerPosition[1],Gdx.graphics.getHeight(),hudViewport.getScreenY(),hudViewport.getScreenHeight());
        hoveredTool=null;
        pointerPixelX=pointerPosition[0]*Gdx.graphics.getBackBufferWidth()/Gdx.graphics.getWidth();
        pointerPixelY=pointerPosition[1]*Gdx.graphics.getBackBufferHeight()/Gdx.graphics.getHeight();
        for(ItemHud.RenderedIcon icon:renderedIcons)if(icon.contains(pointerPixelX,pointerPixelY)){hoveredTool=icon.item;break;}
    }
    private void drawPointer(){
        if(Gdx.app.getType()!=com.badlogic.gdx.Application.ApplicationType.Desktop)return;
        boolean inside=cursorFocused&&pointerPixelX>=0&&pointerPixelY>=0&&pointerPixelX<Gdx.graphics.getBackBufferWidth()&&pointerPixelY<Gdx.graphics.getBackBufferHeight();
        if(inside!=cursorHidden){Gdx.graphics.setSystemCursor(inside?com.badlogic.gdx.graphics.Cursor.SystemCursor.None:com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow);cursorHidden=inside;}
        if(!inside)return;
        // Draw last in framebuffer space: the cursor center is exactly the point used by icon hit testing.
        int width=Gdx.graphics.getBackBufferWidth(),height=Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0,0,width,height);
        cursorProjection.setToOrtho2D(0,0,width,height);shapes.setProjectionMatrix(cursorProjection);
        float x=pointerPixelX,y=height-pointerPixelY,r=Math.max(6,hudViewport.getScreenWidth()*7f/1280*width/Gdx.graphics.getWidth());
        shapes.begin(ShapeRenderer.ShapeType.Filled);shapes.setColor(Color.BLACK);
        shapes.rect(x-r-2,y-2,r*2+4,4);shapes.rect(x-2,y-r-2,4,r*2+4);
        shapes.setColor(Color.RED);shapes.rect(x-r,y-1,r*2,2);shapes.rect(x-1,y-r,2,r*2);shapes.end();
    }
    private void drawItemTooltip(){
        GameWorld.Item item=hoveredTool;if(item==null)return;
        float px=pointerHudX,py=pointerHudY;hudViewport.apply();
        float ix=px,top=Math.min(598,py-10),width=440,height=220;
        ix=MathUtils.clamp(ix,8,VIEW_W-width-8);top=Math.max(height+8,top);
        shapes.setProjectionMatrix(hud.combined);shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(.9f,.75f,.4f,1);shapes.rect(ix,top-height,width,height);
        shapes.setColor(.07f,.12f,.17f,1);shapes.rect(ix+3,top-height+3,width-6,height-6);shapes.end();
        batch.setProjectionMatrix(hud.combined);batch.begin();batch.setColor(Color.WHITE);
        batch.draw(itemSprites[item.ordinal()],ix+14,top-42,32,32);
        text(item.title+"  x"+world.itemCount(item),ix+57,top-15,1.15f,Color.GOLD);
        font.getData().setScale(1.05f);font.setColor(Color.WHITE);
        font.draw(batch,world.itemSummary(item),ix+15,top-57,width-30,com.badlogic.gdx.utils.Align.left,true);
        font.getData().setScale(.95f);font.setColor(Color.LIGHT_GRAY);
        font.draw(batch,world.itemDescription(item),ix+15,top-109,width-30,com.badlogic.gdx.utils.Align.left,true);
        text("보유 수량: "+world.itemCount(item)+"개 / 수집 제한 없음",ix+15,top-height+20,.8f,Color.GOLD);
        batch.end();
    }
    private void text(String value,float x,float y,float scale,Color color) {
        font.getData().setScale(scale); font.setColor(color); font.draw(batch,value,x,y);
    }
    @Override public void resize(int width,int height) { if(viewport!=null) viewport.update(width,height,false);if(hudViewport!=null)hudViewport.update(width,height,true); }
    @Override public void pause() {cursorFocused=false;if(cursorHidden){Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow);cursorHidden=false;}if(world!=null)world.paused=true;}
    @Override public void resume(){cursorFocused=true;}
    @Override public void dispose() { if(cursorHidden)Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow); for(Texture texture:itemTextures)texture.dispose(); environment.dispose();caveEnvironment.dispose();volcanoEnvironment.dispose();for(CreatureAnimation animation:bossAnimations)animation.dispose();heroAnimation.dispose();ratAnimation.dispose();snakeAnimation.dispose();batch.dispose(); shapes.dispose(); font.dispose(); charmander.dispose(); charmeleon.dispose(); charizard.dispose(); rattata.dispose(); ekans.dispose(); }
}
