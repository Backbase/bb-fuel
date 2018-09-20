package com.backbase.ct.dataloader.dto;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataGroupCollection {

    private String generalEurId;

    private String generalUsdId;

    private String amsterdamId;

    private String portlandId;

    private String vancouverId;

    private String londonId;

    private String internationalTradeId;

    private String financeInternationalId;

    private String payrollId;

    public List<String> getDataGroupIds() {
        return asList(
            this.getGeneralEurId(),
            this.getGeneralUsdId(),
            this.getAmsterdamId(),
            this.getPortlandId(),
            this.getVancouverId(),
            this.getLondonId(),
            this.getInternationalTradeId(),
            this.getFinanceInternationalId(),
            this.getPayrollId())
            .parallelStream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
