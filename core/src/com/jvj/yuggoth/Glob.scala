package com.jvj.yuggoth

import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys._
import com.badlogic.gdx.math.Vector2

/* This is the global settings object for Yuggoth.
 * TODO: It should be readable from XML.
 * */
object Glob {

  var debugPhysics = true
  
  val gravity = new Vector2(0, -5f)
  
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