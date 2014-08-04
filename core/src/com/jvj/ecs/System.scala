package com.jvj.ecs

/* Systems can be applied to whole collections or
 * individual entities.
 * */
abstract class System {
  def apply (ec:EntityCollection):EntityCollection = {
    ec.foreach((e) => this(ec, e))
    ec
  }
  def apply(ec:EntityCollection, e:Entity):Unit
}