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
        // TODO: Child layers!
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
    	    	  
    	    	  // TODO: Hey... no body component?
    	    	  // TODO: Make this into a fixture list, make a body component!
    	    	  var bod = world.createBody(bdef)
    	    	  bod.createFixture(f)
    	    	  //yield 
    	      }
    	    }
    	}
      }
      case _ => ;
    }
    ec
  }
  
  
  
  
  
  /* This algorithm analyzes the tiled map and creates edge shapes on co-linear edges.
   * Check order is counterclockwise.
   * */
  def collectEdges(layers:Seq[TiledMapTileLayer]):Seq[(Vector2, Vector2)] = {
    
    // Type definitions for the edge collection algorithm
	abstract class TileEdge{
	  def checkOrder:List[TileEdge]
	  def vertices:(Vector2,Vector2)
	}
	case class EBottom(x:Int, y:Int) extends TileEdge{
	  override def checkOrder = List(ERight(x,y), EBottom(x+1,y), ELeft(x+1,y-1))
	}
	case class ERight(x:Int, y:Int) extends TileEdge{
  	  override def checkOrder = List(ETop(x,y), ERight(x,y+1), EBottom(x+1,y+1))
  	}
	case class ELeft(x:Int, y:Int) extends TileEdge {
	  override def checkOrder = List(EBottom(x,y), ELeft(x,y-1), ETop(x-y,y-1))
	}
  	case class ETop(x:Int, y:Int) extends TileEdge{
  	  override def checkOrder = List(ELeft(x,y), ETop(x-1,y), ERight(x-1,y+1))
  	}
  	
  	// LEFTOFF: The rest of this algorithm
    
  	// Ease-of use functions for accessing the tile edges
  	
    List()
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