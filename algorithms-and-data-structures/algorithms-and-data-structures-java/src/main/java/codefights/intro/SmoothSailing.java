package codefights.intro;

import java.util.ArrayList;
import java.util.Arrays;

public class SmoothSailing {

    public static String[] allLongestStrings(String[] inputArray) {
        int max = 0;
        ArrayList<String> list = new ArrayList<>();
        for (String currentString : inputArray) {
            int currentLength = currentString.length();
            if (currentLength > max) {
                max = currentLength;
                list = new ArrayList<>();
            }
            if (currentLength == max)
                list.add(currentString);
        }
        return list.toArray(new String[list.size()]);
    }

    public static int commonCharacterCount(String s1, String s2) {
        char[] c1 = s1.toCharArray();
        char[] c2 = s2.toCharArray();
        int sum = 0;
        for (char currentChar : c1) {
            for (int j = 0; j < c2.length; j++) {
                if (currentChar == c2[j]) {
                    sum++;
                    c2[j] = '\0';
                    break;
                }
            }
        }
        return sum;
    }

    public static boolean isLucky(int n) {
        String str = String.valueOf(n);
        int num1, num2;
        num1 = num2 = 0;

        for (Character c : str.substring(0, str.length() / 2).toCharArray())
            num1 += (c - '0');

        for (Character c : str.substring(str.length() / 2, str.length()).toCharArray())
            num2 += (c - '0');

        return num1 == num2;
    }

    public static String reverseParentheses(String s) {
        int rightPIdx = s.indexOf(')');

        if (rightPIdx == -1)
            return s;

        int leftPIdx = rightPIdx;

        for (; s.charAt(leftPIdx) != '(' && leftPIdx > 0; leftPIdx--)
            ;

        s = s.replace(
                s.substring(leftPIdx, rightPIdx + 1),
                new StringBuilder(s.substring(leftPIdx + 1, rightPIdx)).reverse().toString()
        );

        return reverseParentheses(s);
    }

    public static int[] sortByHeight(int[] a) {
        int[] temp = Arrays.copyOf(a, a.length);
        Arrays.sort(temp);

        int j = 0;
        for (; j < a.length; j++)
            if (temp[j] != -1)
                break;

        for (int i = 0; i < a.length; i++) {
            if (a[i] == -1)
                continue;
            a[i] = temp[j++];
        }

        return a;
    }

}
