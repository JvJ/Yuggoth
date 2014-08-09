package com.jvj.ecs

/* Systems can be applied to whole collections or
 * individual entities.
 * */
abstract class System {
  
  /* Apply the system to an entire entity collection.
   * */
  def apply (ec:EntityCollection):EntityCollection = ec.foldLeft(ec)(apply)
    
  /* Apply the system to a single entity in this collection.
   * */
  def apply(ec:EntityCollection, e:Entity):EntityCollection
}