package com.oozeander.mailer_back.controller;

import com.oozeander.mailer_back.dto.MailRequest;
import com.oozeander.mailer_back.service.MailerService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("mailer")
@RequiredArgsConstructor
public class MailController {

    private final MailerService mailService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> sendMail(
            @NotEmpty(message = "Le subject ne peut pas être vide")
            @RequestParam("subject") String subject,
            @NotEmpty(message = "Tu dois fournir au moins 1 destinataire")
            @RequestParam("receivers") List<@Email(message = "Email invalide") String> receivers,
            @NotEmpty(message = "Aucun fichier ajouté")
            @RequestParam("files") List<MultipartFile> files
    ) {
        mailService.sendFilesByEmail(new MailRequest(subject, receivers), files);
        return ResponseEntity.ok().build();
    }
}
