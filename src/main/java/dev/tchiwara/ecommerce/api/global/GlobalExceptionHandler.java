package dev.tchiwara.ecommerce.api.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,   String>> handleValidationErrors(
            MethodArgumentNotValidException exception
    ){
        var errors=new HashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error->{
            errors.put(error.getField(),error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,   String>> handleResourceNotFound(
            ResourceNotFoundException exception
    ){
        var error=new HashMap<String, String>();
        error.put("message",exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var error = new HashMap<String, String>();
        error.put("message", "Invalid value '" + ex.getValue() + "' for parameter: " + ex.getName());
        return ResponseEntity.badRequest().body(error);
    }

}
