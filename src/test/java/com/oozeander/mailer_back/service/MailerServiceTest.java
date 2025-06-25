package com.oozeander.mailer_back.service;

import com.oozeander.mailer_back.dto.MailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailerServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailerService mailerService;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @Test
    void shouldSendEmailWithMultipleFilesAndReceivers() throws MessagingException, IOException {
        // Given
        MimeMessage mimeMessage = new MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        List<String> receivers = List.of("user1@example.com", "user2@example.com");
        String subject = "Test Subject";

        MailRequest request = new MailRequest(subject, receivers);

        MockMultipartFile file1 = new MockMultipartFile("file", "doc.txt", "text/plain", "Bonjour".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "report.csv", "text/csv", "id,name\n1,Alice".getBytes());

        // When
        mailerService.sendFilesByEmail(request, List.of(file1, file2));

        // Then
        verify(mailSender).send(mimeMessageCaptor.capture());

        MimeMessage captured = mimeMessageCaptor.getValue();
        assertThat(captured).isNotNull();
        assertThat(captured.getAllRecipients()).hasSize(2);
        assertThat(captured.getSubject()).isEqualTo("Test Subject");

        MimeMultipart content = (MimeMultipart) captured.getContent();
        var bodyPart = content.getBodyPart(0);
        Object partContent = bodyPart.getContent();

        String bodyText;
        if (partContent instanceof String str) {
            bodyText = str;
        } else if (partContent instanceof MimeMultipart nestedMultipart) {
            bodyText = (String) nestedMultipart.getBodyPart(0).getContent();
        } else {
            throw new IllegalStateException("Type de contenu inattendu : " + partContent.getClass());
        }

        assertThat(bodyText).contains("==== doc.txt ====");
        assertThat(bodyText).contains("Bonjour");
        assertThat(bodyText).contains("==== report.csv ====");
        assertThat(bodyText).contains("id,name\n1,Alice");
    }
}