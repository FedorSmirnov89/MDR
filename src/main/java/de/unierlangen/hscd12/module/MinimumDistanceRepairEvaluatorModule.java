package de.unierlangen.hscd12.module;

import de.unierlangen.hscd12.evaluator.MinimumDistanceRepairEvaluator;
import net.sf.opendse.optimization.evaluator.EvaluatorModule;

public class MinimumDistanceRepairEvaluatorModule extends EvaluatorModule {

	@Override
	protected void config() {
		bindEvaluator(MinimumDistanceRepairEvaluator.class);
	}
}
