package com.anthem.oss.nimbus.core.domain.command.execution.search;

import com.anthem.oss.nimbus.core.domain.command.Command;
import com.anthem.oss.nimbus.core.domain.command.execution.ExecutionContext;
import com.anthem.oss.nimbus.core.domain.model.config.ModelConfig;
import com.anthem.oss.nimbus.core.domain.model.state.EntityState.Param;
import com.anthem.oss.nimbus.core.domain.model.state.repo.db.ModelRepository;
import com.anthem.oss.nimbus.core.entity.SearchCriteria.ExampleSearchCriteria;
import com.anthem.oss.nimbus.core.entity.SearchCriteria.ProjectCriteria;

/**
 * @author Rakesh Patel
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultSearchFunctionHandlerExample<T, R> extends DefaultSearchFunctionHandler<T, R> {

	@Override
	public R execute(ExecutionContext executionContext, Param<T> actionParameter) {
		ModelConfig<?> mConfig = getRootDomainConfig(executionContext);
		
		ExampleSearchCriteria exampleSearchCriteria = createSearchCriteria(executionContext, mConfig);
		Class<?> criteriaClass = mConfig.getReferredClass();
		String alias = findRepoAlias(criteriaClass);
		
		ModelRepository rep = getRepFactory().get(mConfig.getRepo());
		
		return (R)rep._search(criteriaClass, alias, exampleSearchCriteria);
	}


	@Override
	protected ExampleSearchCriteria createSearchCriteria(ExecutionContext executionContext, ModelConfig<?> mConfig) {
		
		Command cmd = executionContext.getCommandMessage().getCommand();
		
		ExampleSearchCriteria exampleSearchCriteria = new ExampleSearchCriteria<>();
		exampleSearchCriteria.validate(executionContext);
				
		Class<?> criteriaClass = mConfig.getReferredClass();
		T criteria = (T)getConverter().convert(criteriaClass, executionContext.getCommandMessage());
		
		exampleSearchCriteria.setWhere(criteria);
		
		String aggregateAs = cmd.getFirstParameterValue("aggregate");
		exampleSearchCriteria.setAggregateCriteria(aggregateAs);
		
		ProjectCriteria projectCriteria = new ProjectCriteria();
		String projectAlias = cmd.getFirstParameterValue("projection.alias");
		projectCriteria.setAlias(projectAlias);
		
		exampleSearchCriteria.setProjectCriteria(projectCriteria);
		
		return exampleSearchCriteria;
		
	}
}