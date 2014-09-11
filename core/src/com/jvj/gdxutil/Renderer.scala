package com.jvj.gdxutil;

import com.jvj.ecs._
import scala.collection._
import com.badlogic.gdx._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.math._
import com.badlogic.gdx.graphics.g2d._

abstract class Renderer extends Component{
  
  override val typeTags = List(classOf[Renderer])
  
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

class TextureComponent(batch:SpriteBatch, texR:TextureRegion, size:Vector2) extends Renderer{
  
  def this(bat:SpriteBatch, tex:Texture, size:Vector2) = {
    this(bat, TextureRegion.split(tex, tex.getWidth, tex.getHeight)(0)(0), size)
  }
  
  origin = new Vector2(size.x/2.0f, size.y/2.0f)
  //origin = new Vector2(texR.getRegionHeight()/2.0f, texR.getRegionWidth()/2.0f)
  //size = new Vector2(texR.getRegionWidth(), texR.getRegionHeight())
  
  val textureRegion = texR
  
  def render(dt: Float, ec:EntityCollection, e:Entity):Unit = {
    
     e[TransformComponent] match {
      case Some(t) =>
        //val position = t.toScreen(t.globalPosition)
        //val origin = t.toScreen(t.localOrigin)
        //val size = t.toScreen(t.globalSize)
        
        val position = t.globalPosition
        val origin = t.localOrigin
        val size = t.globalPosition
        
        val scale = t.globalScale
        // TODO: Transform rotation?
        val rotation = t.globalRotation
        val (fx, fy) = (if (t.flipX) -1 else 1, if (t.flipY ) -1 else 1)
        batch.draw(texR,
            position.x, position.y,
        	//position .x - fx * origin.x, position .y - fy * origin.y,
        origin.x, origin.y,
        size.x, size.y,
        scale.x, scale.y, // Scale
        rotation)
      case _ =>
    }
    
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
  
  // TODO: this is a little obsolete now...
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
      var proj = new Matrix4(camera.projection)
      proj.scale(pixToWorld.x, pixToWorld.y, 0)
      batch.setProjectionMatrix(proj)
      batch.setTransformMatrix(new Matrix4().translate(new Vector3(camera .position).scl(-1)))
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
  
  def apply(ec:EntityCollection, e:Entity):EntityCollection = {
    e.component[Renderer] match {
      case Some(r) => r.render(Gdx.graphics.getDeltaTime(), ec, e)
      case None => ;
    }
    ec
  }
}