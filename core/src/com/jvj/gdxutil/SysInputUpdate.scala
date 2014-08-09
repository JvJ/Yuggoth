package com.jvj.gdxutil

import com.jvj.ecs._

// LEFTOFF: Do this!
object SysInputUpdate extends System{

  override def apply(ec:EntityCollection) = {
    
    ec
  }
  
  override def apply(ec:EntityCollection, e:Entity) = ec
  
}