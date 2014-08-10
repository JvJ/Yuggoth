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

class BodyComponent(
    world:World,
    fixDefs:Seq[(FixtureDef, FixtureData)],
    bodyType:BodyDef.BodyType,
    position:Vector2,
    val beginContactEvent:(Contact =>Unit),
    val endContactEvent:(Contact => Unit))
    extends Component {
  
  override val componentType = classOf[BodyComponent]
  
  var bdef = new BodyDef()
  bdef.`type` = bodyType
  bdef.position.x = position.x
  bdef.position.y = position.y
  
  val body:Body = world.createBody(bdef)
  
  // Create all the fixtures and add user data
  fixDefs foreach {case (f,d) => body.createFixture(f).setUserData(d)}
  
  // Set user data of the body to the entity
  body.setUserData(this.getEntity())
  
}

/* This is used to extend 
 * */
class CollisionHandler(ec:EntityCollection) extends ContactListener {
  override def beginContact(c:Contact) = {
    // Execute contact events for each bodyComponent in the fixture
    for (fx <- List(c.getFixtureA(), c.getFixtureB())){
      fx.getBody().getUserData() match {
        case e:Entity => e[BodyComponent] match {
          case Some(b) => b.beginContactEvent (c)
          case _ => ;
        }
        case _ => ;
      }
    }
  }
  
  override def endContact(c:Contact)  = {
    // Execute contact events for each bodyComponent in the fixture
    for (fx <- List(c.getFixtureA(), c.getFixtureB())){
      fx.getBody().getUserData() match {
        case e:Entity => e[BodyComponent] match {
          case Some(b) => b.endContactEvent (c)
          case _ => ;
        }
        case _ => ;
      }
    }
  }
  
  override def preSolve(c:Contact, m:Manifold) = {
    // TODO: Don't know what this is
  }
  
  override def postSolve(c:Contact, imp:ContactImpulse) = {
    // TODO: Don't know what this is either
  }
  
}

object SysRenderableBody extends System{
  def apply(ec:EntityCollection, e:Entity) = {
  	(e[Renderer], e[BodyComponent]) match {
  	  case (Some(r), Some(b)) => {
  	    e[PositionComponent] match{
  	      case Some(WorldPosition(v)) => v.set(b.body.getPosition())
  	      case _ => ;
  	    }
  	    e[RotationComponent] match {
  	      case Some(wr@WorldRotation(_)) => wr.rot = b.body.getAngle()
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
      
      var pos = new Vector2(SysRender.position)
      var scal = new Vector2(SysRender.pixToWorld)
      pos.scl(-1)
      pos.scl(SysRender.pixToWorld)
      mat.translate(new Vector3(pos.x, pos.y, 0))
      mat.scale(scal.x, scal.y, 0)
      dbgRenderer.render(sp.world, mat)
    }
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity):EntityCollection = ec
}