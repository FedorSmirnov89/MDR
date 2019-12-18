package de.unierlangen.hscd12.model;

import java.util.Set;

import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.parameter.Parameters;
import net.sf.opendse.model.properties.AbstractPropertyService;
import net.sf.opendse.model.properties.TaskPropertyService;

public class TaskParamPropertyService extends AbstractPropertyService {

	public enum TaskProperties {
		MAPPING_TARGET("mapping target");
		protected String xmlName;

		private TaskProperties(String xmlName) {
			this.xmlName = xmlName;
		}
	}

	/**
	 * Checks whether the given task is a process. Throws an
	 * {@link IllegalArgumentException} otherwise.
	 * 
	 * @param task
	 *            the given task.
	 */
	protected static void checkTask(Task task) {
		if (!TaskPropertyService.isProcess(task)) {
			throw new IllegalArgumentException("this property service should be only used with processes");
		}
	}

	/**
	 * Returns the active resource of the given task. Used AFTER the parametric
	 * choice.
	 * 
	 * @param arch
	 *            the architecture
	 * @param task
	 *            the given task
	 * @return the active resource of the given task
	 */
	public static Resource getActiveResource(Task task) {
		checkTask(task);
		String attrName = TaskProperties.MAPPING_TARGET.xmlName;
		checkAttribute(task, attrName);
		return task.getAttribute(attrName);
	}

	/**
	 * Sets the parameters describing the possible targets of the given task.
	 * 
	 * @param task
	 *            the given task
	 * @param possibleTargets
	 *            the possible targets
	 */
	public static void setPossibleTargets(Task task, Set<Resource> possibleTargets) {
		checkTask(task);
		String attrName = TaskProperties.MAPPING_TARGET.xmlName;
		task.setAttribute(attrName, Parameters.select(possibleTargets.iterator().next(), possibleTargets.toArray()));
	}

	/**
	 * Sets the resource chosen for the given task (Used during the repair).
	 * 
	 * @param task
	 *            the given task
	 * @param res
	 *            the given resource
	 */
	public static void setChosenTarget(Task task, Resource res) {
		checkTask(task);
		String attrName = TaskProperties.MAPPING_TARGET.xmlName;
		task.setAttribute(attrName, res);
	}

}
