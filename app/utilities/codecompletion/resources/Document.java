package utilities.codecompletion.resources;

import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.util.ExitStatus;

import javax.swing.text.BadLocationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Document {
    private File file;
    private VDMSL vdmsl;
    private ModuleList modules;
    private ExitStatus parseStatus;
    private ExitStatus typeCheckStatus;

    public Document(File file)
    {
        this.file = file;
        vdmsl = new VDMSL();
        modules = new ModuleList();
    }

    /**
     * @return  ast if latest parse and type check was successful,
     *          otherwise the last valid ast is returned.
     */
    public ModuleList getModules() {
        try
        {
            ModuleList latest = vdmsl.getInterpreter().getModules();

            if (parseStatus == ExitStatus.EXIT_OK && typeCheckStatus == ExitStatus.EXIT_OK)
            {
                modules.clear();
                modules.addAll(latest);
            }

            return modules;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public char getChar(int offset) throws BadLocationException {
        char c = 0;

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));

            int charCount = 0;
            int r;

            while ((r = br.read()) != -1 && charCount <= offset)
            {
                c = (char) r;
                charCount++;
            }

            br.close();
        }
        catch (FileNotFoundException e0)
        {
            e0.printStackTrace();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        return c;
    }

    public ExitStatus parse()
    {
        List<File> files = new ArrayList<File>();
        files.add(file);
        parseStatus = vdmsl.parse(files);
        return parseStatus;
    }

    public ExitStatus typeCheck()
    {
        typeCheckStatus = vdmsl.typeCheck();
        return typeCheckStatus;
    }
}
