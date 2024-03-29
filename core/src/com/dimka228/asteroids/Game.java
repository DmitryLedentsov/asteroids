package com.dimka228.asteroids;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.BloomEffect;
import com.crashinvaders.vfx.effects.BloomEffect.Settings;
import com.crashinvaders.vfx.effects.GaussianBlurEffect.BlurType;
import com.dimka228.asteroids.objects.*;
import com.dimka228.asteroids.objects.interfaces.GameObject;
import com.dimka228.asteroids.objects.interfaces.GameObject.Status;
import com.dimka228.asteroids.objects.interfaces.GameObject.Type;
import com.dimka228.asteroids.objects.particles.ExplosionParticle;
import com.dimka228.asteroids.objects.particles.ThrustParticle;
import com.dimka228.asteroids.physics.CollusionListener;
import com.dimka228.asteroids.utils.AnyShapeIntersector;

import com.dimka228.asteroids.utils.Random;
import com.dimka228.asteroids.utils.VectorUtils;

import static com.dimka228.asteroids.utils.VectorUtils.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import space.earlygrey.shapedrawer.ShapeDrawer;

public class Game extends ApplicationAdapter {
	public static final int SCREEN_WIDTH = 1000;
	public static final int SCREEN_HEIGHT = 800;
	public static final int WORLD_WIDTH = 1000;
	public static final int WORLD_HEIGHT = 450;

	public static final float WORLD_TO_VIEW = 7f;
   	public static final float VIEW_TO_WORLD = 1 / WORLD_TO_VIEW;

	public final int PLAYER_OFFSET = 100;
	public final int GAP = 200;
	public final String TITLE = "игра епта";

	private static Game instance;
	public static  Game getInstance() {
		if (instance == null) {
			instance = new Game();
		}
		return instance;
	}

	SpriteBatch renderer;
	ShapeDrawer shapeDrawer;
	
	FrameBuffer fbo;
	Viewport viewport;
	VfxManager vfxManager;
	// Texture background;
	// Sprite backgroundSprite;

	volatile float x = 10;
	float elasticity = 1;

	private LinkedList<GameObject> objects;
	private LinkedList<GameObject> newObjects;
	private Player player;
	private volatile long count;
	private boolean isRunning;
	private Stage stage;
	private OrthographicCamera camera;
	World world;


	

	public long getTick() {
		return count;
	}

	public void stop() {
		isRunning = false;
	}

	public void addObjectDirectly(GameObject o) {
		objects.add(o);
	}
	public void addObject(GameObject o){
		newObjects.add(o);
	}



	Label bodyCount;

	ShaderProgram glowShader,fxaaShader;
	@Override
	public void create() {
		isRunning = true;
		world = new World(new Vector2(0,0), true);
		
		/*/
		glowShader = new ShaderProgram(Gdx.files.internal("shaders/Vertex.glsl"), Gdx.files.internal("shaders/GlowFragment.glsl"));
		if(glowShader.isCompiled())Gdx.app.log("Glow shader Log", '\n' + glowShader.getLog());
		else Gdx.app.error("Glow shader Log", '\n' + glowShader.getLog());
		
		fxaaShader = new ShaderProgram(Gdx.files.internal("shaders/Vertex.glsl"), Gdx.files.internal("shaders/fxaaFragment.glsl"));
		if(fxaaShader.isCompiled())Gdx.app.log("Fxaa shader Log", '\n' + fxaaShader.getLog());
		else Gdx.app.error("Fxaa shader Log", '\n' + fxaaShader.getLog());
		
		
		renderer = new PolygonSpriteBatch(200,200,glowShader);*/

		//glowShader = new ShaderProgram(Gdx.files.internal("shaders/Vertex.glsl"), Gdx.files.internal("shaders/GlowFragment.glsl"));
		//if(glowShader.isCompiled())Gdx.app.log("Glow shader Log", '\n' + glowShader.getLog());
		//else Gdx.app.error("Glow shader error", '\n' + glowShader.getLog());
		
		
		renderer = new SpriteBatch();
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.drawPixel(0, 0);
		Texture texture = new Texture(pixmap); //remember to dispose of later
		pixmap.dispose();
		TextureRegion region = new TextureRegion(texture, 0, 0, 1, 1);
		shapeDrawer = new ShapeDrawer(renderer,region);
		
		vfxManager = new VfxManager(Format.RGBA8888);
		BloomEffect be = new BloomEffect();
		be.setBlurType(BlurType.Gaussian5x5b);
		be.setBlurPasses(20);
		be.setBlurAmount(0.1f);
		//be.setBaseSaturation(2);
		be.setBloomIntensity(2.5f);
		be.setBloomSaturation(2f);
		be.setThreshold(0.3f);
		vfxManager.addEffect(be);
		//FrameBuffer fbo =  new FrameBuffer(Format.RGBA4444, SCREEN_WIDTH, SCREEN_HEIGHT, false,false);
		shapeDrawer.setDefaultLineWidth(1);


		
		



		//renderer.setShader(null);
		objects = new LinkedList<>();
		newObjects = new LinkedList<>();
		// backgroundObjects = new LinkedList<>();
		

		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		viewport = new ExtendViewport(SCREEN_WIDTH,SCREEN_HEIGHT,camera);
		viewport.apply();
		stage = new Stage(new ScreenViewport());
	

		Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(),Color.BROWN);
		
		bodyCount = new Label("bodies", labelStyle);
		bodyCount.setSize(20, 10);
		stage.addActor(bodyCount);

		bodyCount.setPosition(10, 10);



		player = new Player(100,100, Teams.A);
		objects.add(player);

		// objects.add(new Wall(this));
		// background = new Background(this,WIDTH);
		addObjectDirectly(new Wall(0, WORLD_HEIGHT/2, 10, WORLD_HEIGHT/2));
		addObjectDirectly(new Wall(WORLD_WIDTH, WORLD_HEIGHT/2, 10, WORLD_HEIGHT/2));
		addObjectDirectly(new Wall(WORLD_WIDTH/2, 0, WORLD_WIDTH/2, 10));
		addObjectDirectly(new Wall(WORLD_WIDTH/2, WORLD_HEIGHT, WORLD_WIDTH/2, 10));
		

		objects.add(new ExplosionParticle(0,0));

		for(int i=0; i<100; i++){
			Asteroid a = new Asteroid(MathUtils.random()*WORLD_WIDTH, MathUtils.random()*WORLD_HEIGHT);
			addObjectDirectly(a);
		}

		for(int i=0; i<100; i++){
			SimpleBot a = new SimpleBot((WORLD_WIDTH/6), MathUtils.random()*WORLD_HEIGHT, Teams.A);
			addObjectDirectly(a);
			
			SimpleBot b = new SimpleBot(WORLD_WIDTH*5/6, MathUtils.random()*WORLD_HEIGHT, Teams.B);
			addObjectDirectly(b);
		}
		
		for(int i=0; i<10; i++){
			SimpleBot a = new SimpleBot(WORLD_WIDTH/2 , MathUtils.random()*WORLD_HEIGHT, Teams.C);
			addObjectDirectly(a);
		}
		//objects.add(player);
		world.setContactListener(new CollusionListener());
		//PhysicsWorld<PhysicsBody world = new P

		
	}

	@Override
   	public void resize(int width, int height){
    	viewport.update(width, height);
    	camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
   	}

	public void updateBackground() {

	}

	@Override
	public void render() {
	
		/*Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glBlendFunc( GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA );
        Gdx.gl.glEnable( GL20.GL_BLEND );*/
		Gdx.gl.glColorMask(true, true, true, true);
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		bodyCount.setText(Integer.toString(world.getBodyCount()) + " : " + Integer.toString(objects.size()));
		stage.draw();
		stage.act();
		
		//camera.position.set(100, 100, 0);
		
		if(player!=null) {
		camera.position.set(player.getViewPosition().x, player.getViewPosition().y, 0);
		camera.update();

		renderer.setProjectionMatrix(camera.combined);
		}
		else if(player.getStatus()==Status.DESTROYED) player = null;


		if (Gdx.input.isKeyJustPressed(Input.Keys.P))
			isRunning = !isRunning;
	

		
		
		//glowShader.bind();
		//glowShader.setUniformf("u_texelSize", new Vector2(1f / Gdx.graphics.getWidth() * 4,  1f / Gdx.graphics.getHeight() * 4));
		//glowShader.setUniformi("horizontal", 0);
		//if(isRunning)renderer.setShader(glowShader);
		//else renderer.setShader(null);
		//glowShader.bind();
		
		vfxManager.setDisabled(!isRunning);
		vfxManager.cleanUpBuffers();
		vfxManager.beginInputCapture();

		renderer.begin();

		//renderer.flush();
		shapeDrawer.setColor(Color.GREEN);
		shapeDrawer.setDefaultLineWidth(3);
		shapeDrawer.rectangle(10*WORLD_TO_VIEW, 10*WORLD_TO_VIEW, (WORLD_WIDTH-20)*WORLD_TO_VIEW, (WORLD_HEIGHT-20)*WORLD_TO_VIEW);

		
		objects.addAll(newObjects);
		newObjects.clear();
		Iterator<GameObject> iterator = objects.iterator();
		while(iterator.hasNext()){
			GameObject obj = iterator.next();
			if(obj.getStatus()==Status.DESTROYED){
				obj.destroy();
				iterator.remove();
			} else {
				if( isRunning)obj.update();
				if(obj.getStatus()!=Status.DESTROYED && (VectorUtils.distance(obj.getPosition(), player.getPosition())<250 || player==null)) obj.render();
			}
		}
		
		

		/*objects.stream().sorted(new GameObject.SortingComparator()).forEachOrdered((obj) -> {
			
			if (isRunning || obj.getType() == Type.BACKGROUND)
				obj.update();

			obj.render();
		
			if (!isRunning)
				return;
			if (obj.getType() == Type.BACKGROUND)
				return;
			if (player != obj && CollisionUtils.collides(obj.getBody().getShape(), player.getBody().getShape())) {
	
				player.collide(obj);
				obj.collide(player);
				//CollisionUtils.processCollusion(player.getBody(), obj.getBody());
				//System.out.println("player collides obj");
			}
		});*/
		
		
		
		//renderer.flush();
		
		renderer.end();
		vfxManager.endInputCapture();
		vfxManager.applyEffects();
		vfxManager.renderToScreen();
		if(isRunning)world.step(0.3f, 10, 10);

		
		//if(Teams.A.getPlayers().size()<100)  addObject(new SimpleBot((WORLD_WIDTH/6), MathUtils.random()*WORLD_HEIGHT, Teams.A));
		//if(Teams.B.getPlayers().size()<100) addObject(new SimpleBot(WORLD_WIDTH*5/6, MathUtils.random()*WORLD_HEIGHT, Teams.B));
	}

	public SpriteBatch getRenderer() {
		return renderer;
	}

	public ShapeDrawer getDrawer(){
		return shapeDrawer;
	}
	public Camera getCamera() {
		return camera;
	}

	public Player getPlayer() {
		return player;
	}

	public World getWorld(){
		return world;
	}

	public Deque<GameObject> getObjects() {
		return objects;
	}

	@Override
	public void dispose() {
		renderer.dispose();
		// background.dispose();
	}
}
