package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.maps._
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.physics.box2d.BodyDef

/* Static vars for the MapComponent class.
 * */
object MapComponent {
  
  var tileSize = 0.5f
  
  private var mapLoader:TmxMapLoader = null
  
}


class MapComponent(filename:String, batch:SpriteBatch) extends Renderer{
  
  if (MapComponent.mapLoader == null){
    MapComponent.mapLoader  = new TmxMapLoader()
  }
  
  var map = MapComponent.mapLoader.load(filename)
  // TODO: Children for tile layers
  
  
  var tilePixelSize = {
    // TODO: Can this generate NULL at any point?
    var props = map.getProperties()
    
    var x = props.get[Integer]("tilewidth", classOf[Integer]) 
    var y = props.get[Integer]("tileheight", classOf[Integer])
    
    new Vector2(
        if (x != null) x.floatValue() else SysRender.pixToWorld.x,
        if (y != null) y.floatValue() else SysRender.pixToWorld.y)
  }
  
  var mapRenderer = new OrthogonalTiledMapRenderer(
      map,
      (SysRender.pixToWorld .y / tilePixelSize.y) * MapComponent.tileSize,
      batch)
 
  // LEFTOFF: How to incorporate layers
  override def render(dt:Float, ec:EntityCollection, e:Entity) = {
    map.getLayers().get(0) match {
      case l:TiledMapTileLayer => mapRenderer.renderTileLayer(l)
      case _ => ;
    }
  }
  
  // Disposable
  def dispose:Unit = {
    // TODO: How to dispose of textures??
  }
  
  // 
  
}

class SysMapInitPhysics(world:World) extends System {
  override def apply (ec:EntityCollection, e:Entity) = {
    e[Renderer] match {
      case Some(m:MapComponent) => {
    	m.map.getLayers().get(0) match {
    	  case tml:TiledMapTileLayer =>

    	    for (x <- 0 until tml.getWidth();
    	         y <- 0 until tml.getHeight()){
    	      
    	      if (tml.getCell(x, y) != null){
    	        
    	    	  var f = new FixtureDef()
    	    	  var s = new PolygonShape()
    	    	  s.setAsBox(MapComponent.tileSize/2f , MapComponent.tileSize/2f )
    	    	  f.shape = s
    	    	  var bdef = new BodyDef()
    	    	  bdef.`type` = BodyDef.BodyType.StaticBody
    	    	  bdef.position.x = (x+0.5f) * MapComponent.tileSize
    	    	  bdef.position.y = (y+0.5f) * MapComponent.tileSize
    	    	  
    	    	  var bod = world.createBody(bdef)
    	    	  bod.createFixture(f)
    	      }
    	    }
    	}
      }
      case _ => ;
    }
    ec
  }
}

object SysMapUpdate extends System{
  // TODO: Apply should always return an entity collection
  // This way, we can FOLD, instead of mapping
  override def apply(ec:EntityCollection, e:Entity):EntityCollection = {
    e[Renderer] match {
      case Some(m:MapComponent) => {
        m.mapRenderer.setView(SysRender.camera )
      }
      case _ => ; // NOP
    }
    ec
  }
}