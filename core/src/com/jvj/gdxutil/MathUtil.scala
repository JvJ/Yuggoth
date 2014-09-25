package com.jvj.gdxutil

import com.badlogic.gdx.math._

object MathUtil {
 
  def v2(x:Float, y:Float) = new Vector2(x,y)
  def v2(v:Vector2) = new Vector2(v)
  def v2(v:Vector3) = new Vector2(v.x, v.y)
  
  implicit class OpVec2(val v:Vector2) {
    
    def + (vv: Vector2) : Vector2 = new Vector2(v).add(vv)
    def += (vv: Vector2) : Vector2 = v.add(vv)
    
    def - (vv: Vector2) : Vector2 = new Vector2(v).sub(vv)
    def -= (vv: Vector2) :Vector2 = v.sub(vv)
    
    def unary_- () : Vector2 = new Vector2(v).scl(-1)
    
    def * (f: Float) : Vector2 = new Vector2(v).scl(f)
    def *= (f:Float) : Vector2 = v.scl(f)
    
    def / (f: Float) : Vector2 = new Vector2(v).scl(1f/f)
    def /= (f: Float) : Vector2 = v.scl(1f/f)
    
    def * (vv:Vector2) : Vector2 = new Vector2(v).scl(vv)
    def *= (vv:Vector2) : Vector2 = v.scl(vv)
    
    def * (m: Matrix3) : Vector2 = new Vector2(v).mul(m)
    def *= (m: Matrix3) : Vector2 = v.mul(m)
    
    // Rotation
    def ^> (f : Float) : Vector2 = new Vector2(v).rotate(f)
    def ^>= (f : Float) : Vector2 = v.rotate(f)
    // Radian rotation
    def ^^> (f:Float) : Vector2 = new Vector2(v).rotateRad(f)
    def ^^>= (f:Float) : Vector2 = v.rotateRad(f)
    
    
    def % (vv: Vector2) : Float = new Vector2(v).dot(vv)
    
    def unary_~ () :Vector2 = new Vector2(v).nor()
  }
  
  def v3(x:Float, y:Float, z:Float) = new Vector3(x,y,z)
  def v3(v:Vector3) = new Vector3(v)
  def v3(v:Vector2) = new Vector3(v.x, v.y, 0)
  
  implicit class OpVec3(val v:Vector3) {
    
    def + (vv: Vector3) : Vector3 = new Vector3(v).add(vv)
    def += (vv: Vector3) : Vector3 = v.add(vv)
    
    def - (vv: Vector3) : Vector3 = new Vector3(v).sub(vv)
    def -= (vv: Vector3) :Vector3 = v.sub(vv)
    
    def unary_- () : Vector3 = new Vector3(v).scl(-1)
    
    def >< (vv:Vector3):Vector3 = new Vector3(v).crs(vv)
    def ><= (vv:Vector3):Vector3 = v.crs(vv)
    
    def * (f: Float) : Vector3 = new Vector3(v).scl(f)
    def *= (f:Float) : Vector3 = v.scl(f)
    
    def / (f: Float) : Vector3 = new Vector3(v).scl(1f/f)
    def /= (f: Float) : Vector3 = v.scl(1f/f)
    
    def * (m: Matrix4) : Vector3 = new Vector3(v).mul(m)
    def *= (m: Matrix4) : Vector3 = v.mul(m)
    
    def % (vv: Vector3) : Float = new Vector3(v).dot(vv)
  }
  
  def m3() = new Matrix3()
  def m3(m:Matrix3) = new Matrix3(m)
  
  implicit class OpMat3 (val m:Matrix3) {
    
    def * (mm: Matrix3) : Matrix3 = new Matrix3(m).mul(mm)
    def *= (mm: Matrix3) : Matrix3 = m.mul(mm)
    
    def * (f:Float) = new Matrix3(m).scl(f)
    def *= (f:Float) = m.scl(f)
    
    def * (v:Vector2) = new Matrix3(m).scl(v)
    def *= (v:Vector2) = m.scl(v)
    
    def * (v:Vector3) = new Matrix3(m).scl(v)
    def *= (v:Vector3) = m.scl(v)
    
    def ^> (f: Float) = new Matrix3(m).rotate(f)
    def ^>= (f:Float) = m.rotate(f)
    
    def ^^> (f:Float) = new Matrix3(m).rotateRad(f)
    def ^^>= (f:Float) = m.rotateRad(f)
    
    
  }
  
  def m4() = new Matrix4()
  def m4(m:Matrix4) = new Matrix4(m)
  
  implicit class OpMat4 (val m:Matrix4) {
    
    def * (mm: Matrix4) : Matrix4 = new Matrix4(m).mul(mm)
    def *= (mm: Matrix4) : Matrix4 = m.mul(mm)
    
    def * (f:Float) = new Matrix4(m).scl(f)
    def *= (f:Float) = m.scl(f)
    
    def * (v:Vector3) = new Matrix4(m).scl(v)
    def *= (v:Vector3) = m.scl(v)
  }
}