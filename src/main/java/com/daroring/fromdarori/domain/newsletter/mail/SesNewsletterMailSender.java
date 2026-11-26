package com.daroring.fromdarori.domain.newsletter.mail;

import java.util.UUID;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Attachment;
import software.amazon.awssdk.services.sesv2.model.AttachmentContentDisposition;
import software.amazon.awssdk.services.sesv2.model.AttachmentContentTransferEncoding;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.MessageTag;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

final class SesNewsletterMailSender implements NewsletterMailSender {

    private final SesV2Client client;
    private final String from;
    private final String testRecipient;

    SesNewsletterMailSender(SesV2Client client, String from, String testRecipient) {
        Assert.hasText(from, "SES requires a newsletter sender address");
        Assert.hasText(testRecipient, "SES requires a verified sandbox recipient");
        this.client = client;
        this.from = from.strip();
        this.testRecipient = testRecipient.strip();
    }

    @Override
    public String recipientFilter() {
        return testRecipient;
    }

    @Override
    public String send(UUID deliveryId, String recipient, NewsletterMailContent content) throws SendFailure {
        if (!testRecipient.equals(recipient)) {
            throw new SendFailure(FailureKind.PERMANENT, "Recipient is outside the SES sandbox test scope");
        }

        var attachments = content.inlineImages().stream().map(image -> Attachment.builder()
                .fileName(image.contentId().equals("darori-logo") ? "darori-logo.png" : image.contentId())
                .contentId(image.contentId())
                .contentType(image.contentType())
                .contentDisposition(AttachmentContentDisposition.INLINE)
                .contentTransferEncoding(AttachmentContentTransferEncoding.BASE64)
                .rawContent(SdkBytes.fromByteArray(image.bytes()))
                .build()).toList();
        var request = SendEmailRequest.builder()
                .fromEmailAddress(from)
                .destination(destination -> destination.toAddresses(recipient))
                .emailTags(MessageTag.builder().name("delivery_id").value(deliveryId.toString()).build())
                .content(email -> email.simple(message -> message.subject(utf8(content.subject()))
                        .body(body -> body.html(utf8(content.htmlBody())).text(utf8(content.textBody())))
                        .attachments(attachments)))
                .build();
        try {
            return client.sendEmail(request).messageId();
        } catch (SesV2Exception exception) {
            int status = exception.statusCode();
            var kind = status == 429 ? FailureKind.RETRYABLE
                    : status >= 400 && status < 500 && status != 408 ? FailureKind.PERMANENT : FailureKind.UNKNOWN;
            String code = exception.awsErrorDetails() == null ? "" : " (" + exception.awsErrorDetails().errorCode() + ")";
            throw new SendFailure(kind, "SES returned HTTP " + status + code);
        } catch (SdkClientException exception) {
            throw new SendFailure(FailureKind.UNKNOWN, "SES request acceptance could not be confirmed");
        }
    }

    private static Content utf8(String value) {
        return Content.builder().charset("UTF-8").data(value).build();
    }
}
