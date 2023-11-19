package top.wml.common.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FriendMsg extends Message{
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;
}
