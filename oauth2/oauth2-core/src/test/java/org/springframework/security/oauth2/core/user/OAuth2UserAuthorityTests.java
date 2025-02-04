/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.security.oauth2.core.user;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link OAuth2UserAuthority}.
 *
 * @author Joe Grandja
 */
public class OAuth2UserAuthorityTests {

	private static final String AUTHORITY = "ROLE_USER";

	private static final Map<String, Object> ATTRIBUTES = Collections.singletonMap("username", "test");

	@Test
	public void constructorWhenAuthorityIsNullThenThrowIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OAuth2UserAuthority(null, ATTRIBUTES));
	}

	@Test
	public void constructorWhenAttributesIsNullThenThrowIllegalArgumentException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OAuth2UserAuthority(AUTHORITY, null));
	}

	@Test
	public void constructorWhenAttributesIsEmptyThenThrowIllegalArgumentException() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new OAuth2UserAuthority(AUTHORITY, Collections.emptyMap()));
	}

	@Test
	public void constructorWhenAllParametersProvidedAndValidThenCreated() {
		OAuth2UserAuthority userAuthority = new OAuth2UserAuthority(AUTHORITY, ATTRIBUTES);
		assertThat(userAuthority.getAuthority()).isEqualTo(AUTHORITY);
		assertThat(userAuthority.getAttributes()).isEqualTo(ATTRIBUTES);
	}

}
