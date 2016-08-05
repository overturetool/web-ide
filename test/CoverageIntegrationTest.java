import controllers.Coverage;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;

public class CoverageIntegrationTest {
    @Test
    public void CodeGen() {
        Coverage controller = new Coverage();
        Result result = controller.coverage("111425625270532893915", "barSL");
        assertEquals(200, result.status());
    }
}
