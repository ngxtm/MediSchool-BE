package com.medischool.backend.util.format.error;


import com.medischool.backend.dto.response.RestResponseDTO;
import com.medischool.backend.util.format.error.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value={CustomException.class})
    public ResponseEntity<RestResponseDTO<Object>> customException(CustomException e) {
        RestResponseDTO<Object> restResponseException = new RestResponseDTO<>();
        restResponseException.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponseException.setErrorMessage(e.getMessage());
        restResponseException.setMessage("Exception !");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponseException);
    }


}