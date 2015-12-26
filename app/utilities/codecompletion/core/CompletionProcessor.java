package utilities.codecompletion.core;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.statements.PStm;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.PType;
import org.overture.ast.util.modules.ModuleList;
import utilities.codecompletion.analysis.adaptors.CompleteNewAnalysisAdaptor;
import utilities.codecompletion.analysis.adaptors.CompleteQuotesAnalysisAdaptor;
import utilities.codecompletion.resources.CompletionProposal;
import utilities.codecompletion.resources.ICompletionProposal;
import utilities.codecompletion.resources.SearchType;

import java.util.List;
import java.util.Vector;

public class CompletionProcessor
{
    public void computeCompletionProposals(CompletionContext info, ModuleList modules,
                                           List<ICompletionProposal> proposals,
                                           int offset)
    {
        List<ICompletionProposal> calculatedProposals = new Vector<ICompletionProposal>();

        switch (info.type) {
            case CallParam:
                break;
            case Dot:
                break;
            case Mk:
                break;
            case New:
                completeNew(info, modules, calculatedProposals, offset);
                break;
            case Quote:
                completeQuotes(info, modules, calculatedProposals, offset);
                break;
            case Types:
                completeTypes(info, modules, calculatedProposals, offset);
                break;
            default:
                break;
        }

        proposals.addAll(calculatedProposals);
    }

    private void completeNew(final CompletionContext info, ModuleList modules,
                             final List<ICompletionProposal> proposals,
                             final int offset)
    {
        for (INode container : modules)
        {
            try
            {
                container.apply(new CompleteNewAnalysisAdaptor(info, proposals, offset));
            }
            catch (AnalysisException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void completeQuotes(final CompletionContext info, ModuleList modules,
                                final List<ICompletionProposal> proposals,
                                final int offset)
    {
        for (INode container : modules)
        {
            try
            {
                container.apply(new CompleteQuotesAnalysisAdaptor(info, proposals, offset));
            }
            catch (AnalysisException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void completeQuotes(final CompletionContext info, INode node,
                                final List<ICompletionProposal> proposals,
                                final int offset)
    {
        try
        {
            node.apply(new CompleteQuotesAnalysisAdaptor(info, proposals, offset));
        }
        catch (AnalysisException e)
        {
            e.printStackTrace();
        }
    }

    private void completeTypes(final CompletionContext info, ModuleList modules,
                               final List<ICompletionProposal> proposals,
                               final int offset)
    {
        for (INode container : modules)
        {
            if (info.type == SearchType.Types)
            {
                String name = getName(container);
                if (name != null)
                {
                    if (name.startsWith(info.proposalPrefix) || name.length() == 0)
                    {
                        proposals.add(new CompletionProposal(name, offset, 0, name.length()));
                    }
                }
            }

            addContainerTypes(info, container, proposals, offset);
        }
    }

    private void addContainerTypes(final CompletionContext info2, INode def,
                                   final List<ICompletionProposal> proposals,
                                   final int offset)
    {
        if (def instanceof SClassDefinition)
        {
            SClassDefinition cd = (SClassDefinition) def;
            for (PDefinition element : cd.getDefinitions())
            {
                if (element instanceof ATypeDefinition)
                {
                    String name = cd.getName() + "`" + element.getName();
                    proposals.add(new CompletionProposal(name, offset, 0, name.length()));
                }
            }

            completeQuotes(info2, def, proposals, offset);
        }
        else if (def instanceof AModuleModules)
        {
            AModuleModules m = (AModuleModules) def;
            for (PDefinition element : m.getDefs())
            {
                String prefix = "";
                if (element.getAncestor(AModuleModules.class) != def)
                {
                    prefix = m.getName() + "`";
                }

                if (element instanceof ATypeDefinition)
                {
                    String name = prefix + element.getName();

                    if (name.toLowerCase().startsWith(info2.proposalPrefix.toLowerCase()))
                    {
                        proposals.add(new CompletionProposal(
                                name, offset - info2.proposalPrefix.length(),
                                info2.proposalPrefix.length(), name.length()));
                    }
                }
            }

            completeQuotes(info2, m, proposals, offset);
        }
    }

    /*
     * Auxiliary method
     */
    private static String getName(INode node)
    {
        if (node instanceof PDefinition)
        {
            if (node instanceof AValueDefinition)
            {
                return ((AValueDefinition) node).getPattern().toString();
            }

            return ((PDefinition) node).getName().getName();
        }
        else if (node instanceof AModuleModules)
        {
            return ((AModuleModules) node).getName()==null?null:((AModuleModules) node).getName().getName();
        }
        else if (node instanceof PStm)
        {
            return ((PStm) node).getLocation().getModule();
        }
        else if (node instanceof PExp)
        {
            return ((PExp) node).getLocation().getModule();
        }
        else if (node instanceof PType)
        {
            return ((PType) node).getLocation().getModule();
        }
        else if (node instanceof AFieldField)
        {
            return ((AFieldField) node).getTagname().getName();
        }

        return "Unresolved Name";
    }
}
