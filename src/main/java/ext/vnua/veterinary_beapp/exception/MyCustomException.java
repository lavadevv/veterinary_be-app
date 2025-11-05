package ext.vnua.veterinary_beapp.exception;

public class MyCustomException extends RuntimeException{
    public MyCustomException(String mes){
        super(mes);
    }
    
    public MyCustomException(String mes, Throwable cause){
        super(mes, cause);
    }
}
