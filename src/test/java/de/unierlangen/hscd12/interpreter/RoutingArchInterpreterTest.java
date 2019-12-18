package de.unierlangen.hscd12.interpreter;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Routings;
import net.sf.opendse.model.Task;
import properties.LinkInvasicPropertyService;
import properties.LinkInvasicPropertyService.LinkTypes;
import properties.ResourceInvasicPropertyService;
import properties.RouterPropertyService;
import properties.ResourceInvasicPropertyService.ResourceTypes;

public class RoutingArchInterpreterTest {

	@Test
	public void test() {

		Task t0 = new Task("t0");
		Task t1 = new Task("t1");
		Communication comm = new Communication("comm");

		Application<Task, Dependency> implAppl = new Application<>();
		Dependency dep0 = new Dependency("d0");
		Dependency dep1 = new Dependency("d1");
		implAppl.addEdge(dep0, t0, comm, EdgeType.DIRECTED);
		implAppl.addEdge(dep1, comm, t1, EdgeType.DIRECTED);

		Architecture<Resource, Link> specArch = new Architecture<>();
		Resource rs = new Resource("rs");
		ResourceInvasicPropertyService.setType(rs, ResourceTypes.Processor);
		ResourceInvasicPropertyService.setRouterId(rs, "r0");
		Resource re = new Resource("re");
		ResourceInvasicPropertyService.setType(re, ResourceTypes.Processor);
		ResourceInvasicPropertyService.setRouterId(re, "r5");
		Link ls = new Link("ls");
		LinkInvasicPropertyService.setLinkType(ls, LinkTypes.BusLink);
		Link le = new Link("le");
		LinkInvasicPropertyService.setLinkType(le, LinkTypes.BusLink);
		Resource res0 = makeRes(0, 0, 2);
		Resource res1 = makeRes(1, 1, 2);
		Resource res2 = makeRes(2, 0, 1);
		Resource res3 = makeRes(3, 1, 1);
		Resource res4 = makeRes(4, 0, 0);
		Resource res5 = makeRes(5, 1, 0);
		t0.setAttribute("mapping target", rs);
		t1.setAttribute("mapping target", re);
		
		Link l0 = new Link("l0");
		LinkInvasicPropertyService.setLinkType(l0, LinkTypes.NocLink);
		Link l1 = new Link("l1");
		LinkInvasicPropertyService.setLinkType(l1, LinkTypes.NocLink);
		Link l2 = new Link("l2");
		LinkInvasicPropertyService.setLinkType(l2, LinkTypes.NocLink);
		Link l3 = new Link("l3");
		LinkInvasicPropertyService.setLinkType(l3, LinkTypes.NocLink);
		Link l4 = new Link("l4");
		LinkInvasicPropertyService.setLinkType(l4, LinkTypes.NocLink);
		Link l5 = new Link("l5");
		LinkInvasicPropertyService.setLinkType(l5, LinkTypes.NocLink);
		Link l6 = new Link("l6");
		LinkInvasicPropertyService.setLinkType(l6, LinkTypes.NocLink);
		specArch.addEdge(l0, res0, res2, EdgeType.UNDIRECTED);
		specArch.addEdge(l1, res2, res4, EdgeType.UNDIRECTED);
		specArch.addEdge(l2, res0, res1, EdgeType.UNDIRECTED);
		specArch.addEdge(l3, res2, res3, EdgeType.UNDIRECTED);
		specArch.addEdge(l4, res4, res5, EdgeType.UNDIRECTED);
		specArch.addEdge(l5, res1, res3, EdgeType.UNDIRECTED);
		specArch.addEdge(l6, res3, res5, EdgeType.UNDIRECTED);
		specArch.addEdge(ls, rs, res0, EdgeType.UNDIRECTED);
		specArch.addEdge(le, re, res5, EdgeType.UNDIRECTED);

		Architecture<Resource, Link> implArch = new Architecture<>();
		Routings<Task, Resource, Link> implRoutings = new Routings<>();
		RoutingArchInterpreter interpreter = new RoutingArchInterpreter();
		interpreter.interpretArchitectureRouting(implArch, implRoutings, implAppl, specArch);
		assertEquals(6, implArch.getVertexCount());
		assertEquals(5, implArch.getEdgeCount());
		assertTrue(implArch.getVertex("r0") != null);
		assertTrue(implArch.getVertex("rs") != null);
		assertTrue(implArch.getVertex("re") != null);
		assertTrue(implArch.getVertex("r1") != null);
		assertTrue(implArch.getVertex("r3") != null);
		assertTrue(implArch.getVertex("r5") != null);
		assertTrue(implArch.getEdge("l2") != null);
		assertTrue(implArch.getEdge("l5") != null);
		assertTrue(implArch.getEdge("l6") != null);
		assertTrue(implArch.getEdge("ls") != null);
		assertTrue(implArch.getEdge("le") != null);

		assertEquals(6, implRoutings.get(comm).getVertexCount());
		assertEquals(5, implRoutings.get(comm).getEdgeCount());
		assertTrue(implRoutings.get(comm).getVertex("r0") != null);
		assertTrue(implRoutings.get(comm).getVertex("re") != null);
		assertTrue(implRoutings.get(comm).getVertex("rs") != null);
		assertTrue(implRoutings.get(comm).getVertex("r1") != null);
		assertTrue(implRoutings.get(comm).getVertex("r3") != null);
		assertTrue(implRoutings.get(comm).getVertex("r5") != null);
		assertTrue(implRoutings.get(comm).getEdge("l2") != null);
		assertTrue(implRoutings.get(comm).getEdge("l5") != null);
		assertTrue(implRoutings.get(comm).getEdge("l6") != null);
		assertTrue(implRoutings.get(comm).getEdge("ls") != null);
		assertTrue(implRoutings.get(comm).getEdge("le") != null);
	}

	/**
	 * Makes a resource for the test.
	 * 
	 * @param id
	 *            the resource id
	 * @param xPos
	 *            the x pos of the resource
	 * @param yPos
	 *            the y pos of the resource
	 * @return the resource
	 */
	protected static Resource makeRes(int id, int xPos, int yPos) {
		String idString = "r" + id;
		Resource result = new Resource(idString);
		ResourceInvasicPropertyService.setType(result, ResourceTypes.Router);
		ResourceInvasicPropertyService.setRouterId(result, idString);
		RouterPropertyService.setXposition(result, xPos);
		RouterPropertyService.setYposition(result, yPos);
		return result;
	}

}
