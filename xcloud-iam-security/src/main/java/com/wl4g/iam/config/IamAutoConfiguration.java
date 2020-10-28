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
package com.wl4g.iam.config;

import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import static com.wl4g.components.core.constants.IAMDevOpsConstants.URI_S_BASE;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.wl4g.components.core.config.mapping.PrefixHandlerMapping;
import com.wl4g.components.core.framework.operator.GenericOperatorAdapter;
import com.wl4g.components.core.kit.access.IPAccessControl;
import com.wl4g.components.support.concurrent.locks.JedisLockManager;
import com.wl4g.iam.authc.credential.GenericCredentialsHashedMatcher;
import com.wl4g.iam.authc.credential.Oauth2AuthorizingBoundMatcher;
import com.wl4g.iam.authc.credential.SmsCredentialsHashedMatcher;
import com.wl4g.iam.authc.credential.secure.DefaultCredentialsSecurer;
import com.wl4g.iam.authc.credential.secure.IamCredentialsSecurer;
import com.wl4g.iam.authc.pam.ExceptionModularRealmAuthenticator;
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
import com.wl4g.iam.config.properties.CryptoProperties;
import com.wl4g.iam.config.properties.IamProperties;
import com.wl4g.iam.configure.AnynothingSecurityCoprocessor;
import com.wl4g.iam.configure.DefaultSecureConfigureAdapter;
import com.wl4g.iam.configure.SecureConfigureAdapter;
import com.wl4g.iam.configure.ServerSecurityCoprocessor;
import com.wl4g.iam.crypto.DSASecureCryptService;
import com.wl4g.iam.crypto.ECCSecureCryptService;
import com.wl4g.iam.crypto.RSASecureCryptService;
import com.wl4g.iam.crypto.SecureCryptService;
import com.wl4g.iam.crypto.SecureCryptService.CryptKind;
import com.wl4g.iam.filter.AuthenticatorAuthenticationFilter;
import com.wl4g.iam.filter.DingtalkAuthenticationFilter;
import com.wl4g.iam.filter.FacebookAuthenticationFilter;
import com.wl4g.iam.filter.GenericAuthenticationFilter;
import com.wl4g.iam.filter.GithubAuthenticationFilter;
import com.wl4g.iam.filter.GoogleAuthenticationFilter;
import com.wl4g.iam.filter.LogoutAuthenticationFilter;
import com.wl4g.iam.filter.QQAuthenticationFilter;
import com.wl4g.iam.filter.QrcodeAuthenticationFilter;
import com.wl4g.iam.filter.ROOTAuthenticationFilter;
import com.wl4g.iam.filter.ServerInternalAuthenticationFilter;
import com.wl4g.iam.filter.SinaAuthenticationFilter;
import com.wl4g.iam.filter.SmsAuthenticationFilter;
import com.wl4g.iam.filter.TwitterAuthenticationFilter;
import com.wl4g.iam.filter.WechatAuthenticationFilter;
import com.wl4g.iam.filter.WechatMpAuthenticationFilter;
import com.wl4g.iam.handler.CentralAuthenticatingHandler;
import com.wl4g.iam.handler.risk.SimpleRcmEvaluatorHandler;
import com.wl4g.iam.realm.AbstractAuthorizingRealm;
import com.wl4g.iam.realm.DingtalkAuthorizingRealm;
import com.wl4g.iam.realm.FacebookAuthorizingRealm;
import com.wl4g.iam.realm.GenericAuthorizingRealm;
import com.wl4g.iam.realm.GithubAuthorizingRealm;
import com.wl4g.iam.realm.GoogleAuthorizingRealm;
import com.wl4g.iam.realm.QQAuthorizingRealm;
import com.wl4g.iam.realm.QrcodeAuthorizingRealm;
import com.wl4g.iam.realm.SinaAuthorizingRealm;
import com.wl4g.iam.realm.SmsAuthorizingRealm;
import com.wl4g.iam.realm.TwitterAuthorizingRealm;
import com.wl4g.iam.realm.WechatAuthorizingRealm;
import com.wl4g.iam.realm.WechatMpAuthorizingRealm;
import com.wl4g.iam.session.mgt.IamServerSessionManager;
import com.wl4g.iam.verification.CompositeSecurityVerifierAdapter;
import com.wl4g.iam.verification.SecurityVerifier;
import com.wl4g.iam.verification.SimpleJPEGSecurityVerifier;
import com.wl4g.iam.verification.SmsSecurityVerifier;
import com.wl4g.iam.verification.SmsSecurityVerifier.PrintSmsHandleSender;
import com.wl4g.iam.verification.SmsSecurityVerifier.SmsHandleSender;
import com.wl4g.iam.web.CentralAuthenticatingEndpoint;

/**
 * IAM server auto configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年03月19日
 * @since
 */
public class IamAutoConfiguration extends AbstractIamConfiguration {
	final public static String BEAN_ROOT_FILTER = "rootAuthenticationFilter";
	final public static String BEAN_AUTH_FILTER = "authenticatorAuthenticationFilter";
	final public static String BEAN_OAUTH2_MATCHER = "oauth2BoundMatcher";

	// ==============================
	// Configuration properties.
	// ==============================

	@Bean
	@ConditionalOnMissingBean(IamProperties.class)
	public IamProperties iamProperties() {
		return new IamProperties();
	}

	// ==============================
	// Cryptic graphic's
	// ==============================

	@Bean
	public CryptoProperties cryptoProperties() {
		return new CryptoProperties();
	}

	@Bean
	public RSASecureCryptService rsaSecureCryptService(JedisLockManager lockManager) {
		return new RSASecureCryptService(lockManager);
	}

	@Bean
	public DSASecureCryptService dsaSecureCryptService(JedisLockManager lockManager) {
		return new DSASecureCryptService(lockManager);
	}

	// @Bean
	public ECCSecureCryptService eccSecureCryptService(JedisLockManager lockManager) {
		return new ECCSecureCryptService(lockManager);
	}

	@Bean
	public GenericOperatorAdapter<CryptKind, SecureCryptService> compositeCryptServiceAdapter(
			List<SecureCryptService> cryptServices) {
		return new GenericOperatorAdapter<CryptKind, SecureCryptService>(cryptServices) {
		};
	}

	// ==============================
	// SHIRO manager and filter's
	// ==============================

	@Bean
	public DefaultWebSecurityManager securityManager(IamSubjectFactory subjectFactory, IamServerSessionManager sessionManager,
			ModularRealmAuthenticator authenticator, EnhancedModularRealmAuthorizer authorizer) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setSessionManager(sessionManager);
		securityManager.setRealms(authorizer.getRealms());
		securityManager.setSubjectFactory(subjectFactory);
		// Multiple realm authenticator controller
		securityManager.setAuthenticator(authenticator);
		securityManager.setAuthorizer(authorizer);
		return securityManager;
	}

	@Bean
	public ExceptionModularRealmAuthenticator exceptionModularRealmAuthenticator(AuthenticationStrategy authenticationStrategy) {
		ExceptionModularRealmAuthenticator authenticator = new ExceptionModularRealmAuthenticator();
		authenticator.setAuthenticationStrategy(authenticationStrategy);
		List<Realm> realms = actx.getBeansOfType(AbstractAuthorizingRealm.class).values().stream().collect(toList());
		authenticator.setRealms(realms);
		return authenticator;
	}

	@Bean
	public FirstSuccessfulStrategy firstSuccessfulStrategy() {
		return new FirstSuccessfulStrategy();
	}

	@Bean
	@ConditionalOnMissingBean(IamServerSessionManager.class)
	public IamServerSessionManager iamServerSessionManager(IamSessionFactory sessionFactory, JedisIamSessionDAO sessionDao,
			IamCacheManager cacheManager, IamCookie cookie, IamProperties config) {
		IamServerSessionManager sessionManager = new IamServerSessionManager(config, cacheManager);
		sessionManager.setSessionFactory(sessionFactory);
		sessionManager.setSessionDAO(sessionDao);
		sessionManager.setSessionIdCookie(cookie);
		sessionManager.setCacheManager(cacheManager);
		sessionManager.setSessionIdUrlRewritingEnabled(config.getSession().isUrlRewriting());
		sessionManager.setSessionIdCookieEnabled(true);
		sessionManager.setSessionValidationInterval(config.getSession().getSessionValidationInterval());
		sessionManager.setGlobalSessionTimeout(config.getSession().getGlobalSessionTimeout());
		return sessionManager;
	}

	// ==============================
	// Credentials hashing matcher`s.
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public GenericCredentialsHashedMatcher genericCredentialsHashedMatcher() {
		return new GenericCredentialsHashedMatcher();
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsCredentialsHashedMatcher smsCredentialsHashedMatcher() {
		return new SmsCredentialsHashedMatcher();
	}

	@Bean(BEAN_OAUTH2_MATCHER)
	public Oauth2AuthorizingBoundMatcher oauth2AuthorizingBoundMatcher() {
		return new Oauth2AuthorizingBoundMatcher();
	}

	// ==============================
	// Credentials securer's.
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public SecureConfigureAdapter securerConfigureAdapter() {
		return new DefaultSecureConfigureAdapter();
	}

	@Bean
	@ConditionalOnMissingBean
	public IamCredentialsSecurer iamCredentialsSecurer(SecureConfigureAdapter adapter, JedisIamCacheManager cacheManager) {
		return new DefaultCredentialsSecurer(adapter.configure(), cacheManager);
	}

	// ==============================
	// Authentication filter`s.
	// ==============================

	@Bean(BEAN_AUTH_FILTER)
	public AuthenticatorAuthenticationFilter authenticatorAuthenticationFilter() {
		return new AuthenticatorAuthenticationFilter();
	}

	@Bean(BEAN_ROOT_FILTER)
	public ROOTAuthenticationFilter rootAuthenticationFilter() {
		return new ROOTAuthenticationFilter();
	}

	@Bean
	public ServerInternalAuthenticationFilter internalWhiteListServerAuthenticationFilter(IPAccessControl control,
			AbstractIamProperties<? extends ParamProperties> config) {
		return new ServerInternalAuthenticationFilter(control, config);
	}

	@Bean
	public QrcodeAuthenticationFilter qrcodeAuthenticationFilter() {
		return new QrcodeAuthenticationFilter();
	}

	@Bean
	public FacebookAuthenticationFilter facebookAuthenticationFilter() {
		return new FacebookAuthenticationFilter();
	}

	@Bean
	public SmsAuthenticationFilter smsAuthenticationFilter() {
		return new SmsAuthenticationFilter();
	}

	@Bean
	public WechatAuthenticationFilter wechatAuthenticationFilter() {
		return new WechatAuthenticationFilter();
	}

	@Bean
	public WechatMpAuthenticationFilter wechatMpAuthenticationFilter() {
		return new WechatMpAuthenticationFilter();
	}

	@Bean
	public GenericAuthenticationFilter genericAuthenticationFilter() {
		return new GenericAuthenticationFilter();
	}

	@Bean
	public LogoutAuthenticationFilter logoutAuthenticationFilter() {
		return new LogoutAuthenticationFilter();
	}

	@Bean
	public DingtalkAuthenticationFilter dingtalkAuthenticationFilter() {
		return new DingtalkAuthenticationFilter();
	}

	@Bean
	public GoogleAuthenticationFilter googleAuthenticationFilter() {
		return new GoogleAuthenticationFilter();
	}

	@Bean
	public TwitterAuthenticationFilter twitterAuthenticationFilter() {
		return new TwitterAuthenticationFilter();
	}

	@Bean
	public QQAuthenticationFilter qqAuthenticationFilter() {
		return new QQAuthenticationFilter();
	}

	@Bean
	public GithubAuthenticationFilter githubAuthenticationFilter() {
		return new GithubAuthenticationFilter();
	}

	@Bean
	public SinaAuthenticationFilter sinaAuthenticationFilter() {
		return new SinaAuthenticationFilter();
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
	public FilterRegistrationBean<ServerInternalAuthenticationFilter> internalServerFilterRegistrationBean(
			ServerInternalAuthenticationFilter filter) {
		FilterRegistrationBean<ServerInternalAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<FacebookAuthenticationFilter> facebookFilterRegistrationBean(
			FacebookAuthenticationFilter filter) {
		FilterRegistrationBean<FacebookAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<SmsAuthenticationFilter> smsFilterRegistrationBean(SmsAuthenticationFilter filter) {
		FilterRegistrationBean<SmsAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<QrcodeAuthenticationFilter> qrcodeFilterRegistrationBean(QrcodeAuthenticationFilter filter) {
		FilterRegistrationBean<QrcodeAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<WechatAuthenticationFilter> wechatFilterRegistrationBean(WechatAuthenticationFilter filter) {
		FilterRegistrationBean<WechatAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<WechatMpAuthenticationFilter> wechatMpFilterRegistrationBean(
			WechatMpAuthenticationFilter filter) {
		FilterRegistrationBean<WechatMpAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<GenericAuthenticationFilter> genericFilterRegistrationBean(GenericAuthenticationFilter filter) {
		FilterRegistrationBean<GenericAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<LogoutAuthenticationFilter> logoutFilterRegistrationBean(LogoutAuthenticationFilter filter) {
		FilterRegistrationBean<LogoutAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<DingtalkAuthenticationFilter> dingtalkFilterRegistrationBean(
			DingtalkAuthenticationFilter filter) {
		FilterRegistrationBean<DingtalkAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<GoogleAuthenticationFilter> googleFilterRegistrationBean(GoogleAuthenticationFilter filter) {
		FilterRegistrationBean<GoogleAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<QQAuthenticationFilter> qqFilterRegistrationBean(QQAuthenticationFilter filter) {
		FilterRegistrationBean<QQAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<TwitterAuthenticationFilter> twitterFilterRegistrationBean(TwitterAuthenticationFilter filter) {
		FilterRegistrationBean<TwitterAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<GithubAuthenticationFilter> githubFilterRegistrationBean(GithubAuthenticationFilter filter) {
		FilterRegistrationBean<GithubAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<SinaAuthenticationFilter> sinaFilterRegistrationBean(SinaAuthenticationFilter filter) {
		FilterRegistrationBean<SinaAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	// ==============================
	// Authorizing realm`s
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public GenericAuthorizingRealm genericAuthorizingRealm(GenericCredentialsHashedMatcher matcher) {
		return new GenericAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsAuthorizingRealm smsAuthorizingRealm(SmsCredentialsHashedMatcher matcher) {
		return new SmsAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public QrcodeAuthorizingRealm qrcodeAuthorizingRealm(GenericCredentialsHashedMatcher matcher) {
		return new QrcodeAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public FacebookAuthorizingRealm facebookAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new FacebookAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public WechatAuthorizingRealm wechatAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new WechatAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public WechatMpAuthorizingRealm wechatMpAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new WechatMpAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public DingtalkAuthorizingRealm dingtalkAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new DingtalkAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public GoogleAuthorizingRealm googleAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new GoogleAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public QQAuthorizingRealm qqAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new QQAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public TwitterAuthorizingRealm twitterAuthorizingRealm(
			@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new TwitterAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public SinaAuthorizingRealm sinaAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new SinaAuthorizingRealm(matcher);
	}

	@Bean
	@ConditionalOnMissingBean
	public GithubAuthorizingRealm githubAuthorizingRealm(@Qualifier(BEAN_OAUTH2_MATCHER) Oauth2AuthorizingBoundMatcher matcher) {
		return new GithubAuthorizingRealm(matcher);
	}

	// ==============================
	// Authentication handler's
	// ==============================

	@Bean
	public CentralAuthenticatingHandler centralAuthenticationHandler() {
		return new CentralAuthenticatingHandler();
	}

	@Bean
	public SimpleRcmEvaluatorHandler simpleRiskRecognizerHandler() {
		return new SimpleRcmEvaluatorHandler();
	}

	// ==============================
	// Security verification's
	// ==============================

	/**
	 * {@link com.wl4g.devops.iam.captcha.verification.GifSecurityVerifier}.
	 * {@link com.wl4g.devops.iam.captcha.verification.KaptchaSecurityVerifier}.
	 * {@link com.wl4g.devops.iam.captcha.verification.JigsawSecurityVerifier}.
	 *
	 * @return
	 */
	@Bean
	public CompositeSecurityVerifierAdapter compositeSecurityVerifierAdapter(List<SecurityVerifier> verifiers) {
		return new CompositeSecurityVerifierAdapter(verifiers);
	}

	@Bean
	public SimpleJPEGSecurityVerifier simpleJPEGSecurityVerifier() {
		return new SimpleJPEGSecurityVerifier();
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsSecurityVerifier smsVerification() {
		return new SmsSecurityVerifier();
	}

	@Bean
	@ConditionalOnMissingBean
	public SmsHandleSender smsHandleSender() {
		return new PrintSmsHandleSender();
	}

	// ==============================
	// IAM controller's
	// ==============================

	@Bean
	public CentralAuthenticatingEndpoint centralAuthenticatingEndpoint() {
		return new CentralAuthenticatingEndpoint();
	}

	@Bean
	public PrefixHandlerMapping iamCentralAuthenticatingEndpointPrefixHandlerMapping() {
		return super.newIamControllerPrefixHandlerMapping(URI_S_BASE);
	}

	// ==============================
	// IAM configure's
	// ==============================

	@Bean
	@ConditionalOnMissingBean
	public ServerSecurityCoprocessor serverSecurityCoprocessor() {
		return new AnynothingSecurityCoprocessor();
	}

}