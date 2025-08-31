package ext.vnua.veterinary_beapp.common;

public interface Constant {
    interface ErrorMessageAuthValidation{
        String WRONG_USERNAME_OR_PASSWORD="Sai tài khoản hoặc mật khẩu";
    }

    interface ErrMessageUserValidation{
        String EMAIL_VALIDATE="Email không hợp lệ";
        String EMAIL_NOT_BLANK="Email không được để trống";
        String OTP_NOT_BLANK = "OTP không được để trống";
    }

    interface SortType {
        String DESC = "DESC";
        String ASC = "ASC";
    }
}