package codefights.intro;

public class IslandOfKnowledge {

    public static boolean areEquallyStrong(int yourLeft, int yourRight, int friendsLeft, int friendsRight) {
        return (yourLeft == friendsLeft) && (yourRight == friendsRight) || (yourLeft == friendsRight) && (yourRight == friendsLeft);
    }

    public static int arrayMaximalAdjacentDifference(int[] inputArray) {
        int max = 0;
        for (int i = 0; i < inputArray.length - 1; i++) {
            int dif = inputArray[i] - inputArray[i + 1];
            if (dif < 0) dif *= -1;
            if (dif > max)
                max = dif;
        }
        return max;
    }

    public static boolean isIPv4Address(String inputString) {
        String[] split = inputString.split("\\.");
        if (split.length != 4)
            return false;
        for (String s : split) {
            int num;
            try {
                num = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }
            if (num < 0 || num > 255)
                return false;
        }
        return true;
    }

}
