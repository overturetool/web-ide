package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.overture.interpreter.util.ExitStatus;
import play.mvc.Result;
import utilities.ServerConfigurations;
import utilities.codecompletion.assistant.ContentAssistProcessor;
import utilities.codecompletion.proposal.mapper.ProposalToJsonMapper;
import utilities.codecompletion.resources.Document;
import utilities.codecompletion.resources.ICompletionProposal;
import utilities.file_system.IVF;
import utilities.file_system.commons_vfs2.CommonsVF;

import java.util.List;

public class codecompletion extends Application {
    public Result proposal(String account, String absPath) {
        String offsetStr = request().getQueryString("offset");
        int offset = Integer.parseInt(offsetStr);

        String path = ServerConfigurations.basePath + "/" + account + "/" + absPath;
        IVF file = new CommonsVF(path);

        Document document = new Document(file.getIOFile());

        if (ExitStatus.EXIT_ERRORS == document.parse())
            return ok("Errors on parse");

        if (ExitStatus.EXIT_ERRORS == document.typeCheck())
            return ok("Errors on typeCheck");

        ContentAssistProcessor cap = new ContentAssistProcessor();
        List<ICompletionProposal> proposals = cap.computeCompletionProposals(document, offset);

        ProposalToJsonMapper mapper = new ProposalToJsonMapper();
        List<ObjectNode> proposalsAsJson = mapper.toJson(proposals);

        return ok(proposalsAsJson.toString());
    }
}
