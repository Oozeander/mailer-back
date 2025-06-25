package com.oozeander.mailer_back.service;

import com.oozeander.mailer_back.dto.MailRequest;
import com.oozeander.mailer_back.exception.SendEmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MailerService {

    private static final String ERROR_MESSAGE = "Error sending email";
    private final JavaMailSender mailSender;

    public void sendFilesByEmail(MailRequest mailRequest, List<MultipartFile> files) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(mailRequest.receivers().toArray(new String[0]));
            helper.setSubject(mailRequest.subject());

            StringBuilder bodyBuilder = new StringBuilder("Contenu des fichiers reçus :\n\n");
            files.forEach(file -> {
                bodyBuilder.append("========================= ").append(file.getOriginalFilename()).append(" =========================\n");
                String content;
                try {
                    content = new String(file.getBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new SendEmailException(ERROR_MESSAGE, e);
                }
                bodyBuilder.append(content).append("\n\n");
            });

            helper.setText(bodyBuilder.toString(), false);

            for (MultipartFile file : files) {
                InputStreamSource source = new ByteArrayResource(file.getBytes());
                helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), source);
            }
        } catch (MessagingException | IOException e) {
            throw new SendEmailException(ERROR_MESSAGE, e);
        }

        mailSender.send(message);
    }
}
