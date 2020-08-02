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
package com.wl4g.devops.iam.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.wl4g.devops.iam.client.config.IamClientAutoConfiguration;
import com.wl4g.devops.iam.common.config.CorsAutoConfiguration;
import com.wl4g.devops.iam.common.config.ReplayAutoConfiguration;
import com.wl4g.devops.iam.common.config.XsrfAutoConfiguration;
import com.wl4g.devops.iam.common.config.XssAutoConfiguration;

/**
 * When enabled, all requests go through spring.cloud.devops . iam.client.filter
 * -The interceptor specified by chains will be intercepted and authenticated
 * unless it is a filter of type anon.</br>
 * 
 * Note: it is mutually exclusive with {@link EnableIamClientTest}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018-12-20
 * @since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Import({ IamClientAutoConfiguration.class, CorsAutoConfiguration.class, XsrfAutoConfiguration.class, XssAutoConfiguration.class,
		ReplayAutoConfiguration.class })
public @interface EnableIamClient {

}