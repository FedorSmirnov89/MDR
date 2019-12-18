package de.unierlangen.hscd12.interpreter;

import org.opt4j.satdecoding.Model;

import net.sf.opendse.model.Specification;
import net.sf.opendse.optimization.encoding.Interpreter;

/**
 * The {@link InterpreterParameter} interprets the mapping choices that were
 * made for each of the tasks by decoding them into an implementation and
 * repairing the implementation if necessary.
 * 
 * @author Fedor Smirnov
 *
 */
public class InterpreterParameter implements Interpreter {

	@Override
	public Specification toImplementation(Specification specification, Model model) {
		// nothing to do here, just return the specification
		return specification;
	}
}
