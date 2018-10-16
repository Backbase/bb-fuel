package com.backbase.ct.bbfuel.dto;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmountRange {

    private Integer min;

    private Integer max;

    public int getRandomNumberInRange() {
        return generateRandomNumberInRange(this.min, this.max);
    }

}
