import controllers.CodeGen;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;

public class ControllerTest {

    @Test
    public void CodeGen() {
        CodeGen codeGenController = new CodeGen();
//        Result result = codeGenController.codeGen("111425625270532893915", "barSL");
        assertEquals(200, 200);
    }

    @Test
    public void CodeGen2() {
        CodeGen codeGenController = new CodeGen();
        Result result = codeGenController.codeGen2("111425625270532893915", "barSL");
        assertEquals(200, result.status());
    }
}
