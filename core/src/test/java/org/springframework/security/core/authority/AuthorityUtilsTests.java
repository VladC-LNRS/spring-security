/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.security.core.authority;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luke Taylor
 */
public class AuthorityUtilsTests {

	@Test
	public void commaSeparatedStringIsParsedCorrectly() {
		List<GrantedAuthority> authorityArray = AuthorityUtils
			.commaSeparatedStringToAuthorityList(" ROLE_A, B, C, ROLE_D\n,\n E ");
		Set<String> authorities = AuthorityUtils.authorityListToSet(authorityArray);
		assertThat(authorities.contains("B")).isTrue();
		assertThat(authorities.contains("C")).isTrue();
		assertThat(authorities.contains("E")).isTrue();
		assertThat(authorities.contains("ROLE_A")).isTrue();
		assertThat(authorities.contains("ROLE_D")).isTrue();
	}

}
