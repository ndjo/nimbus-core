/**
 * 
 */
package com.anthem.oss.nimbus.core.domain.command.execution;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.anthem.oss.nimbus.core.BeanResolverStrategy;
import com.anthem.oss.nimbus.core.domain.command.CommandElement.Type;
import com.anthem.oss.nimbus.core.domain.definition.Constants;
import com.anthem.oss.nimbus.core.domain.model.state.EntityState.Param;
import com.anthem.oss.nimbus.core.entity.client.user.ClientUser;
import com.anthem.oss.nimbus.core.session.UserEndpointSession;
import com.anthem.oss.nimbus.core.utils.ParamPathExpressionParser;

/**
 * @author Soham Chakravarti
 *
 */
public class DefaultCommandPathVariableResolver implements CommandPathVariableResolver {

	private final CommandMessageConverter converter;
	
	public DefaultCommandPathVariableResolver(BeanResolverStrategy beanResolver) {
		this.converter = beanResolver.get(CommandMessageConverter.class);
	}
	
	
	@Override
	public String resolve(Param<?> param, String urlToResolve) {
		Map<Integer, String> entries = ParamPathExpressionParser.parse(urlToResolve);
		if(MapUtils.isEmpty(entries))
			return urlToResolve;
		
		String out = urlToResolve;
		for(Integer i : entries.keySet()) {
			String key = entries.get(i);
			
			// look for relative path to passed in param's parent model
			String pathToResolve = ParamPathExpressionParser.stripPrefixSuffix(key);
			
			String val = map(param, pathToResolve);
			
			out = StringUtils.replace(out, key, val, 1);
		}
		
		return out;
	}
	
	protected String map(Param<?> param, String pathToResolve) {
		if(StringUtils.startsWithIgnoreCase(pathToResolve, Constants.MARKER_SESSION_SELF.code))
			return mapSelf(param, pathToResolve);
		
		if(StringUtils.startsWithIgnoreCase(pathToResolve, Constants.MARKER_COMMAND_PARAM_CURRENT_SELF.code))
			return param.getPath();
		
		if(StringUtils.startsWithIgnoreCase(pathToResolve, Constants.MARKER_REF_ID.code)) {
			return param.getRootExecution().getRootCommand().getRefId(Type.DomainAlias);
			//return eCtx.getCommandMessage().getCommand().getRefId(Type.DomainAlias);
		}
		
		return mapQuad(param, pathToResolve);
	}
	
	//TODO bean path evaluation to get value
	protected String mapSelf(Param<?> param, String pathToResolve) {
		if(StringUtils.endsWith(pathToResolve, "loginId"))
			return Optional.ofNullable(UserEndpointSession.getStaticLoggedInUser()).orElseGet(() -> new ClientUser()).getLoginId();
		
		return param.getRootExecution().getRootCommand().getElement(Type.ClientAlias).get().getAlias();
	}
	
	protected String mapQuad(Param<?> param, String pathToResolve) {
		if(StringUtils.startsWith(pathToResolve, "json(")) {
			String paramPath = StringUtils.substringBetween(pathToResolve, "json(", ")");
			Param<?> p = param.getParentModel().findParamByPath(paramPath);
			
			Object state = p.getLeafState();
			String json = converter.convert(state);
			return json;
		} else {
			Param<?> p = param.getParentModel().findParamByPath(pathToResolve);
			return String.valueOf(p.getState());
		}
	}

}
