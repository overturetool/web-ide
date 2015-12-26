package utilities.codecompletion.analysis.adaptors;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.ast.expressions.AQuoteLiteralExp;
import org.overture.ast.node.INode;
import org.overture.ast.types.AQuoteType;
import utilities.codecompletion.core.CompletionContext;
import utilities.codecompletion.resources.CompletionProposal;
import utilities.codecompletion.resources.ICompletionProposal;

import java.util.List;

public class CompleteQuotesAnalysisAdaptor extends DepthFirstAnalysisAdaptor {

    private final CompletionContext info;
    private final List<ICompletionProposal> proposals;
    private final int offset;

    public CompleteQuotesAnalysisAdaptor(final CompletionContext info,
                                         final List<ICompletionProposal> proposals,
                                         final int offset)
    {
        this.info = info;
        this.proposals = proposals;
        this.offset = offset;
    }

    @Override
    public void caseAQuoteLiteralExp(AQuoteLiteralExp node) throws AnalysisException
    {
        populateQuotes(node, node.getValue().getValue(), node.toString());
    }

    @Override
    public void caseAQuoteType(AQuoteType node) throws AnalysisException
    {
        populateQuotes(node, node.getValue().getValue(), node.toString());
    }

    void populateQuotes(INode node, String baseValue, String name)
    {
        // if (!info2.prefix.toString().equals(baseValue))
        {
            int curOffset = offset + info.offset;// - info2.proposalPrefix.length();
            int length = name.length();
            int replacementLength = info.proposalPrefix.length();

            if (info.proposalPrefix.equals("<" + baseValue + ">"))
            {
                // replacementLength+=1;
                // length+=1;
                curOffset = offset;
                replacementLength = 0;
            }

            if (("<" + baseValue).toLowerCase().startsWith(info.proposalPrefix.toLowerCase()))
            {
                proposals.add(new CompletionProposal(name, curOffset, replacementLength, length));
            }
        }
    }
}
