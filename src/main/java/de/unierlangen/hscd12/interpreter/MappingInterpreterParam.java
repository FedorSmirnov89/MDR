package de.unierlangen.hscd12.interpreter;

import de.unierlangen.hscd12.model.TaskParamPropertyService;
import de.unierlangen.hscd12.preprocessing.MappingParameterAnnotation;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * The {@link MappingInterpreterParam} decodes the mappings chosen using select
 * parameters annotated by the {@link MappingParameterAnnotation}.
 * 
 * @author Fedor Smirnov
 *
 */
public class MappingInterpreterParam {

	/**
	 * Generates the implementation mappings based on the parameters chosen for the
	 * tasks.
	 * 
	 * @param implAppl
	 *            the implementation application
	 * @param specMappings
	 *            the specification mappings
	 * @param implArch
	 *            the implementation architecture
	 * @return the implementation mappings based on the parameters chosen for the
	 *         tasks
	 */
	public Mappings<Task, Resource> interpretMappings(Application<Task, Dependency> implAppl,
			Mappings<Task, Resource> specMappings, Architecture<Resource, Link> implArch) {
		Mappings<Task, Resource> result = new Mappings<>();
		for (Task t : implAppl) {
			if (TaskPropertyService.isProcess(t)) {
				Resource target = implArch.getVertex(TaskParamPropertyService.getActiveResource(t));
				Mapping<Task, Resource> specMapping = specMappings.get(t, target).iterator().next();
				Mapping<Task, Resource> implMapping = new Mapping<>(specMapping.getId(), t, target);
				implMapping.setParent(specMapping);
				result.add(implMapping);
			}
		}
		return result;
	}
}
