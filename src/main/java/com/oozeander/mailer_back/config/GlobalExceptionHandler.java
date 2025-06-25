package com.oozeander.mailer_back.config;

import com.oozeander.mailer_back.exception.SendEmailException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException ex) {
        var errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Erreur de validation");
        problemDetail.setDetail(errors);

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler({SendEmailException.class})
    public ProblemDetail handleSendEmailException(SendEmailException exception) {
        return buildProblemDetail(exception);
    }

    private ProblemDetail buildProblemDetail(SendEmailException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Erreur à l'envoi du mail");
        problemDetail.setDetail(exception.getMessage());
        problemDetail.setProperty("exception", exception.getCause());
        return problemDetail;
    }
}
