package de.unierlangen.hscd12.preprocessing;

import java.util.Set;

import com.google.inject.Inject;

import de.unierlangen.hscd12.model.TaskParamPropertyService;
import net.sf.opendse.encoding.preprocessing.SpecificationPreprocessorComposable;
import net.sf.opendse.encoding.preprocessing.SpecificationPreprocessorMulti;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * The {@link MappingParameterAnnotation} annotates the mapping alternatives of
 * each task as a set of exclusive parameters.
 * 
 * @author Fedor Smirnov
 *
 */
public class MappingParameterAnnotation extends SpecificationPreprocessorComposable{

	@Inject
	public MappingParameterAnnotation(SpecificationPreprocessorMulti multiPreprocessor) {
		multiPreprocessor.addPreprocessor(this);
	}
	
	@Override
	public void preprocessSpecification(Specification userSpecification) {
		Application<Task, Dependency> appl = userSpecification.getApplication();
		Mappings<Task, Resource> mappings = userSpecification.getMappings();
		// iterate all tasks
		for (Task t : appl) {
			if (TaskPropertyService.isProcess(t)) {
				Set<Resource> targets = mappings.getTargets(t);
				TaskParamPropertyService.setPossibleTargets(t, targets);
			}
		}
	}
}
