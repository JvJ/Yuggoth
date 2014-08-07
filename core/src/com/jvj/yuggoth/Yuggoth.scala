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


object SysPrint extends System{
  override def apply(ec:EntityCollection, e:Entity) = {
    println("Id: "+e.id )
    e.component[Renderer] match{
      case Some(r) => println ("layer: "+r.layer)
      case None => ;
    }
  }
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
    
    SysRender.position = new Vector2(0,0)
    SysRender.pixToWorld = new Vector2(64f, 64f)
    
    sysPhysics  = new SysPhysics(new Vector2(0,-10))
    sysPhysicsRender = new SysPhysicsRender(sysPhysics, true)
    
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
        
        Spaceman.create(new Vector2(2,6), sysPhysics.world , batch),
        new Entity(
            new TextureComponent(batch, img) withInit {
              t=>
                t.position = new Vector2(1,0)
                t.size = new Vector2(1f, 1f)
                },
            new BodyComponent(sysPhysics.world,
            		List(testFix),
            		BodyDef.BodyType.StaticBody,
            		new Vector2(1.5f,0))))
  }
  
  override def render(){
    Gdx.gl.glClearColor(0, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	
    ents.runSystem(sysPhysics)
		.runSystem(SysRenderableBody)
		.runSystem(SysRender)
		.runSystem(sysPhysicsRender )
	
	
  }
  
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