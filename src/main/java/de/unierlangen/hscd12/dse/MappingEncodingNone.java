package de.unierlangen.hscd12.dse;

import java.util.HashSet;
import java.util.Set;

import org.opt4j.satdecoding.Constraint;

import net.sf.opendse.encoding.MappingEncoding;
import net.sf.opendse.encoding.variables.ApplicationVariable;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

/**
 * Encodes no mapping constraints.
 * 
 * @author Fedor Smirnov
 *
 */
public class MappingEncodingNone implements MappingEncoding {

	@Override
	public Set<Constraint> toConstraints(Mappings<Task, Resource> mappings,
			Set<ApplicationVariable> applicationVariables) {
		return new HashSet<>();
	}
}
