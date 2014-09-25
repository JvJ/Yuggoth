package com.jvj.gdxutil
//import scala.c
import scala.collection._
import com.jvj.ecs._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.math._
import com.badlogic.gdx.Gdx
import MathUtil._

abstract class FrameList
case class Frames(mode:Animation.PlayMode, delay:Float, cells:(Int,Int)*) extends FrameList

/* Construct a specification for a sprite.
 * It consists of the texture file, 
 * */
class SpriteSpec(
    val fileName:String,
    val cellSize:(Int,Int),
    theStates:(Symbol, FrameList)*){
  
  val states = theStates.toMap
  
}

private object SpriteCache{
  var textures = new mutable.HashMap[String, Texture]()
  def apply(s:String) = textures.get(s)
}

/* Represents an animated sprite.
 * A sprite component is constructed from a specification.
 * */
class SpriteComponent (startingState:Symbol, batch:SpriteBatch, spec:SpriteSpec) extends Renderer{
  
  val texture = SpriteCache(spec.fileName) match {
    case Some(t) => t
    case None =>
      val t = new Texture(Gdx.files.internal(spec.fileName))
      SpriteCache.textures += spec.fileName -> t
      t
  }
  
  val frames = TextureRegion.split(texture, spec.cellSize._1, spec.cellSize._2 )
  
  if (frames.length == 0){ throw new Exception(s"Error loading file: ${spec.fileName}.  Zero frames.") }
  
  val dimensions = (frames.length, frames(0).length)
  
  val animations =  spec.states map {
    _ match {
      case (state, Frames(mode, delay, cells @ _*)) => {
        var frameArray = cells map { (_, dimensions) match {
            case ((r,c), (w,h)) =>
              if (r >= w || r < 0 || c >= h || c < 0){
                throw new Exception(s"Nonexistent cell in ${spec.fileName}: ($r,$c)")
              }
              else {
                frames(r)(c)
              }
          }} toArray
          
        state ->
          new Animation(delay,
              new com.badlogic.gdx.utils.Array(frameArray),
              mode)
      }
      }
    }
  
  private var currentState = startingState
  private var currentAnimation:Animation = null
  private var stateTime = 0.0f
  
  def state = currentState
  
  def setState(s:Symbol) = {
    animations.get(s) match {
      case Some(a) =>
        if (s != currentState) stateTime = 0.0f
        currentState = s
        currentAnimation = a
      case None =>
        throw new Exception(s"Invalid animation state: $s.")
    }
  }
  
  // Switch state to continue construction
  setState(startingState)
  
  override def render(dt:Float, ec:EntityCollection, e:Entity) = {
    stateTime += dt

    var texR = currentAnimation.getKeyFrame(stateTime)
    
    if (e.id  == EntityName("Spaceman")){
      println("Updating Spaceman.")
    }
    
    e[TransformComponent] match {
      case Some(tc) =>
        
        var trans = new Matrix3(tc.parentTransform)
        var fx = if (tc.flipX) -1f else 1f
        var fy = if (tc.flipY) -1f else 1f
        trans.scale(fx, fy)
        
        var c1 = (tc.position - ((tc.origin * tc.scale ) ^> tc.rotation)) * trans
        var c2 = (c1 + ((tc.sizev * tc.scale ) ^> tc.rotation )) * trans
        
        // LEFTOFF: This was a terrible idea, due to rotations!!
        var b = Math.min(c1.y, c2.y)
        var t = Math.max(c1.y, c2.y)
        var l = Math.min(c1.x, c2.x)
        var r = Math.max(c1.x, c2.x)
        
        val clr = Color.WHITE.toFloatBits()
        
        var uv1 = v2(if (fx > 0) texR.getU() else texR.getU2(),
        			 if (fy > 0) texR.getV() else texR.getV2())
        var uv2 = v2(if (fx > 0) texR.getU2() else texR.getU(),
        			 if (fy > 0) texR.getV2() else texR.getV())
        
        // Vertices are made up of x,y,color,u,v
        var verts = Array[Float](
            l, b, clr, uv1.x, uv2.y,
            l, t, clr, uv1.x, uv1.y,
            r, t, clr, uv2.x, uv1.y,
            r, b, clr, uv2.x, uv2.y);
        
        // TODO: Regions and rects!
        batch.draw(texR.getTexture(), verts, 0, verts.length);
        
        
        
      case _ =>
    }	
    
    
  }
}
