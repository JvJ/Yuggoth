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

// Custom fixture data classes
case object FixSpacemanBody extends FixtureData
case class FixSpacemanCircle(var contactFloor:Boolean) extends FixtureData

/* This is a singleton object with methods for generating
 * a spaceman entity and related data.
 * // TODO: Extend EntityFactory?
 * */
object Spaceman extends EntityFactory{
  
  def fixtures():Seq[(FixtureDef, FixtureData)] = {
    
    val bWidth = 0.6f
    val bHeight = 1.45f
    val numCircles = 8
    val circleRadius = (bWidth/numCircles)/2f
    
    var box = new FixtureDef()
    var boxShape = new PolygonShape()
    boxShape.setAsBox(bWidth/2f,
        bHeight/2f - circleRadius,
        new Vector2(0f, circleRadius/2f),
        0f)
    box.shape = boxShape
    box.friction = 0
    box.density = 1
    
    
    var (fix1, fix2) = (new FixtureDef(), new FixtureDef())
    var (circ1, circ2) = (new CircleShape(), new CircleShape())
    circ1.setRadius(circleRadius)
    circ2.setRadius(circleRadius)
    circ1.setPosition(new Vector2(circleRadius - (bWidth/2), circleRadius-(bHeight/2)))
    circ2.setPosition(new Vector2((bWidth/2) - circleRadius, circleRadius-(bHeight/2)))
    fix1.shape = circ1
    fix2.shape = circ2
    fix1.friction = 2f
    fix2.friction = 2f
    fix1.density = 1
    fix2.density = 1
    
    var fixLine = new FixtureDef()
    //var line = new EdgeShape()
    //line.set(circleRadius - (bWidth/2), -(bHeight/2),
    //    (bWidth/2) - circleRadius, -(bHeight/2))
    var box2 = new PolygonShape()
    box2.setAsBox(bWidth/2f-circleRadius, circleRadius, new Vector2(0f, -(bHeight/2) + circleRadius), 0f)
    fixLine.shape = box2
    fixLine.density = 1f
    fixLine.friction = 2f
        
    List((box, FixSpacemanBody),
        (fix1, FixSpacemanCircle(false)),
        (fix2, FixSpacemanCircle(false)),
        (fixLine, FixSpacemanCircle(false)))
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
          'Jumping ->	Frames(LOOP, 0.1f,
        		  			(1,0)))
  
  class SpacemanState extends Component{
    val typeTags = List(classOf[SpacemanState])
    
    def getGrounded = circles.foldLeft(false){
        (acc, fix) => fix match {
          case FixSpacemanCircle(b) => acc || b
          case _ => acc
        }
      }
    
    def setGrounded(b:Boolean) = {
      for (f@FixSpacemanCircle(_) <- circles) f.contactFloor = b
    }
    
    var circles = List():List[FixtureData]
    
    }
  
  /* A collision handler for the spaceman.*/
  def collision(start:Boolean)(e:Entity, thisFix:Fixture, thatFix:Fixture, c:Contact) = {
   
    (e[SpacemanState],
        thisFix.getUserData(),
        thatFix.getUserData()) match {
      case (Some(state),
          f@FixSpacemanCircle(_),
          Floor) => {
            
        f.contactFloor = start
      }
      case _ => ;
    }
   
  }
  
  override def create(position:Vector2,
      world:World,
      batch:SpriteBatch):Entity = {
    
    val fixes = fixtures
    val circs =  (fixes map {
      case (_, f@FixSpacemanCircle(_)) => List(f)
      case _ => List()
    }).foldLeft(List():List[FixtureData])(_++_)
    
    val ent = new Entity(
        EntityName("Spaceman"),
        new SpacemanState() withInit {
          t=> t.circles = circs
        },
        new WorldTransform(
            position,
            new Vector2(1f, 1.5f),
            new Vector2(1.0f, 1.0f),
            new Vector2(0.5f, 0.75f),
            0f,
            (false, false),
            'HoverBit -> HoverBit.create(
                -v2(1f, 0),
                world,
                batch
                )
            ),
        new SpriteComponent('Standing, batch, sprite) withInit {
    	  t => t.layer = 1
    	},
    	new BodyComponent(world, fixes,
    	    BodyDef.BodyType.DynamicBody, position,
    	    collision(true),
    	    collision(false)) withInit {
    	  t =>
    	    t.body.setFixedRotation(true)
    	})
    
    ent
  }
  
  object updater extends System {
    def apply(ec:EntityCollection, e:Entity) = {
      
      (e[SpacemanState],
       e[BodyComponent],
       e[TransformComponent],
       e[Renderer]) match {
        case (Some(state),
            Some(b),
            Some(t),
            Some(sprite:SpriteComponent)) =>{
          
              // Cap the velocity if grounded
              // TODO: Sliding after thrusting?
	          var v = b.body.getLinearVelocity()
	          if (state.getGrounded && Math.abs(v.x) > Glob.Spaceman.maxSpeed  ){
	            v.x = Math.signum(v.x) * Glob.Spaceman.maxSpeed 
	            b.body .setLinearVelocity(v)
	          }
	          
	          // Left and right movement
	          if (state.getGrounded){
		          (KeyState('MoveLeft), KeyState('MoveRight)) match {
		            // TODO: Set appropriate speed
		            case (Some(Held(_)|Pressed), None) =>
		              b.body.applyForce(-10, 0, 0, 0, true); t.flipX = true; if (state.getGrounded) sprite.setState('Walking)
		            case (None, Some(Held(_)|Pressed)) =>
		              b.body.applyForce(10, 0, 0, 0, true);; t.flipX = false; if (state.getGrounded) sprite.setState('Walking)
		            case _ => v.x = 0; if (state.getGrounded) sprite.setState('Standing)
		          }
	          }
	          
	          // Thrusters
	          // TODO: Should the thrusters be... held?
	          (KeyState('ThrustLeft), KeyState('ThrustRight)) match {
	            case (Some(Pressed), None) =>
	              b.body .applyLinearImpulse(new Vector2(-1, 0), b.body .getWorldCenter(), true)
	            case (None, Some(Pressed)) =>
	              b.body .applyLinearImpulse(new Vector2(1, 0), b.body .getWorldCenter(), true)
	            case _ => ;
	          }
	          
	          // Jumps
	          KeyState('Jump) match {
	            case Some(Pressed) =>
	              state.setGrounded(false)
	              b.body .applyLinearImpulse(new Vector2(0,Glob.Spaceman.firstImpulse ), b.body.getWorldCenter(), true);
	            case Some(Held(t)) if(t<Glob.Spaceman.maxJumpTime  ) =>
	              b.body .applyLinearImpulse(new Vector2(0,Glob.Spaceman.impulseFactor ), b.body.getWorldCenter(), true); 
	            case _ => ;
	          }
	          
            // Whatevs
            (KeyState(NUM_3), KeyState(NUM_4)) match{
              case (Some(Held(_)), None) => t.scale -= v2(0.1f,0.1f)
              case (None, Some(Held(_))) => t.scale += v2(0.1f,0.1f)
              case _ => ;
            }
            
	          if (!state.getGrounded){
	            sprite.setState('Jumping); 
	          }
	          
	          //b.body.setLinearVelocity(v)
	          
	          // Update camera
	          val pos = new Vector2(b.body.getPosition())
	          SysRender.camera.position.set(pos.x, pos.y, 0)
        }
        case _ => ;
      }      
      ec
    }
  }
  
}