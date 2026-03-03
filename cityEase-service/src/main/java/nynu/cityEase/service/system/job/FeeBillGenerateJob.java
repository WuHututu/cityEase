package nynu.cityEase.service.system.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import nynu.cityEase.service.pms.service.IPmsFeeBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FeeBillGenerateJob {

    @Autowired
    private IPmsFeeBillService feeBillService;

    @XxlJob("generateMonthlyFeeBillJob")
    public void generateMonthlyFeeBill() {
        
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        XxlJobHelper.log("开始自动生成 {} 月份的物业费账单...", currentMonth);
        
        try {
            feeBillService.generateMonthlyBills(currentMonth);
            XxlJobHelper.log("本月物业费账单生成任务执行成功！");
        } catch (Exception e) {
            XxlJobHelper.log("生成账单发生异常: " + e.getMessage());
            throw e;
        }
    }
}