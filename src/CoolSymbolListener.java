import org.antlr.v4.runtime.tree.ParseTreeListener;

public class CoolSymbolListener extends CoolParserBaseListener {
    private SymbolTable symbolTable;

    public CoolSymbolListener(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void enterFeature(CoolParser.FeatureContext ctx) {
        if (ctx.ASSIGN() != null || (ctx.LPAREN() == null && ctx.RPAREN() == null)) {
            String varName = ctx.ID(0).getText();
            String varType = ctx.ID(1).getText();
            symbolTable.addVariable(varName, varType);
        }
    }

    @Override
    public void enterFormal(CoolParser.FormalContext ctx) {
        String paramName = ctx.ID(0).getText();
        String paramType = ctx.ID(1).getText();
        symbolTable.addVariable(paramName, paramType);
    }
}