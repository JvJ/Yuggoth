package com.jvj.ecs

import scala.collection._
import java.util.UUID


abstract class EntityID
case class EntityUUID(id: UUID) extends EntityID
case class EntityName(id: String) extends EntityID

/**
 * The entity represents a collection of components.
 */
class Entity (identifier:EntityID, components: Component*){
  
  // Extra constructors, making a random UUID or an entity name
  def this(name:String, cs:Component*) = this(EntityName(name), cs:_*)
  def this(cs:Component*) = this(EntityUUID(UUID.randomUUID()), cs:_*)
  
  /*The entity's identifier*/
  val id = identifier
  
  /*The map of component classes to components*/
  private var _comps = new mutable.HashMap[Class[_ <: Component], Component]()
  
  // An addComponent function
  def addComponent(c : Component) = _comps += (c.entityType -> c)
  
  // Continue construction
  components.foreach((c)=>_comps+=(c.entityType->c))
  
  /* Get some component based on its type.
   * */
  def component[C <: Component : reflect.ClassTag]:Option[C] = {
    
    val clazz = reflect.classTag[C].runtimeClass.asInstanceOf[Class[C]]
    
    return _comps.get(clazz) match {
      case Some(comp) => Some(comp.asInstanceOf[C])
      case None => None
    }
  }
  
  def apply[C<:Component:reflect.ClassTag] = component[C]
}

/**
 * An unordered collection of entities.
 */
class EntityCollection(ents : Entity*){
  
  private var _ents = new mutable.HashMap[EntityID, Entity]()
  
  def addEntity(e : Entity) = {
    // LEFTOFF: Fail if already exists
    _ents.get(e.id) match {
      case Some(_) => throw new Exception(s"Duplicate entity ID:${e.id}.")
      case None => _ents += (e.id -> e)
    }
    
  }
  
  ents.foreach(addEntity)
  
  /*Run a system over the entity collections.
   * A system is a Unit function taking the entity collection and the current
   * entity.
   * Returns self for chaining.*/
  def runSystem(sys:System):EntityCollection = sys(this)
  
  def foreach(f:(Entity)=>Unit):Unit = _ents.foreach((e)=>f(e._2))
}