import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public class CoolSymbolVisitor extends CoolParserBaseVisitor<Void> {
    private SymbolTable symbolTable;

    public CoolSymbolVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public Void visitFeature(CoolParser.FeatureContext ctx) {
        // Handle attribute: ID COLON ID (ASSIGN expr)? SEMI
        if (ctx.ASSIGN() != null || (ctx.LPAREN() == null && ctx.RPAREN() == null)) {
            String varName = ctx.ID(0).getText();  // First ID is the variable name
            String varType = ctx.ID(1).getText();  // Second ID is the type
            symbolTable.addVariable(varName, varType);
        }
        // Visit children (e.g., the expr in ASSIGN expr)
        return visitChildren(ctx);
    }

    @Override
    public Void visitFormal(CoolParser.FormalContext ctx) {
        // Handle method parameters: ID COLON ID
        String paramName = ctx.ID(0).getText();
        String paramType = ctx.ID(1).getText();
        symbolTable.addVariable(paramName, paramType);
        return null;  // No children to visit
    }
}