package com.jvj.gdxutil

import scala.collection._
import scala.util.control.Breaks._
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
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.jvj.gdxutil.CollisionHandler
import com.jvj.gdxutil.CollisionHandler
import com.badlogic.gdx.physics.box2d.CircleShape

// Fixture data classes for the map edges
case object Floor extends FixtureData
case object Ceiling extends FixtureData
case object Wall extends FixtureData

/* Static vars for the MapComponent class.
 * */
object MapComponent {
  
  var tileSize = 1f
  
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
    // Nothing at all!
  }
  
  override def whenAdded(e:Entity) = {
    
    // Add the children
	  e.addComponent(
	      new ChildrenComponent(
	          (for (i <- 0 until map.getLayers().getCount())
	    		        yield (Symbol(s"Layer_$i"),
	    		        		new Entity(
	    		        		    new MapLayerComponent(this, i, map.getLayers().get(i))))):_*))
    
  }
  
  // Disposable
  def dispose:Unit = {
    // TODO: How to dispose of textures??
  }
  
}

class MapLayerComponent(mc:MapComponent, i:Int, m:MapLayer) extends Renderer {
  
  layer = i
  
  override def render(dt:Float, ec:EntityCollection, e:Entity) = {
    mc.map.getLayers().get(i) match {
      case l:TiledMapTileLayer => mc.mapRenderer.renderTileLayer(l)
      case _ => ;
    }
  }
}

class SysMapInitPhysics(world:World) extends System {
  
  
  override def apply (ec:EntityCollection, e:Entity) = {
    
    e [Renderer] match {
      case Some(m:MapComponent) => {
        val verts = collectEdges(
            for(i <- 0 until m.map.getLayers().getCount();
            	l <- List(m.map.getLayers().get(i));
            	if (l.isInstanceOf[TiledMapTileLayer])) yield l.asInstanceOf[TiledMapTileLayer]
            )
            
        e.addComponent(new BodyComponent(world,
            verts.toList,
            BodyDef.BodyType.StaticBody,
            new Vector2(0,0),
            CollisionHandler.nop,
            CollisionHandler.nop))
        
      }
      case _ => ;
    }

    ec
  }
  
  
  /* This algorithm analyzes the tiled map and creates edge shapes on co-linear edges.
   * Check order is counterclockwise.
   * */
  def collectEdges(layers:Seq[TiledMapTileLayer]):Set[(FixtureDef, FixtureData)] = {
    
    // Type definitions for the edge collection algorithm
	abstract class TileEdge{
	  def x:Int
	  def y:Int
	  def next:TileEdge
	  def prev:TileEdge
	  def vertices:(Vector2,Vector2)
	  def fixData:FixtureData
	}
	case class EBottom(x:Int, y:Int) extends TileEdge{
	  override def next = EBottom(x+1,y)
	  override def prev = EBottom(x-1,y)
	  override def vertices =
	    (new Vector2(x,y).scl(MapComponent.tileSize),
	     new Vector2(x+1,y).scl(MapComponent.tileSize))
	  override def fixData = Ceiling
	}
	case class ERight(x:Int, y:Int) extends TileEdge{
  	  override def next = ERight(x,y+1)
  	  override def prev = ERight(x,y-1)
  	  override def vertices =
	    (new Vector2(x+1,y).scl(MapComponent.tileSize),
	     new Vector2(x+1,y+1).scl(MapComponent.tileSize))
	  override def fixData = Wall
  	}
	case class ETop(x:Int, y:Int) extends TileEdge{
  	  override def next = ETop(x-1,y)
  	  override def prev = ETop(x+1,y)
  	  override def vertices =
	    (new Vector2(x+1,y+1).scl(MapComponent.tileSize),
	     new Vector2(x,y+1).scl(MapComponent.tileSize))
	  override def fixData = Floor
  	}
	case class ELeft(x:Int, y:Int) extends TileEdge {
	  override def next = ELeft(x,y-1)
	  override def prev = ELeft(x,y+1)
	  override def vertices =
	    (new Vector2(x,y+1).scl(MapComponent.tileSize),
	     new Vector2(x,y).scl(MapComponent.tileSize))
	  override def fixData = Wall
	}
  	
	// A set of tiles coords for verification
	val tiles = (for (tml <- layers;
					x <- 0 until tml.getWidth();
					y <- 0 until tml.getHeight();
					if (tml.getCell(x, y) != null)) yield (x,y)) toSet

	// Initialize the edges set
	var edges = new mutable.HashSet() ++ (for ((x,y) <- tiles) yield {
	  var ret = List():List[TileEdge]
	  if (!tiles(x,y-1)) ret ++= List(EBottom(x,y))
	  if (!tiles(x+1,y)) ret ++= List(ERight(x,y))
	  if (!tiles(x,y+1)) ret ++= List(ETop(x,y))
	  if (!tiles(x-1,y)) ret ++= List(ELeft(x,y))
	  ret
	}).foldLeft(List():List[TileEdge])(_++_)
    

	// TODO: This code is very imperative.  Maybe it should be refactored?
	var acc = Set():Set[((Vector2, Vector2), FixtureData)]
  	while(!edges.isEmpty){
  	  var current = edges.head
  	  
  	  // Back-up the current edge
  	  while(edges(current.prev)) current = current.prev
  	  var (trailing,leading) = current.vertices
  	  while(edges(current.next)){
  	    edges -= current
  	    current = current.next
  	    leading = current.vertices._2 
  	  }
  	  edges -= current
  	  acc += (((trailing, leading), current.fixData))
  	}
	
	(acc map {
	  case ((v1, v2), fdat) =>
	    var fd = new FixtureDef()
	    var es = new EdgeShape()
	    es.set(v1,v2)
	    fd.shape = es
	    // TODO: Floor friction level?
	    fd.friction = 1f
	    (fd, fdat)
	})
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