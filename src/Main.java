import ast.AstPrintVisitor;
import ast.AstXMLSerializer;
import ast.Program;
import ex1_final.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                throw new UnsupportedOperationException("TODO - Ex. 4");
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    throw new UnsupportedOperationException("TODO - Ex. 3");

                } else if (action.equals("compile")) {
                    throw new UnsupportedOperationException("TODO - Ex. 2");

                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
                    var originalLine = args[4];
                    var newName = args[5];

                    boolean isMethod = false;
                    if (type.equals("var")) {
                        isMethod = false;
                    } else if (type.equals("method")) {
                        isMethod = true;
                    } else {
                        throw new IllegalArgumentException("unknown rename type " + type);
                    }
                    VisitorCreateTable builderVisitor = new VisitorCreateTable();
                    builderVisitor.visit(prog);
                    InheritanceUpdate inheritanceUpdater = new InheritanceUpdate(builderVisitor.classesToTables, prog);
                    inheritanceUpdater.run();
                    if (!isMethod){
                        VisitorRenameVar visitorRenameVar = new VisitorRenameVar(originalName, newName, Integer.parseInt(originalLine));
                        visitorRenameVar.visit(prog);
                    } else {
                        InheritanceBuildMapMethods inheritanceBuildMapMethods = new InheritanceBuildMapMethods(builderVisitor.classesToTables, prog);
                        Map<MethodHierarchyKey, ArrayList<Symbol>> methodHierarchyToTables;
                        methodHierarchyToTables = inheritanceBuildMapMethods.buildMethodsHierarchyMap();
                        VisitorMethodFinderByDecl visitorMethodFinderByDecl = new VisitorMethodFinderByDecl(builderVisitor.classesToTables, originalName, newName, Integer.parseInt(originalLine), methodHierarchyToTables);
                        visitorMethodFinderByDecl.visit(prog);
                    }
                     //convert ast to xml
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }
}
