package core.codecompletion.resources;

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
    private List<Integer> offsetList;

    public Document(File file) {
        this.file = file;
        this.vdmsl = new VDMSL();
        this.modules = new ModuleList();
        this.offsetList = generateOffsetList(file);
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

    private List<Integer> generateOffsetList(File file) {
        List<Integer> tmpOffsetList = new ArrayList<>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (br == null)
            return tmpOffsetList;

        try {
            String line;
            while ((line = br.readLine()) != null) {
                tmpOffsetList.add(line.length());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tmpOffsetList;
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
        catch (IOException e)
        {
            e.printStackTrace();
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

    public int getLine(int offset) {
        int sum = 0;
        int line = 0;

        for (int i = 0; i < offsetList.size(); i++) {
            sum = sum + offsetList.get(i) + 1;
            if (sum > offset) {
                line = i;
                break;
            }
        }

        return line + 1;
    }

    public int getColumn(int offset) {
        int line = getLine(offset);
        return offsetList.get(line - 1) + 1;
    }

    public int getOffset(int line, int column) {
        int offset = 0;

        for (int i = 0; i < line - 1; i++) {
            offset = offset + offsetList.get(i) + 1;
        }

        return (offset + column) - 1;
    }
}
