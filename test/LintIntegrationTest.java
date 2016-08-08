import controllers.Lint;
import org.junit.Test;
import play.mvc.Result;

import static org.junit.Assert.assertEquals;

public class LintIntegrationTest {

    @Test
    public void Lint() {
        Lint controller = new Lint();
        Result result = controller.file("111425625270532893915", "BOMSL");
        assertEquals(200, result.status());
    }
}
