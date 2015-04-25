package com.jvj.ecs

import scala.collection._

/**
 * A trigger component waits for some trigger condition, as specified in the method
 * fireCondition.  On a per-frame basis, update is called with the containing entity
 * and entity collection.
 */
abstract class TriggerComponent extends Component {
  
  type triggerFunction = (EntityCollection,Entity => EntityCollection)
  
  def update(ec:EntityCollection, e:Entity):EntityCollection
 
  def fireCondition(ec:EntityCollection, e:Entity):EntityCollection
}


/**
 * Triggers plural.  This is a component that represents an associative map of other trigger components.
 * Since multiple triggers are a common use case, they are indexable by symbol here.
 * 
 * The default constructor takes a list of symbol - trigger component pairs.
 */
class Triggers(trigs:(Symbol, TriggerComponent)*) extends Component with Iterable[(Symbol, TriggerComponent)] {
  
  override val typeTags = List(classOf[Triggers])
  
  protected var _trigs = new mutable.HashMap() ++ trigs.toMap
  
  def iterator = _trigs.iterator
  
  /**
   * Obtain the trigger component associated with a particular symbol.
   */
  def apply(s:Symbol):Option[TriggerComponent] = _trigs.get(s)
  
  /**
   * A typesafe generic version of the initial apply.  This only returns
   * the component if it is present and matches the given class.
   */
  def apply[T<:TriggerComponent:reflect.ClassTag](s:Symbol): Option[T] =
    _trigs.get(s) match {
    case Some(t:T) => Some(t)
    case _ => None
  }
  
  def addTrigger(s:Symbol, t:TriggerComponent) = _trigs(s) = t
  
  def removeTrigger(s:Symbol):Option[TriggerComponent] = _trigs.remove(s)
}

// LEFTOFF: Here, Aug. 14.
object SysTrigger extends System{
 
  override def apply(ec:EntityCollection, e:Entity):EntityCollection = {
    ec
  }
  
}

