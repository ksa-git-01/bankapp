package ru.yandex.practicum.accounts.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class OAuth2RestTemplateConfig {

    private final RestTemplateBuilder restTemplateBuilder;

    public OAuth2RestTemplateConfig(
            RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    public RestTemplate restTemplate(
            OAuth2AuthorizedClientManager authorizedClientManager) {
        return restTemplateBuilder
                .additionalInterceptors(dualTokenInterceptor(authorizedClientManager))
                .build();
    }

    private ClientHttpRequestInterceptor dualTokenInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return (request, body, execution) -> {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId("keycloak")
                    .principal(createServiceAccountPrincipal())
                    .build();

            OAuth2AuthorizedClient authorizedClient =
                    authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();

                request.getHeaders().setBearerAuth(token);
            } else {
                log.error("Failed to obtain OAuth2 token!");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {

                String userJwt = extractUserJwt(authentication);

                if (userJwt != null) {
                    request.getHeaders().set("X-User-JWT", userJwt);
                }
            }
            return execution.execute(request, body);
        };
    }

    private String extractUserJwt(Authentication authentication) {
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

    private Authentication createServiceAccountPrincipal() {
        return new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );
    }
}