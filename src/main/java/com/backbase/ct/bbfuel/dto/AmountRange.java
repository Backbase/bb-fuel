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

    private Integer randomNumberInRange;

    public int getRandomNumberInRange() {
        if (this.randomNumberInRange == null) {
            this.randomNumberInRange = generateRandomNumberInRange(this.min, this.max);
        }

        return randomNumberInRange;
    }

    public void setRandomNumberInRange(int randomNumber) {
        if (randomNumber < this.min || randomNumber > this.max ) {
            throw new IllegalArgumentException(String.format(
                "Number must be between min (%s) and max (%s)", this.min, this.max));
        }

        this.randomNumberInRange = randomNumber;
    }

}
