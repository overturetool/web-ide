import controllers.Lint;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;

public class LintIntegrationTest {

    @Test
    public void Lint() {
        Lint lintController = new Lint();
        Result result = lintController.file("111425625270532893915", "BOMSL");
        assertEquals(200, result.status());
    }
}
