package com.jvj.ecs

import scala.collection._

/* This component stores references to other
 * entities.
 * */
class ChildrenComponent(ents:(Symbol, Entity)*)
extends Component
with Iterable[(Symbol,Entity)]{
  
  val componentType = classOf[ChildrenComponent]
  
  private var _ents = new mutable.HashMap[Symbol,Entity]()
  _ents ++= ents map { _ match { case (k,v) => k -> v }}
  
  def iterator = _ents.iterator
  
  /* Parents an existing entity.  Do not parent a new entity
   * unless it has been added to the collection!
   * */
  def parent(s:Symbol, e:Entity):Entity = {
    _ents(s) = e
    e
  }
  
  /* Unparent the entity referred to by a symbol.
   * Return the ID (if present).
   * */
  def unparent(s:Symbol):Option[Entity] = {
    _ents.get(s) match {
      case Some(e) =>
        _ents.remove(s)
        Some(e)
      case None =>
        None
    }
  }
}

object SysInitChildren extends System{
  
  def apply(ec:EntityCollection, e:Entity) = {
    e[ChildrenComponent] match {
      case Some(cc) =>
        for ((_,ee)<- cc ){
          ec.addEntity(ee)
        }
      case None => ;
    }
  }
}