package com.jvj.yuggoth.entities

import com.jvj.ecs._
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.math._


/* Inherited by other entity factories!
 * */
abstract class EntityFactory {

  /*Override this to create the entity!*/
  def create(
      position:Vector2,
      world:World,
      batch:SpriteBatch) : Entity
  
}