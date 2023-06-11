package ru.practicum.ewmstatisticsclient;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:stats-application.properties")
public class StatsClientImpl implements StatsClient {

    @Value("${statistics-server.url}")
    private String statServerUrl;

    private final WebClient webClient = WebClient.builder()
            .baseUrl(statServerUrl)
            .build();

    @Override
    public void save(String app, String uri, String ip, LocalDateTime localDateTime) {
        EndPointHitDto endPointHitDto = EndPointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(localDateTime)
                .build();

        webClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(endPointHitDto))
                .retrieve()
                .bodyToMono(EndPointHitDto.class)
                .block();
    }

    @Override
    public List<ViewStatDto> get(String start, String end, List<String> uris, Boolean unique) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats");
        uriBuilder.queryParam("start", start);
        uriBuilder.queryParam("end", end);

        if (!Objects.isNull(uris) && !uris.isEmpty()) {
            String params = String.join(",", uris);
            uriBuilder.queryParam("uris", params);
        }

        if (!Objects.isNull(unique)) {
            uriBuilder.queryParam("unique", unique);
        }

        URI uri = uriBuilder.build().toUri();

        return webClient.get()
                .uri(String.valueOf(uri))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ViewStatDto.class)
                .collectList()
                .block();
    }
}
