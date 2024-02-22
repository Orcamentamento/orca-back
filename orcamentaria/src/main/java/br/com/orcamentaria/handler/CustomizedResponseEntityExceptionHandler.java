package br.com.orcamentaria.handler;

import br.com.orcamentaria.exception.ExceptionResponse;
import br.com.orcamentaria.exception.RequiredObjectNotPresentException;
import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request){
        ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDate.now(), "Internal Server Error", request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({RequiredObjectNotPresentException.class, ConstraintViolationException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ExceptionResponse> handleIntegrityViolationExceptions(RuntimeException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDate.now(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionSystemExceptions(RuntimeException ex, WebRequest request) {
        if(ex.getCause() instanceof RollbackException && ex.getCause().getCause() instanceof ConstraintViolationException) {
            ExceptionResponse exceptionResponse = new ExceptionResponse(LocalDate.now(), ex.getCause().getCause().getMessage(), request.getDescription(false));
            return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
        }
        return handleAllExceptions(ex, request);
    }
}
