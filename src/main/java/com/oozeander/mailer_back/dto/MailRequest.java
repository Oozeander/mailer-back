package com.oozeander.mailer_back.dto;

import java.util.List;

public record MailRequest(
        String subject,
        List<String> receivers
) {
}
