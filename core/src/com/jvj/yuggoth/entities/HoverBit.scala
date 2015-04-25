package com.jvj.yuggoth.entities

import com.jvj.yuggoth._
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints._
import com.badlogic.gdx.graphics.g2d._
import com.jvj.ecs._
import com.jvj.gdxutil._
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.JointDef.JointType
import com.jvj.gdxutil.MathUtil._
import com.badlogic.gdx.Input.Keys._

object HoverBit extends EntityFactory{
  
  
  def sprite():SpriteSpec = 
    new SpriteSpec(
        "hoverbit.png", (32,32),
        'Still -> Frames(LOOP, 1f, (0,0)))
  
  override def create(position:Vector2,
      world:World,
      batch:SpriteBatch) = {
    
    new Entity(
        EntityName("HoverBit"),
        new WorldTransform(
            position,
            v2(1.5f, 1.5f),
            v2(1f, 1f),
            v2(0.75f, 0.75f),
            0,
            (false, false),
            'BabyBit -> new Entity(
                EntityName("BabyBit"),
                new WorldTransform(
                  v2(0,-0.2f),
                  v2(0.5f, 0.5f),
                  v2(1f,1f),
                  v2(0.25f, 0.25f),
                  0,
                  (false, false)),
                new SpriteComponent('Still, batch, sprite) withInit {
                    t => t.layer = 3
                  }
                ),
            'BabyBit2 -> new Entity(
                EntityName("BabyBit2"),
                new WorldTransform(
                  v2(-1f,-1f),
                  v2(0.5f, 0.5f),
                  v2(1f,1f),
                  v2(0.5f, 0.5f),
                  0,
                  (false, false)),
                new SpriteComponent('Still, batch, sprite) withInit {
                    t => t.layer = 3
                  }
                )
            ),
         new SpriteComponent('Still, batch, sprite) withInit {
          t => t.layer = 1
        }
        )
    
  }
  
}


object hoverBitUpdater extends System {
  def apply(ec:EntityCollection, e:Entity) = 
    (e.id,
     e[TransformComponent]) match {
      case (EntityName("HoverBit"),
          Some(tc)) => {
            var tcc = tc
            // LEFTOFF: BabyBit movement here
            (KeyState(NUM_1), KeyState(NUM_2)) match {
              case (Some(Held(_)), None) => tc.rotation += -0.4f
              case (None, Some(Held(_))) => tc.rotation += 0.4f
              case _ => ;
            }
            (KeyState(NUM_3), KeyState(NUM_4)) match {
              case _ => ;
            }
            ec
          }
      case _ => ec;
    }
}

object babyBitUpdater extends System {
  def apply(ec:EntityCollection, e:Entity) = 
    (e.id,
     e[TransformComponent]) match {
      case (EntityName("BabyBit"),
          Some(tc)) => {
            var tcc = tc
            // LEFTOFF: BabyBit movement here
            (KeyState('CamRight), KeyState('CamLeft)) match {
              case (Some(Held(_)), None) => tc.position += v2(0.05f,0f)
              case (None, Some(Held(_))) => tc.position += v2(-0.05f,0f)
              case _ => ;
            }
            (KeyState('CamUp), KeyState('CamDown)) match {
              case _ => ;
            }
            ec
          }
      case _ => ec;
    }
}