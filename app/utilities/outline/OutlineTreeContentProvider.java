package utilities.outline;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.overture.ast.definitions.*;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.modules.AFromModuleImports;
import org.overture.ast.modules.AModuleImports;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.modules.PImport;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.util.modules.ModuleList;
import org.overture.interpreter.VDMSL;
import org.overture.interpreter.util.ExitStatus;
import org.overture.typechecker.assistant.TypeCheckerAssistantFactory;
import play.libs.Json;
import utilities.file_system.IVF;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class OutlineTreeContentProvider {
    private ModuleList ast;

    public OutlineTreeContentProvider(IVF file) {
        this.ast = new ModuleList();
        List<File> files = new ArrayList<>();

        if (file.isDirectory()) {
            return;
        } else {
            files.add(file.getIOFile());
        }

        // Look into using the VDMJ class instead
        VDMSL vdmsl = new VDMSL();
        ExitStatus parseStatus = vdmsl.parse(files);

        if (parseStatus == ExitStatus.EXIT_OK) {
            ExitStatus typeCheckStatus = vdmsl.typeCheck();

            if (typeCheckStatus == ExitStatus.EXIT_OK) {
                try {
                    this.ast = vdmsl.getInterpreter().getModules();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<ObjectNode> toJSON(List<Object> objectList) {
        List<ObjectNode> jsonList = new ArrayList<>();

        for (Object node : objectList) {
            ObjectNode json = Json.newObject();
            ILexLocation location = null;

            if (node instanceof ATypeDefinition) {
                String name = ((ATypeDefinition) node).getName().getName();
                String module = ((ATypeDefinition) node).getName().getModule();

                ANamedInvariantType subType = (ANamedInvariantType) ((ATypeDefinition) node).getInvType();
                String type = subType.getType().toString();

                json.put("name", name);
                json.put("type", type);
                json.put("module", module);

                location = ((ATypeDefinition) node).getName().getLocation();

            } else if (node instanceof ALocalDefinition) {
                ALocalDefinition definition = (ALocalDefinition)node;

                String name = definition.getName().toString();
                String type = definition.getType().toString();

                String expression = ((AValueDefinition) definition.parent()).getExpression().toString();

                json.put("name", name);
                json.put("type", type);
                json.put("expression", expression);

                location = ((ALocalDefinition) node).getName().getLocation();

            } else if (node instanceof AExplicitFunctionDefinition) {
                AExplicitFunctionDefinition definition = (AExplicitFunctionDefinition)node;

                String name = definition.getName().toString();
                String type = definition.getType().toString();

                LinkedList<PType> params = definition.getType().getParameters();
                ArrayNode resolvedParam = Json.newArray();

                for (PType param : params) {
                    resolvedParam.add(param.toString());
                }

                String expectedResult = definition.getExpectedResult().toString();
                String actualResult = definition.getActualResult().toString();

                json.put("name", name);
                json.put("type", type);
                json.set("parameters", resolvedParam);
                json.put("expectedResult", expectedResult);
                json.put("actualResult", actualResult);

                location = ((AExplicitFunctionDefinition) node).getName().getLocation();
            }

            if (location != null) {
                int startLine = location.getStartLine();
                int endLine = location.getEndLine();
                int startOffset = location.getStartOffset();
                int endOffset = location.getEndOffset();
                int startPos = location.getStartPos();
                int endPos = location.getEndPos();

                ObjectNode locationNode = Json.newObject();

                locationNode.put("startLine", startLine);
                locationNode.put("endLine", endLine);
                locationNode.put("startOffset", startOffset);
                locationNode.put("endOffset", endOffset);
                locationNode.put("startPos", startPos);
                locationNode.put("endPos", endPos);

                json.putPOJO("location", locationNode);
            }

            jsonList.add(json);
        }

        return jsonList;
    }

    public List<Object> getContent() {
        List<Object> list = new ArrayList<>();

        for (Object node : ast) {
            Collections.addAll(list, getChildren(node));
        }

        return list;
    }

    public ModuleList getAst() {
        return ast;
    }

    public Object[] getChildren(Object parentElement) {
        TypeCheckerAssistantFactory factory = new TypeCheckerAssistantFactory();

        if (parentElement instanceof SClassDefinition) {
            // get definitions from the current class without inherited definitions
            List<PDefinition> defs = factory.createPDefinitionListAssistant().singleDefinitions(((SClassDefinition) parentElement).getDefinitions());
            return filterDefinitionList(defs).toArray();

        } else if (parentElement instanceof AModuleModules) {
            List<Object> all = new ArrayList<Object>();

//            AModuleModules module = (AModuleModules) parentElement;

//            if (module.getImports() != null) {
//                all.add(new ImportsContainer(module.getImports(), module.getImportdefs()));
//            }

            all.addAll(filterDefinitionList(factory.createPDefinitionListAssistant().singleDefinitions(((AModuleModules) parentElement).getDefs())));
            filterSLModule(all);
            return all.toArray();
        } else if (parentElement instanceof AModuleImports) {
            return ((AModuleImports) parentElement).getImports().toArray();
        }
//        else if (parentElement instanceof ImportsContainer) {
//            ImportsContainer container = (ImportsContainer) parentElement;
//
//            if (!container.getImportDefs().isEmpty()) {
//                return container.getImportDefs().toArray();
//            } else {
//                return container.getImports().getImports().toArray();
//            }
//        }
        else if (parentElement instanceof AFromModuleImports) {
            List<Object> all = new ArrayList<Object>();

            for (List<PImport> iterable_element : ((AFromModuleImports) parentElement).getSignatures()) {
                all.addAll(iterable_element);
            }

            return all.toArray();
        } else if (parentElement instanceof ATypeDefinition) {
            ATypeDefinition typeDef = (ATypeDefinition) parentElement;
            SInvariantType type = typeDef.getInvType();

            if (type instanceof ARecordInvariantType) {
                ARecordInvariantType rType = (ARecordInvariantType) type;
                return rType.getFields().toArray();
            }
        }

        return null;
    }

    private void filterSLModule(List<Object> all) {
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i) instanceof ALocalDefinition) {
                ALocalDefinition localDef = (ALocalDefinition) all.get(i);
                if (localDef.getNameScope().name().equals("OLDSTATE")) {
                    all.remove(i);
                    i--;
                }
            }
        }
    }

    private List<PDefinition> filterDefinitionList(List<PDefinition> fInput)
    {
        for (int i = 0; i < fInput.size(); i++) {
            PDefinition def = fInput.get(i);

            if (def != null) {
                try {
                    def.hashCode();

//                    if (def instanceof AClassInvariantDefinition)
//                    {
//
//                    }

                    if (def instanceof AExplicitFunctionDefinition) {
                        if (def.getName().getName().startsWith("pre_") || def.getName().getName().startsWith("post_")) {
                            fInput.remove(i);
                            i--;
                        }
                    }

                    // if (def instanceof InheritedDefinition)
                    // {
                    // fInput.remove(i);
                    // i--;
                    // }

                } catch (NullPointerException e) {
                    fInput.remove(i);
                    i--;
                }
            } else {
                fInput.remove(i);
                i--;
            }
        }

        return fInput;
    }
}
