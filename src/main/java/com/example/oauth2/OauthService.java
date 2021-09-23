package com.example.oauth2;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class OauthService {

    private final InMemoryProviderRepository inMemoryProviderRepository;

    public OauthService(InMemoryProviderRepository inMemoryProviderRepository) {
        this.inMemoryProviderRepository = inMemoryProviderRepository;
    }

    public LoginResponse login(String providerName, String code) {
        // 프론트에서 넘어온 provider 이름을 통해 InMemoryProviderRepository에서 OauthProvider 가져오기
        OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);

        // access token 가져오기
        OauthTokenResponse tokenResponse = getToken(code, provider);

        // TODO 유저 정보 가져오기
        // TODO 유저 DB에 저장
        return null;
    }

    private OauthTokenResponse getToken(String code, OauthProvider provider) {
        return WebClient.create()
                        .post()
                        .uri(provider.getTokenUrl())
                        .headers(header -> {
                            header.setBasicAuth(provider.getClientId(), provider.getClientSecret());
                            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                            header.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                            header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                        })
                        .bodyValue(tokenRequest(code, provider))
                        .retrieve()
                        .bodyToMono(OauthTokenResponse.class)
                        .block();
    }

    private MultiValueMap<String, String> tokenRequest(String code, OauthProvider provider) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", provider.getRedirectUrl());
        return formData;
    }
}
