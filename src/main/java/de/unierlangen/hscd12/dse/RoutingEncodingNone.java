package de.unierlangen.hscd12.dse;

import java.util.HashSet;
import java.util.Set;

import org.opt4j.satdecoding.Constraint;

import net.sf.opendse.encoding.RoutingEncoding;
import net.sf.opendse.encoding.variables.ApplicationVariable;
import net.sf.opendse.encoding.variables.MappingVariable;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Routings;
import net.sf.opendse.model.Task;

/**
 * Encodes no routing constraints.
 * 
 * @author Fedor Smirnov
 *
 */
public class RoutingEncodingNone implements RoutingEncoding {

	@Override
	public Set<Constraint> toConstraints(Set<ApplicationVariable> applicationVariables,
			Set<MappingVariable> mappingVariables, Routings<Task, Resource, Link> routings) {
		return new HashSet<>();
	}
}
