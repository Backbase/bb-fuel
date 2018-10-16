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
public class Amount {

    private Integer min;

    private Integer max;

    private Integer random;

    public int getRandom() {
        this.random = generateRandomNumberInRange(this.min, this.max);

        return random;
    }

    public void setRandom(int random) {
        this.random = random;
    }

}
