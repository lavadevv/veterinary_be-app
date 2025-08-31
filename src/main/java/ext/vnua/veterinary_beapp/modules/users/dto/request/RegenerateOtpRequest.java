package ext.vnua.veterinary_beapp.modules.users.dto.request;

import ext.vnua.veterinary_beapp.common.Constant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegenerateOtpRequest {
    @Email(regexp = ".+[@].+[\\.].+",message = Constant.ErrMessageUserValidation.EMAIL_VALIDATE)
    @NotBlank(message = Constant.ErrMessageUserValidation.EMAIL_NOT_BLANK)
    private String email;
}
