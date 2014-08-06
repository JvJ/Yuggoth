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
    
    sysPhysics  = new SysPhysics(new Vector2(0,0))
    sysPhysicsRender = new SysPhysicsRender(sysPhysics, true)
    
    var testFix = new FixtureDef()
    testFix.density = 1
    var shape = new CircleShape()
    shape.setRadius(0.75f)
    testFix.shape = shape
    
    
    // A test sprite
    val spaceManSprite =
      new SpriteSpec(
          // Filename and cell size
          "spaceman_sheet_hires.png", (64,96),
          // Specifying sprite states
          'Walking ->	Frames(Animation.PlayMode.LOOP, 0.1f,
          					(0,1),(0,2),(0,3),(0,4),(0,5)),
          'Standing ->	Frames(Animation.PlayMode.LOOP, 0.1f, (0,0)),
          'Jumping ->	Frames(Animation.PlayMode.LOOP, 0.1f, (1,0)))
    
    // Set up some new Entities
    ents = new EntityCollection(
        new Entity(
            new SpriteComponent('Walking, batch, spaceManSprite) withInit {
              t =>
                t.size = new Vector2(1f, 1.5f)
            },
            new BodyComponent(sysPhysics.world, testFix, BodyDef.BodyType.DynamicBody, new Vector2(2,2)) withInit {
              t=>
                t.body.setAngularVelocity(1.0f)
            }
        ),
        new Entity(
            new TextureComponent(batch, img) withInit {
              t=>
                t.position = new Vector2(1,0)
                t.size = new Vector2(1f, 1f)
                } ))
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