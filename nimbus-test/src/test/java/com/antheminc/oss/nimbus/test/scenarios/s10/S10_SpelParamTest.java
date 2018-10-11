/**
 *  Copyright 2016-2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.antheminc.oss.nimbus.test.scenarios.s10;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.antheminc.oss.nimbus.InvalidConfigException;
import com.antheminc.oss.nimbus.domain.cmd.Action;
import com.antheminc.oss.nimbus.domain.model.state.EntityState.Param;
import com.antheminc.oss.nimbus.support.expr.ExpressionEvaluator;
import com.antheminc.oss.nimbus.support.expr.SpelExpressionEvaluator;
import com.antheminc.oss.nimbus.test.domain.support.AbstractFrameworkIntegrationTests;
import com.antheminc.oss.nimbus.test.domain.support.utils.ExtractResponseOutputUtils;
import com.antheminc.oss.nimbus.test.domain.support.utils.MockHttpRequestBuilder;
import com.antheminc.oss.nimbus.test.domain.support.utils.ParamUtils;

/**
 * @author Andrew Jo
 * @author Tony Lopez
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class S10_SpelParamTest extends AbstractFrameworkIntegrationTests {
	
	private ExpressionEvaluator expressionEvaluator = new SpelExpressionEvaluator();
	ExpressionParser parser = new SpelExpressionParser();

	public static String VIEW_ROOT = PLATFORM_ROOT + "/s10v_main";

	
	@SuppressWarnings({ "unchecked" })
	@Test()
	public void t00_checkOriginalState() throws Exception {
	
		Object controllerResp_new = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addAction(Action._new).getMock(), null);
		
		Param<?> p_view = ExtractResponseOutputUtils.extractOutput(controllerResp_new);
		Assert.assertNotNull(p_view);
		final Long refId = ExtractResponseOutputUtils.extractDomainRootRefId(controllerResp_new);
		
		p_view.findParamByPath("/vp/vt/vs/vf/foodPreference").setState(1);
	
		Object btnSetFoodToServe_get = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addNested("/vp/vt/vs/vf/btnSetFoodToServeNoSpel1")
				.addAction(Action._get).getMock(), null);
		
		Param<String> favoriteFood1Param = ParamUtils.extractResponseByParamPath(btnSetFoodToServe_get, "/favoriteFood1");
		Assert.assertEquals("apple", favoriteFood1Param.getState());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test(expected=InvalidConfigException.class)
	public void t01_checkOriginalStateInvalidPath() throws Exception {
	
		Object controllerResp_new = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addAction(Action._new).getMock(), null);
		
		Param<?> p_view = ExtractResponseOutputUtils.extractOutput(controllerResp_new);
		Assert.assertNotNull(p_view);
		final Long refId = ExtractResponseOutputUtils.extractDomainRootRefId(controllerResp_new);
		
		p_view.findParamByPath("/vp/vt/vs/vf/foodPreference").setState(3);
		Object btnSetFoodToServe_get = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addNested("/vp/vt/vs/vf/btnSetFoodToServeNoSpel3")
				.addAction(Action._get).getMock(), null);
		
		Param<String> favoriteFood1Param = ParamUtils.extractResponseByParamPath(btnSetFoodToServe_get, "/favoriteFood3");
		Assert.assertEquals("apple", favoriteFood1Param.getState());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void t02_configAndSpelConditional1() throws Exception {
	
		Object controllerResp_new = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addAction(Action._new).getMock(), null);
		
		Param<?> p_view = ExtractResponseOutputUtils.extractOutput(controllerResp_new);
		Assert.assertNotNull(p_view);
		final Long refId = ExtractResponseOutputUtils.extractDomainRootRefId(controllerResp_new);
		
		p_view.findParamByPath("/vp/vt/vs/vf/foodPreference").setState(1);
		Object btnSetFoodToServe_get = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addNested("/vp/vt/vs/vf/btnSetFoodToServe")
				.addAction(Action._get).getMock(), null);
		
		Param<String> favoriteFood1Param = ParamUtils.extractResponseByParamPath(btnSetFoodToServe_get, "/favoriteFood1");
		Assert.assertEquals("apple", favoriteFood1Param.getState());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void t03_configAndSpelConditional2() throws Exception {
	
		Object controllerResp_new = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addAction(Action._new).getMock(), null);
		
		Param<?> p_view = ExtractResponseOutputUtils.extractOutput(controllerResp_new);
		Assert.assertNotNull(p_view);
		final Long refId = ExtractResponseOutputUtils.extractDomainRootRefId(controllerResp_new);
		
		p_view.findParamByPath("/vp/vt/vs/vf/foodPreference").setState(2);
		Object btnSetFoodToServe_get = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addNested("/vp/vt/vs/vf/btnSetFoodToServe")
				.addAction(Action._get).getMock(), null);
		
		Param<String> favoriteFood1Param = ParamUtils.extractResponseByParamPath(btnSetFoodToServe_get, "/favoriteFood2");
		Assert.assertEquals("apple", favoriteFood1Param.getState());
	}
	
	@SuppressWarnings({ "unchecked" })
	@Test(expected=InvalidConfigException.class)
	public void t04_configAndSpelConditionalInvalid() throws Exception {

		Object controllerResp_new = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addAction(Action._new).getMock(), null);
		
		Param<?> p_view = ExtractResponseOutputUtils.extractOutput(controllerResp_new);
		Assert.assertNotNull(p_view);
		final Long refId = ExtractResponseOutputUtils.extractDomainRootRefId(controllerResp_new);
		
		p_view.findParamByPath("/vp/vt/vs/vf/foodPreference").setState(3);
		Object btnSetFoodToServe_get = controller.handleGet(MockHttpRequestBuilder.withUri(VIEW_ROOT)
				.addNested("/vp/vt/vs/vf/btnSetFoodToServe")
				.addAction(Action._get).getMock(), null);
		
		Param<String> favoriteFood1Param = ParamUtils.extractResponseByParamPath(btnSetFoodToServe_get, "/favoriteFood3");
		Assert.assertEquals("apple", favoriteFood1Param.getState());
	}
	
	
}