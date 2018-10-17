import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

public class ListTests {

    @Test
    public void removeElementThatDoesNotExist(){
        LinkedList<Integer> list = new LinkedList<>();
        boolean expectedResult = false;

        boolean result = list.remove(new Integer(1));

        Assert.assertEquals(expectedResult, result);
    }
}
