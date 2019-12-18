package de.unierlangen.hscd12.interpreter;

import net.sf.opendse.model.Application;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static net.sf.opendse.encoding.interpreter.InterpreterVariable.copy;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Takes the application as is, yet introduces the parent-child relations.
 * 
 * @author Fedor Smirnov
 *
 */
public class ApplicationInterpreterNone {

	/**
	 * Generates the implementation application.
	 * 
	 * @param specAppl
	 *            the specification application
	 * @return the implementation application
	 */
	public Application<Task, Dependency> generateImplAppl(Application<Task, Dependency> specAppl) {
		Application<Task, Dependency> result = new Application<>();
		// copy all vertices
		for (Task t : specAppl) {
			Task copy = copy(t);
			result.addVertex(copy);
		}
		// copy all edges
		for (Dependency dep : specAppl.getEdges()) {
			Task src = result.getVertex(specAppl.getSource(dep));
			Task dest = result.getVertex(specAppl.getDest(dep));
			result.addEdge(copy(dep), src, dest, EdgeType.DIRECTED);
		}
		return result;
	}
}
