package com.jvj.yuggoth.entities

import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.graphics.g2d._
import com.jvj.ecs._
import com.jvj.gdxutil._
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

// Custom fixture data classes
case object FixSpacemanBody extends FixtureData
case class FixSpacemanCircle(index:Int) extends FixtureData

/* This is a singleton object with methods for generating
 * a spaceman entity and related data.
 * // TODO: Extend EntityFactory?
 * */
object Spaceman extends EntityFactory{

  def fixtures():Seq[(FixtureDef, FixtureData)] = {
    
    var box = new FixtureDef()
    var boxShape = new PolygonShape()
    boxShape.setAsBox(0.3f, 0.7f, new Vector2(0f, 0.0f), 0f)
    box.shape = boxShape
    box.friction = 0
    box.density = 1
    
    // Cons it onto the rest
    (box, FixSpacemanBody)::(for (x <- -2 to 1)  yield {
      var fix = new FixtureDef()
      var circ = new CircleShape()
      circ.setRadius(0.3f/4)
      circ.setPosition(new Vector2(x * 0.3f/2f + (0.3f/4), -0.65f))
      fix.shape = circ
      fix.friction = 1
      fix.density = 1
      (fix, FixSpacemanCircle(x+2))
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
  
  class SpacemanState extends Component{
    val componentType = classOf[SpacemanState]
    
    var grounded = false
    
    }
  
  /* A collision handler for the spaceman.*/
  def collision(c:Contact) = {
    
    for (fix<-List(c.getFixtureA(), c.getFixtureB())){
      fix.getUserData() match {
        case FixSpacemanCircle(i) => println(s"Circle $i made contact.")
        case _ => ;
      }
    }
    
  }
  
  override def create(position:Vector2,
      world:World,
      batch:SpriteBatch):Entity = {
    
    new Entity(
        new SpacemanState(),
        Flip(false, false),
    	WorldPosition(position),
    	WorldRotation(0),
    	WorldOrigin(new Vector2(0.5f, 0.75f)),
    	WorldSize(new Vector2(1f, 1.5f)),
        new SpriteComponent('Standing, batch, sprite) withInit {
    	  t => t.layer = 1
    	},
    	new BodyComponent(world, fixtures(),
    	    BodyDef.BodyType.DynamicBody, position,
    	    collision,
    	    {_=>}) withInit {
    	  t =>
    	    t.body.setFixedRotation(true)
    	}
        )
  }
  
  object updater extends System {
    def apply(ec:EntityCollection, e:Entity) = {
      
      (e[SpacemanState],
       e[BodyComponent],
       e[FlipComponent],
       e[Renderer]) match {
        case (Some(_),
            Some(b),
            Some(f@Flip(_,_)),
            Some(sprite:SpriteComponent)) =>{
          
          var v = b.body.getLinearVelocity()
          
          // Left and right movement
          (KeyState('MoveLeft), KeyState('MoveRight)) match {
            // TODO: Set appropriate speed
            case (Some(Held(_)|Pressed), None) => v.x = -2; f.x = true; sprite.setState('Walking)
            case (None, Some(Held(_)|Pressed)) => v.x = 2; f.x = false; sprite.setState('Walking)
            case _ => v.x = 0; sprite.setState('Standing)
          }
          
          // Jumps
          // TODO: Collision handler for groundedness
          KeyState('Jump) match {
            case Some(Pressed) => v.y = 5
            case _ => ;
          }
          
          b.body.setLinearVelocity(v)
          
          // Update camera
          val pos = b.body.getPosition().scl(SysRender.pixToWorld )
          SysRender.camera .position.set(pos.x, pos.y, 0)
        }
        case _ => ;
      }      
      ec
    }
  }
  
}