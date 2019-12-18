package de.unierlangen.hscd12.module;

import org.opt4j.core.config.annotations.Required;
import org.opt4j.core.problem.ProblemModule;
import org.opt4j.core.start.Constant;
import org.opt4j.viewer.VisualizationModule;

import com.google.inject.multibindings.Multibinder;

import de.unierlangen.hscd12.dse.AllocationEncodingNone;
import de.unierlangen.hscd12.dse.ApplicationEncodingNone;
import de.unierlangen.hscd12.dse.MappingEncodingNone;
import de.unierlangen.hscd12.dse.RoutingEncodingNone;
import de.unierlangen.hscd12.interpreter.InterpreterParameter;
import de.unierlangen.hscd12.preprocessing.MappingParameterAnnotation;
import encoding.isolation_schemes.AllocationEncodingTileUtilization;
import net.sf.opendse.encoding.ApplicationEncoding;
import net.sf.opendse.encoding.ImplementationEncodingModular;
import net.sf.opendse.encoding.ImplementationEncodingModularDefault;
import net.sf.opendse.encoding.MappingEncoding;
import net.sf.opendse.encoding.RoutingEncoding;
import net.sf.opendse.optimization.DesignSpaceExplorationCreator;
import net.sf.opendse.optimization.DesignSpaceExplorationDecoder;
import net.sf.opendse.optimization.DesignSpaceExplorationEvaluator;
import net.sf.opendse.optimization.ImplementationEvaluator;
import net.sf.opendse.optimization.ImplementationWidgetService;
import net.sf.opendse.optimization.SATConstraints;
import net.sf.opendse.optimization.SATCreatorDecoder;
import net.sf.opendse.optimization.SpecificationToolBarService;
import net.sf.opendse.optimization.StagnationRestart;
import net.sf.opendse.optimization.constraints.SpecificationCapacityConstraints;
import net.sf.opendse.optimization.constraints.SpecificationConnectConstraints;
import net.sf.opendse.optimization.constraints.SpecificationConstraints;
import net.sf.opendse.optimization.constraints.SpecificationConstraintsMulti;
import net.sf.opendse.optimization.constraints.SpecificationElementsConstraints;
import net.sf.opendse.optimization.constraints.SpecificationRouterConstraints;
import net.sf.opendse.optimization.encoding.ImplementationEncoding;
import net.sf.opendse.optimization.encoding.Interpreter;

public class MinimumDistanceRepairModule extends ProblemModule {

	protected boolean stagnationRestartEnabled = true;

	@Required(property = "stagnationRestartEnabled", elements = { "TRUE" })
	@Constant(value = "maximalNumberStagnatingGenerations", namespace = StagnationRestart.class)
	protected int maximalNumberStagnatingGenerations = 20;

	@Constant(value = "variableorder", namespace = SATCreatorDecoder.class)
	protected boolean useVariableOrder = false;

	@Constant(value = "preprocessing", namespace = SATConstraints.class)
	protected boolean usePreprocessing = true;

	public boolean isUseVariableOrder() {
		return useVariableOrder;
	}

	public void setUseVariableOrder(boolean useVariableOrder) {
		this.useVariableOrder = useVariableOrder;
	}

	public boolean isUsePreprocessing() {
		return usePreprocessing;
	}

	public void setUsePreprocessing(boolean usePreprocessing) {
		this.usePreprocessing = usePreprocessing;
	}

	public boolean isStagnationRestartEnabled() {
		return stagnationRestartEnabled;
	}

	public void setStagnationRestartEnabled(boolean stagnationRestartEnabled) {
		this.stagnationRestartEnabled = stagnationRestartEnabled;
	}

	public int getMaximalNumberStagnatingGenerations() {
		return maximalNumberStagnatingGenerations;
	}

	public void setMaximalNumberStagnatingGenerations(int maximalNumberStagnatingGenerations) {
		this.maximalNumberStagnatingGenerations = maximalNumberStagnatingGenerations;
	}

	@Override
	protected void config() {
		// stuff that replaces the optimization module
		bindProblem(DesignSpaceExplorationCreator.class, DesignSpaceExplorationDecoder.class,
				DesignSpaceExplorationEvaluator.class);
		VisualizationModule.addIndividualMouseListener(binder(), ImplementationWidgetService.class);
		VisualizationModule.addToolBarService(binder(), SpecificationToolBarService.class);
		bind(SpecificationConstraints.class).to(SpecificationConstraintsMulti.class).in(SINGLETON);
		Multibinder<SpecificationConstraints> scmulti = Multibinder.newSetBinder(binder(),
				SpecificationConstraints.class);
		scmulti.addBinding().to(SpecificationCapacityConstraints.class);
		scmulti.addBinding().to(SpecificationConnectConstraints.class);
		scmulti.addBinding().to(SpecificationElementsConstraints.class);
		scmulti.addBinding().to(SpecificationRouterConstraints.class);
		Multibinder.newSetBinder(binder(), ImplementationEvaluator.class);
		if (stagnationRestartEnabled) {
			addOptimizerIterationListener(StagnationRestart.class);
		}
		// stuff that binds the actually new things
		bind(Interpreter.class).to(InterpreterParameter.class);
		bind(ImplementationEncoding.class).to(ImplementationEncodingModular.class);
		bind(ImplementationEncodingModular.class).to(ImplementationEncodingModularDefault.class);

		bind(MappingParameterAnnotation.class).asEagerSingleton();
		bind(ApplicationEncoding.class).to(ApplicationEncodingNone.class);
		bind(MappingEncoding.class).to(MappingEncodingNone.class);
		bind(RoutingEncoding.class).to(RoutingEncodingNone.class);
		bind(AllocationEncodingTileUtilization.class).to(AllocationEncodingNone.class);
	}
}
