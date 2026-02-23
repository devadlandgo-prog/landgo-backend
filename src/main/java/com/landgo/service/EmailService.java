package com.landgo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@landgo.com}")
    private String fromEmail;

    @Value("${app.mail.reset-password-url:http://localhost:3000/reset-password}")
    private String resetPasswordBaseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String userName, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("LandGo - Verify Your Email Address");
            helper.setText(buildVerificationEmailHtml(userName, code), true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        try {
            String resetLink = resetPasswordBaseUrl + "?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("LandGo - Password Reset Request");
            helper.setText(buildResetEmailHtml(userName, resetLink), true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildResetEmailHtml(String userName, String resetLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        .header { background-color: #1B5E20; padding: 30px; text-align: center; }
                        .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                        .header p { color: #C8E6C9; margin: 5px 0 0; font-size: 14px; }
                        .body { padding: 40px 30px; }
                        .body h2 { color: #333333; margin-top: 0; }
                        .body p { color: #555555; line-height: 1.6; }
                        .btn { display: inline-block; background-color: #1B5E20; color: #ffffff; text-decoration: none; padding: 14px 40px; border-radius: 8px; font-size: 16px; font-weight: bold; margin: 20px 0; }
                        .btn:hover { background-color: #2E7D32; }
                        .footer { background-color: #f9f9f9; padding: 20px 30px; text-align: center; font-size: 12px; color: #999999; }
                        .warning { background-color: #FFF3E0; border-left: 4px solid #FF9800; padding: 12px 16px; margin: 20px 0; border-radius: 4px; }
                        .link-text { word-break: break-all; font-size: 12px; color: #888888; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>LandGo</h1>
                            <p>Find. Build. Grow.</p>
                        </div>
                        <div class="body">
                            <h2>Hi %s,</h2>
                            <p>We received a request to reset the password for your LandGo account. Click the button below to set a new password:</p>
                            <p style="text-align: center;">
                                <a href="%s" class="btn">Reset Password</a>
                            </p>
                            <div class="warning">
                                <strong>⏰ This link expires in 30 minutes.</strong><br>
                                If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.
                            </div>
                            <p>If the button doesn't work, copy and paste this link into your browser:</p>
                            <p class="link-text">%s</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 LandGo. All rights reserved.</p>
                            <p>This is an automated email. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, resetLink, resetLink);
    }

    private String buildVerificationEmailHtml(String userName, String code) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        .header { background-color: #1B5E20; padding: 30px; text-align: center; }
                        .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                        .header p { color: #C8E6C9; margin: 5px 0 0; font-size: 14px; }
                        .body { padding: 40px 30px; }
                        .body h2 { color: #333333; margin-top: 0; }
                        .body p { color: #555555; line-height: 1.6; }
                        .code-box { text-align: center; margin: 30px 0; }
                        .code { display: inline-block; background-color: #E8F5E9; color: #1B5E20; font-size: 36px; font-weight: bold; letter-spacing: 8px; padding: 16px 32px; border-radius: 12px; border: 2px dashed #4CAF50; }
                        .footer { background-color: #f9f9f9; padding: 20px 30px; text-align: center; font-size: 12px; color: #999999; }
                        .warning { background-color: #FFF3E0; border-left: 4px solid #FF9800; padding: 12px 16px; margin: 20px 0; border-radius: 4px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>LandGo</h1>
                            <p>Find. Build. Grow.</p>
                        </div>
                        <div class="body">
                            <h2>Hi %s,</h2>
                            <p>Welcome to LandGo! Please verify your email address by entering the following 6-digit code:</p>
                            <div class="code-box">
                                <span class="code">%s</span>
                            </div>
                            <div class="warning">
                                <strong>⏰ This code expires in 15 minutes.</strong><br>
                                If you didn't create a LandGo account, you can safely ignore this email.
                            </div>
                            <p>Once verified, you'll have full access to all LandGo features including listing properties and managing your portfolio.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 LandGo. All rights reserved.</p>
                            <p>This is an automated email. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, code);
    }

    @Async
    public void sendSavedSearchNotification(String toEmail, String userName, String searchName, long newMatchCount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("LandGo - " + newMatchCount + " New Match" + (newMatchCount > 1 ? "es" : "") + " for \"" + searchName + "\"");
            helper.setText(buildSavedSearchNotificationHtml(userName, searchName, newMatchCount), true);

            mailSender.send(message);
            log.info("Saved search notification email sent to: {} for search: '{}'", toEmail, searchName);
        } catch (MessagingException e) {
            log.error("Failed to send saved search notification to: {}", toEmail, e);
        }
    }

    private String buildSavedSearchNotificationHtml(String userName, String searchName, long newMatchCount) {
        String matchText = newMatchCount == 1
                ? "1 new listing matches"
                : newMatchCount + " new listings match";

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                        .header { background-color: #1B5E20; padding: 30px; text-align: center; }
                        .header h1 { color: #ffffff; margin: 0; font-size: 28px; }
                        .header p { color: #C8E6C9; margin: 5px 0 0; font-size: 14px; }
                        .body { padding: 40px 30px; }
                        .body h2 { color: #333333; margin-top: 0; }
                        .body p { color: #555555; line-height: 1.6; }
                        .highlight { background-color: #E8F5E9; border-left: 4px solid #4CAF50; padding: 16px 20px; margin: 20px 0; border-radius: 4px; }
                        .highlight strong { color: #1B5E20; font-size: 18px; }
                        .btn { display: inline-block; background-color: #1B5E20; color: #ffffff; text-decoration: none; padding: 14px 40px; border-radius: 8px; font-size: 16px; font-weight: bold; margin: 20px 0; }
                        .footer { background-color: #f9f9f9; padding: 20px 30px; text-align: center; font-size: 12px; color: #999999; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>LandGo</h1>
                            <p>Find. Build. Grow.</p>
                        </div>
                        <div class="body">
                            <h2>Hi %s,</h2>
                            <p>Great news! We found new listings that match your saved search.</p>
                            <div class="highlight">
                                <strong>🔍 "%s"</strong><br>
                                <p style="margin: 8px 0 0;">%s your saved search criteria.</p>
                            </div>
                            <p style="text-align: center;">
                                <a href="https://landgo.com/saved-searches" class="btn">View Matches</a>
                            </p>
                            <p style="font-size: 13px; color: #888;">You're receiving this because you have notifications enabled for this saved search. You can disable notifications from your profile settings.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 LandGo. All rights reserved.</p>
                            <p>This is an automated email. Please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(userName, searchName, matchText);
    }
}
