package de.unierlangen.hscd12.interpreter;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

public class ApplicationInterpreterTest {

	@Test
	public void test() {
		Application<Task, Dependency> specAppl = new Application<>();
		Task t0 = new Task("t0");
		Task t1 = new Task("t1");
		Task t2 = new Task("t2");

		Communication comm0 = new Communication("c0");
		Communication comm1 = new Communication("c1");

		Dependency dep0 = new Dependency("dep0");
		Dependency dep1 = new Dependency("dep1");
		Dependency dep2 = new Dependency("dep2");
		Dependency dep3 = new Dependency("dep3");

		specAppl.addEdge(dep0, t0, comm0, EdgeType.DIRECTED);
		specAppl.addEdge(dep1, t1, comm1, EdgeType.DIRECTED);
		specAppl.addEdge(dep2, comm0, t2, EdgeType.DIRECTED);
		specAppl.addEdge(dep3, comm1, t2, EdgeType.DIRECTED);

		ApplicationInterpreterNone interpreter = new ApplicationInterpreterNone();
		Application<Task, Dependency> implAppl = interpreter.generateImplAppl(specAppl);
		assertEquals(5, implAppl.getVertexCount());
		assertEquals(4, implAppl.getEdgeCount());
		assertTrue(implAppl.getVertex("t0") != null);
		assertEquals(t0, implAppl.getVertex("t0").getParent());
	}
}
