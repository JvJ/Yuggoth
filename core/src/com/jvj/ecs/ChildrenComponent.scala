package com.jvj.ecs

import scala.collection._

/* This component stores references to other
 * entities.
 * */
class ChildrenComponent(ents:(Symbol, Entity)*)
extends Component
with Iterable[(Symbol,Entity)]{
  
  override def typeTags = List(classOf[ChildrenComponent]):Seq[Class[_ <: Component]]
  
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
  
  def removeChild(s:Symbol, ec:EntityCollection):Option[Entity] = {
    _ents.get(s) match {
      case Some(e) => ec.get(e.id ) match {
        case Some(ee) => ec.removeEntity(ee)
        case _ => None
      }
      case _ => None
    }
  }
}

object SysInitChildren extends System{
  
  def apply(ec:EntityCollection, e:Entity) = {
    
    def internalRec(ent:Entity):Unit = {
      ent[ChildrenComponent] match {
      case Some(cc) =>
        for ((_,ee)<- cc ){
          internalRec(ee)
        }
      case None => ;
      }
      
      ec.get(ent.id) match {
        case None => ec.addEntity(ent)
        case _ => ;
     }
    }
    
    internalRec(e)
    
    ec
  }
}

/* Remove children that are no longer present in the entity
 * collection.
 * */
object SysUpdateChildren extends System{
  
  def apply(ec:EntityCollection, e:Entity):EntityCollection = {
    e[ChildrenComponent] foreach {
      cc => for ((sym,child) <- cc){
        ec.get(child.id ) match {
          case None => cc.removeChild(sym, ec)
          case _ => ;
        }
      }
    }
    ec
  }
  
}