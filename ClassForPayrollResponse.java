package account;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassForPayrollResponse {
    private String name;
    private String lastname;
    private String period;
    private String salary;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Map<String, String> months = new HashMap<>() {{
    put("01", "January");put("02", "February");put("03", "March");put("04", "April");put("05", "May");put("06", "June");
    put("07", "July");put("08", "August");put("09", "September");put("10", "October");put("11", "November");put("12", "December");
    }};
    public void setPeriod(String period) {
        String[] parts = period.split("-");
        String month = "";
        for (Map.Entry<String, String> stringStringEntry : months.entrySet()) {
            if (parts[0].equals(stringStringEntry.getKey())) {
                month = stringStringEntry.getValue();
            }
        }
        this.period = month + "-" + parts[1];
    }

    public void setSalary(long salary) {
        this.salary = salary/100 + " dollar(s) " + salary%100 + " cent(s)";
    }
}
