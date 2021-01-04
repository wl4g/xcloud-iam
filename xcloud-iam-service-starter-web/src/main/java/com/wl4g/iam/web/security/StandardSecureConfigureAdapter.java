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
package com.wl4g.iam.web.security;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import com.wl4g.iam.configure.SecureConfig;
import com.wl4g.iam.configure.SecureConfigureAdapter;

/**
 * Define security configuration adapter (security signature algorithm
 * configuration, etc.)
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年10月16日
 * @since
 */
@Component
public class StandardSecureConfigureAdapter implements SecureConfigureAdapter, InitializingBean {

	@Autowired
	protected ConfigurableEnvironment environment;

	private SecureConfig secureConfig;

	@Override
	public void afterPropertiesSet() throws Exception {
		String appName = environment.getRequiredProperty("spring.application.name");
		String envFlag = getApplicationActiveEnvironmentFlag();
		String privateSalt = appName.concat(envFlag);
		this.secureConfig = new SecureConfig(new String[] { "MD5", "SHA-256", "SHA-384", "SHA-512" }, privateSalt, 5,
				2 * 60 * 60 * 1000L, 3 * 60 * 1000L);
	}

	@Override
	public SecureConfig configure() {
		return secureConfig;
	}

	private String getApplicationActiveEnvironmentFlag() {
		String active = environment.getRequiredProperty("spring.profiles.active");

		Set<String> envFlags = new HashSet<>();
		Matcher matcher = Pattern.compile("dev|fat|uat|pro").matcher(active);
		while (matcher.find()) {
			envFlags.add(matcher.group());
		}
		if (envFlags.size() != 1) {
			throw new IllegalStateException(
					format("Unable initialization secure configuration. Ambiguous environments flag. - %s", active));
		}
		return envFlags.iterator().next();
	}

}