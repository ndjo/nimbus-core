/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.model.state.internal;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.anthem.oss.nimbus.core.domain.command.Action;
import com.anthem.oss.nimbus.core.domain.command.Command;
import com.anthem.oss.nimbus.core.domain.command.CommandBuilder;
import com.anthem.oss.nimbus.core.domain.command.CommandMessage;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecutorGateway;
import com.anthem.oss.nimbus.core.domain.model.config.ParamValue;
import com.anthem.oss.nimbus.core.domain.command.execution.CommandExecution.MultiOutput;

/**
 * @author Rakesh Patel
 *
 */
public class StaticCodeValueBasedCodeToDescConverter extends RepoBasedCodeToDescriptionConverter {

	
	@SuppressWarnings("unchecked")
	@Override
	public String serialize(String input) {
		//TODO - need to make this generic
		Command cmd = CommandBuilder.withUri("Anthem/icr/p/staticCodeValue/_search?fn=lookup&where=staticCodeValue.paramValues.any().code.eq('"+input+"')").getCommand();
		cmd.setAction(Action._search);
		
		CommandMessage cmdMsg = new CommandMessage();
		cmdMsg.setCommand(cmd);
		
		MultiOutput multiOp = gateway.execute(cmdMsg);
		
		List<ParamValue> paramValues = (List<ParamValue>)multiOp.getSingleResult();
		
		if(CollectionUtils.isEmpty(paramValues))
			return input;
		
		return paramValues.stream()
					.filter((pv) -> StringUtils.equalsIgnoreCase(input, (String)pv.getCode()))
					.map((pv)-> pv.getLabel())
					.findFirst()
					.orElse(input);
					
		
	}
	
	@Override
	public String deserialize(String input) {
		throw new UnsupportedOperationException("RepoBasedCodeToDescConverter.deserialize is not implemented since it is not needed.");
	}
	
}