package com.daroring.fromdarori.domain.newsletter.mail;

import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.eclipse.angus.mail.smtp.SMTPAddressFailedException;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Profile("newsletter-mailpit & !newsletter-ses")
public class SmtpNewsletterMailSender implements NewsletterMailSender {

    private final JavaMailSender sender;
    private final String from;

    public SmtpNewsletterMailSender(JavaMailSender sender, @Value("${darori.newsletter.from}") String from) {
        this.sender = sender;
        this.from = from;
    }

    @Override
    public String send(UUID deliveryId, String recipient, NewsletterMailContent content) throws SendFailure {
        var message = sender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(content.subject());
            helper.setText(content.textBody(), content.htmlBody());
            for (var image : content.inlineImages()) {
                helper.addInline(image.contentId(), new ByteArrayResource(image.bytes()), image.contentType());
            }
            message.setHeader("X-Newsletter-Delivery-Id", deliveryId.toString());
        } catch (MessagingException exception) {
            throw new SendFailure(FailureKind.PERMANENT, "Invalid email message");
        }

        try {
            sender.send(message);
        } catch (MailAuthenticationException exception) {
            throw new SendFailure(FailureKind.PERMANENT, "SMTP authentication rejected");
        } catch (MailSendException exception) {
            // A connection failure before any message was submitted is safe to retry.
            if (exception.getFailedMessages().isEmpty() && exception.getCause() instanceof
                    org.eclipse.angus.mail.util.MailConnectException) {
                throw new SendFailure(FailureKind.RETRYABLE, "SMTP connection could not be established");
            }
            for (var failure : exception.getMessageExceptions()) {
                for (Exception current = failure; current instanceof MessagingException smtp;
                     current = smtp.getNextException()) {
                    int code;
                    if (smtp instanceof SMTPSendFailedException reply) {
                        code = reply.getReturnCode();
                    } else if (smtp instanceof SMTPAddressFailedException reply) {
                        code = reply.getReturnCode();
                    } else {
                        continue;
                    }
                    if (code >= 400 && code < 600) {
                        throw new SendFailure(code < 500 ? FailureKind.RETRYABLE : FailureKind.PERMANENT,
                                "SMTP rejected message with status " + code);
                    }
                }
            }
            throw new SendFailure(FailureKind.UNKNOWN, "SMTP acceptance could not be confirmed");
        } catch (MailException exception) {
            throw new SendFailure(FailureKind.UNKNOWN, "SMTP acceptance could not be confirmed");
        }
        try {
            return message.getMessageID();
        } catch (MessagingException exception) {
            return null;
        }
    }
}
