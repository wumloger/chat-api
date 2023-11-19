package top.wml.common.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupMsg extends Message{
    @JsonSerialize(using = ToStringSerializer.class)
    private Long groupId;

    private String fromUserNickname;

    private String fromUserAvatar;

}
