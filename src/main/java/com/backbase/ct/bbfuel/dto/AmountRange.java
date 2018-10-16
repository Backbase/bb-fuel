package com.backbase.ct.bbfuel.dto;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmountRange {

    @Setter
    private Integer min;

    @Setter
    private Integer max;

    private Integer numberInRange;

    public int getNumberInRange() {
        if (this.numberInRange == null) {
            this.numberInRange = generateRandomNumberInRange(this.min, this.max);
        }

        return numberInRange;
    }

    public void setNumberInRange(int number) {
        if (number < this.min || number > this.max ) {
            throw new IllegalArgumentException(String.format(
                "Number must be between min (%s) and max (%s)", this.min, this.max));
        }

        this.numberInRange = number;
    }

}
