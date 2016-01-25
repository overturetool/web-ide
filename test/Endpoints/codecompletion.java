package Endpoints;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

public class codecompletion {
    private static String hostWithPort = "http://localhost:9000";

    @Test
    public void codecompletionReturnCorrectNumberOfObjects() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/bom.vdmsl?line=20&column=1")
                .then().contentType(ContentType.JSON);

        response.statusCode(200);
        response.body("size()", equalTo(3));
    }

    @Test
    public void codecompletionResponseWithStatusCode404GivenPathToDirectory() {
        get(hostWithPort + "/codecompletion/test/test_ws").then().statusCode(404);
    }

    @Test
    public void codecompletionResponseWithStatusCode422AndMessageFileNotFound() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/non_existing_file.vdmsl?line=20&column=1").then();
        response.statusCode(422);
        response.body(equalTo("File not found"));
    }

    @Test
    public void codecompletionResponseWithStatusCode422AndErrorMessageOnMissingLineValue() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/non_existing_file.vdmsl?line=&column=1").then();
        response.statusCode(422);
        response.body(equalTo("Invalid query argument format"));
    }

    @Test
    public void codecompletionResponseWithStatusCode422AndErrorMessageOnMissingColumnValue() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/non_existing_file.vdmsl?line=20&column=").then();
        response.statusCode(422);
        response.body(equalTo("Invalid query argument format"));
    }

    @Test
    public void codecompletionResponseWithStatusCode422AndErrorMessageWhenLineParamIsMissing() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/non_existing_file.vdmsl?column=1").then();
        response.statusCode(422);
        response.body(equalTo("Missing query argument"));
    }

    @Test
    public void codecompletionResponseWithStatusCode422AndErrorMessageWhenColumnParamIsMissing() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/non_existing_file.vdmsl?line=20").then();
        response.statusCode(422);
        response.body(equalTo("Missing query argument"));
    }

    @Test
    public void codecompletionResponseWithStatusCode422AndErrorMessageWhenQueryParamIsNotInteger() {
        ValidatableResponse response = get(hostWithPort + "/codecompletion/test/test_ws/non_existing_file.vdmsl?line=a&column=1").then();
        response.statusCode(422);
        response.body(equalTo("Invalid query argument format"));
    }
}
