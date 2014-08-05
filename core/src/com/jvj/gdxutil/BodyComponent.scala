package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.math._



class BodyComponent(
    world:World,
    fixDef:FixtureDef,
    bodyType:BodyDef.BodyType,
    position:Vector2)
    extends Component {

  override val entityType = classOf[BodyComponent]
  
  var bdef = new BodyDef()
  bdef.`type` = bodyType
  
  val body:Body = world.createBody(bdef)
  
}

class SysPhysics(gravity:Vector2, dbg:Boolean) extends System{
  
  var debug = dbg
  val world = new World(gravity, true)
  val dbgRenderer:Box2DDebugRenderer = new Box2DDebugRenderer()
  
  override def apply(ec:EntityCollection) = {
    
    var gl = Gdx.gl
    var gl20 = Gdx.gl20
    var gl30 = Gdx.gl30
    
    world.step(Gdx.graphics.getDeltaTime() , 6, 2)
    
    if (debug){
      // TODO: This uses an identity matrix.  This needs to change
      dbgRenderer.render(world, new Matrix4())
    }
    
    ec
  }
  
  def apply(ec:EntityCollection, e:Entity) = {
    // Nop!
  }
}