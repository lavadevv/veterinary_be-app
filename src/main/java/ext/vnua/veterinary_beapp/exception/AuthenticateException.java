package ext.vnua.veterinary_beapp.exception;

import lombok.Data;

@Data
public class AuthenticateException extends RuntimeException{
    public AuthenticateException(String mes){
        super(mes);
    }
}
