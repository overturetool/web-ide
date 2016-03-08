package core.outline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.utilities.PathHelper;
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
import org.overture.typechecker.assistant.TypeCheckerAssistantFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class OutlineTreeContentProvider {
    private ModuleList ast;

    public OutlineTreeContentProvider(ModuleList ast) {
        this.ast = ast;
    }

    public List<ObjectNode> toJSON(List<Object> objectList, String target) {
        List<ObjectNode> jsonList = new ArrayList<>();

        for (Object node : objectList) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            ILexLocation location = null;

            if (node instanceof ATypeDefinition) {
                ATypeDefinition definition = (ATypeDefinition) node;

                String name = definition.getName().getName();
                String module = definition.getName().getModule();

                String type;
                try {
                    ANamedInvariantType subType = (ANamedInvariantType) definition.getInvType();
                    type = subType.toString();
                } catch (ClassCastException e) {
                    type = definition.getInvType().toString();
                }

                json.put("name", name);
                json.put("module", module);
                json.put("type", type);

                location = definition.getName().getLocation();
            } else if (node instanceof ALocalDefinition) {
                ALocalDefinition definition = (ALocalDefinition) node;

                String name = definition.getName().toString();
                String type = definition.getType().toString();

                String expression;
                try {
                    expression = ((AValueDefinition) definition.parent()).getExpression().toString();
                } catch (ClassCastException e) {
                    expression = definition.parent().toString();
                }

                json.put("name", name);
                json.put("type", type);
                json.put("expression", expression);

                location = definition.getName().getLocation();
            } else if (node instanceof AExplicitFunctionDefinition) {
                AExplicitFunctionDefinition definition = (AExplicitFunctionDefinition) node;

                String name = definition.getName().toString();
                String type = definition.getType().toString();

                LinkedList<PType> params = definition.getType().getParameters();
                ArrayNode resolvedParam = mapper.createArrayNode();

                for (PType param : params) {
                    resolvedParam.add(param.toString());
                }

                PType expectedResult = definition.getExpectedResult();
                PType actualResult = definition.getActualResult();

                json.put("name", name);
                json.put("type", type);
                json.set("parameters", resolvedParam);
                json.put("expectedResult",  expectedResult != null ? expectedResult.toString() : "");
                json.put("actualResult", actualResult != null ? actualResult.toString() : "");

                location = definition.getName().getLocation();
            } else if (node instanceof AExplicitOperationDefinition) {
                AExplicitOperationDefinition definition = (AExplicitOperationDefinition) node;

                String name = definition.getName().toString();
                String type = definition.getType().toString();

                LinkedList<PDefinition> params = definition.getParamDefinitions();
                ArrayNode resolvedParam = mapper.createArrayNode();

                for (PDefinition param : params) {
                    resolvedParam.add(param.toString());
                }

                PType actualResult = definition.getActualResult();

                json.put("name", name);
                json.put("type", type);
                json.set("parameters", resolvedParam);
                json.put("expectedResult", "");
                json.put("actualResult", actualResult != null ? actualResult.toString() : "");

                location = definition.getName().getLocation();
            } /* else if (node instanceof ImportsContainer) {
                ImportsContainer importsContainer = (ImportsContainer) node;
                AModuleModules modules = (AModuleModules) importsContainer.getImports().parent();
                ILexIdentifierToken identifierToken = modules.getName();
                boolean isInModule = Objects.equals(identifierToken.getLocation().getFile().getName(), target);
                if (!isInModule)
                    continue;

                List<PDefinition> importDefs = importsContainer.getImportDefs();
//                List<Object> importDefsObjects = new ArrayList<>();
                for (PDefinition pdef : importDefs) {
                    Object def = ((AImportedDefinition) pdef).getDef();
                    AImportedDefinition pdef1 = (AImportedDefinition) pdef;

                    String name = pdef.getName().toString();
                    String type = pdef.getType().toString();
                    location = pdef1.getLocation();

//                    Class<? extends PDefinition> classDefinition = pdef1.getDef().getClass();
//                    Object cast = classDefinition.cast(pdef);
//                    importDefsObjects.add(classDefinition.cast(pdef));
                }

//                List<ObjectNode> objectNodes = toJSON(importDefsObjects, target);
//                if (!objectList.isEmpty()) {
//                    json.put("name", "imports");
//                    json.putPOJO("imports", objectNodes);
//                }
            }*/

            if (location != null) {
                if (!location.getFile().getName().equals(target))
                    continue;

                int startLine = location.getStartLine();
                int endLine = location.getEndLine();
                int startOffset = location.getStartOffset();
                int endOffset = location.getEndOffset();
                int startPos = location.getStartPos();
                int endPos = location.getEndPos();

                ObjectNode locationNode = mapper.createObjectNode();

                locationNode.put("executable", location.getExecutable());
                locationNode.put("file", PathHelper.RemoveBase(location.getFile().getPath()));
                locationNode.put("module", location.getModule());
                locationNode.put("startLine", startLine);
                locationNode.put("endLine", endLine);
                locationNode.put("startOffset", startOffset);
                locationNode.put("endOffset", endOffset);
                locationNode.put("startPos", startPos);
                locationNode.put("endPos", endPos);

                json.putPOJO("location", locationNode);
            }

            if (json.hasNonNull("name"))
                jsonList.add(json);
        }

        return jsonList;
    }

    public List<Object> getContent() {
        List<Object> list = new ArrayList<>();

        if (this.ast == null)
            return list;

        for (Object node : this.ast)
            Collections.addAll(list, getChildren(node));

        return list;
    }

    public Object[] getChildren(Object parentElement) {
        TypeCheckerAssistantFactory factory = new TypeCheckerAssistantFactory();

        if (parentElement instanceof SClassDefinition) {
            // get definitions from the current class without inherited definitions
            List<PDefinition> defs = factory.createPDefinitionListAssistant().singleDefinitions(((SClassDefinition) parentElement).getDefinitions());
            return filterDefinitionList(defs).toArray();
        } else if (parentElement instanceof AModuleModules) {
            List<Object> all = new ArrayList<Object>();

            AModuleModules module = (AModuleModules) parentElement;

            if (module.getImports() != null) {
                all.add(new ImportsContainer(module.getImports(), module.getImportdefs()));
            }

            all.addAll(filterDefinitionList(factory.createPDefinitionListAssistant().singleDefinitions(((AModuleModules) parentElement).getDefs())));
            filterSLModule(all);
            return all.toArray();
        } else if (parentElement instanceof AModuleImports) {
            return ((AModuleImports) parentElement).getImports().toArray();
        } else if (parentElement instanceof ImportsContainer) {
            ImportsContainer container = (ImportsContainer) parentElement;

            if (!container.getImportDefs().isEmpty()) {
                return container.getImportDefs().toArray();
            } else {
                return container.getImports().getImports().toArray();
            }
        } else if (parentElement instanceof AFromModuleImports) {
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

    private List<PDefinition> filterDefinitionList(List<PDefinition> fInput) {
        for (int i = 0; i < fInput.size(); i++) {
            PDefinition def = fInput.get(i);

            if (def != null) {
                try {
                    def.hashCode();

                    if (def instanceof AExplicitFunctionDefinition) {
                        if (def.getName().getName().startsWith("pre_") || def.getName().getName().startsWith("post_")) {
                            fInput.remove(i);
                            i--;
                        }
                    }

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

    public boolean hasChildren(Object element) {
        if (element instanceof SClassDefinition) {
            return ((SClassDefinition) element).getDefinitions().size() > 0;
        } else if (element instanceof AModuleModules) {
            return ((AModuleModules) element).getDefs().size() > 0;
        } else if (element instanceof AModuleImports) {
            return ((AModuleImports) element).getImports().size() > 0;
        } else if (element instanceof AFromModuleImports) {
            return ((AFromModuleImports) element).getSignatures().size() > 0;
        } else if (element instanceof ImportsContainer) {
            return ((ImportsContainer) element).getImports().getImports().size() > 0;
        } else if (element instanceof ATypeDefinition) {
            ATypeDefinition typeDef = (ATypeDefinition) element;
            SInvariantType type = typeDef.getInvType();
            if (type instanceof ARecordInvariantType) {
                return ((ARecordInvariantType) type).getFields().size() > 0;
            }
        }

        return false;
    }
}
