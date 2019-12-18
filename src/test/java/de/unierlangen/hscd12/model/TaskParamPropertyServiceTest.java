package de.unierlangen.hscd12.model;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.parameter.ParameterSelect;

public class TaskParamPropertyServiceTest {

	@Test(expected = IllegalArgumentException.class)
	public void testCheck() {
		Communication comm = new Communication("comm");
		TaskParamPropertyService.checkTask(comm);
	}

	@Test
	public void testGetter() {
		Architecture<Resource, Link> arch = new Architecture<>();
		Resource res = new Resource("res");
		arch.addVertex(res);
		Task task = new Task("t");
		task.setAttribute(TaskParamPropertyService.TaskProperties.MAPPING_TARGET.xmlName, res);
		assertEquals(res, TaskParamPropertyService.getActiveResource(task));
	}

	@Test
	public void testSetter() {
		Resource res0 = new Resource("res0");
		Resource res1 = new Resource("res1");
		Set<Resource> targets = new LinkedHashSet<>();
		targets.add(res0);
		targets.add(res1);
		Task task = new Task("task");
		TaskParamPropertyService.setPossibleTargets(task, targets);
		ParameterSelect parameter = (ParameterSelect) task
				.getAttributeParameter(TaskParamPropertyService.TaskProperties.MAPPING_TARGET.xmlName);
		assertEquals(2, parameter.getElements().length);
	}
}
