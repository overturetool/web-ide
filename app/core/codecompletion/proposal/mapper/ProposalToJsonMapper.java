package core.codecompletion.proposal.mapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import core.codecompletion.resources.ICompletionProposal;

import java.util.ArrayList;
import java.util.List;

public class ProposalToJsonMapper {
    public List<ObjectNode> toJson(List<ICompletionProposal> proposals) {
        List<ObjectNode> jsonList = new ArrayList<>();

        for (ICompletionProposal proposal : proposals) {
            jsonList.add(toJson(proposal));
        }

        return jsonList;
    }

    public ObjectNode toJson(ICompletionProposal proposal) {
        ObjectNode node = Json.newObject();
        node.put("replacementString", proposal.getReplacementString());
        node.put("replacementOffset", proposal.getReplacementOffset());
        node.put("replacementLength", proposal.getReplacementLength());
        node.put("cursorPosition", proposal.getCursorPosition());
        return node;
    }
}
