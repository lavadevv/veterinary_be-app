package ext.vnua.veterinary_beapp.exception;

import lombok.Data;

@Data
public class DataExistException extends RuntimeException{
    public DataExistException(String message){
        super(message);
    }
}
