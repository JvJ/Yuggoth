package com.jvj.gdxutil

import scala.collection._
import com.jvj.ecs._
import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.math._
import com.badlogic.gdx.Gdx

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
        position.x - origin.x, position.y - origin.y,
        origin.x,origin.y,
        size.x, size.y,
        sx, sy, // Scale
        rotation)
  }
}