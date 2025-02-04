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

package org.springframework.security.web.server.csrf;

import java.security.cert.X509Certificate;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.SslInfo;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Eric Deandrea
 * @author Thomas Vitale
 * @author Alonso Araya
 * @since 5.1
 */
public class CookieServerCsrfTokenRepositoryTests {

	private CookieServerCsrfTokenRepository csrfTokenRepository;

	private MockServerHttpRequest.BaseBuilder<?> request;

	private String expectedHeaderName = CookieServerCsrfTokenRepository.DEFAULT_CSRF_HEADER_NAME;

	private String expectedParameterName = CookieServerCsrfTokenRepository.DEFAULT_CSRF_PARAMETER_NAME;

	private Duration expectedMaxAge = Duration.ofSeconds(-1);

	private String expectedDomain = null;

	private String expectedPath = "/";

	private boolean expectedSecure = false;

	private boolean expectedHttpOnly = true;

	private String expectedCookieName = CookieServerCsrfTokenRepository.DEFAULT_CSRF_COOKIE_NAME;

	private String expectedCookieValue = "csrfToken";

	@BeforeEach
	public void setUp() {
		this.csrfTokenRepository = new CookieServerCsrfTokenRepository();
		this.request = MockServerHttpRequest.get("/someUri");
	}

	@Test
	public void generateTokenWhenDefaultThenDefaults() {
		generateTokenAndAssertExpectedValues();
	}

	@Test
	public void generateTokenWhenCustomHeaderThenCustomHeader() {
		setExpectedHeaderName("someHeader");
		generateTokenAndAssertExpectedValues();
	}

	@Test
	public void generateTokenWhenCustomParameterThenCustomParameter() {
		setExpectedParameterName("someParam");
		generateTokenAndAssertExpectedValues();
	}

	@Test
	public void generateTokenWhenCustomHeaderAndParameterThenCustomHeaderAndParameter() {
		setExpectedHeaderName("someHeader");
		setExpectedParameterName("someParam");
		generateTokenAndAssertExpectedValues();
	}

	@Test
	public void saveTokenWhenNoSubscriptionThenNotWritten() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.csrfTokenRepository.saveToken(exchange, createToken());
		assertThat(exchange.getResponse().getCookies().getFirst(this.expectedCookieName)).isNull();
	}

	@Test
	public void saveTokenWhenDefaultThenDefaults() {
		saveAndAssertExpectedValues(createToken());
	}

	@Test
	public void saveTokenWhenNullThenDeletes() {
		saveAndAssertExpectedValues(null);
	}

	@Test
	public void saveTokenWhenHttpOnlyFalseThenHttpOnlyFalse() {
		setExpectedHttpOnly(false);
		saveAndAssertExpectedValues(createToken());
	}

	@Test
	public void saveTokenWhenCookieMaxAgeThenCookieMaxAge() {
		setExpectedCookieMaxAge(3600);
		saveAndAssertExpectedValues(createToken());
	}

	@Test
	public void saveTokenWhenCustomPropertiesThenCustomProperties() {
		setExpectedDomain("spring.io");
		setExpectedCookieName("csrfCookie");
		setExpectedPath("/some/path");
		setExpectedHeaderName("headerName");
		setExpectedParameterName("paramName");
		setExpectedCookieMaxAge(3600);
		saveAndAssertExpectedValues(createToken());
	}

	@Test
	public void saveTokenWhenSslInfoPresentThenSecure() {
		this.request.sslInfo(new MockSslInfo());
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.csrfTokenRepository.saveToken(exchange, createToken()).block();
		ResponseCookie cookie = exchange.getResponse().getCookies().getFirst(this.expectedCookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isSecure()).isTrue();
	}

	@Test
	public void saveTokenWhenSslInfoNullThenNotSecure() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.csrfTokenRepository.saveToken(exchange, createToken()).block();
		ResponseCookie cookie = exchange.getResponse().getCookies().getFirst(this.expectedCookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isSecure()).isFalse();
	}

	@Test
	public void saveTokenWhenSecureFlagTrueThenSecure() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.csrfTokenRepository.setSecure(true);
		this.csrfTokenRepository.saveToken(exchange, createToken()).block();
		ResponseCookie cookie = exchange.getResponse().getCookies().getFirst(this.expectedCookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isSecure()).isTrue();
	}

	@Test
	public void saveTokenWhenSecureFlagFalseThenNotSecure() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.csrfTokenRepository.setSecure(false);
		this.csrfTokenRepository.saveToken(exchange, createToken()).block();
		ResponseCookie cookie = exchange.getResponse().getCookies().getFirst(this.expectedCookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isSecure()).isFalse();
	}

	@Test
	public void saveTokenWhenSecureFlagFalseAndSslInfoThenNotSecure() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.request.sslInfo(new MockSslInfo());
		this.csrfTokenRepository.setSecure(false);
		this.csrfTokenRepository.saveToken(exchange, createToken()).block();
		ResponseCookie cookie = exchange.getResponse().getCookies().getFirst(this.expectedCookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.isSecure()).isFalse();
	}

	@Test
	public void loadTokenWhenCookieExistThenTokenFound() {
		loadAndAssertExpectedValues();
	}

	@Test
	public void loadTokenWhenCustomThenTokenFound() {
		setExpectedParameterName("paramName");
		setExpectedHeaderName("headerName");
		setExpectedCookieName("csrfCookie");
		saveAndAssertExpectedValues(createToken());
	}

	@Test
	public void loadTokenWhenNoCookiesThenNullToken() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		CsrfToken csrfToken = this.csrfTokenRepository.loadToken(exchange).block();
		assertThat(csrfToken).isNull();
	}

	@Test
	public void loadTokenWhenCookieExistsWithNoValue() {
		setExpectedCookieValue("");
		loadAndAssertExpectedValues();
	}

	@Test
	public void loadTokenWhenCookieExistsWithNullValue() {
		setExpectedCookieValue(null);
		loadAndAssertExpectedValues();
	}

	private void setExpectedHeaderName(String expectedHeaderName) {
		this.csrfTokenRepository.setHeaderName(expectedHeaderName);
		this.expectedHeaderName = expectedHeaderName;
	}

	private void setExpectedParameterName(String expectedParameterName) {
		this.csrfTokenRepository.setParameterName(expectedParameterName);
		this.expectedParameterName = expectedParameterName;
	}

	private void setExpectedDomain(String expectedDomain) {
		this.csrfTokenRepository.setCookieDomain(expectedDomain);
		this.expectedDomain = expectedDomain;
	}

	private void setExpectedPath(String expectedPath) {
		this.csrfTokenRepository.setCookiePath(expectedPath);
		this.expectedPath = expectedPath;
	}

	private void setExpectedHttpOnly(boolean expectedHttpOnly) {
		this.expectedHttpOnly = expectedHttpOnly;
		this.csrfTokenRepository.setCookieHttpOnly(expectedHttpOnly);
	}

	private void setExpectedCookieName(String expectedCookieName) {
		this.expectedCookieName = expectedCookieName;
		this.csrfTokenRepository.setCookieName(expectedCookieName);
	}

	private void setExpectedCookieMaxAge(int expectedCookieMaxAge) {
		this.csrfTokenRepository.setCookieMaxAge(expectedCookieMaxAge);
		this.expectedMaxAge = Duration.ofSeconds(expectedCookieMaxAge);
	}

	private void setExpectedCookieValue(String expectedCookieValue) {
		this.expectedCookieValue = expectedCookieValue;
	}

	private void loadAndAssertExpectedValues() {
		MockServerHttpRequest.BodyBuilder request = MockServerHttpRequest.post("/someUri")
			.cookie(new HttpCookie(this.expectedCookieName, this.expectedCookieValue));
		MockServerWebExchange exchange = MockServerWebExchange.from(request);
		CsrfToken csrfToken = this.csrfTokenRepository.loadToken(exchange).block();
		if (StringUtils.hasText(this.expectedCookieValue)) {
			assertThat(csrfToken).isNotNull();
			assertThat(csrfToken.getHeaderName()).isEqualTo(this.expectedHeaderName);
			assertThat(csrfToken.getParameterName()).isEqualTo(this.expectedParameterName);
			assertThat(csrfToken.getToken()).isEqualTo(this.expectedCookieValue);
		}
		else {
			assertThat(csrfToken).isNull();
		}
	}

	private void saveAndAssertExpectedValues(CsrfToken token) {
		if (token == null) {
			this.expectedMaxAge = Duration.ofSeconds(0);
			this.expectedCookieValue = "";
		}
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		this.csrfTokenRepository.saveToken(exchange, token).block();
		ResponseCookie cookie = exchange.getResponse().getCookies().getFirst(this.expectedCookieName);
		assertThat(cookie).isNotNull();
		assertThat(cookie.getMaxAge()).isEqualTo(this.expectedMaxAge);
		assertThat(cookie.getDomain()).isEqualTo(this.expectedDomain);
		assertThat(cookie.getPath()).isEqualTo(this.expectedPath);
		assertThat(cookie.isSecure()).isEqualTo(this.expectedSecure);
		assertThat(cookie.isHttpOnly()).isEqualTo(this.expectedHttpOnly);
		assertThat(cookie.getName()).isEqualTo(this.expectedCookieName);
		assertThat(cookie.getValue()).isEqualTo(this.expectedCookieValue);
	}

	private void generateTokenAndAssertExpectedValues() {
		MockServerWebExchange exchange = MockServerWebExchange.from(this.request);
		CsrfToken csrfToken = this.csrfTokenRepository.generateToken(exchange).block();
		assertThat(csrfToken).isNotNull();
		assertThat(csrfToken.getHeaderName()).isEqualTo(this.expectedHeaderName);
		assertThat(csrfToken.getParameterName()).isEqualTo(this.expectedParameterName);
		assertThat(csrfToken.getToken()).isNotBlank();
	}

	private CsrfToken createToken() {
		return createToken(this.expectedHeaderName, this.expectedParameterName, this.expectedCookieValue);
	}

	private static CsrfToken createToken(String headerName, String parameterName, String tokenValue) {
		return new DefaultCsrfToken(headerName, parameterName, tokenValue);
	}

	static class MockSslInfo implements SslInfo {

		@Override
		public String getSessionId() {
			return "sessionId";
		}

		@Override
		public X509Certificate[] getPeerCertificates() {
			return new X509Certificate[] {};
		}

	}

}
