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
    
    e [Renderer] match {
      case Some(m:MapComponent) => {
        val verts = collectEdges(
            for(i <- 0 until m.map.getLayers().getCount();
            	l <- List(m.map.getLayers().get(i));
            	if (l.isInstanceOf[TiledMapTileLayer])) yield l.asInstanceOf[TiledMapTileLayer]
            )
        val vertsWithData = verts map {v => (v,FixtureNoData)} toList
            
        e.addComponent(new BodyComponent(world,
            vertsWithData,
            BodyDef.BodyType.StaticBody,
            new Vector2(0,0),
            {_=>}, {_=>}))
        
      }
      case _ => ;
    }
    ec
  }
  
  
  /* This algorithm analyzes the tiled map and creates edge shapes on co-linear edges.
   * Check order is counterclockwise.
   * */
  def collectEdges(layers:Seq[TiledMapTileLayer]):Set[FixtureDef] = {
    
    // Type definitions for the edge collection algorithm
	abstract class TileEdge{
	  def x:Int
	  def y:Int
	  def sameDir(t:TileEdge):Boolean
	  def checkOrder:List[TileEdge]
	  def vertices:(Vector2,Vector2)
	}
	case class EBottom(x:Int, y:Int) extends TileEdge{
	  override def sameDir(e:TileEdge) = {e match {case EBottom(_,_) => true; case _ => false}}
	  override def checkOrder = List(ERight(x,y), EBottom(x+1,y), ELeft(x+1,y-1))
	  override def vertices =
	    (new Vector2(x,y).scl(MapComponent.tileSize),
	     new Vector2(x+1,y).scl(MapComponent.tileSize))
	}
	case class ERight(x:Int, y:Int) extends TileEdge{
	  override def sameDir(e:TileEdge) = {e match {case ERight(_,_) => true; case _ => false}}
  	  override def checkOrder = List(ETop(x,y), ERight(x,y+1), EBottom(x+1,y+1))
  	  override def vertices =
	    (new Vector2(x+1,y).scl(MapComponent.tileSize),
	     new Vector2(x+1,y+1).scl(MapComponent.tileSize))
  	}
	case class ETop(x:Int, y:Int) extends TileEdge{
  	  override def sameDir(e:TileEdge) = {e match {case ETop(_,_) => true; case _ => false}}
  	  override def checkOrder = List(ELeft(x,y), ETop(x-1,y), ERight(x-1,y+1))
  	  override def vertices =
	    (new Vector2(x+1,y+1).scl(MapComponent.tileSize),
	     new Vector2(x,y+1).scl(MapComponent.tileSize))
  	}
	case class ELeft(x:Int, y:Int) extends TileEdge {
	  override def sameDir(e:TileEdge) = {e match {case ELeft(_,_) => true; case _ => false}}
	  override def checkOrder = List(EBottom(x,y), ELeft(x,y-1), ETop(x-y,y-1))
	  override def vertices =
	    (new Vector2(x,y+1).scl(MapComponent.tileSize),
	     new Vector2(x,y).scl(MapComponent.tileSize))
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
	var acc = Set():Set[(Vector2, Vector2)]
  	while(!edges.isEmpty){
  	  var current = edges.head
  	  var (trailing,leading) = current.vertices
  	  edges -= current
  	  breakable {while(true){
	  	  current.checkOrder.dropWhile(!edges(_)).headOption match {
	  	    case None =>
	  	      acc += ((trailing, leading))
	  	      break
	  	    case Some(e) =>
	  	      if (current.sameDir(e)){
	  	        leading = e.vertices._2
	  	        current = e
	  	      }
	  	      else {
	  	        acc += ((trailing, leading))
	  	        break
	  	      }
	  	  }
  	  }}
  	}
	
	acc map {
	  case (v1, v2) =>
	    var fd = new FixtureDef()
	    var es = new EdgeShape()
	    es.set(v1,v2)
	    fd.shape = es
	    // TODO: Floor friction level?
	    fd.friction = 1
	    fd
	}
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