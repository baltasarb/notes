package codefights.intro;

public class TheJourneyBegins {

    public static int add(int num1, int num2) {
        return num1 + num2;
    }

    public static int centuryFromYear(int year) {
        if (year < 101)
            return 1;
        if (year < 201)
            return 2;
        int n = year / 100;

        return year % n == 0 ? n : n + 1;
    }

    public static boolean checkPalindrome(String inputString) {
        StringBuilder sb = new StringBuilder();
        for (int i = inputString.length() - 1; i >= 0; i--)
            sb.append(inputString.charAt(i));
        return sb.toString().equals(inputString);
    }

}
