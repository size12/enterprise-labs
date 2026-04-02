package com.lab1.jms;

import com.lab1.dto.WelcomeEmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationConsumer {

    @Autowired(required = false) // Опционально, если нет реальной почты
    private JavaMailSender mailSender;

    @JmsListener(destination = "${app.queue.email}", containerFactory = "queueListenerFactory")
    public void receiveWelcomeEmail(WelcomeEmailMessage message) {
        log.info("Получено сообщение для отправки email: {}", message);

        try {
            // Имитация долгой операции (отправка email может занимать время)
            Thread.sleep(2000);

            // Реальная отправка email (если настроено)
            if (mailSender != null) {
                sendActualEmail(message);
            } else {
                log.info("[ИМИТАЦИЯ] Приветственное письмо отправлено на адрес: {}", message.getEmail());
            }

            log.info("Email успешно обработан для клиента ID: {}", message.getCustomerId());

        } catch (InterruptedException e) {
            log.error("Ошибка при отправке email: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Ошибка при отправке email: {}", e.getMessage());
            // Исключение приведёт к повторной попытке (если настроено)
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendActualEmail(WelcomeEmailMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(message.getEmail());
        mailMessage.setSubject("Добро пожаловать в наш сервис!");
        mailMessage.setText(String.format(
            "Уважаемый %s!\n\nСпасибо за регистрацию в нашем сервисе. " +
            "Ваш аккаунт успешно создан.\n\nС уважением, команда поддержки.",
            message.getFirstName()
        ));
        mailSender.send(mailMessage);
    }
}
