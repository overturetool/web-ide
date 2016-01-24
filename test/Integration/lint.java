package Integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

public class lint {

    private static String hostWithPort = "http://localhost:9000";

    @Test
    public void lintReturnsCorrectNumberOfObjects() {
        ValidatableResponse response = get(hostWithPort + "/lint/test/test_ws/bom.vdmsl").then().contentType(ContentType.JSON);
        response.body("size()", equalTo(4));
        response.body("parserWarnings.size()", equalTo(0));
        response.body("parserErrors.size()", equalTo(0));
        response.body("typeCheckerWarnings.size()", equalTo(11));
        response.body("typeCheckerErrors.size()", equalTo(0));
    }

    @Test
    public void lintResponseWithStatusCode200GivenPathToDirectory() {
        get(hostWithPort + "/lint/test/test_ws").then().statusCode(200);
    }

    @Test
    public void lintResponseWithStatusCode422AndMessageFileNotFound() {
        ValidatableResponse response = get(hostWithPort + "/lint/test/test_ws/non_existing_file.vdmsl").then();
        response.statusCode(422);
        response.body(equalTo("File not found"));
    }
}
