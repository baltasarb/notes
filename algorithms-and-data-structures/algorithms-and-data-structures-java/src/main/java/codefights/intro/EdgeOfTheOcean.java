package codefights.intro;

public class EdgeOfTheOcean {

    public static int adjacentElementsProduct(int[] inputArray) {
        int temp, maxValue = Integer.MIN_VALUE;
        for (int i = 0; i < (inputArray.length - 1); i++) {
            temp = inputArray[i] * inputArray[i + 1];
            if (temp > maxValue)
                maxValue = temp;
        }
        return maxValue;
    }

    public static int makeArrayConsecutive2(int[] statues) {
        int min = 20, max = 0;

        for (int currentElement : statues) {
            if (currentElement > max)
                max = currentElement;
            if (currentElement < min)
                min = currentElement;
        }

        return (max - min) - statues.length + 1;
    }

    public static int matrixElementsSum(int[][] matrix) {
        boolean isHaunted = false;
        int i, j, sum;
        i = j = sum = 0;

        for (int k = 0; k < (matrix[0].length * matrix.length); k++) {
            int currentRoom = matrix[i][j];
            if (currentRoom == 0)
                isHaunted = true;
            if (!isHaunted)
                sum += currentRoom;
            if (i == matrix.length - 1) {
                i = -1;
                j++;
                isHaunted = false;
            }
            i++;
        }
        return sum;
    }

    public static boolean almostIncreasingSequence(int[] sequence) {
        int left, right;

        for (int i = 0; i < sequence.length - 1; i++) {
            left = sequence[i];
            right = sequence[i + 1];
            if (left >= right) {
                if (i == 0)
                    return isIncreasingSequence(sequence, i);
                return isIncreasingSequence(sequence, sequence[i - 1] < right ? i : i + 1);
            }
        }
        return true;
    }

    private static boolean isIncreasingSequence(int[] sequence, int toSkip) {
        int seq_length = toSkip == sequence.length - 1 ? sequence.length - 2 : sequence.length - 1;
        for (int i = 0; i < seq_length; i++) {
            if (i == toSkip)
                continue;
            int nextIdx = i + 1 == toSkip ? i + 2 : i + 1;
            if (sequence[i] >= sequence[nextIdx])
                return false;
        }
        return true;
    }

    public static int shapeArea(int n) {
        return calculateArea(n);
    /*
    int result = 1;
    while(n-->1)
        result += n*4;
    return result;
    */
    }

    private static int calculateArea(int n) {
        return n > 1 ? ((n - 1) * 4) + calculateArea(n - 1) : 1;
    }

}
