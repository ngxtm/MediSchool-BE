package com.medischool.backend.util.format;
import com.medischool.backend.dto.response.RestResponseDTO;
import com.medischool.backend.util.annotation.ApiMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;// format all response
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = httpServletResponse.getStatus();
        RestResponseDTO<Object> restResponse = new RestResponseDTO();
        restResponse.setStatusCode(status);
        if(body instanceof String){
            return body;
        }
        if(status>=400){
            return body;
        }else{
            restResponse.setData(body);
            ApiMessage message=returnType.getMethodAnnotation(ApiMessage.class);
            restResponse.setMessage(message!=null?message.value():"Success !");
        }
        return restResponse;
    }
}