package de.unierlangen.hscd12.evaluator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opt4j.core.Objectives;

import de.unierlangen.hscd12.interpreter.ApplicationInterpreterNone;
import de.unierlangen.hscd12.interpreter.MappingInterpreterParam;
import de.unierlangen.hscd12.interpreter.RoutingArchInterpreter;
import de.unierlangen.hscd12.model.TaskParamPropertyService;
import evaluators.concrete.SlotReservationChecker;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Routings;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;
import net.sf.opendse.optimization.ImplementationEvaluator;
import properties.CommunicationInvasicPropertyService;
import properties.LinkInvasicPropertyService;
import properties.MappingInvasicPropertyService;
import properties.NocLinkInvasicPropertyService;
import properties.ProcessInvasicPropertyService;
import properties.ProcessorInvasicPropertyService;
import properties.ResourceInvasicPropertyService;
import properties.TaskInvasicPropertyService;
import utilities.ArchitectureUtilities;

/**
 * The {@link MinimumDistanceRepairEvaluator} is assumed to get an
 * implementation where the only design choices are the setting of the mapping
 * parameters. Does all the decoding steps, checks the feasibility w.r.t. the
 * allocated processor slots, and repairs the implementation by remapping tasks.
 * 
 * @author Fedor Smirnov
 *
 */
public class MinimumDistanceRepairEvaluator implements ImplementationEvaluator {

	protected static final String capViolationAttr = "capacity violation";

	protected final ApplicationInterpreterNone applInterpreter = new ApplicationInterpreterNone();
	protected final RoutingArchInterpreter routingArchInterpreter = new RoutingArchInterpreter();
	protected final MappingInterpreterParam mappingInterpreter = new MappingInterpreterParam();

	protected final List<Resource> procList = new ArrayList<>();
	protected boolean listInit = false;

	@Override
	public Specification evaluate(Specification specification, Objectives objectives) {
		// init the procList
		if (!listInit) {
			listInit = true;
			for (Resource res : specification.getArchitecture()) {
				if (ResourceInvasicPropertyService.isProcessor(res)) {
					procList.add(res);
				}
			}
		}
		// build the impl according to the params
		Specification impl = buildImpl(specification);
		// annotate the impl
		annotateImplementation(impl, specification);
		boolean feasible = checkImplementationFeasibility(impl);
		while (!feasible) {
			repairMapping(impl, specification);
			impl = buildImpl(specification);
			annotateImplementation(impl, specification);
			feasible = checkImplementationFeasibility(impl);
		}
		return impl;
	}

	/**
	 * Repairs the implementation by reassigning one task from each violating core
	 * to a non violating core.
	 * 
	 * TODO shift this from random to sth list-based
	 * 
	 * @param impl
	 *            the infeasible implementation with annotated violations
	 * @param spec
	 *            the specification
	 */
	protected void repairMapping(Specification impl, Specification spec) {
		// gather the violating resources
		Set<Resource> violations = new HashSet<>();
		for (Resource res : impl.getArchitecture()) {
			if (res.getAttribute(capViolationAttr) != null) {
				violations.add(res);
			}
		}
		for (Resource violation : violations) {
			Task toMove = impl.getMappings().get(violation).iterator().next().getSource();
			Resource newTarget = null;
			int startPos = procList.indexOf(violation);
			for (int offset = 1; offset <= procList.size(); offset++) {
				Resource res = procList.get((startPos + offset) % procList.size());
				if (!violations.contains(res)) {
					newTarget = res;
					break;
				}
			}
			Task specTask = spec.getApplication().getVertex(toMove);
			Resource specRes = spec.getArchitecture().getVertex(newTarget);
			TaskParamPropertyService.setChosenTarget(specTask, specRes);
		}
	}

	/**
	 * Returns {@code true} if the implementation is feasible w.r.t. processor and
	 * link capacity.
	 * 
	 * @param impl
	 *            the implementation
	 * @return {@code true} if the implementation is feasible w.r.t. processor and
	 *         link capacity
	 */
	protected boolean checkImplementationFeasibility(Specification impl) {
		return calculateProcessorSlots(impl);
	}

	/**
	 * Returns {@code true} iff the given implementation is feasible w.r.t. the
	 * required processor slots, i.e. if the provided implementation provides the
	 * necessary processor slots for each of the tasks.
	 * 
	 * @param implementation
	 *            the provided implementation
	 * @return {@code true} iff the given implementation is feasible w.r.t. the
	 *         required processor slots
	 */
	protected boolean calculateProcessorSlots(Specification implementation) {
		Mappings<Task, Resource> bindings = implementation.getMappings();
		boolean feasible = true;
		for (Mapping<Task, Resource> mapping : bindings) {
			Task task = mapping.getSource();
			Resource res = mapping.getTarget();
			int taskSlots = ProcessInvasicPropertyService.getNumSIslots(task);
			if (TaskPropertyService.isProcess(task)) {
				int availSlots = ProcessorInvasicPropertyService.getMaximumNumOfSlots(res)
						- ProcessorInvasicPropertyService.getNumReservedSISlots(res);
				if (availSlots < taskSlots) {
					feasible = false;
					res.setAttribute(capViolationAttr, "true");
				} else {
					ProcessorInvasicPropertyService.addReservedSISlots(res, taskSlots);
				}
			}
		}
		return feasible;
	}

	/**
	 * Creates an implementation based on the mapping decision implicit to the
	 * parameter choices in the spec
	 * 
	 * @param spec
	 *            the spec (only parameter choices made)
	 * @return an implementation based on the mapping decision implicit to the
	 *         parameter choices in the spec
	 */
	protected Specification buildImpl(Specification spec) {
		Application<Task, Dependency> implAppl = applInterpreter.generateImplAppl(spec.getApplication());
		Architecture<Resource, Link> implArch = new Architecture<>();
		Routings<Task, Resource, Link> implRoutings = new Routings<>();
		routingArchInterpreter.interpretArchitectureRouting(implArch, implRoutings, implAppl, spec.getArchitecture());
		Mappings<Task, Resource> implMappings = mappingInterpreter.interpretMappings(implAppl, spec.getMappings(),
				implArch);
		return new Specification(implAppl, implArch, implMappings, implRoutings);
	}

	@Override
	public int getPriority() {
		// very first thing to happen
		return -1;
	}

	/**
	 * Annotates the decoded implementation.
	 * 
	 * @param impl
	 *            the decoded implementation
	 */
	protected void annotateImplementation(Specification impl, Specification spec) {
		assignPrimaryAttributesMeshNoC(impl);
		setActiveTimesMeshNoC(impl, spec);
		calculateTaskSlotsMeshNoc(impl);
		setLoadMeshNoC(impl);
		calculateMessageSlotsMeshNoc(impl);
	}

	/**
	 * Calculates and annotates the number of slots required by the messages in the
	 * application of the provided implementation.
	 * 
	 * @param implementation
	 *            the provided implementation
	 */
	protected void calculateMessageSlotsMeshNoc(Specification implementation) {
		for (Task msg : implementation.getApplication().getVertices()) {
			if (TaskPropertyService.isCommunication(msg)) {
				int sl = 0;
				double payLoad = CommunicationInvasicPropertyService.getPayload(msg);
				double period = TaskInvasicPropertyService.getPeriod(msg);
				Architecture<Resource, Link> route = implementation.getRoutings().get(msg);
				for (Link link : route.getEdges()) {
					if (LinkInvasicPropertyService.isNocLink(link)) {
						double bandwidth = NocLinkInvasicPropertyService.getBandWidth(link);
						int maxSL = NocLinkInvasicPropertyService.getMaxServiceLevel(link);
						int link_sl = SlotReservationChecker.payload2SL(payLoad, period, bandwidth, maxSL);
						sl = Math.max(sl, link_sl);
					}
				}
				CommunicationInvasicPropertyService.setServiceLevel(msg, sl);
			}
		}
	}

	/**
	 * Sets the load of each of the processes in the provided implementation.
	 * 
	 * @param implementation
	 *            the provided implementation
	 */
	protected void setLoadMeshNoC(Specification implementation) {
		Mappings<Task, Resource> bindings = implementation.getMappings();
		for (Mapping<Task, Resource> mapping : bindings) {
			Task task = mapping.getSource();
			Resource res = mapping.getTarget();
			if (TaskPropertyService.isProcess(task)) {
				double si_os = ProcessorInvasicPropertyService.getOsSchedulingInterval(res);
				double si = ProcessorInvasicPropertyService.getSchedulingInterval(res);
				double period = TaskInvasicPropertyService.getPeriod(task);
				double activeTime = ProcessInvasicPropertyService.getActiveTime(task);
				double load = SlotReservationChecker.calculateLoad(activeTime, period, si, si_os);
				ProcessInvasicPropertyService.setLoad(task, load);
			}
		}
	}

	/**
	 * Sets the active time of the processes in the provided implementation.
	 * 
	 * @param implementation
	 *            the provided implementation
	 * @param specification
	 *            the provided specification
	 */
	protected void setActiveTimesMeshNoC(Specification implementation, Specification specification) {
		Mappings<Task, Resource> bindings = implementation.getMappings();
		Architecture<Resource, Link> spec_architecture = specification.getArchitecture();
		HashSet<Resource> coresWithTasks = new HashSet<>();
		for (Mapping<Task, Resource> mapping : bindings) {
			coresWithTasks.add(mapping.getTarget());
		}
		for (Mapping<Task, Resource> mapping : bindings) {
			Task task = mapping.getSource();
			Resource res = mapping.getTarget();
			if (TaskPropertyService.isProcess(task)) {
				double activeTime = 0;
				// (1) WCET
				activeTime += ProcessInvasicPropertyService.getWCET(task);
				// (2) read input messages
				Resource memory = ArchitectureUtilities.getMemory(res, spec_architecture);
				int tileActiveProcessorsCount = ArchitectureUtilities.getNumActiveProcessorsOnTile(res,
						spec_architecture, implementation.getArchitecture(), coresWithTasks);
				double clockCycleDuration = ProcessorInvasicPropertyService.getClockCycleDuration(res);
				for (Task msg : implementation.getApplication().getPredecessors(task)) {
					if (msg instanceof Communication) {
						double payLoadBitCount = CommunicationInvasicPropertyService.getPayload(msg);
						int memAccessCycles = ArchitectureUtilities.getMemoryAccessLatencyCycles(spec_architecture, res,
								memory, tileActiveProcessorsCount, payLoadBitCount);
						activeTime += memAccessCycles * clockCycleDuration;
					}
				}
				// (3) write output messages
				for (Task msg : implementation.getApplication().getSuccessors(task)) {
					if (msg instanceof Communication) {
						double payLoadBitCount = CommunicationInvasicPropertyService.getPayload(msg);
						int memAccessCycles = ArchitectureUtilities.getMemoryAccessLatencyCycles(spec_architecture, res,
								memory, tileActiveProcessorsCount, payLoadBitCount);
						activeTime += memAccessCycles * clockCycleDuration;
					}
				}
				ProcessInvasicPropertyService.setActiveTime(task, activeTime);
			}
		}
	}

	/**
	 * Assigns the processor slots of the processes in the provided implementation.
	 * 
	 * @param implementation
	 *            the provided implementation
	 */
	protected void calculateTaskSlotsMeshNoc(Specification implementation) {
		Mappings<Task, Resource> bindings = implementation.getMappings();
		for (Mapping<Task, Resource> mapping : bindings) {
			Task task = mapping.getSource();
			Resource res = mapping.getTarget();
			if (TaskPropertyService.isProcess(task)) {
				double activeTime = ProcessInvasicPropertyService.getActiveTime(task);
				double period = TaskInvasicPropertyService.getPeriod(task);
				double SI = ProcessorInvasicPropertyService.getSchedulingInterval(res);
				double SI_OS = ProcessorInvasicPropertyService.getOsSchedulingInterval(res);
				int maxNumSlot = ProcessorInvasicPropertyService.getMaximumNumOfSlots(res);
				int numSlots = SlotReservationChecker.activeTime2SI(activeTime, period, SI, SI_OS, maxNumSlot);
				ProcessInvasicPropertyService.setNumSIslots(task, numSlots);
			}
		}
	}

	/**
	 * Assigns the relevant parameters (WCET, Maximal power, context bit count) of
	 * the processes in the provided implementation.
	 * 
	 * @param implementation
	 *            the provided implementation
	 */
	protected void assignPrimaryAttributesMeshNoC(Specification implementation) {
		Mappings<Task, Resource> bindings = implementation.getMappings();
		for (Mapping<Task, Resource> mapping : bindings) {
			Task task = mapping.getSource();
			if (TaskPropertyService.isProcess(task)) {
				ProcessInvasicPropertyService.setContextBitCount(task,
						MappingInvasicPropertyService.getContextBitCount(mapping));
				ProcessInvasicPropertyService.setWCET(task, MappingInvasicPropertyService.getWCET(mapping));
				ProcessInvasicPropertyService.setMaxPower(task, MappingInvasicPropertyService.getMaxPower(mapping));
			}
		}
	}
}
