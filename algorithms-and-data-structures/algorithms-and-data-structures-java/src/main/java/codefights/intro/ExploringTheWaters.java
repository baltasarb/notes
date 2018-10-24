package codefights.intro;

public class ExploringTheWaters {

    public static String[] addBorder(String[] picture) {
        String res[] = new String[picture.length + 2];

        for (int i = 0; i < picture.length; i++)
            res[i + 1] = String.format("*%s*", picture[i]);

        res[0] = "";

        for (int i = 0; i < res[1].length(); i++)
            res[0] += "*";

        res[res.length - 1] = res[0];

        return res;
    }

    public static int[] alternatingSums(int[] a) {
        int res[] = new int[2];
        for (int i = 0; i < a.length; i++)
            res[i % 2] += a[i];
        return res;
    }

    public static boolean areSimilar(int[] a, int[] b) {
        int idx = 0;
        int[] differentElementsIdx = new int[2];

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                if (idx < 2)
                    differentElementsIdx[idx] = i;
                idx++;
            }
        }

        return a[differentElementsIdx[0]] == b[differentElementsIdx[1]]
                && a[differentElementsIdx[1]] == b[differentElementsIdx[0]]
                && idx <= 2;
    }

    public static int arrayChange(int[] inputArray) {
        int counter = 0;
        for (int i = 0; i < inputArray.length - 1; i++) {
            while (inputArray[i] >= inputArray[i + 1]) {
                inputArray[i + 1]++;
                counter++;
            }
        }
        return counter;
    }

    public static boolean palindromeRearranging(String inputString) {
        int abc[] = new int[26];
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.toLowerCase().charAt(i);
            if (c < 'a' || c > 'z')
                continue;
            abc[c - 'a']++;
        }

        boolean oddNumberFound = false;

        for (int letter : abc) {
            if (letter % 2 != 0) {
                if (oddNumberFound)
                    return false;
                oddNumberFound = true;
            }
        }
        return true;
    }

}
