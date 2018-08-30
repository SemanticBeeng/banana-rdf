package org.w3.banana.io

import org.w3.banana.{RDF, RDFOps}
import scalaz._


abstract class TriGTestSuite[Rdf <: RDF, M[+_] : Monad : Comonad](implicit
                                                                  ops: RDFOps[Rdf],
                                                                  reader: RDFReader[Rdf, M, TriG],
                                                                  val writer: RDFWriter[Rdf, M, TriG]
) extends SerialisationTestSuite[Rdf, M, TriG, TriG]("TriG", "trig") {

  val referenceGraphSerialisedForSyntax = """
<https://www.w3.org/TR/trig/> <http://purl.org/dc/elements/1.1/creator> "Chris Bizer", "Richard Cyganiak" ;
                                      <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
  """

}
