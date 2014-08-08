package com.jvj.gdxutil;

import com.jvj.ecs._
import scala.collection._
import com.badlogic.gdx._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.math._
import com.badlogic.gdx.graphics.g2d._

abstract class Renderer extends Component{
  
  override val componentType = classOf[Renderer]
  var layer:Int = 0
  var position:Vector2 = new Vector2(0,0)
  var origin:Vector2 = new Vector2(0, 0)
  var size:Vector2 = new Vector2(1, 1)
  var scale:Vector2 = new Vector2(1.0f,1.0f)
  var rotation:Float = 0.0f
  var flipX:Boolean = false
  var flipY:Boolean = false
  
  def render(dt:Float, ec: EntityCollection, e:Entity):Unit
  
  /* Set the layer and return self.
   * */
  def withLayer(l:Int) = { 
    layer = l
    this
  }
}

class MultiRenderer(rends:AbstractSeq[Renderer]) extends Renderer{
  
  val _rends = rends.toList
  
  def render(dt:Float, ec: EntityCollection, e:Entity):Unit =
    _rends.foreach{r =>r.render(dt, ec, e)}
}

class TextureComponent(batch:SpriteBatch, texR:TextureRegion) extends Renderer{
  
  def this(bat:SpriteBatch, tex:Texture) = {
    this(bat, TextureRegion.split(tex, tex.getWidth, tex.getHeight)(0)(0))
  }
  
  origin = new Vector2(texR.getRegionHeight()/2.0f, texR.getRegionWidth()/2.0f)
  size = new Vector2(texR.getRegionWidth(), texR.getRegionHeight())
  
  val textureRegion = texR
  
  def render(dt: Float, ec:EntityCollection, e:Entity):Unit = {
    var position = e[PositionComponent] match {
      case Some(wp@WorldPosition(_)) => wp.transform
      case Some(ScreenPosition(v)) => v
      case _ => new Vector2(0,0);
    }
    
    var size = e[SizeComponent] match {
      case Some(ws@WorldSize(_)) => ws.transform
      case Some(ScreenSize(v)) => v
      case _ => new Vector2(texR.getRegionWidth(),texR.getRegionHeight())
    }
    
    var origin = e[OriginComponent] match {
      case Some(wo@WorldOrigin(_)) => wo.transform
      case Some(ScreenOrigin(v)) => v
      case _ => new Vector2(0,0)
    }
    
    var (sx, sy) = e[FlipComponent] match {
      case Some(Flip(false ,false)) => (1,1)
      case Some(Flip(false ,true)) => (1,-1)
      case Some(Flip(true ,false)) => (-1,1)
      case Some(Flip(true ,true)) => (-1,-1)
      case _ => (1,1)
    }
    
    var rotation = e[RotationComponent] match {
      case Some(WorldRotation(r)) => r * (180.0f/Math.PI.toFloat)
      case Some(ScreenRotation(r)) => r
      case _ => 0
    }
    
    batch.draw(texR,
        position.x, position.y,
        origin.x,origin.y,
        size.x, size.y,
        sx, sy, // Scale
        rotation)
    
  }
}

/* Implements layered rendering of objects.
 * Higher layers are closer to the screen.
 * */
object SysRender extends System{
  
  // Some rendering globals
  var batch:SpriteBatch = null
  var camera:OrthographicCamera = null
  var position = new Vector2(0,0)
  var pixToWorld = new Vector2(1,1)
  
  /**
   * Transform coordinates to the proper place on this screen.
   */
  def transformV(v:Vector2):Vector2 = {
    var ret = new Vector2(v)
    ret.scl(pixToWorld)
    ret.mulAdd(new Vector2(position).scl(-1), pixToWorld)
  }
  
  override def apply (ec:EntityCollection) = {
    var min:Int = Integer.MAX_VALUE
    var max:Int = Integer.MIN_VALUE
  
    if (camera != null){
      camera.update()
    }
    
    ec foreach {
      e =>
      e.component[Renderer] match {
        case Some(r) =>{
          if (r.layer < min) min = r.layer
          if (r.layer > max) max = r.layer 
        }
        case None => ;
      }
    }
    
    batch.begin()
    for(l <- min to max){
      for (e <- ec){
        e.component[Renderer] match {
          case Some(r) => if (r.layer == l) this(ec,e)
          case None => ;
        }
      }
    }
    batch.end()
    
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity) = {
    e.component[Renderer] match {
      case Some(r) => r.render(Gdx.graphics.getDeltaTime(), ec, e)
      case None => ;
    }
  }
}