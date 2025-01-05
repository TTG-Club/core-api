package club.ttg.dnd5.service;

import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailService {
    @Value("${app.url}")
    private String APP_URL;

    private final JavaMailSender mailSender;
    private final OneTimeTokenService oneTimeTokenService;

    public void sendEmail(String recipientAddress, String subject, String message) {
        SimpleMailMessage email = new SimpleMailMessage();

        email.setTo(recipientAddress);
        email.setFrom("TTG Club <support@ttg.club>");
        email.setSubject(subject);
        email.setText(message);

        try {
            mailSender.send(email);
        } catch (MailException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка отправки сообщения на почту");
        }
    }

    public void sendEmailToUsers(List<User> users, String subject, String message) {
        users.stream()
                .map(User::getEmail)
                .forEach(recipientAddress -> sendEmail(recipientAddress, subject, message));
    }

    @Async
    public void confirmEmail(User user) {
        UUID token = oneTimeTokenService.createOneTimeToken(user);

        String subject = "Подтверждение электронной почты";
        String message = "Подтвердите ваш e-mail адрес, перейдя по ссылке:";
        String confirmationUrl = String.format("%s/confirm/email?token=%s", APP_URL, token);

        sendEmail(user.getEmail(), subject, String.format("%s %s", message, confirmationUrl));
    }

    @Async
    public void resetPassword(User user) {
        UUID token = oneTimeTokenService.createOneTimeToken(user);

        String subject = "Сброс пароля";
        String message = "Для сброса пароля перейдите по ссылке и введите новый пароль:";
        String confirmationUrl = String.format("%s/reset/password?token=%s", APP_URL, token);

        sendEmail(user.getEmail(), subject, String.format("%s %s", message, confirmationUrl));
    }
}
