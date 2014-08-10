package com.jvj.yuggoth

import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys._

/* This is the global settings object for Yuggoth.
 * TODO: It should be readable from XML.
 * */
object Glob {

  var debugPhysics = true
  
  // Global key mappings
  val keyMappings = Map (
      
      // For debugs
      'TogglePhysics -> F1,
      'ZoomIn -> EQUALS,
      'ZoomOut -> MINUS,
      
      // For controls
      'MoveLeft -> A,
      'MoveDown -> S,
      'MoveUp -> W,
      'MoveRight -> D,
      'Jump -> SPACE
      
      )
  
}