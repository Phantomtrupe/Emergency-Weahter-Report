package com.github.phantomtrupe.alertservice.client;

import com.github.phantomtrupe.commons.dto.UserDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(WebClient userServiceWebClient) {
        this.webClient = userServiceWebClient;
    }

    public Flux<UserDTO> getUsersByCity(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users")
                        .queryParam("city", city)
                        .build())
                .retrieve()
                .bodyToFlux(UserDTO.class);
    }
}
