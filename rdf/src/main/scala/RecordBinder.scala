package org.w3.banana

import org.w3.banana.util.BananaValidation
import scalaz.{ Validation, Success, Failure }

object RecordBinder {

  private def po[Rdf <: RDF, T](t: T, property: Property[Rdf, T]): (Rdf#URI, PointedGraph[Rdf]) = {
    import property.{ uri, binder }
    (uri, binder.toPointedGraph(t))
  }

  private def make[Rdf <: RDF](pos: (Rdf#URI, PointedGraph[Rdf])*)(implicit ops: RDFOperations[Rdf]): PointedGraph[Rdf] = {
    val subject = ops.makeUri("#" + java.util.UUID.randomUUID().toString)
    var triples: Set[Rdf#Triple] = Set.empty
    for (po <- pos.toIterable) {
      val (p, pg) = po
      triples += ops.makeTriple(subject, p, pg.pointer)
      triples ++= ops.graphToIterable(pg.graph)
    }
    PointedGraph(subject, ops.makeGraph(triples))
  }

}

/**
 * helper functions for binding Scala records (typically case classes)
 *
 * here is the recipe, given one type T:
 * - start by declaring the various elements that make T
 *   this is done through [PGBElem]s, and RecordBinder provide some helpers (see constant, property and uriTemplate)
 * - say how to combine the elements with a contructor (apply-like function) and an extractor (unapply-like function)
 *   there is done with the pgb helper function
 */
trait RecordBinder[Rdf <: RDF] {
  self: Diesel[Rdf] =>

  import RecordBinder._
  import ops._

  def classUrisFor[T](uri: Rdf#URI, uris: Rdf#URI*): ClassUrisFor[Rdf, T] = new ClassUrisFor[Rdf, T] {
    val classes = uri :: uris.toList
  }

  /**
   * binds a type T to one unique URI
   *
   * consT is typically a singleton object and T is its singleton type
   */
  def constant[T](constT: T, constUri: Rdf#URI): PointedGraphBinder[Rdf, T] = {

    val uriBinder = new URIBinder[Rdf, T] {
      def fromUri(uri: Rdf#URI): Validation[BananaException, T] =
        if (constUri == uri)
          Success(constT)
        else
          Failure(WrongExpectation(constUri + " does not equal " + uri))

      def toUri(t: T): Rdf#URI = constUri
    }

    NodeToPointedGraphBinder(UriToNodeBinder(uriBinder))

  }

  /**
   * declares a Property/Object element where T is in the object position
   */
  def property[T](predicate: Rdf#URI)(implicit objectBinder: PointedGraphBinder[Rdf, T]): Property[Rdf, T] = new Property[Rdf, T] {
    val uri = predicate
    val binder = objectBinder
    def extract(pointed: PointedGraph[Rdf]): BananaValidation[T] =
      (pointed / predicate).as[T](binder)
  }

  def newUri(prefix: String): Rdf#URI = uri(prefix + java.util.UUID.randomUUID().toString)

  /**
   * combine PointedGraphBinder elements and apply/unapply functions to build binders
   *
   * TODO
   * - provide other apply methods with different arity
   * - use shapeless to generalize
   */

  def pgb[T] = new PGB[T]

  class PGB[T] {

    def apply[T1](p1: Property[Rdf, T1])(apply: (T1) => T, unapply: T => Option[T1]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some(t1) = unapply(t)
        make(po(t1, p1))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        for (t1 <- v1) yield apply(t1)
      }

    }

    def apply[T1, T2](p1: Property[Rdf, T1], p2: Property[Rdf, T2])(apply: (T1, T2) => T, unapply: T => Option[(T1, T2)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2)) = unapply(t)
        make(po(t1, p1), po(t2, p2))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        for (t1 <- v1; t2 <- v2) yield apply(t1, t2)
      }

    }

    def apply[T1, T2, T3](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3])(apply: (T1, T2, T3) => T, unapply: T => Option[(T1, T2, T3)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3) yield apply(t1, t2, t3)
      }

    }

    def apply[T1, T2, T3, T4](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4])(apply: (T1, T2, T3, T4) => T, unapply: T => Option[(T1, T2, T3, T4)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4) yield apply(t1, t2, t3, t4)
      }

    }

    def apply[T1, T2, T3, T4, T5](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5])(apply: (T1, T2, T3, T4, T5) => T, unapply: T => Option[(T1, T2, T3, T4, T5)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5) yield apply(t1, t2, t3, t4, t5)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6])(apply: (T1, T2, T3, T4, T5, T6) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5), po(t6, p6))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6) yield apply(t1, t2, t3, t4, t5, t6)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7])(apply: (T1, T2, T3, T4, T5, T6, T7) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5), po(t6, p6), po(t7, p7))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7) yield apply(t1, t2, t3, t4, t5, t6, t7)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7], p8: Property[Rdf, T8])(apply: (T1, T2, T3, T4, T5, T6, T7, T8) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7, T8)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7, t8)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5), po(t6, p6), po(t7, p7), po(t8, p8))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        def v8 = p8.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7; t8 <- v8) yield apply(t1, t2, t3, t4, t5, t6, t7, t8)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7], p8: Property[Rdf, T8], p9: Property[Rdf, T9])(apply: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7, T8, T9)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7, t8, t9)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5), po(t6, p6), po(t7, p7), po(t8, p8), po(t9, p9))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        def v8 = p8.extract(pointed)
        def v9 = p9.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7; t8 <- v8; t9 <- v9) yield apply(t1, t2, t3, t4, t5, t6, t7, t8, t9)
      }

    }

    def apply[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](p1: Property[Rdf, T1], p2: Property[Rdf, T2], p3: Property[Rdf, T3], p4: Property[Rdf, T4], p5: Property[Rdf, T5], p6: Property[Rdf, T6], p7: Property[Rdf, T7], p8: Property[Rdf, T8], p9: Property[Rdf, T9], p10: Property[Rdf, T10])(apply: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => T, unapply: T => Option[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)]): PointedGraphBinder[Rdf, T] = new PointedGraphBinder[Rdf, T] {

      def toPointedGraph(t: T): PointedGraph[Rdf] = {
        val Some((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)) = unapply(t)
        make(po(t1, p1), po(t2, p2), po(t3, p3), po(t4, p4), po(t5, p5), po(t6, p6), po(t7, p7), po(t8, p8), po(t9, p9), po(t10, p10))
      }

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] = {
        def v1 = p1.extract(pointed)
        def v2 = p2.extract(pointed)
        def v3 = p3.extract(pointed)
        def v4 = p4.extract(pointed)
        def v5 = p5.extract(pointed)
        def v6 = p6.extract(pointed)
        def v7 = p7.extract(pointed)
        def v8 = p8.extract(pointed)
        def v9 = p9.extract(pointed)
        def v10 = p10.extract(pointed)
        for (t1 <- v1; t2 <- v2; t3 <- v3; t4 <- v4; t5 <- v5; t6 <- v6; t7 <- v7; t8 <- v8; t9 <- v9; t10 <- v10) yield apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
      }

    }

  }

}
