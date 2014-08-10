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
  def addComponent(c : Component) = {
    _comps += (c.componentType -> c)
    c.owner = this
    }
  
  // Continue construction
  components.foreach((c)=>_comps+=(c.componentType->c))
  
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
class EntityCollection(ents : Entity*) extends Iterable[Entity]{
  
  private var _ents = new mutable.HashMap[EntityID, Entity]()
  
  def iterator = {
    _ents.toStream map (e=>e._2) iterator
  }
  
  def addEntity(e : Entity) = {
    // Add entity, but fail if it's already there
    _ents.get(e.id) match {
      case Some(_) => throw new Exception(s"Duplicate entity ID:${e.id}.")
      case None => _ents += (e.id -> e)
    }
    this
  }
  
  def removeEntity(e:Entity) = {
    _ents.remove(e.id)
  }
  
  ents.foreach(addEntity)
  
  /*Run a system over the entity collections.
   * A system is a Unit function taking the entity collection and the current
   * entity.
   * Returns self for chaining.*/
  def runSystem(sys:System):EntityCollection = sys(this)
  
  def runSystems(syss:System*):EntityCollection
  	= syss.foldLeft(this){(t,s) => t.runSystem(s)}
  
  
  def get(eid:EntityID) = _ents.get(eid)
}