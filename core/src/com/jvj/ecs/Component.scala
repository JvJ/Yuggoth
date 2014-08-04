package com.jvj.ecs

import scala.collection._

/**
 * The component class. Basically, it can be anything.
 */
abstract class Component {
  
  /* EntityType : Which Component type is this object associated with?
   * Used to limit inheritance-based reflection.
   * (i.e. All subclasses of "Renderable" have EntityType = Renderable, so that
   * all sub-objects take up the Renderable slot in the Component map.)
   * */
  def entityType : Class[_ <: Component]
  
  def withInit(f:(this.type)=>Unit):this.type = {
    f(this)
    this
  }
  
}