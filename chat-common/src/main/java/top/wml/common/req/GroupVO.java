package top.wml.common.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.wml.common.entity.Friend;
import top.wml.common.entity.User;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupVO {
    private String name;

    private String avatar;

    private String intro;

    private List<Friend> members;

}
