package top.wml.common.resp;

import lombok.Data;

/**
 * 通用返回对象
 *
 * @param <T> 泛型
 */
@Data
public class CommonResp<T> {
    private String code;
    private String msg;
    private T data;

    /**
     * 构造一个成功的统一返回对象
     * @param data 数据
     * @return 构建成功的 CommonResp 对象
     */
    public void success(T data){
        this.code = "200";
        this.msg = "成功";
        this.data = data;
    }

    /**
     * 创建一个失败的统一返回对象
     *
     * @param msg 失败信息
     * @return CommonResp对象
     */
    public void fail(String msg){
        this.code = "500";
        this.msg = msg;
    }
}
