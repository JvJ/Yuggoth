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

class TextureComponent(batch:SpriteBatch, tex:Texture) extends Renderer{
  var position:Vector2 = new Vector2(0,0)
  val texture = tex
  
  def render(dt: Float, ec: EntityCollection, e:Entity):Unit ={
    batch.draw(tex, position.x, position.y)
  }
}

/* Implements layered rendering of objects.
 * Higher layers are closer to the screen.
 * */
object SysRender extends System{
  
  override def apply (ec:EntityCollection) = {
    var min:Int = Integer.MAX_VALUE
    var max:Int = Integer.MIN_VALUE
  
    ec.foreach((e) => {
      e.component[Renderer] match {
        case Some(r) =>{
          if (r.layer < min) min = r.layer
          if (r.layer > max) max = r.layer 
        }
        case None => ;
      }
    })
    
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