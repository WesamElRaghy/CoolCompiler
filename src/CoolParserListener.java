// Generated from /home/wesam/IdeaProjects/CoolCompiler/grammar/CoolParser.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CoolParser}.
 */
public interface CoolParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CoolParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(CoolParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(CoolParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#classDef}.
	 * @param ctx the parse tree
	 */
	void enterClassDef(CoolParser.ClassDefContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#classDef}.
	 * @param ctx the parse tree
	 */
	void exitClassDef(CoolParser.ClassDefContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#feature}.
	 * @param ctx the parse tree
	 */
	void enterFeature(CoolParser.FeatureContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#feature}.
	 * @param ctx the parse tree
	 */
	void exitFeature(CoolParser.FeatureContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#formal}.
	 * @param ctx the parse tree
	 */
	void enterFormal(CoolParser.FormalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#formal}.
	 * @param ctx the parse tree
	 */
	void exitFormal(CoolParser.FormalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(CoolParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(CoolParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(CoolParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(CoolParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#assignExpr}.
	 * @param ctx the parse tree
	 */
	void enterAssignExpr(CoolParser.AssignExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#assignExpr}.
	 * @param ctx the parse tree
	 */
	void exitAssignExpr(CoolParser.AssignExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#logicalExpr}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpr(CoolParser.LogicalExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#logicalExpr}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpr(CoolParser.LogicalExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#comparisonExpr}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpr(CoolParser.ComparisonExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#comparisonExpr}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpr(CoolParser.ComparisonExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#additiveExpr}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(CoolParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#additiveExpr}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(CoolParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpr(CoolParser.MultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpr(CoolParser.MultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(CoolParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(CoolParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link CoolParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(CoolParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link CoolParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(CoolParser.PrimaryExprContext ctx);
}