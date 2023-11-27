package com.jung.planet.admin.service;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.slack.api.webhook.WebhookPayloads.payload;

@Service
public class SlackNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SlackNotificationService.class);

    @Value("${slack.err.url}")
    private String errWebhookUrl;

    @Value("${slack.report.url}")
    private String reportWebhookUrl;

    Slack slackClient = Slack.getInstance();

    private List<Field> generateFieldsFromMap(Map<String, String> mapData) {
        return mapData.entrySet().stream()
                .map(entry -> generateSlackField(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public void sendSlackOkNotification(String message, Map<String, String> additionalData) {
        List<Field> fields = generateFieldsFromMap(additionalData);

        try {
            slackClient.send(errWebhookUrl, payload(p -> p
                    .text("성공 🥳: " + message)
                    .attachments(
                            List.of(
                                    generateSlackOkAttachment(fields.toArray(new Field[0]))
                            )
                    )
            ));
        } catch (IOException slackError) {
            logger.debug("Slack 통신과의 예외 발생");
        }
    }

    public void sendSlackErrorNotification(String message, HttpServletRequest request) {

        try {
            slackClient.send(errWebhookUrl, payload(p -> p
                    .text("서버 에러 !! 😡")
                    .attachments(
                            List.of(generateSlackErrorAttachment(message, request))
                    )
            ));
        } catch (IOException slackError) {
            logger.debug("Slack 통신과의 예외 발생");
        }
    }

    private Attachment generateSlackErrorAttachment(String errorMsg, HttpServletRequest request) {
        String requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(LocalDateTime.now());
        String xffHeader = request.getHeader("X-FORWARDED-FOR");
        return Attachment.builder()
                .color("ff0000")
                .title(requestTime + " 발생 에러 로그")
                .fields(List.of(
                                generateSlackField("Request IP", xffHeader == null ? request.getRemoteAddr() : xffHeader),
                                generateSlackField("Request URL", request.getRequestURL() + " " + request.getMethod()),
                                generateSlackField("Error Message", errorMsg)
                        )
                )
                .build();
    }

    private Attachment generateSlackOkAttachment(Field... fields) {
        String requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(LocalDateTime.now());
        return Attachment.builder()
                .color("36a64f")
                .title(requestTime + " 성공 🥳")
                .fields(Arrays.asList(fields))
                .build();
    }


    private Field generateSlackField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(false)
                .build();
    }


    public void sendSlackReportNotification(String message, Map<String, String> additionalData) {
        List<Field> fields = generateFieldsFromMap(additionalData);

        try {
            slackClient.send(reportWebhookUrl, payload(p -> p
                    .text("신고 접수 💣 : " + message)
                    .attachments(
                            List.of(
                                    generateSlackReportAttachment(fields.toArray(new Field[0]))
                            )
                    )
            ));
        } catch (IOException slackError) {
            logger.debug("Slack 통신과의 예외 발생");
        }
    }


    private Attachment generateSlackReportAttachment(Field... fields) {
        String requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(LocalDateTime.now());
        return Attachment.builder()
                .color("ff0000")
                .title(requestTime + " 신고 접수 ⏰")
                .fields(Arrays.asList(fields))
                .build();
    }
}

