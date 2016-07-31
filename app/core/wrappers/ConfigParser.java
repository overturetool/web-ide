package core.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.vfs.IVFS;
import org.apache.commons.vfs2.FileObject;
import org.overture.ast.lex.Dialect;
import org.overture.config.Release;

import java.io.IOException;
import java.io.InputStream;

public class ConfigParser {
    private IVFS<FileObject> file;

    public ConfigParser(IVFS<FileObject> file) {
        this.file = file;
    }

    public Release getRelease() {
        try {
            String attribute = "release";
            FileObject projectRoot = this.file.getProjectRoot();
            if (projectRoot == null)
                return Release.DEFAULT;

            FileObject projectFile = projectRoot.getChild(".project");
            if (projectFile == null)
                return Release.DEFAULT;

            InputStream content = projectFile.getContent().getInputStream();
            JsonNode node = new ObjectMapper().readTree(content);
            Release release = null;
            if (node != null && node.hasNonNull(attribute))
                release = Release.lookup(node.get(attribute).textValue());

            return release != null ? release : Release.DEFAULT;
        } catch (IOException e) {
            return Release.DEFAULT;
        }
    }

    public Dialect getDialect() {
        try {
            String attribute = "dialect";
            FileObject projectRoot = this.file.getProjectRoot();
            if (projectRoot == null)
                return Dialect.VDM_PP;

            FileObject projectFile = projectRoot.getChild(".project");
            if (projectFile == null)
                return Dialect.VDM_PP;

            InputStream content = projectFile.getContent().getInputStream();
            JsonNode node = new ObjectMapper().readTree(content);

            if (node == null || !node.hasNonNull(attribute))
                return Dialect.VDM_PP;

            String dialect = node.get(attribute).textValue().replaceAll("-", "");

            if (dialect.equalsIgnoreCase("vdmpp"))
                return Dialect.VDM_PP;
            else if (dialect.equalsIgnoreCase("vdmrt"))
                return Dialect.VDM_RT;
            else if (dialect.equalsIgnoreCase("vdmsl"))
                return Dialect.VDM_SL;
        } catch (IOException | NullPointerException e) { /* ignored */ }
        return Dialect.VDM_PP;
    }
}
