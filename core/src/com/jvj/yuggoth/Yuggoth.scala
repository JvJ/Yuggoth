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
  
  override def create(){
    batch = new SpriteBatch()
    img = new Texture("badlogic.jpg")
    
    // Set up some new Entities
    ents = new EntityCollection(
        new Entity(
            new TextureComponent(batch, img).withInit(
                (t)=>{
                  t.layer  = 1
                  t.position = new Vector2(50,50)
                })),
        new Entity(
            new TextureComponent(batch, img)))
  }
  
  override def render(){
    Gdx.gl.glClearColor(1, 0, 0, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	batch.begin();
	
	ents.runSystem(SysRender)
		
	
	batch.end();
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