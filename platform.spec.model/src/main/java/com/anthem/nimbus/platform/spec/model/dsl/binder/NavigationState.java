/**
 * 
 */
package com.anthem.nimbus.platform.spec.model.dsl.binder;

import java.io.Serializable;

import com.anthem.oss.nimbus.core.domain.definition.Domain;
import com.anthem.oss.nimbus.core.domain.definition.Execution;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Jayant Chaudhuri
 *
 */

@Domain("navigationstate")
@Execution.Input.Default @Execution.Output.Default
@ToString
@Getter @Setter
public class NavigationState implements Serializable{
	private static final long serialVersionUID = 1L;
	private PageHolder pageHolder;
}
