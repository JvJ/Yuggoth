package com.jvj.gdxutil;

import com.jvj.ecs._
import scala.collection._
import com.badlogic.gdx._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.math._
import com.badlogic.gdx.graphics.g2d._
import MathUtil._
import scala.util.Random

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
  
  var tm = 0f
  def render(dt: Float, ec:EntityCollection, e:Entity):Unit = {
    
    e[TransformComponent] match {
      case Some(tc) =>
        
        var trans = new Matrix3(tc.parentTransform)
        var fx = if (tc.flipX) -1f else 1f
        var fy = if (tc.flipY) -1f else 1f
        //trans.scale(fx, fy)
        
        var c1 = (tc.position - ((tc.origin * tc.scale ) ^> tc.rotation)) * trans
        var c2 = (c1 + ((tc.sizev * tc.scale ) ^> tc.rotation )) * trans
        
        var b = Math.min(c1.y, c2.y)
        var t = Math.max(c1.y, c2.y)
        var l = Math.min(c1.x, c2.x)
        var r = Math.max(c1.x, c2.x)
        
        val clr = Color.WHITE.toFloatBits()
        
        // Vertices are made up of x,y,color,u,v
        var verts = Array[Float](
            l, b, clr, 0, 1f,
            l, t, clr, 0, 0,
            r, t, clr, 1f, 0,
            r, b, clr, 1f, 1f);
        
        // TODO: Regions and rects!
        batch.draw(texR.getTexture(), verts, 0, verts.length);
        /*
        val position = t.position
        val origin = t.origin 
        val size = t.sizev
        val scale = t.scale 
        
        batch.draw(texR, position.x, position.y,
        	origin.x, origin.y,
        	size.x, size.y,
        	scale.x, scale.y,
        	rotation)*/
        
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
      
      //proj.scale(1f/pixToWorld.x, 1f/pixToWorld.y, 0)
      batch.setProjectionMatrix(proj)
      
      var mat = new Matrix4().translate(new Vector3(camera.position).scl(-1))
      batch.setTransformMatrix(mat)
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
    
    
    
    for(l <- min to max){
      batch.begin()
      for (e <- ec){
        val reg = """BabyBit.*""".r
        
        e.component[Renderer] match {
            case Some(r) => if (r.layer == l){
              e.id match {
            case EntityName(str @ reg(_ *)) => 
              print("Updating "+str);
            case _ => ;
            }
            this(ec,e)
          }
          case None => ;
        }
      }
      batch.end()
    }
    
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