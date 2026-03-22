package com.cricform.service;

import com.cricform.dto.external.ExternalPlayerProfileDTO;
import com.cricform.dto.ScorecardDTO;
import com.cricform.dto.SeriesDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CricketApiService {

    @Qualifier("rapidApiRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${rapidapi.baseurl}")
    private String baseUrl;

    public List<SeriesDTO> fetchRecentInternationalSeries() {
        rateLimit();
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                baseUrl + "/series/v1/international",
                JsonNode.class
        );

        JsonNode root = response.getBody();
        List<SeriesDTO> seriesList = new ArrayList<>();
        if (root == null) {
            return seriesList;
        }

        JsonNode map = root.path("seriesMapProto");
        if (map.isMissingNode() || map.isNull()) {
            return seriesList;
        }

        map.fields().forEachRemaining(entry -> {
            JsonNode wrappers = entry.getValue().path("series");
            if (wrappers.isArray()) {
                wrappers.forEach(wrapper -> {
                    JsonNode node = wrapper.path("seriesAdWrapper");
                    if (node.isMissingNode() || node.isNull()) {
                        return;
                    }

                    SeriesDTO dto = SeriesDTO.builder()
                            .seriesId(node.path("seriesId").asLong())
                            .seriesName(node.path("seriesName").asText(null))
                            .seriesType(node.path("seriesType").asText(null))
                            .build();

                    JsonNode matches = node.path("matches");
                    if (matches.isArray()) {
                        matches.forEach(match -> {
                            if (match.hasNonNull("matchInfo")) {
                                dto.getMatchIds().add(match.path("matchInfo").path("matchId").asLong());
                            } else if (match.hasNonNull("matchId")) {
                                dto.getMatchIds().add(match.path("matchId").asLong());
                            }
                        });
                    }

                    if (!dto.getMatchIds().isEmpty()) {
                        seriesList.add(dto);
                    }
                });
            }
        });

        return seriesList;
    }

    public ScorecardDTO fetchMatchScorecard(Long matchId) {
        rateLimit();
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                baseUrl + "/mcenter/v1/" + matchId + "/scard",
                JsonNode.class
        );

        return ScorecardDTO.builder()
                .matchId(matchId)
                .payload(response.getBody())
                .build();
    }

    public ExternalPlayerProfileDTO fetchPlayerProfile(Long playerId) {
        rateLimit();
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                baseUrl + "/stats/v1/player/" + playerId,
                JsonNode.class
        );

        JsonNode body = response.getBody();
        if (body == null) {
            return ExternalPlayerProfileDTO.builder().id(playerId).build();
        }

        return ExternalPlayerProfileDTO.builder()
                .id(body.path("id").asLong(playerId))
                .name(body.path("name").asText(null))
                .role(body.path("role").asText(null))
                .teamId(body.path("teamId").isMissingNode() ? null : body.path("teamId").asLong())
                .teamName(body.path("teamName").asText(null))
                .build();
    }

    public List<Long> fetchPlayerMatchIds(Long playerId, String type) {
        rateLimit();
        String safeType = "bowling".equalsIgnoreCase(type) ? "bowling" : "batting";
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                baseUrl + "/stats/v1/player/" + playerId + "/" + safeType,
                JsonNode.class
        );

        List<Long> matchIds = new ArrayList<>();
        JsonNode body = response.getBody();
        if (body == null) {
            return matchIds;
        }

        JsonNode values = body.path("values");
        if (values.isArray()) {
            values.forEach(node -> {
                if (node.hasNonNull("matchId")) {
                    matchIds.add(node.path("matchId").asLong());
                }
            });
        }

        return matchIds;
    }

    private void rateLimit() {
        try {
            Thread.sleep(500L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Rate limiter sleep interrupted", ie);
        }
    }
}
