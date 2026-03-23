package nynu.cityEase.api.vo.pms;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BindAuditPageVO {
    private Long id;
    private Long relId;
    private String ownerName;
    private String phone;
    private String roomInfo;
    private Integer status;
    private List<String> attachmentsList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;
}
