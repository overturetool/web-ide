package Integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class outline {

    private static String hostWithPort = "http://localhost:9000";

    @Test
    public void outlineContains14Objects() {
        ValidatableResponse response = get(hostWithPort + "/outline/test/test_ws/bom.vdmsl").then().contentType(ContentType.JSON);

        response.body("size()", equalTo(14));

        response.body("name", hasItems(
                "BOM", "inv_BOM", "Pn", "bom", "cycle", "Parts", "TransClos",
                "IncrAcc", "Explode", "Exps", "Enter", "Delete", "Add", "Erase"));

        response.body("findAll { it.size() > 1 }.size()", equalTo(14));
    }

    @Test
    public void outlineResponseWithStatusCode404GivenPathToDirectory() {
        get(hostWithPort + "/outline/test/test_ws").then().statusCode(404);
    }

    @Test
    public void outlineResponseWithStatusCode422AndMessageFileNotFound() {
        ValidatableResponse response = get(hostWithPort + "/outline/test/test_ws/non_existing_file.vdmsl").then();
        response.statusCode(422);
        response.body(equalTo("File not found"));
    }
}
