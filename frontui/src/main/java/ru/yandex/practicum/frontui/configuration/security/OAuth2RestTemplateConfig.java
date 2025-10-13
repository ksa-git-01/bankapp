package ru.yandex.practicum.frontui.configuration.security;

import lombok.extern.slf4j.Slf4j;  // ДОБАВИТЬ
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j  // ДОБАВИТЬ
public class OAuth2RestTemplateConfig {

    @Value("${accounts.service.url}")
    private String accountsServiceUrl;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(
            OAuth2AuthorizedClientManager authorizedClientManager) {

        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(accountsServiceUrl)
                .build();

        restTemplate.getInterceptors().add(
                dualTokenInterceptor(authorizedClientManager)
        );

        return restTemplate;
    }

    private ClientHttpRequestInterceptor dualTokenInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return (request, body, execution) -> {
            // ДОБАВИТЬ логи
            log.info("┌─────────────────────────────────────────────────");
            log.info("│ [FRONTUI → KEYCLOAK] Requesting OAuth2 token...");

            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak")
                    .principal(createServiceAccountPrincipal())
                    .build();

            OAuth2AuthorizedClient authorizedClient =
                    authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();
                String tokenPreview = token.substring(0, Math.min(50, token.length())) + "...";

                // ДОБАВИТЬ логи
                log.info("│ [KEYCLOAK → FRONTUI] ✓ OAuth2 token received");
                log.info("│   Token preview: {}", tokenPreview);
                log.info("│   Expires at: {}", authorizedClient.getAccessToken().getExpiresAt());

                request.getHeaders().setBearerAuth(token);

                // ДОБАВИТЬ логи
                log.info("│ [FRONTUI → ACCOUNTS] Sending request");
                log.info("│   Method: {} {}", request.getMethod(), request.getURI());
                log.info("│   With OAuth2 Bearer token");
            } else {
                // ДОБАВИТЬ логи
                log.error("│ [KEYCLOAK → FRONTUI] ✗ Failed to obtain OAuth2 token!");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {

                String userJwt = extractUserJwt(authentication);

                if (userJwt != null) {
                    String jwtPreview = userJwt.substring(0, Math.min(50, userJwt.length())) + "...";
                    // ДОБАВИТЬ логи
                    log.info("│   With User JWT: {}", jwtPreview);
                    request.getHeaders().set("X-User-JWT", userJwt);
                }
            }

            var response = execution.execute(request, body);

            // ДОБАВИТЬ логи
            log.info("│ [ACCOUNTS → FRONTUI] Response: {}", response.getStatusCode());
            log.info("└─────────────────────────────────────────────────");

            return response;
        };
    }

    private String extractUserJwt(Authentication authentication) {
        if (authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }

        if (authentication.getDetails() instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> details =
                    (java.util.Map<String, Object>) authentication.getDetails();
            return (String) details.get("jwt");
        }

        return null;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService
                );

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    private org.springframework.security.core.Authentication createServiceAccountPrincipal() {
        return new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );
    }
}