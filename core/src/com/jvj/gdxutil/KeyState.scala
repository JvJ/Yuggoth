package com.jvj.gdxutil

import com.jvj.ecs._
import scala.collection._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.input._
import com.badlogic.gdx.Input
import java.util.EnumSet
import com.badlogic.gdx.InputProcessor

abstract class ButtonState
case object Pressed extends ButtonState
case class Held(time:Float) extends ButtonState
case object Released extends ButtonState
case class Axis(value:Float) extends ButtonState



/* This system should be executed at the beginning of an update cycle.
 * */
object KeyState extends System with InputProcessor{

  private var nameToKey = Map[Symbol, Int]()
  private var keyToName = Map[Int, Symbol]()
  private var keyToState = new mutable.HashMap[Int, ButtonState]()
  
  def setMappings(mappings:Map[Symbol, Int]) = {
    nameToKey = mappings map identity
    keyToName = mappings map {case (k,v) => v->k}
    this
  }

  def getKey(s:Symbol) = nameToKey.get(s)
  def getName(k:Int) = keyToName.get(k)
  
  def getKeyState(s:Symbol):Option[ButtonState] = getKey(s) flatMap keyToState.get
  def getKeyState(keyCode:Int):Option[ButtonState] = keyToState.get(keyCode)
  def apply(s:Symbol) = getKeyState(s)
  def apply(k:Int) = getKeyState(k)
  
  override def apply(ec:EntityCollection) = {
    
    val dt = Gdx.graphics.getDeltaTime()
    
    // Check all the inputs!
    for ((k, v) <- keyToState ){
      v match {
        case Released =>
          keyToState.remove(k)
        case Pressed =>
          keyToState(k) = Held(dt)
        case Held(t) =>
          keyToState(k) = Held(t+dt)
        case _ => ;
      }
    }    
    
    ec
  }
  
  override def apply(ec:EntityCollection, e:Entity) = ec
  
  
  // Input processor methods
  
  override def keyDown(keyCode:Int) = {
    val dt = Gdx.graphics.getDeltaTime()
    
    keyToState.get(keyCode) match {
    	case None => keyToState(keyCode) = Pressed
    	case _ => ;
    	}
    true
  }
  
  override def keyUp(keyCode:Int) = {
    keyToState(keyCode) = Released
    true
  }
  
  override def keyTyped(character:Char) = false
  
  override def mouseMoved(screenX:Int, screenY:Int) = false
  
  override def scrolled(amount:Int) = false
  
  override def touchDown(screenX:Int, screenY:Int, pointer:Int, button:Int) = false
  
  override def touchDragged(screenX:Int, screenY:Int, ponter:Int) = false
  
  override def touchUp(screenX:Int, screenY:Int, pointer:Int, button:Int) = false
}