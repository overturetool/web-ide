import controllers.CodeGen;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;

public class CodeGenIntegrationTest {

    @Test
    public void CodeGen() {
        CodeGen codeGenController = new CodeGen();
        Result result = codeGenController.codeGen("111425625270532893915", "BOMSL");
        assertEquals(200, result.status());
    }

    @Test
    public void CodeGen2() {
        CodeGen codeGenController = new CodeGen();
        Result result = codeGenController.codeGen2("111425625270532893915", "looseSL");
        assertEquals(200, result.status());
    }
}
