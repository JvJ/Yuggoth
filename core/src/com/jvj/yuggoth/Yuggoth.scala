package com.jvj.yuggoth

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.ApplicationAdapter
import com.jvj.ecs._
import com.jvj.gdxutil._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.utils.GdxNativesLoader
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode._
import com.jvj.yuggoth.entities._
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.Input


object SysDebug extends System{
  override def apply(ec:EntityCollection) = {
	
    KeyState('TogglePhysics) match {
      case Some(Pressed) => Glob.debugPhysics = !Glob.debugPhysics 
      case _ => ;
    }
    (KeyState('ZoomIn), KeyState('ZoomOut)) match {
      case (Some(Pressed | Held(_)), None) => SysRender.camera .zoom -= 0.05f
      case (None, Some(Pressed | Held(_))) => SysRender.camera .zoom += 0.05f
      case _ => ;
    }
    
    ec
  }
  
  override def apply (ec:EntityCollection, e:Entity) = ec
}

class Yuggoth extends ApplicationAdapter{
  
  
  
  var batch:SpriteBatch = null
  var img:Texture = null
  var ents:EntityCollection = null
  var sysPhysics:SysPhysics = null
  var sysPhysicsRender:SysPhysicsRender = null
  
  override def create(){
    
    batch = new SpriteBatch()
    img = new Texture("badlogic.jpg")
    
    var w = Gdx.graphics.getWidth()
    
    SysRender.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())
    SysRender.camera.translate(new Vector2(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2))
    SysRender.batch = batch
    // TODO: YAY!!!!!!!!!!  It's working!
    // Keep this up!
    
    
    SysRender.position = new Vector2(0,0)
    SysRender.pixToWorld = new Vector2(64f, 64f)
    
    sysPhysics  = new SysPhysics(Glob.gravity)
    sysPhysicsRender = new SysPhysicsRender(sysPhysics, true)
    
    // Input
    KeyState.setMappings(Glob.keyMappings)
    Gdx.input.setInputProcessor(KeyState)
    
    // A test fixture for the entity
    
    var testFix = new FixtureDef()
    testFix.density = 1
    testFix.friction = 1
    var shape = new CircleShape()
    shape.setRadius(0.75f)
    testFix.shape = shape
    
    var testFix2 = new FixtureDef()
    testFix2.density = 1
    var shape2 = new CircleShape()
    shape2.setRadius(0.4f)
    testFix2.shape = shape2
    
    // Set up some new Entities
    ents = new EntityCollection(
        
        new Entity(new MapComponent("testmap.tmx", batch)),
        
        Spaceman.create(new Vector2(2,6), sysPhysics.world , batch),
        new Entity(
            EntityName("theTex"),
            new TextureComponent(batch, img),
            WorldPosition(new Vector2(1,1)),
            WorldSize(new Vector2(1,1)),
            WorldRotation(0),
            WorldOrigin(new Vector2(1,1)),
            new BodyComponent(sysPhysics.world,
            		List((testFix, FixtureNoData)),
            		BodyDef.BodyType.StaticBody,
            		new Vector2(1.5f,0),
            		CollisionHandler.nop,
            		CollisionHandler.nop))
        )
    
    
    var smp = new SysMapInitPhysics(sysPhysics.world)
    
    ents.runSystem(smp)
    
    sysPhysics.world .setContactListener(new CollisionHandler(ents))
    
  }
  
  override def render(){
    Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
	sysPhysicsRender.debug = Glob.debugPhysics
	
	ents.runSystems(
	    
	    SysMapUpdate,
	    sysPhysics,
	    SysRenderableBody,
	    
	    Spaceman.updater,
	    
	    SysRender,
	    sysPhysicsRender,
	    SysDebug,
	    KeyState
	    )
	    
  }
  
  //override def 
  
}


/*
package com.jvj.yuggoth;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Yuggoth extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		T.foo();
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
}
*/