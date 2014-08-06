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
        stateTime = 0.0f
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
    
    var position = SysRender.transformV(new Vector2(this.position).sub( new Vector2(this.size).scl(0.5f)))
    var size = new Vector2(this.size).scl(SysRender.pixToWorld )
    var origin = new Vector2(size).scl(0.5f)
    
    var (sx,sy)=(scale.x, scale.y)
    
    if (flipX) { sx = -sx }
    if (flipY) { sy = -sy }
    
    batch.draw(currentAnimation.getKeyFrame(stateTime),
        position.x, position.y,
        origin.x,origin.y,
        size.x, size.y,
        sx, sy, // Scale
        rotation)
  }
}