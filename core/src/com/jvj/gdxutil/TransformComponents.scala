package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.math.Vector2

abstract class PositionComponent extends Component{
  val componentType = classOf[PositionComponent]
}

case class ScreenPosition(var pos:Vector2) extends PositionComponent
case class WorldPosition(var pos:Vector2) extends PositionComponent

abstract class VelocityComponent extends Component {
  val componentType = classOf[VelocityComponent]
}

case class ScreenVelocity(var pos:Vector2) extends VelocityComponent
case class WorldVelocity(var pos:Vector2) extends VelocityComponent

abstract class RotationComponent extends Component{
  val componentType = classOf[RotationComponent]
}

case class ScreenRotation(var rot:Float) extends RotationComponent
case class WorldRotation(var rot:Float) extends RotationComponent

abstract class SizeComponent extends Component{
  val componentType = classOf[SizeComponent]
}

case class ScreenSize(var rot:Float) extends RotationComponent
case class WorldSize(var rot:Float) extends RotationComponent