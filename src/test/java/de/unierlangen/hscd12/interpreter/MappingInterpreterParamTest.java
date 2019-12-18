package de.unierlangen.hscd12.interpreter;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappingInterpreterParamTest {

	@Test
	public void test() {
		Resource r0 = new Resource("r0");
		Resource r1 = new Resource("r1");
		Resource r2 = new Resource("r2");
		
		Task t0 = mock(Task.class);
		when(t0.getId()).thenReturn("t0");
		when(t0.getAttribute("mapping target")).thenReturn(r1);
		Task t1 = mock(Task.class);
		when(t1.getId()).thenReturn("t1");
		when(t1.getAttribute("mapping target")).thenReturn(r1);
		

		
		Mappings<Task, Resource> specMappings = new Mappings<>();
		Mapping<Task, Resource> m0 = new Mapping<Task, Resource>("m0", t0, r0);
		Mapping<Task, Resource> m1 = new Mapping<Task, Resource>("m1", t0, r1);
		Mapping<Task, Resource> m2 = new Mapping<Task, Resource>("m2", t0, r2);
		Mapping<Task, Resource> m3 = new Mapping<Task, Resource>("m3", t1, r1);
		Mapping<Task, Resource> m4 = new Mapping<Task, Resource>("m4", t1, r2);
		specMappings.add(m0);
		specMappings.add(m1);
		specMappings.add(m2);
		specMappings.add(m3);
		specMappings.add(m4);
		
		Application<Task, Dependency> implAppl = new Application<>();
		implAppl.addVertex(t0);
		implAppl.addVertex(t1);
		
		Architecture<Resource, Link> implArch = new Architecture<>();
		implArch.addVertex(r1);
		
		MappingInterpreterParam inter = new MappingInterpreterParam();
		Mappings<Task, Resource> implMappings = inter.interpretMappings(implAppl, specMappings, implArch);
		assertEquals(2, implMappings.size());
		assertEquals(1, implMappings.get(t0, r1).size());
		assertEquals(1, implMappings.get(t1, r1).size());
	}
}
