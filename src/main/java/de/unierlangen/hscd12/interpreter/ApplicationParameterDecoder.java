package de.unierlangen.hscd12.interpreter;

import java.util.Set;

import org.opt4j.satdecoding.Model;

import com.google.inject.Inject;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.parameter.ParameterReference;
import net.sf.opendse.model.parameter.ParameterSelect;
import net.sf.opendse.optimization.constraints.SpecificationConstraints;
import net.sf.opendse.optimization.encoding.variables.Variables;

/**
 * The {@link ApplicationParameterDecoder} decodes the parameters chosen for the
 * application.
 * 
 * @author Fedor Smirnov
 *
 */
public class ApplicationParameterDecoder {

	protected final Set<ParameterReference> activeVariables;

	@Inject
	public ApplicationParameterDecoder(SpecificationConstraints specConstraints) {
		this.activeVariables = specConstraints.getActiveParameters();
	}

	/**
	 * Decodes the parameters chosen for the given application and annotates the
	 * corresponding element attributes.
	 * 
	 * @param appl
	 *            the implementation application.
	 * @param model
	 *            the variable assignment
	 */
	public void decodeApplicationParameters(Application<Task, Dependency> appl, Model model) {
		for (ParameterReference paramRef : activeVariables) {
			String id = paramRef.getId();
			String attribute = paramRef.getAttribute();
			Task element = appl.getVertex(id);
			if (element != null) {
				ParameterSelect parameter = (ParameterSelect) element.getAttributeParameter(attribute);
				for (int i = 0; i < parameter.getElements().length; i++) {
					Object v = parameter.getElements()[i];
					Boolean b = model.get(Variables.var(element, attribute, v, i));
					if (b) {
						element.setAttribute(attribute, v);
					}
				}
			}
		}
	}
}
