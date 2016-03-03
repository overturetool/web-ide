import core.vfs.CollisionPolicy;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import core.wrappers.ModelWrapper;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class Playground {

    @Test
    public void repl() {
        String account = "rsreimer";
        String filename = "bom.vdmsl";
        String path = "bom/" + filename;

        IVFS vfs = new CommonsVFS(account, path);
        ModelWrapper model = new ModelWrapper(vfs);
        String value0 = model.evaluate("1 + 1");
        assertEquals(value0, "2");

        String value1 = model.evaluate("Parts(1,bom)");
        assertEquals(value1, "{2, 3, 4, 5, 6}");

        String value2 = model.evaluate("Parts(2,bom)");
        assertEquals(value2, "{3, 4, 5, 6}");

        String value3 = model.evaluate("bom");
        assertEquals(value3, "{1 |-> {2, 4}, 2 |-> {3, 4, 5}, 3 |-> {5, 6}, 4 |-> {6}, 5 |-> {4}, 6 |-> {}}");

        String value4 = model.evaluate("Parts");
        assertEquals(value4, "(Pn * map (Pn) to (set of (Pn)) -> set of (Pn))");

        String value5 = model.evaluate("functions");
        assertEquals(value5, "Error 2034: Unexpected token in expression in 'DEFAULT' (console) at line 1:1");

        String value6 = model.evaluate("bom(1)");
        assertEquals(value6, "{2, 4}");

        String value7 = model.evaluate("Parts(6, bom)");
        assertEquals(value7, "{}");
    }

    //@Test
    public void moveFile() {
        String account = "test1";
        String filename = "bom.vdmsl";
        String path = "test_ws/" + filename;

        IVFS vfs = new CommonsVFS(account, path);

        vfs.move(Paths.get(account, "test_ws/1").toString());

        String relativePath = vfs.getRelativePath();

        // Clean up
        vfs.move(Paths.get(account, "test_ws").toString());

        assertEquals(relativePath, Paths.get(account, "test_ws/1/" + filename).toString());
    }

    //@Test
    public void moveDir() {
        String account = "test1";
        String dirname = "2";
        String path = "test_ws/1/" + dirname;

        IVFS vfs = new CommonsVFS(account, path);

        vfs.move(Paths.get(account, "test_ws/1/2a").toString());

        String relativePath = vfs.getRelativePath();

        // Clean up
        //vfs.moveFile(PathHelper.JoinPath(account, "test_ws"));

        assertEquals(relativePath, Paths.get(account, "test_ws/1/2a/" + dirname).toString());
    }

    //@Test
    public void handleCollision() {
        String account = "test1";
        String filename = "bom1.vdmsl";
        String path = "test_ws/" + filename;

        IVFS vfs = new CommonsVFS(account, path);

        vfs.move(Paths.get(account, "test_ws/1/bom1.vdmsl").toString(), CollisionPolicy.KeepBoth);

        String relativePath = vfs.getRelativePath();

        // Clean up
//        vfs.moveFile(PathHelper.JoinPath(account, "test_ws"));

        assertEquals(relativePath, Paths.get(account, "test_ws/1/2a/" + filename.replace(".", "1.")).toString());
    }
}
