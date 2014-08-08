package com.jvj.gdxutil

import com.jvj.ecs._
import com.badlogic.gdx.maps._
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.graphics.g3d.Renderable
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.graphics.OrthographicCamera

/* Static vars for the MapComponent class.
 * */
object MapComponent {
  
  private var mapLoader:TmxMapLoader = null
  
}


class MapComponent(filename:String, batch:SpriteBatch) extends Renderer{
  
  if (MapComponent.mapLoader == null){
    MapComponent.mapLoader  = new TmxMapLoader()
  }
  
  var map = MapComponent.mapLoader.load(filename)
  // TODO: How to set sprite batch??
  var mapRenderer = new OrthogonalTiledMapRenderer(map, 1, batch)
 
  // LEFTOFF: How to incorporate layers
  override def render(dt:Float, ec:EntityCollection, e:Entity) = {
    map.getLayers().get(0) match {
      case l:TiledMapTileLayer => mapRenderer.renderTileLayer(l)
      case _ => ;
    }
  }
  
  // TODO: Implement all these
  
  // Disposable
  def dispose:Unit = {
    // TODO: How to dispose of textures??
  }
  
  // 
 
  
}