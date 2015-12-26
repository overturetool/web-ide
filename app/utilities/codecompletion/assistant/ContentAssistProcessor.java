package utilities.codecompletion.assistant;

import org.overture.ast.lex.VDMToken;
import utilities.codecompletion.core.CompletionContext;
import utilities.codecompletion.core.CompletionProcessor;
import utilities.codecompletion.resources.Document;
import utilities.codecompletion.resources.ICompletionProposal;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.List;

public class ContentAssistProcessor {
    CompletionProcessor processor = new CompletionProcessor();

    /**
     * Computes the code completion proposals for a vdm document
     *
     * @param document  Encapsulates the ast, plus auxiliary methods.
     * @param offset    Cursor position in the document as offset from the beginning of the document.
     *                  The first position in a document is 1.
     *
     * @return a list of proposals.
     */
    public List<ICompletionProposal> computeCompletionProposals(Document document, int offset)
    {
        List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

        processor.computeCompletionProposals(
                computeCompletionContext(document, offset),
                document.getModules(),
                proposals,
                offset);

        return proposals;
    }

    private VDMToken getToken(char c)
    {
        String name = "" + c;
        for (VDMToken token : VDMToken.values())
        {
            if (token.toString() != null && token.toString().equals(name))
            {
                return token;
            }
        }

        return null;
    }

    private CompletionContext computeCompletionContext(Document document, int offset)
    {
        // Use string buffer to collect characters
        StringBuffer scanned = new StringBuffer();
        while (true)
        {
            try
            {
                if(offset - 1 == -1)
                {
                    //EOF
                    break;
                }

                // Read character backwards
                char c = document.getChar(--offset);

                VDMToken token = null;

                if ((token = getToken(c)) != null)
                {
                    // Break if 'token' does not equal "<", ".", or "("
                    if (!(token == VDMToken.LT || token == VDMToken.POINT || token == VDMToken.BRA))
                    {
                        break;
                    }
                }

                scanned.append(c);

                if (c == 'n' && scanned.length() > 3 && scanned.substring(scanned.length() - 4, scanned.length()).matches("\\swen"))
                {
                    break;
                }
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
                break;
            }
        }

        return new CompletionContext(scanned.reverse());
    }
}
