import controllers.Linting;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;

public class LintIntegrationTest {

    @Test
    public void Lint() {
        Linting controller = new Linting();
        Result result = controller.file("111425625270532893915", "BOMSL");
        assertEquals(200, result.status());
    }
}
