package de.unierlangen.hscd12.dse;

import java.util.HashSet;
import java.util.Set;

import org.opt4j.satdecoding.Constraint;

import net.sf.opendse.encoding.ApplicationEncoding;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * 
 * 
 * @author Encodes no constraints.
 *
 */
public class ApplicationEncodingNone implements ApplicationEncoding {

	@Override
	public Set<Constraint> toConstraints(Application<Task, Dependency> application) {
		return new HashSet<>();
	}
}
