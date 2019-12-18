package de.unierlangen.hscd12.dse;

import java.util.HashSet;
import java.util.Set;

import org.opt4j.satdecoding.Constraint;

import encoding.isolation_schemes.AllocationEncodingTileUtilization;
import net.sf.opendse.encoding.variables.MappingVariable;
import net.sf.opendse.encoding.variables.RoutingVariable;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;

/**
 * Encodes no allocation constraints.
 * 
 * @author Fedor Smirnov
 *
 */
public class AllocationEncodingNone extends AllocationEncodingTileUtilization {

	public AllocationEncodingNone() {
		super(false);
	}

	@Override
	public Set<Constraint> toConstraints(Set<MappingVariable> mappingVariables, Set<RoutingVariable> routingVariables,
			Architecture<Resource, Link> architecture) {
		return new HashSet<>();
	}

}
