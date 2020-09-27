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
package com.wl4g.iam.client.config;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_C_BASE;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.core.config.mapping.PrefixHandlerMapping;
import com.wl4g.components.core.kit.access.IPAccessControl;
import com.wl4g.iam.client.authc.secondary.SecondaryAuthenticationAdvice;
import com.wl4g.iam.client.authc.secondary.SimpleSecondaryAuthenticationHandler;
import com.wl4g.iam.client.configure.AnynothingClientSecurityConfigurer;
import com.wl4g.iam.client.configure.AnynothingClientSecurityCoprocessor;
import com.wl4g.iam.client.configure.ClientSecurityConfigurer;
import com.wl4g.iam.client.configure.ClientSecurityCoprocessor;
import com.wl4g.iam.client.filter.AuthenticatorAuthenticationFilter;
import com.wl4g.iam.client.filter.ClientInternalAuthenticationFilter;
import com.wl4g.iam.client.filter.LogoutAuthenticationFilter;
import com.wl4g.iam.client.filter.ROOTAuthenticationFilter;
import com.wl4g.iam.client.realm.FastCasAuthorizingRealm;
import com.wl4g.iam.client.session.mgt.IamClientSessionManager;
import com.wl4g.iam.client.validation.ExpiredSessionIamValidator;
import com.wl4g.iam.client.validation.FastCasTicketIamValidator;
import com.wl4g.iam.client.validation.IamValidator;
import com.wl4g.iam.client.web.ClientAuthenticatorEndpoint;
import com.wl4g.iam.common.authz.EnhancedModularRealmAuthorizer;
import com.wl4g.iam.common.cache.IamCacheManager;
import com.wl4g.iam.common.cache.JedisIamCacheManager;
import com.wl4g.iam.common.config.AbstractIamConfiguration;
import com.wl4g.iam.common.config.AbstractIamProperties;
import com.wl4g.iam.common.config.AbstractIamProperties.ParamProperties;
import com.wl4g.iam.common.mgt.IamSubjectFactory;
import com.wl4g.iam.common.session.mgt.IamSessionFactory;
import com.wl4g.iam.common.session.mgt.JedisIamSessionDAO;
import com.wl4g.iam.common.web.servlet.IamCookie;

/**
 * IAM client auto configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年03月19日
 * @since
 */
public class IamClientAutoConfiguration extends AbstractIamConfiguration {

	// ==============================
	// Configuration properties.
	// ==============================

	@Bean
	@ConditionalOnMissingBean(IamClientProperties.class)
	public IamClientProperties iamClientProperties() {
		return new IamClientProperties();
	}

	// ==============================
	// SHIRO manager and filter's
	// ==============================

	@Bean
	public DefaultWebSecurityManager securityManager(IamSubjectFactory subjectFactory, IamClientSessionManager sessionManager,
			EnhancedModularRealmAuthorizer authorizer) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setSessionManager(sessionManager);
		securityManager.setRealms(authorizer.getRealms());
		securityManager.setSubjectFactory(subjectFactory);
		securityManager.setAuthorizer(authorizer);
		return securityManager;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	@ConditionalOnMissingBean(IamClientSessionManager.class)
	public IamClientSessionManager iamClientSessionManager(IamSessionFactory sessionFactory, JedisIamSessionDAO sessionDAO,
			IamCacheManager cacheManager, IamCookie cookie, IamClientProperties config,
			@Qualifier(BEAN_SESSION_VALIDATOR) IamValidator validator) {
		IamClientSessionManager sessionManager = new IamClientSessionManager(config, cacheManager, validator);
		sessionManager.setSessionFactory(sessionFactory);
		sessionManager.setSessionDAO(sessionDAO);
		sessionManager.setSessionIdCookie(cookie);
		sessionManager.setCacheManager(cacheManager);
		sessionManager.setSessionIdUrlRewritingEnabled(config.getSession().isUrlRewriting());
		sessionManager.setSessionIdCookieEnabled(true);
		sessionManager.setSessionValidationInterval(config.getSession().getSessionValidationInterval());
		sessionManager.setGlobalSessionTimeout(config.getSession().getGlobalSessionTimeout());
		return sessionManager;
	}

	// ==============================
	// Authentication filter`s.
	// ==============================

	@Bean(BEAN_AUTH_FILTER)
	public AuthenticatorAuthenticationFilter authenticatorAuthenticationFilter(IamClientProperties config,
			ClientSecurityConfigurer context, ClientSecurityCoprocessor coprocessor, JedisIamCacheManager cacheManager) {
		return new AuthenticatorAuthenticationFilter(config, context, coprocessor, cacheManager);
	}

	@Bean(BEAN_ROOT_FILTER)
	public ROOTAuthenticationFilter rootAuthenticationFilter(IamClientProperties config, ClientSecurityConfigurer context,
			ClientSecurityCoprocessor coprocessor, JedisIamCacheManager cacheManager) {
		return new ROOTAuthenticationFilter(config, context, coprocessor, cacheManager);
	}

	@Bean
	@ConditionalOnMissingBean
	public ClientInternalAuthenticationFilter internalWhiteListClientAuthenticationFilter(IPAccessControl control,
			AbstractIamProperties<? extends ParamProperties> config) {
		return new ClientInternalAuthenticationFilter(control, config);
	}

	@Bean
	public LogoutAuthenticationFilter logoutAuthenticationFilter(IamClientProperties config, ClientSecurityConfigurer context,
			ClientSecurityCoprocessor coprocessor, JedisIamCacheManager cacheManager, RestTemplate restTemplate) {
		return new LogoutAuthenticationFilter(config, context, coprocessor, cacheManager, restTemplate);
	}

	// ==============================
	// Authentication filter`s registration
	// Reference See: http://www.hillfly.com/2017/179.html
	// org.apache.catalina.core.ApplicationFilterChain#internalDoFilter
	// ==============================

	@Bean
	public FilterRegistrationBean<AuthenticatorAuthenticationFilter> authenticatorFilterRegistrationBean(
			@Qualifier(BEAN_AUTH_FILTER) AuthenticatorAuthenticationFilter filter) {
		FilterRegistrationBean<AuthenticatorAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<ROOTAuthenticationFilter> rootFilterRegistrationBean(
			@Qualifier(BEAN_ROOT_FILTER) ROOTAuthenticationFilter filter) {
		FilterRegistrationBean<ROOTAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<ClientInternalAuthenticationFilter> internalClientFilterRegistrationBean(
			ClientInternalAuthenticationFilter filter) {
		FilterRegistrationBean<ClientInternalAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<LogoutAuthenticationFilter> logoutFilterRegistrationBean(LogoutAuthenticationFilter filter) {
		FilterRegistrationBean<LogoutAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	// ==============================
	// Authorizing realm`s
	// ==============================

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
	@ConditionalOnMissingBean
	public FastCasAuthorizingRealm fastCasAuthorizingRealm(IamClientProperties config,
			@Qualifier(BEAN_TICKET_VALIDATOR) IamValidator validator) {
		return new FastCasAuthorizingRealm(config, validator);
	}

	// ==============================
	// Authentication ticket validator's
	// ==============================

	@SuppressWarnings("rawtypes")
	@Bean(BEAN_TICKET_VALIDATOR)
	public IamValidator fastCasTicketValidator(IamClientProperties config, RestTemplate restTemplate) {
		return new FastCasTicketIamValidator(config, restTemplate);
	}

	@SuppressWarnings("rawtypes")
	@Bean(BEAN_SESSION_VALIDATOR)
	public IamValidator expireSessionValidator(IamClientProperties config, RestTemplate restTemplate) {
		return new ExpiredSessionIamValidator(config, restTemplate);
	}

	// ==============================
	// IAM context interceptor's
	// ==============================

	/**
	 * Notes for using `@ConditionalOnMissingBean': 1, `@Bean'method return
	 * value type must be the type using `@Autowired' annotation; 2, or use
	 * `Conditional OnMissing Bean'(MyInterface. class) in this way.`
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public ClientSecurityConfigurer anynothingClientSecurityConfigurer() {
		return new AnynothingClientSecurityConfigurer();
	}

	/**
	 * Notes for using `@ConditionalOnMissingBean': 1, `@Bean'method return
	 * value type must be the type using `@Autowired' annotation; 2, or use
	 * `Conditional OnMissing Bean'(MyInterface. class) in this way.`
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public ClientSecurityCoprocessor anynothingClientSecurityCoprocessor() {
		return new AnynothingClientSecurityCoprocessor();
	}

	// ==============================
	// IAM AOP asppect's
	// ==============================

	@Bean
	public SecondaryAuthenticationAdvice secondaryAuthenticationAdvice(SimpleSecondaryAuthenticationHandler handler) {
		return new SecondaryAuthenticationAdvice(handler);
	}

	@Bean
	@ConditionalOnMissingBean
	public SimpleSecondaryAuthenticationHandler secondAuthenticateProcessor(IamClientProperties config, RestTemplate restTemplate,
			BeanFactory beanFactory) {
		return new SimpleSecondaryAuthenticationHandler(config, restTemplate, beanFactory);
	}

	// ==============================
	// IAM controller's
	// ==============================

	@Bean
	public ClientAuthenticatorEndpoint clientAuthenticatorController() {
		return new ClientAuthenticatorEndpoint();
	}

	@Bean
	public PrefixHandlerMapping iamClientAuthenticatorControllerPrefixHandlerMapping() {
		return super.newIamControllerPrefixHandlerMapping(URI_C_BASE);
	}

	final private static String BEAN_ROOT_FILTER = "rootAuthenticationFilter";
	final private static String BEAN_AUTH_FILTER = "authenticatorAuthenticationFilter";
	final private static String BEAN_TICKET_VALIDATOR = "fastCasTicketValidator";
	final private static String BEAN_SESSION_VALIDATOR = "expireSessionValidator";

}