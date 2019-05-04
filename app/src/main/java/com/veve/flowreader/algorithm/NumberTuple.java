package com.veve.flowreader.algorithm;

import java.util.Arrays;

public class NumberTuple {

    private int[] numbers;

    public NumberTuple(int[] numbers) {
        this.numbers = new int[4];
        System.arraycopy(numbers, 0, this.numbers, 0,4);
    }

    public int get(int i) {
        return numbers[i];
    }

    public NumberTuple change(int pos, int val) {
        switch (pos) {
            case 0:
                int[] newNumbers = new int[] {val, numbers[1], numbers[2], numbers[3]};
                return new NumberTuple(newNumbers);
            case 1:
                newNumbers = new int[] {numbers[0], val, numbers[2], numbers[3]};
                return new NumberTuple(newNumbers);
            case 2:
                newNumbers = new int[] {numbers[0], numbers[1], val, numbers[3]};
                return new NumberTuple(newNumbers);
            case 3:
                newNumbers = new int[] {numbers[0], numbers[1], numbers[2], val};
                return new NumberTuple(newNumbers);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberTuple that = (NumberTuple) o;
        return Arrays.equals(numbers, that.numbers);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(numbers);
    }
}
