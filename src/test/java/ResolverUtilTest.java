import org.apache.ibatis.io.VFS;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by wb-whz291815 on 2017/7/7.
 */
public class ResolverUtilTest {


    @Test
    public void test() throws IOException {
        List<String> children = VFS.getInstance().list("com/whz");
        System.out.println(children);

        String alias = Object.class.getSimpleName();
        System.out.println(alias);
    }



}
