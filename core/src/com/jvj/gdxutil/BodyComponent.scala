package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.math._

/*Please extend this class to make use of any fixture identifiers you
 * see fit.*/
abstract class FixtureData
case object FixtureNoData extends FixtureData
case class FixtureTag(s:Symbol) extends FixtureData

/* A body component.
 * The beginContactEvent and endContactEvent take four parameters:
 * 	-This entity:Entity
 *  -This fixture:Fixture
 *  -Other fixture:Fixture
 *  -The contact event:Contact
 * */
class BodyComponent(
    world:World,
    fixDefs:Seq[(FixtureDef, FixtureData)],
    bodyType:BodyDef.BodyType,
    position:Vector2,
    val beginContactEvent:((Entity, Fixture, Fixture, Contact) =>Unit),
    val endContactEvent:((Entity, Fixture, Fixture, Contact) => Unit))
    extends Component {
  
  override val typeTags = List(classOf[BodyComponent])
  
  var bdef = new BodyDef()
  bdef.`type` = bodyType
  bdef.position.x = position.x
  bdef.position.y = position.y
  
  val body:Body = world.createBody(bdef)
  
  // Create all the fixtures and add user data
  fixDefs foreach {case (f,d) => body.createFixture(f).setUserData(d)}
  
  // Set user data of the body to the entity
  override def whenAdded(e:Entity) = {
    super.whenAdded(e)
    body.setUserData(this.getEntity())
  }
  
}

/* This is used to extend collision handling behaviour.
 * */
object CollisionHandler {
  def nop (e:Entity, thisFix:Fixture, thatFix:Fixture, c:Contact):Unit = {
    // It's a no-op
  }
}
class CollisionHandler(ec:EntityCollection) extends ContactListener {
  
  override def beginContact(c:Contact) = {
    
    println("Begin contact");
    
    // Execute contact events for each bodyComponent in the fixture
    val (fa,fb) = (c.getFixtureA(), c.getFixtureB())
    
    fa.getBody().getUserData() match {
      case e:Entity => e[BodyComponent] match {
        case Some(b) => b.beginContactEvent (e, fa, fb, c)
        case _ => ;
      }
      case _ => ;
    }
    
    fb.getBody().getUserData() match {
      case e:Entity => e[BodyComponent] match {
        case Some(b) => b.beginContactEvent (e, fb, fa, c)
        case _ => ;
      }
      case _ => ;
    }
  }
  
  override def endContact(c:Contact)  = {
    
    println("End contact");
    
    // Execute contact events for each bodyComponent in the fixture
    val (fa,fb) = (c.getFixtureA(), c.getFixtureB())
    
    fa.getBody().getUserData() match {
      case e:Entity => e[BodyComponent] match {
        case Some(b) => b.endContactEvent  (e, fa, fb, c)
        case _ => ;
      }
      case _ => ;
    }
    
    fb.getBody().getUserData() match {
      case e:Entity => e[BodyComponent] match {
        case Some(b) => b.endContactEvent (e, fb, fa, c)
        case _ => ;
      }
      case _ => ;
    }
  }
  
  override def preSolve(c:Contact, m:Manifold) = {
    // TODO: Don't know what this is
  }
  
  override def postSolve(c:Contact, imp:ContactImpulse) = {
    // TODO: Don't know what this is either
  }
  
}


// TODO: Should this be related to graphics anymore?
object SysRenderableBody extends System{
  def apply(ec:EntityCollection, e:Entity) = {
  	(e[Renderer], e[BodyComponent]) match {
  	  case (Some(r), Some(b)) => {
  	    e[TransformComponent] match{
  	      case Some(t) =>
  	        println(s"Setting position to: ${b.body.getPosition()}.")
  	        t.position = b.body.getPosition()
  	        t.rotation = Math.toDegrees(b.body.getAngle()).toFloat
  	      case _ => ;
  	    }
  	  }
  	  case _ => ;
  	}
  	ec
  }
}

class SysPhysics(gravity:Vector2) extends System{
  
  val world = new World(gravity, true)
  
  override def apply(ec:EntityCollection) = {
    
    world.step(Gdx.graphics.getDeltaTime() , 50, 20)
    
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity):EntityCollection = ec
}

class SysPhysicsRender(sp:SysPhysics, dbg:Boolean) extends System{
  
  var debug = dbg
  val dbgRenderer:Box2DDebugRenderer = new Box2DDebugRenderer()
  
  override def apply(ec:EntityCollection) = {
    if (debug){

      var mat = new Matrix4(SysRender.camera.combined)
      
      var pos = new Vector2(SysRender.position).scl(-1f)
      
      dbgRenderer.render(sp.world, mat.translate(pos.x, pos.y, 0))
    }
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity):EntityCollection = ec
}