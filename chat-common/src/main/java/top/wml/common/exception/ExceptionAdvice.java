package top.wml.common.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import top.wml.common.resp.CommonResp;

/**
 * 统一异常处理
 */
@ControllerAdvice
@ResponseBody
public class ExceptionAdvice {

    @ExceptionHandler(BusinessException.class)
    public CommonResp handleException(BusinessException e) {
        CommonResp response = new CommonResp();
        response.fail(e.getMessage());
        return response;
    }
}
