/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Luke Taylor
 * @since 2.0
 */
@ContextConfiguration(locations = { "/sec-936-app-context.xml" })
@ExtendWith(SpringExtension.class)
public class SEC936ApplicationContextTests {

	/**
	 * SessionRegistry is used as the test service interface (nothing to do with the test)
	 */
	@Autowired
	private SessionRegistry sessionRegistry;

	@Test
	public void securityInterceptorHandlesCallWithNoTargetObject() {
		SecurityContextHolder.getContext()
			.setAuthentication(UsernamePasswordAuthenticationToken.unauthenticated("bob", "bobspassword"));
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(this.sessionRegistry::getAllPrincipals);
	}

}
