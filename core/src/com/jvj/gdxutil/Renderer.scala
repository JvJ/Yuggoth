package com.jvj.gdxutil;

import com.jvj.ecs._
import scala.collection._
import com.badlogic.gdx._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.math._
import com.badlogic.gdx.graphics.g2d._

abstract class Renderer extends Component{
  
  override val entityType = classOf[Renderer]
  var layer:Int = 0
  
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
    _rends.foreach((r)=>r.render(dt, ec, e))
}

class TextureComponent(batch:SpriteBatch, texR:TextureRegion) extends Renderer{
  
  def this(bat:SpriteBatch, tex:Texture) = {
    this(bat, TextureRegion.split(tex, tex.getWidth, tex.getHeight)(0)(0))
  }
  
  var position:Vector2 = new Vector2(0,0)
  var origin:Vector2 = new Vector2(texR.getRegionHeight()/2.0f, texR.getRegionWidth()/2.0f)
  var size:Vector2 = new Vector2(texR.getRegionWidth(), texR.getRegionHeight())
  var scale:Vector2 = new Vector2(1.0f,1.0f)
  var rotation:Float = 0.0f
  var flipX:Boolean = false
  var flipY:Boolean = false
  val textureRegion = texR
  
  def render(dt: Float, ec: EntityCollection, e:Entity):Unit ={

    var (sx,sy)=(scale.x, scale.y)
    
    if (flipX) { sx = -sx }
    if (flipY) { sy = -sy }
    
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
  // var 
  
  override def apply (ec:EntityCollection) = {
    var min:Int = Integer.MAX_VALUE
    var max:Int = Integer.MIN_VALUE
  
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
    
    for(l <- min to max){
      for (e <- ec){
        e.component[Renderer] match {
          case Some(r) => if (r.layer == l) this(ec,e)
          case None => ;
        }
      }
    }
    
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity) = {
    e.component[Renderer] match {
      case Some(r) => r.render(Gdx.graphics.getDeltaTime(), ec, e)
      case None => ;
    }
  }
}