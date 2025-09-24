package ext.vnua.veterinary_beapp.common;

import ext.vnua.veterinary_beapp.exception.EmailException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailUtil {
    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            mimeMessage.setHeader("Content-Type", "text/html; charset=UTF-8");
            mimeMessage.setHeader("Content-Transfer-Encoding", "quoted-printable");
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("The Pro - Mã xác thực tài khoản");
            mimeMessageHelper.setText("""
        <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; background-color: #ffffff;">
            <!-- Header -->
            <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                <div style="display: inline-block; background-color: rgba(255,255,255,0.1); padding: 15px 25px; border-radius: 50px; margin-bottom: 15px;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 700; letter-spacing: 1px;">THE PRO</h1>
                </div>
                <p style="color: rgba(255,255,255,0.9); margin: 0; font-size: 16px; font-weight: 300;">Nền tảng dịch vụ chuyên nghiệp</p>
            </div>
            
            <!-- Content -->
            <div style="padding: 40px 30px;">
                <div style="text-align: center; margin-bottom: 35px;">
                    <h2 style="color: #2c3e50; margin: 0 0 10px 0; font-size: 24px; font-weight: 600;">Xác thực tài khoản</h2>
                    <p style="color: #7f8c8d; margin: 0; font-size: 16px; line-height: 1.5;">Vui lòng nhập mã xác thực bên dưới để hoàn tất thiết lập tài khoản</p>
                </div>
                
                <!-- OTP Container -->
                <div style="background: linear-gradient(135deg, #f8f9ff 0%%, #e8edff 100%%); border-radius: 12px; padding: 35px 25px; text-align: center; margin: 30px 0; border: 1px solid #e1e8f7;">
                    <p style="color: #5a6c7d; margin: 0 0 15px 0; font-size: 14px; text-transform: uppercase; letter-spacing: 1px; font-weight: 500;">MÃ XÁC THỰC</p>
                    
                    <div style="background-color: #ffffff; border: 2px solid #667eea; border-radius: 8px; padding: 20px; margin: 15px auto; display: inline-block; box-shadow: 0 2px 10px rgba(102, 126, 234, 0.1);">
                        <span style="font-size: 32px; font-weight: 700; color: #667eea; letter-spacing: 4px; font-family: 'Courier New', monospace;">%s</span>
                    </div>
                    
                    <div style="margin-top: 20px;">
                        <p style="color: #e74c3c; font-size: 14px; margin: 0; font-weight: 500;">
                            Mã có hiệu lực trong <strong>2 phút</strong>
                        </p>
                    </div>
                </div>
                
                <!-- Security Notice -->
                <div style="background-color: #f8f9fa; border-left: 4px solid #17a2b8; padding: 20px; border-radius: 0 8px 8px 0; margin: 30px 0;">
                    <h4 style="color: #17a2b8; margin: 0 0 10px 0; font-size: 16px;">Thông báo bảo mật</h4>
                    <p style="color: #6c757d; margin: 0; font-size: 14px; line-height: 1.5;">
                        Mã này được yêu cầu để bảo mật tài khoản của bạn. Tuyệt đối không chia sẻ mã này với bất kỳ ai. 
                        Nếu bạn không yêu cầu xác thực này, vui lòng bỏ qua email này.
                    </p>
                </div>
            </div>
            
            <!-- Footer -->
            <div style="background-color: #2c3e50; padding: 30px; text-align: center;">
                <p style="color: #bdc3c7; margin: 0 0 10px 0; font-size: 14px;">
                    Cần hỗ trợ? Liên hệ đội ngũ hỗ trợ tại <a href="mailto:support@thepro.com" style="color: #667eea; text-decoration: none;">support@thepro.com</a>
                </p>
                <hr style="border: none; height: 1px; background-color: #34495e; margin: 20px 0;">
                <p style="color: #95a5a6; margin: 0; font-size: 12px;">
                    © 2025 The Pro. Bảo lưu mọi quyền. | Nền tảng dịch vụ chuyên nghiệp
                </p>
            </div>
        </div>
        """.formatted(otp), true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new EmailException(e.getMessage());
        }
    }
}
