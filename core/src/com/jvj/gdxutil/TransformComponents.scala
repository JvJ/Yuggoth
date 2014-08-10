package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.math.Vector2

abstract class PositionComponent extends Component{
  val componentType = classOf[PositionComponent]
}

case class ScreenPosition(var pos:Vector2) extends PositionComponent
case class WorldPosition(var pos:Vector2) extends PositionComponent{
  def transform:Vector2 = SysRender.transformV(new Vector2(pos))
}

abstract class VelocityComponent extends Component {
  val componentType = classOf[VelocityComponent]
}

case class ScreenVelocity(var vel:Vector2) extends VelocityComponent
case class WorldVelocity(var vel:Vector2) extends VelocityComponent{
  def transform:Vector2 = new Vector2(vel).scl(SysRender.pixToWorld)
}

abstract class RotationComponent extends Component{
  val componentType = classOf[RotationComponent]
}

case class ScreenRotation(var rot:Float) extends RotationComponent
case class WorldRotation(var rot:Float) extends RotationComponent

abstract class SizeComponent extends Component{
  val componentType = classOf[SizeComponent]
}

case class ScreenSize(var size:Vector2) extends SizeComponent
case class WorldSize(var size:Vector2) extends SizeComponent{
  def transform:Vector2 = new Vector2(size).scl(SysRender.pixToWorld )
}

/* Represents a rotation origin (i.e. anchor point) for a renderer.
 * */
abstract class OriginComponent extends Component{
  val componentType = classOf[OriginComponent]
}

case class ScreenOrigin(var origin:Vector2) extends OriginComponent
case class WorldOrigin(var origin:Vector2) extends OriginComponent{
  def transform:Vector2 = new Vector2(origin).scl(SysRender.pixToWorld)
}

abstract class FlipComponent extends Component{
  val componentType = classOf[FlipComponent]
}

case class Flip(var x:Boolean, var y:Boolean) extends FlipComponent