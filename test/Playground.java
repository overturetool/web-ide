import core.utilities.PathHelper;
import core.vfs.CollisionPolicy;
import core.vfs.IVFS;
import core.vfs.commons_vfs2.CommonsVFS;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Playground {

    //@Test
    public void moveFile() {
        String account = "test1";
        String filename = "bom.vdmsl";
        String path = "test_ws/" + filename;

        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        vfs.move(PathHelper.JoinPath(account, "test_ws/1"));

        String relativePath = vfs.getRelativePath();

        // Clean up
        vfs.move(PathHelper.JoinPath(account, "test_ws"));

        assertEquals(relativePath, PathHelper.JoinPath(account, "test_ws/1/" + filename));
    }

    //@Test
    public void moveDir() {
        String account = "test1";
        String dirname = "2";
        String path = "test_ws/1/" + dirname;

        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        vfs.move(PathHelper.JoinPath(account, "test_ws/1/2a"));

        String relativePath = vfs.getRelativePath();

        // Clean up
        //vfs.moveFile(PathHelper.JoinPath(account, "test_ws"));

        assertEquals(relativePath, PathHelper.JoinPath(account, "test_ws/1/2a/" + dirname));
    }

    @Test
    public void handleCollision() {
        String account = "test1";
        String filename = "bom1.vdmsl";
        String path = "test_ws/" + filename;

        IVFS vfs = new CommonsVFS(PathHelper.JoinPath(account, path));

        vfs.move(PathHelper.JoinPath(account, "test_ws/1/bom1.vdmsl"), CollisionPolicy.KeepBoth);

        String relativePath = vfs.getRelativePath();

        // Clean up
//        vfs.moveFile(PathHelper.JoinPath(account, "test_ws"));

        assertEquals(relativePath, PathHelper.JoinPath(account, "test_ws/1/2a/" + filename.replace(".", "1.")));
    }
}
