package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.math._



class BodyComponent(
    world:World,
    fixDefs:Seq[FixtureDef],
    bodyType:BodyDef.BodyType,
    position:Vector2)
    extends Component {

  override val componentType = classOf[BodyComponent]
  
  var bdef = new BodyDef()
  bdef.`type` = bodyType
  bdef.position.x = position.x
  bdef.position.y = position.y
  
  val body:Body = world.createBody(bdef)
  
  // Create all the fixtures
  fixDefs foreach {body.createFixture(_)}
  
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
  }
}

class SysPhysics(gravity:Vector2) extends System{
  
  val world = new World(gravity, true)
  
  override def apply(ec:EntityCollection) = {
    
    world.step(Gdx.graphics.getDeltaTime() , 50, 20)
    
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity) = {
    // Nop!
  }
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
  
  def apply(ec:EntityCollection, e:Entity) = {
    // Nop!
  }
}