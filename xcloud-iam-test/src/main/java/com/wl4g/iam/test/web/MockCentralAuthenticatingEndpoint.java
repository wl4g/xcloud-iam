/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.iam.test.web;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.components.common.web.WebUtils2.getFullRequestURL;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_SECOND_VALIDATE;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_SESSION_VALIDATE;
import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_VALIDATE;
import static com.wl4g.iam.common.utils.IamSecurityHolder.getSessionId;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wl4g.components.core.web.BaseController;
import com.wl4g.components.core.web.RespBase;
import com.wl4g.iam.common.annotation.IamController;
import com.wl4g.iam.common.authc.model.SecondAuthcAssertModel;
import com.wl4g.iam.common.authc.model.SessionValidityAssertModel;
import com.wl4g.iam.common.authc.model.TicketValidateModel;
import com.wl4g.iam.common.authc.model.TicketValidatedAssertModel;
import com.wl4g.iam.common.config.AbstractIamProperties;
import com.wl4g.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.common.handler.AuthenticatingHandler;
import com.wl4g.iam.common.subject.IamPrincipalInfo;
import com.wl4g.iam.common.web.AuthenticatingEndpoint;

/**
 * Mock iam central authenticating endpoint
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年1月27日
 * @since
 */
@IamController
public class MockCentralAuthenticatingEndpoint extends BaseController implements AuthenticatingEndpoint {

	@Autowired
	protected AuthenticatingHandler authHandler;

	@Autowired
	protected AbstractIamProperties<? extends ParamProperties> config;

	@PostMapping(URI_S_VALIDATE)
	@ResponseBody
	@Override
	public RespBase<TicketValidatedAssertModel<IamPrincipalInfo>> validate(@NotNull @RequestBody TicketValidateModel param) {
		log.info("Mock Ticket validating, sessionId: {} <= {}", getSessionId(), toJSONString(param));

		RespBase<TicketValidatedAssertModel<IamPrincipalInfo>> resp = new RespBase<>();
		// Ticket assertion.
		resp.setData(authHandler.validate(param));

		log.info("Mock Ticket validated. => {}", resp);
		return resp;
	}

	@PostMapping(URI_S_SECOND_VALIDATE)
	@ResponseBody
	@Override
	public RespBase<SecondAuthcAssertModel> secondaryValidate(HttpServletRequest request) {
		log.info("Mock Secondary validating, sessionId: {} <= {}", getSessionId(), getFullRequestURL(request));

		RespBase<SecondAuthcAssertModel> resp = new RespBase<>();
		// Requires parameters
		String secondAuthCode = WebUtils.getCleanParam(request, config.getParam().getSecondaryAuthCode());
		String fromAppName = WebUtils.getCleanParam(request, config.getParam().getApplication());
		// Secondary authentication assertion.
		resp.setData(authHandler.secondaryValidate(secondAuthCode, fromAppName));

		log.info("Mock Secondary validated. => {}", resp);
		return resp;
	}

	@PostMapping(URI_S_SESSION_VALIDATE)
	@ResponseBody
	@Override
	public RespBase<SessionValidityAssertModel> sessionValidate(@NotNull @RequestBody SessionValidityAssertModel param) {
		log.info("Mock Sessions expires validating, sessionId: {} <= {}", getSessionId(), toJSONString(param));

		RespBase<SessionValidityAssertModel> resp = new RespBase<>();

		// Session expires validate assertion.
		resp.setData(authHandler.sessionValidate(param));

		log.info("Mock Sessions expires validated. => {}", resp);
		return resp;
	}

}