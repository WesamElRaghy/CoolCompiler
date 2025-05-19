import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.Token;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import ast.ProgramNode;
import java.io.FileWriter;
import java.io.IOException;



public class Main {
    public static void main(String[] args) {
        try {
            // Input COOL program
            System.out.println("=== PA1: Lexical Analysis ===");
            CharStream input = CharStreams.fromFileName("src/test.cool");
            CoolLexer lexer = new CoolLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            // Print tokens
            List<Token> tokenList = tokens.getTokens();
            for (Token token : tokenList) {
                String tokenName = CoolLexer.VOCABULARY.getSymbolicName(token.getType());
                String tokenText = token.getText();
                if (tokenName != null) {
                    System.out.printf("%-10s -> %s%n", tokenName, tokenText);
                }
            }

            // Step 2: Parsing (PA2)
            System.out.println("\n=== PA2: Parsing ===");
            CoolParser parser = new CoolParser(tokens);
            ParseTree tree = parser.program();
            System.out.println("Parse Tree: " + tree.toStringTree(parser));
            Trees.inspect(tree, parser);

            // Using Listener to populate symbol table
            SymbolTable listenerSymbolTable = new SymbolTable();
            CoolSymbolListener listener = new CoolSymbolListener(listenerSymbolTable);
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);
            System.out.println("Symbol Table (Listener):");
            listenerSymbolTable.printTable();

            // Using Visitor to populate symbol table
            SymbolTable visitorSymbolTable = new SymbolTable();
            CoolSymbolVisitor visitor = new CoolSymbolVisitor(visitorSymbolTable);
            visitor.visit(tree);
            System.out.println("Symbol Table (Visitor):");
            visitorSymbolTable.printTable();

            // Step 3: AST Construction (PA3)
            System.out.println("\n=== PA3: AST Construction ===");
            ASTBuilder astBuilder = new ASTBuilder();
            ProgramNode ast = (ProgramNode) astBuilder.visit(tree);
            System.out.println(ast.toString());

            // Generate DOT file for visualization
            ast.generateDotFile("ast.dot");
            System.out.println("AST visualization saved to ast.dot");

            // Step 4: Semantic Analysis (PA4)
            System.out.println("\n=== PA4: Semantic Analysis ===");
            SemanticTester semanticTester = new SemanticTester(ast);
            boolean semanticsOk = semanticTester.analyze();

            if (semanticsOk) {
                semanticTester.generateTypedAST("typed_ast.dot");
                System.out.println("Semantic analysis completed successfully.");
            } else {
                System.out.println("Semantic analysis failed with errors.");
            }


            // Step 5: IR Generation (PA5)
            System.out.println("\n=== PA5: IR Generation ===");
            IRGenerator irGenerator = new IRGenerator();
            List<String> irCode = irGenerator.generate(ast);
            System.out.println(irGenerator.getIRCode());

            // Save IR code to file
            try (FileWriter writer = new FileWriter("output.tac")) {
                writer.write(irGenerator.getIRCode());
                System.out.println("IR code saved to output.tac");
            } catch (IOException e) {
                System.err.println("Error writing IR code to file: " + e.getMessage());
            }

            // Step 6: IR Optimization (PA6)
            System.out.println("\n=== PA6: IR Optimization ===");
            IROptimizer irOptimizer = new IROptimizer(irCode);
            List<String> optimizedIR = irOptimizer.optimize();
            System.out.println(irOptimizer.getOptimizedIRCode());

            // Save optimized IR code to file
            try (FileWriter optimizedWriter = new FileWriter("optimized.tac")) {
                optimizedWriter.write(irOptimizer.getOptimizedIRCode());
                System.out.println("Optimized IR code saved to optimized.tac");
            } catch (IOException e) {
                System.err.println("Error writing optimized IR code to file: " + e.getMessage());
            }

            // Step 7: Code Generation (PA7)
            System.out.println("\n=== PA7: Code Generation ===");
            CodeGenerator codeGenerator = new CodeGenerator(optimizedIR);
            List<String> assemblyCode = codeGenerator.generate();
            System.out.println(codeGenerator.getAssemblyCode());

            // Save assembly code to file
            try (FileWriter asmWriter = new FileWriter("output.s")) {
                asmWriter.write(codeGenerator.getAssemblyCode());
                System.out.println("Assembly code saved to output.s");
            } catch (IOException e) {
                System.err.println("Error writing assembly code to file: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}