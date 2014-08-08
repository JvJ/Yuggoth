

package com.jvj.gdxutil

import com.jvj.ecs._
import scala.collection._

abstract class Updater extends Component{
  final val componentType = classOf[Updater]
  
  def update(dt:Float, ec: EntityCollection, e:Entity):Unit
}

class MultiUpdater(ups:AbstractSeq[Updater]) extends Updater{
  
  val _ups = ups.toList
  
  def update(dt:Float, ec: EntityCollection, e:Entity):Unit =
    _ups.foreach((u)=>u.update(dt, ec, e))
}

