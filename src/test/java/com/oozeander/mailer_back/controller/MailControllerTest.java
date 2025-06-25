package com.oozeander.mailer_back.controller;

import com.oozeander.mailer_back.exception.SendEmailException;
import com.oozeander.mailer_back.service.MailerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MailControllerTest {

    private static final String BASE_URL = "http://localhost:%d/api/v1/mailer";
    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @MockitoBean
    private MailerService mailerService;

    @Test
    void shouldSendMailSuccessfully() throws Exception {
        // Arrange
        List<String> receivers = List.of("a@test.com", "b@test.com");
        String subject = "Test";

        Resource file1 = new ByteArrayResource("hello file 1".getBytes()) {
            @Override
            public String getFilename() {
                return "file1.txt";
            }
        };

        Resource file2 = new ByteArrayResource("hello file 2".getBytes()) {
            @Override
            public String getFilename() {
                return "file2.txt";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file1);
        body.add("files", file2);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // Construire l'URL avec query params encodés
        String receiversParams = receivers.stream()
                .map(r -> "receivers=" + URLEncoder.encode(r, StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        String urlWithParams = String.format(BASE_URL, port) + "?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8) + "&" + receiversParams;

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(new URI(urlWithParams), entity, String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mailerService, times(1)).sendFilesByEmail(any(), any());
    }

    @Test
    void shouldReturn400WhenMailerFails() throws Exception {
        doThrow(SendEmailException.class).when(mailerService).sendFilesByEmail(any(), any());

        List<String> receivers = List.of("fail@test.com");
        String subject = "Échec";

        Resource file = new ByteArrayResource("fichier".getBytes()) {
            @Override
            public String getFilename() {
                return "fail.txt";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        String receiversParams = receivers.stream()
                .map(r -> "receivers=" + URLEncoder.encode(r, StandardCharsets.UTF_8))
                .reduce((a, b) -> a + "&" + b)
                .orElse("");

        String urlWithParams = String.format(BASE_URL, port) + "?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8) + "&" + receiversParams;

        ResponseEntity<String> response = restTemplate.postForEntity(new URI(urlWithParams), entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400AndListAllValidationErrors() throws Exception {
        Resource emptyFile = new ByteArrayResource(new byte[0]) {
            @Override
            public String getFilename() {
                return "empty.txt";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", emptyFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        String urlWithParams = String.format(BASE_URL, port)
                + "?subject="
                + "&receivers=invalid-email"
                + "&receivers=another-invalid-email";

        ResponseEntity<String> response = restTemplate.postForEntity(new URI(urlWithParams), entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        String bodyString = response.getBody();
        assertThat(bodyString).contains("Le subject ne peut pas être vide");
        assertThat(bodyString).contains("Email invalide");
    }
}