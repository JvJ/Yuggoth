package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Matrix3
import com.jvj.ecs.ChildrenComponent

import MathUtil._

// LEFTOFF: 
// The semantics of size vs. scale, and position vs. draw-position, need to be re-defined
/* An abstract class representing various types of transformations.
 * */
abstract class TransformComponent(
    pos:Vector2,
    size:Vector2,
    scl:Vector2,
    orig:Vector2,
    rot:Float,
    flp:(Boolean, Boolean),
    ents:(Symbol, Entity)*) extends ChildrenComponent(ents:_*){
  
  // A zero-parameter constructor
  //def this() = this(new Vector2(0,0), new Vector2(1,1), new Vector2(1,1), new Vector2(0,0), 0f, (false, false))
  
  override def typeTags = List(classOf[ChildrenComponent], classOf[TransformComponent])
  private var _parent:TransformComponent = null
 
  // Now that the superconstructor has been called, initialize the child entities
  for ((_,e) <- ents){
    e[TransformComponent] match {
      case Some(tc) => tc._parent = this
      case _ => throw new Exception("Child of transform component requires transform component.")
    }
  }
  
  var position : Vector2 = pos
  var scale : Vector2 = scl
  var sizev : Vector2 = size
  var origin : Vector2 = orig
  var rotation : Float = rot
  var (flipX, flipY):(Boolean, Boolean) = flp
  
  // TODO: This is a hack, but what can I do?
  def flip : (Boolean, Boolean) = {
    if (_parent == null){
      (flipX, flipY)
    }
    else{
      val (fx,fy) = _parent.flip
      (fx ^ flipX, fy ^ flipY)
    }
  }
  
  def localTransform : Matrix3 = {
    var m = new Matrix3()
    var (fx, fy) = 
      (if (flipX) -1 else 1, if (flipY) -1 else 1)
    
      
       m.scale(fx,fy).translate(position).rotate(rotation).scale(scale)
      //m.scale(scale).rotate(rotation).translate(position).scale(fx,fy)
    
  }
  
  // Transformation 
  /* Transform 
   * */
  def transform : Matrix3 = {
    
    new Matrix3(parentTransform).mul(localTransform)
  }
  
  def parentTransform: Matrix3 =
    if (_parent != null)
      _parent.transform
      else new Matrix3()
  
  def globalRotation(): Float = {
      // TODO: How to transform rotation?
    parentTransform.rotate(rotation).getRotation()
  }
  
  def globalPosition(): Vector2 = {
    //new Vector2(position).mul(pare ntTransform)
    new Vector2(0,0).mul(transform)
  }
  
  def localOrigin():Vector2 = {
    var v = new Vector2()
    parentTransform.getScale(v)
    new Vector2(origin).scl(v)
  }
  
  def globalSize():Vector2 = {
    var m = parentTransform
    var v = new Vector2()
    m.getScale(v)
    v.x = Math.abs(v.x)
    v.y = Math.abs(v.y)
    var ret = new Vector2(sizev).scl(v)//.scl(fx, fy)
    
    ret
  }
  
  def globalScale():Vector2 = {
    var v = new Vector2()
    parentTransform.getScale(v)
    
    var (flipX, flipY) = flip
    var (fx, fy) = 
      (if (flipX) -1 else 1, if (flipY) -1 else 1)
    new Vector2(v).scl(fx, fy)
    
  }
  
  def toScreen(v:Vector2):Vector2
  
  /* Set the components based on the transform matrix.
   * */
  def set(par:TransformComponent) : TransformComponent = {
    
    // 
    
    var m = new Matrix3(par.transform).inv()
    
    position.mul(m)
    
    var v = new Vector2()
    m.translate(m.getTranslation(v).scl(-1)).rotate(-m.getRotation())
    scale.mul(m)
    sizev.mul(m)
    origin.mul(m)
    rotation += m.getRotation()
    
    this
  }
  
  /*Parent this entity.  Overridden due to relative transformations.*/
  override def parent(s:Symbol, e:Entity):Entity = {
    //val mat = this.transform.inv()
    e[TransformComponent] match {
      case Some(tc) => tc.set(this)
      case _ => throw new Exception(s"Cannot parent entity ${e.id}.  TransformComponent required.")
    }
    this.owner
  }
  
  /* Unparent the entity referred to by a symbol.
   * Return the ID (if present).
   * */
  override def unparent(s:Symbol):Option[Entity] = {
	None	  
  }
  
}

// LEFTOFF: Implement these, remove the other transforms
// TODO: How to implement transform components with bodies?
class ScreenTransform(pos:Vector2,
    size:Vector2,
    scl:Vector2,
    orig:Vector2,
    rot:Float,
    flp:(Boolean, Boolean),
    ents:(Symbol, Entity)*) extends
    TransformComponent(pos, size, scl, orig, rot, flp, ents:_*) {
   
  def toScreen(v:Vector2):Vector2 = new Vector2(v)
  
}

class WorldTransform(pos:Vector2,
    size:Vector2,
    scl:Vector2,
    orig:Vector2,
    rot:Float,
    flp:(Boolean, Boolean),
    ents:(Symbol, Entity)*) extends
    TransformComponent(pos, size, scl, orig, rot, flp, ents:_*) {
   
  /*Transforms an absolute position vector into screen space.*/
  def toScreen(v:Vector2):Vector2 = {
    println(s"Transforming to screen: $v With pixtoworld: ${SysRender.pixToWorld }")
    val ret = v.scl(SysRender.pixToWorld)
    println(s"Got screen transform: $ret.")
    ret
  }
  
}