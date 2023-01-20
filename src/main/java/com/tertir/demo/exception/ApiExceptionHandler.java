package com.tertir.demo.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(BadRequestException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                e, badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException,badRequest);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleApiRequestException(NotFoundException e){
        HttpStatus notFound = HttpStatus.NOT_FOUND;

        ApiException apiException = new ApiException(
                e.getMessage(),
                e, notFound,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException,notFound);
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleApiRequestException(ConflictException e){
        HttpStatus notFound = HttpStatus.CONFLICT;

        ApiException apiException = new ApiException(
                e.getMessage(),
                e, notFound,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException,notFound);
    }


    @ExceptionHandler(value = {NotAuthorized.class})
    public ResponseEntity<Object> handleApiRequestException(NotAuthorized e){
        HttpStatus notFound = HttpStatus.UNAUTHORIZED;

        ApiException apiException = new ApiException(
                e.getMessage(),
                e, notFound,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException,notFound);
    }

    @ExceptionHandler(value = {QrCodeGenerationException.class})
    public ResponseEntity<Object> handleApiRequestException(QrCodeGenerationException e){
        HttpStatus serverError = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiException apiException = new ApiException(
                e.getMessage(),
                e, serverError,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException,serverError);
    }

    @ExceptionHandler(value = {PdfGenerationException.class})
    public ResponseEntity<Object> handleApiRequestException(PdfGenerationException e){
        HttpStatus serverError = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiException apiException = new ApiException(
                e.getMessage(),
                e, serverError,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(apiException,serverError);
    }


    /**
     * Exception thrown when {@link org.springframework.validation.annotation.Validated} is used in controller.
     * @param ex
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {

        try {
            HttpStatus badRequest = HttpStatus.BAD_REQUEST;
            //List<String> messages = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());

            ApiException apiException = new ApiException(
                    ex.getMessage(),
                    ex, badRequest,
                    ZonedDateTime.now(ZoneId.of("Z"))
            );

            return new ResponseEntity<>(apiException,badRequest);

        } catch (Exception e) {

            ApiException apiException = new ApiException(
                    ex.getMessage(),
                    ex, HttpStatus.INTERNAL_SERVER_ERROR,
                    ZonedDateTime.now(ZoneId.of("Z"))
            );
            return new ResponseEntity<>(apiException, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
