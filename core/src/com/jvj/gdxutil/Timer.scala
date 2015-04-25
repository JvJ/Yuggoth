package com.jvj.gdxutil

import com.jvj.ecs._

/* A trigger that fires when a timeout has elapsed.
 * 
 * Not yet implemented!
 * */
class TimerComponent extends TriggerComponent{

  override val typeTags = List(classOf[TimerComponent])
 
  override def fireCondition(ec:EntityCollection, e:Entity) = {
    ec
  }
  
  override def update(ec:EntityCollection, e:Entity) = {
    ec
  }
  
}