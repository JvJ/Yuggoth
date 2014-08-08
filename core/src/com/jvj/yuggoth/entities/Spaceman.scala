package com.jvj.yuggoth.entities

import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.graphics.g2d._
import com.jvj.ecs._
import com.jvj.gdxutil._
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode._
import com.badlogic.gdx.math.Vector2

/* This is a singleton object with methods for generating
 * a spaceman entity and related data.
 * // TODO: Extend EntityFactory?
 * */
object Spaceman extends EntityFactory{

  def fixtures(w:World):Seq[FixtureDef] = {
    
    var box = new FixtureDef()
    var boxShape = new PolygonShape()
    boxShape.setAsBox(0.35f, 0.7f, new Vector2(0f, 0.05f), 0f)
    box.shape = boxShape
    box.friction = 1
    box.density = 1
    
    // Cons it onto the rest
    box::(for (x <- -2 to 1)  yield {
      var fix = new FixtureDef()
      var circ = new CircleShape()
      circ.setRadius(0.1f)
      circ.setPosition(new Vector2(x * 0.35f/2f + 0.1f, -0.65f))
      fix.shape = circ
      fix.friction = 1
      fix.density = 1
      fix
    }).toList
  }
  
  def sprite():SpriteSpec =
    new SpriteSpec(
          // Filename and cell size
          "spaceman_sheet_hires.png", (64,96),
          // Specifying sprite states with mode, delay,
          // and cell list
          'Walking ->	Frames(LOOP, 0.1f,
          					(0,1),(0,2),(0,3),(0,4),(0,5)),
          'Standing ->	Frames(LOOP, 0.1f,
        		  			(0,0)),
          'Jumping ->	Frames(NORMAL, 0.1f,
        		  			(1,0)))
    
  
  override def create(position:Vector2,
      world:World,
      batch:SpriteBatch):Entity = {
    
    new Entity(
    	WorldPosition(position),
    	WorldRotation(0),
    	WorldOrigin(new Vector2(0.5f, 0.75f)),
    	WorldSize(new Vector2(1f, 1.5f)),
        new SpriteComponent('Standing, batch, sprite),
    	new BodyComponent(world, fixtures(world),
    	    BodyDef.BodyType.DynamicBody, position) withInit {
    	  t =>
    	    t.body.setFixedRotation(true)
    	}
        )
  }
}