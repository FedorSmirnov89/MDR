package de.unierlangen.hscd12.preprocessing;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.encoding.preprocessing.SpecificationPreprocessorMulti;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.parameter.ParameterSelect;
import static org.mockito.Mockito.mock;

public class MappingParameterAnnotationTest {

	@Test
	public void test() {
		Application<Task, Dependency> appl = new Application<>();
		Task t0 = new Task("task0");
		Task t1 = new Task("task1");
		Communication comm = new Communication("comm");
		Dependency dep0 = new Dependency("dep0");
		Dependency dep1 = new Dependency("dep1");
		appl.addEdge(dep0, t0, comm, EdgeType.DIRECTED);
		appl.addEdge(dep1, comm, t1, EdgeType.DIRECTED);

		Architecture<Resource, Link> arch = new Architecture<>();
		Resource res0 = new Resource("res0");
		Resource res1 = new Resource("res1");
		Resource res2 = new Resource("res2");
		Link l0 = new Link("l0");
		Link l1 = new Link("l1");
		arch.addEdge(l0, res0, res1, EdgeType.UNDIRECTED);
		arch.addEdge(l1, res1, res2, EdgeType.UNDIRECTED);

		Mappings<Task, Resource> mappings = new Mappings<>();
		Mapping<Task, Resource> m0 = new Mapping<Task, Resource>("m0", t0, res0);
		Mapping<Task, Resource> m1 = new Mapping<Task, Resource>("m1", t0, res1);
		Mapping<Task, Resource> m2 = new Mapping<Task, Resource>("m2", t0, res2);
		Mapping<Task, Resource> m3 = new Mapping<Task, Resource>("m3", t1, res1);
		Mapping<Task, Resource> m4 = new Mapping<Task, Resource>("m4", t1, res2);
		mappings.add(m0);
		mappings.add(m1);
		mappings.add(m2);
		mappings.add(m3);
		mappings.add(m4);

		Specification spec = new Specification(appl, arch, mappings);
		SpecificationPreprocessorMulti multiPreproc = mock(SpecificationPreprocessorMulti.class);
		MappingParameterAnnotation parameterAnnotation = new MappingParameterAnnotation(multiPreproc);
		parameterAnnotation.preprocessSpecification(spec);

		Set<Resource> expected0 = new LinkedHashSet<>();
		expected0.add(res0);
		expected0.add(res1);
		expected0.add(res2);

		Set<Resource> expected1 = new LinkedHashSet<>();
		expected1.add(res1);
		expected1.add(res2);

		ParameterSelect parSel0 = (ParameterSelect) t0.getAttributeParameter("mapping target");
		assertEquals(3, parSel0.getElements().length);
		ParameterSelect parSel1 = (ParameterSelect) t1.getAttributeParameter("mapping target");
		assertEquals(2, parSel1.getElements().length);
	}

}
