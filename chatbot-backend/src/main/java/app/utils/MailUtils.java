package app.utils;

import java.util.Random;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import app.exception.ResouceLimitException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailUtils {
	public static void sendEmail(JavaMailSender javaMailSender, String to, String subject, String text) {
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(text, true);
			javaMailSender.send(mimeMessage);
		} catch (Exception e) {
			StackTraceElement[] err = e.getStackTrace();
			for (StackTraceElement stackTraceElement : err) {
				log.error(stackTraceElement.toString());
			}
			throw new ResouceLimitException(
					"The server email sending limit has been exceeded! Please try again tomorrow.");
		}
	}

	public static String generateRandomPassword() {
		Random random = new Random();

		String PASSWORD_ALLOW_BASE = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&";
		Integer PASSWORD_LENGTH = 16;

		String password = "";

		for (int i = 0; i < PASSWORD_LENGTH; i++) {
			int rndCharAt = random.nextInt(PASSWORD_ALLOW_BASE.length());
			char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
			password += rndChar;
		}

		return password;
	}
}
