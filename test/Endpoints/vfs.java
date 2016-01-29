package Endpoints;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.ValidatableResponse;
import core.utilities.PathHelper;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class vfs {

    private static String hostWithPort = "http://localhost:9000";

    @Test
    public void appendFileAppendsStringToDocument() {
        String account = "test";
        String path = "test_ws/bom.vdmsl";

        // Save the current state of the document
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));
        String tmp = vfs.readFile();

        given()
            .contentType(ContentType.TEXT).body("String to append")
            .when().put(hostWithPort + "/vfs/appendFile/test/test_ws/bom.vdmsl")
            .then().statusCode(200);

        int newLength = vfs.readFile().length();

        // Clean up
        vfs.writeFile(tmp);

        assertThat(newLength, is(1820));
    }

    @Test
    public void readFileReturnsStatusCode404() {
        get(hostWithPort + "/vfs/readFile/test/test_ws").then().statusCode(404);
    }

    @Test
    public void readFileReturnsContentWithLength1806() {
        String document = get(hostWithPort + "/vfs/readFile/test/test_ws/bom.vdmsl").body().asString();
        assertThat(document.length(), is(1804));
    }

    @Test
    public void readdirReturnsStatusCode404() {
        get(hostWithPort + "/vfs/readdir/test/test_ws/bom.vdmsl").then().statusCode(404);
    }

    @Test
    public void readdirhasCorrectTreeStructure() {
        ValidatableResponse response = when().get(hostWithPort + "/vfs/readdir/test?depth=-1").then().contentType(ContentType.JSON);
        response.body("size()", equalTo(2));

        // Level 0
        response.body("find { it.name == 'test_ws' }.type", equalTo("directory"));
        response.body("find { it.name == 'test_ws' }.children.size()", equalTo(4));
        response.body("find { it.name == 'test_ws' }.children.name", hasItems("1", "barSL", "bom.vdmsl"));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == 'bom.vdmsl' }.type", equalTo("file"));

        // Level 1
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.type", equalTo("directory"));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.children.size()", equalTo(2));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.children.name", hasItems("2", "2a"));

        response.body("find { it.name == 'test_ws' }.children.find { it.name == 'barSL' }.type", equalTo("directory"));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == 'barSL' }.children.size()", equalTo(3));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == 'barSL' }.children.name", hasItems("bag.vdmsl", "bagtest.vdmsl", "bar.vdmsl"));

        // Level 2
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.children.find { it.name == '2' }.type", equalTo("directory"));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.children.find { it.name == '2a' }.type", equalTo("directory"));
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.children.find { it.name == '2' }.children.size()", equalTo(1));

        // Level 3
        response.body("find { it.name == 'test_ws' }.children.find { it.name == '1' }.children.find { it.name == '2' }.children.find { it.name == '3' }.type", equalTo("directory"));
    }

    @Test
    public void writeFileCanOverwriteFile() {
        String account = "test";
        String path = "test_ws/bom.vdmsl";

        // Save the current state of the document
        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));
        String tmp = vfs.readFile();
        int oldLength = tmp.length();

        given()
            .contentType(ContentType.TEXT).body(tmp + " extra string written")
            .when().post(hostWithPort + "/vfs/writeFile/test/test_ws/bom.vdmsl")
            .then().statusCode(200);

        int newLength = vfs.readFile().length();

        // Clean up
        vfs.writeFile(tmp);

        assertThat(oldLength, is(1804));
        assertThat(newLength, is(1825));
    }
}
