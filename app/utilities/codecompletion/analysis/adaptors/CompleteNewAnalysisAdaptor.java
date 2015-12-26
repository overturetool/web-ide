package utilities.codecompletion.analysis.adaptors;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.ast.assistant.definition.PAccessSpecifierAssistant;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.patterns.PPattern;
import utilities.codecompletion.core.CompletionContext;
import utilities.codecompletion.resources.CompletionProposal;
import utilities.codecompletion.resources.ICompletionProposal;

import java.util.Iterator;
import java.util.List;

public class CompleteNewAnalysisAdaptor extends DepthFirstAnalysisAdaptor {
    private final CompletionContext info;
    private final List<ICompletionProposal> proposals;
    private final int offset;

    public CompleteNewAnalysisAdaptor(final CompletionContext info,
                                      final List<ICompletionProposal> proposals,
                                      final int offset)
    {
        this.info = info;
        this.proposals = proposals;
        this.offset = offset;
    }

    @Override
    public void caseAExplicitOperationDefinition(AExplicitOperationDefinition node) throws AnalysisException
    {
        if (node.getIsConstructor() && new PAccessSpecifierAssistant(null).isPublic(node.getAccess()))
        {
            String name = node.getName().getName();
            if (info.proposalPrefix.isEmpty() || name.toLowerCase().startsWith(info.proposalPrefix.toLowerCase()))
            {
                String replacementString = name + "(";

                for (Iterator<PPattern> iterator = node.getParameterPatterns().iterator(); iterator.hasNext();)
                {
                    PPattern pattern = iterator.next();
                    replacementString += pattern.toString();

                    if (iterator.hasNext())
                        replacementString += ", ";
                }

                replacementString += ")";

                proposals.add(new CompletionProposal(
                        replacementString, offset + info.offset,
                        info.proposalPrefix.length(),
                        replacementString.length()));
            }
        }
    }
}
