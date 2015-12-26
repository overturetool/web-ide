package utilities.codecompletion.core;

import utilities.codecompletion.resources.SearchType;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class CompletionContext {
    private StringBuffer rawScan;
    private StringBuffer processedScan;

    public int offset;
    public SearchType type = SearchType.Types;

    public String proposalPrefix = "";

    public List<String> root = new Vector<String>();

    public CompletionContext(StringBuffer rawScan)
    {
        this.rawScan = rawScan;
        init();
    }

    private void init()
    {
        calcSearchType();
        System.out.println("Computed completion context: "+toString());
    }

    private void calcSearchType()
    {
        int index = rawScan.toString().lastIndexOf("<");

        if (index != -1)
        {
            // quote
            processedScan = new StringBuffer(rawScan.subSequence(index, rawScan.length()));
            proposalPrefix = processedScan.toString();
            offset = -(rawScan.length() - index);
            type = SearchType.Quote;
            return;
        }

        index = rawScan.toString().indexOf("new");

        if (index == 0)
        {
            // quote
            processedScan = new StringBuffer(rawScan.subSequence(index + "new".length(), rawScan.length()));
            proposalPrefix = processedScan.toString().trim();

            for (int i = index + "new".length(); i < rawScan.length(); i++)
            {
                if (Character.isJavaIdentifierStart(rawScan.charAt(i)))
                {
                    offset = -(rawScan.length() - i);
                    break;
                }
            }

            type = SearchType.New;
        }
    }

    @Override
    public String toString()
    {
        return type + " - Root: '" + getQualifiedSource() + "' Proposal: '" + proposalPrefix+"'" +" offset: "+offset;
    }

    String getQualifiedSource()
    {
        String res = "";
        if (root != null && !root.isEmpty())
        {
            for (Iterator<String> itr = root.iterator(); itr.hasNext();)
            {
                res += itr.next();
                if (itr.hasNext())
                    res += ".";
            }
        }
        return res;
    }
}
