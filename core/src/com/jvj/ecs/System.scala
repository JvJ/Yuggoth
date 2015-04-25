package com.jvj.ecs

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import reflect.runtime.universe._


/* Systems can be applied to whole collections or
 * individual entities.
 * */
abstract class System {
  
  /* Apply the system to an entire entity collection.
   * */
  def apply (ec:EntityCollection):EntityCollection = ec.foldLeft(ec)(apply)
    
  /* Apply the system to a single entity in this collection.
   * */
  def apply(ec:EntityCollection, e:Entity):EntityCollection
  
}

object System{
  
  def compFunc(fs:(EntityCollection, Entity)=>Any*):((EntityCollection, Entity)=>EntityCollection) = macro implCompFunc

  def implCompFunc
    (c:Context)
    (fs:c.Expr[(EntityCollection, Entity)=>Any]*):c.Expr[(EntityCollection, Entity)=>EntityCollection] = {
    
    import c.universe._
    
    val (outerEc, outerE) = (Ident("__outerEc"), Ident("__outerE"))
    
    val numFuncs = fs.length
    
    // Type-checking the arguments!
    // All return types should be functions of components to EntityCollection
    // TODO: Is typecheck deprecated?
    
    val (uncheckedFuncs, checkedFuncs, outerParamDefs) = fs map {
      f=>
        f.tree match {
          case oFunc:Function =>{
            (oFunc.body, c.typecheck(oFunc.body)) match {
              case (func:Function,
                  tFunc:Function) =>{
                    
                    // The type needs to be a function of component params to entity collection
                    val badArgs = tFunc.vparams filter {
                      vdef =>
                        !(vdef.tpt.tpe <:< typeOf[Component])
                    }
              
                    if (badArgs.length > 0){
                      throw new Exception(s"All parameters must be components.  Received bad params: ${badArgs}")
                    }
                    
                    if (!(tFunc.tpe.typeArgs.last <:< typeOf[EntityCollection])){
                      throw new Exception(s"Return type must be EntityCollection.  Received: ${func.tpe.typeArgs.last}")
                    }
                    
                    // We need to isolate the component and entity param names, as well
                    (func, tFunc, oFunc.vparams)
                    
                  }
            case t => throw new Exception(s"Require function of component params to EntityCollection.  Received:${t}.")
            }
          }
          case t => throw new Exception(s"Require function of (EntityCollection,Entity)=>(Component...)=>EntityCollection.  Received:${t}")
        }
    } unzip3
    
    // TypeApply!
    val matchSelector =
      pq"""
      ((${outerEc}, ${outerE}),
      ${Apply(q"List",
              checkedFuncs.map({
                f => Apply(q"List",
                f.vparams map {
                  p => TypeApply(outerE, List(p.tpt))
                })}).toList)})"""
    
    // TODO: In addition to the list, we need to pattern match on the entiy and entityCollection param
    // names provided
    val caseDefs = uncheckedFuncs.zip(outerParamDefs).zipWithIndex.map({
      case ((f, List(ec, e)),i) =>
        CaseDef(
            // This matches a list of lists, where only our relevant position in 
            // the list is given a pattern
            // LEFTOFF: WHAT???
            pq"""
            ((${Bind(ec.name, pq"_")}, ${Bind(e.name, pq"_")}),
            ${Apply(q"List",
              Stream.continually(pq"_").take(i).toList ++
              List(Apply(q"List",
                  f.vparams.map({
                    p => pq"Some(${Bind(p.name, pq"_")})"
                    }))) ++
              Stream.continually(pq"_").take(numFuncs - i - 1).toList)
            })""",
            q"",// Guard
            f.body)
      case ((_, l),i) => throw new Exception(s"Got incorrect parameter defs: $l")
    }) ++ List(CaseDef(pq"_", q"", q"{$outerEc}"))
    
    
    val ret = c.Expr[(EntityCollection, Entity)=>EntityCollection](
        q"""(${outerEc}:EntityCollection, ${outerE}:Entity)=>{
            ${Match(matchSelector, caseDefs.toList)}
             }""")
             
    println(ret)
    println(showRaw(ret.tree.children(2).children(1)))
    ret
  }
}