package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.core._
import Jena._
import JenaRDFReader._

class JenaAsyncGraphStoreTest() extends AsyncGraphStoreTest[Jena, JenaSPARQL](JenaStore(DatasetGraphFactory.createMem()))
