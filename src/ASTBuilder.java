import ast.*;
import org.antlr.v4.runtime.Token;

public class ASTBuilder extends CoolParserBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        ProgramNode program = new ProgramNode(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        for (CoolParser.ClassDefContext classCtx : ctx.classDef()) {
            ClassNode classNode = (ClassNode) visit(classCtx);
            program.addClass(classNode);
        }

        return program;
    }

    @Override
    public ASTNode visitClassDef(CoolParser.ClassDefContext ctx) {
        Token start = ctx.getStart();
        String className = ctx.ID(0).getText();
        String parentName = ctx.INHERITS() != null ? ctx.ID(1).getText() : null;

        ClassNode classNode = new ClassNode(
                start.getLine(),
                start.getCharPositionInLine(),
                className,
                parentName
        );

        // Add features
        for (CoolParser.FeatureContext featureCtx : ctx.feature()) {
            FeatureNode feature = (FeatureNode) visit(featureCtx);
            classNode.addFeature(feature);
        }

        // Add statements as initialization expressions
        for (CoolParser.StatementContext statementCtx : ctx.statement()) {
            // We'll treat them as special features or methods
            // Depends on the statement type
            if (statementCtx.ID() != null && statementCtx.ASSIGN() != null) {
                // Assignment statement - create attribute initialization
                String varName = statementCtx.ID().getText();
                ExpressionNode expr = (ExpressionNode) visit(statementCtx.expr(0));

                // Special method for class initialization
                MethodNode initMethod = new MethodNode(
                        statementCtx.getStart().getLine(),
                        statementCtx.getStart().getCharPositionInLine(),
                        "__init_" + varName,
                        "Void"
                );

                initMethod.addBodyExpression(new AssignmentNode(
                        statementCtx.getStart().getLine(),
                        statementCtx.getStart().getCharPositionInLine(),
                        varName,
                        expr,
                        AssignmentNode.AssignmentType.SIMPLE
                ));

                classNode.addFeature(initMethod);
            } else {
                // Other statements - add as an initialization method
                ExpressionNode expr = (ExpressionNode) visit(statementCtx);

                MethodNode initMethod = new MethodNode(
                        statementCtx.getStart().getLine(),
                        statementCtx.getStart().getCharPositionInLine(),
                        "__init_stmt_" + classNode.getFeatures().size(),
                        "Void"
                );

                initMethod.addBodyExpression(expr);
                classNode.addFeature(initMethod);
            }
        }

        return classNode;
    }

    @Override
    public ASTNode visitFeature(CoolParser.FeatureContext ctx) {
        Token start = ctx.getStart();
        String name = ctx.ID(0).getText();

        if (ctx.LPAREN() != null) {
            // Method definition
            String returnType = ctx.ID(ctx.ID().size() - 1).getText();
            MethodNode method = new MethodNode(start.getLine(), start.getCharPositionInLine(), name, returnType);

            // Add parameters
            if (ctx.formal() != null) {
                for (CoolParser.FormalContext formalCtx : ctx.formal()) {
                    FormalNode param = (FormalNode) visit(formalCtx);
                    method.addParameter(param);
                }
            }

            // Add method body
            if (ctx.statement() != null) {
                for (CoolParser.StatementContext stmtCtx : ctx.statement()) {
                    ExpressionNode expr = (ExpressionNode) visit(stmtCtx);
                    method.addBodyExpression(expr);
                }
            }

            return method;
        } else {
            // Attribute definition
            String type = ctx.ID(1).getText();
            ExpressionNode init = null;

            if (ctx.expr() != null) {
                init = (ExpressionNode) visit(ctx.expr());
            }

            return new AttributeNode(start.getLine(), start.getCharPositionInLine(), name, type, init);
        }
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        Token start = ctx.getStart();
        String name = ctx.ID(0).getText();
        String type = ctx.ID(1).getText();

        return new FormalNode(start.getLine(), start.getCharPositionInLine(), name, type);
    }

    @Override
    public ASTNode visitStatement(CoolParser.StatementContext ctx) {
        if (ctx.expr() != null && ctx.ID() == null && ctx.IF() == null && ctx.WHILE() == null) {
            // Simple expression statement
            return visit(ctx.expr(0));
        } else if (ctx.ID() != null && ctx.ASSIGN() != null) {
            // Assignment statement
            Token start = ctx.getStart();
            String varName = ctx.ID().getText();
            ExpressionNode expr = (ExpressionNode) visit(ctx.expr(0));

            return new AssignmentNode(
                    start.getLine(),
                    start.getCharPositionInLine(),
                    varName,
                    expr,
                    AssignmentNode.AssignmentType.SIMPLE
            );
        } else if (ctx.IF() != null) {
            // If statement
            Token start = ctx.getStart();

            // Make sure we properly get all the expressions
            ExpressionNode condition = (ExpressionNode) visit(ctx.expr(0));
            ExpressionNode thenExpr = (ExpressionNode) visit(ctx.expr(1));
            ExpressionNode elseExpr = (ExpressionNode) visit(ctx.expr(2));

            return new IfNode(start.getLine(), start.getCharPositionInLine(), condition, thenExpr, elseExpr);
        } else if (ctx.WHILE() != null) {
            // While statement
            Token start = ctx.getStart();
            ExpressionNode condition = (ExpressionNode) visit(ctx.expr(0));

            // For while loops, we visit the statement in the loop body
            ExpressionNode body;
            if (ctx.statement() != null) {
                body = (ExpressionNode) visit(ctx.statement());
            } else {
                // In case there's no body, create an empty statement
                body = new BooleanLiteralNode(start.getLine(), start.getCharPositionInLine(), true);
            }

            return new WhileNode(start.getLine(), start.getCharPositionInLine(), condition, body);
        }

        // Fallback - should not happen with valid COOL code
        return null;
    }

    @Override
    public ASTNode visitExpr(CoolParser.ExprContext ctx) {
        return visit(ctx.assignExpr());
    }

    @Override
    public ASTNode visitAssignExpr(CoolParser.AssignExprContext ctx) {
        if (ctx.assignExpr() != null) {
            // Assignment expression
            Token start = ctx.getStart();
            ExpressionNode leftExpr = (ExpressionNode) visit(ctx.logicalExpr());
            ExpressionNode rightExpr = (ExpressionNode) visit(ctx.assignExpr());

            // The left expression should be an identifier
            if (leftExpr instanceof IdentifierNode) {
                String varName = ((IdentifierNode) leftExpr).getName();

                AssignmentNode.AssignmentType type = AssignmentNode.AssignmentType.SIMPLE;

                if (ctx.PLUSASSIGN() != null) {
                    type = AssignmentNode.AssignmentType.PLUS_ASSIGN;
                } else if (ctx.MINUSASSIGN() != null) {
                    type = AssignmentNode.AssignmentType.MINUS_ASSIGN;
                } else if (ctx.MULTASSIGN() != null) {
                    type = AssignmentNode.AssignmentType.MULT_ASSIGN;
                } else if (ctx.DIVASSIGN() != null) {
                    type = AssignmentNode.AssignmentType.DIV_ASSIGN;
                }

                return new AssignmentNode(
                        start.getLine(),
                        start.getCharPositionInLine(),
                        varName,
                        rightExpr,
                        type
                );
            }
        }

        // Not an assignment, just return the logical expression
        return visit(ctx.logicalExpr());
    }

    @Override
    public ASTNode visitLogicalExpr(CoolParser.LogicalExprContext ctx) {
        if (ctx.AND() != null || ctx.OR() != null) {
            Token start = ctx.getStart();
            ExpressionNode left = (ExpressionNode) visit(ctx.comparisonExpr(0));
            ExpressionNode right = (ExpressionNode) visit(ctx.comparisonExpr(1));

            BinaryOperationNode.Operator op;
            if (ctx.AND() != null) {
                op = BinaryOperationNode.Operator.AND;
            } else {
                op = BinaryOperationNode.Operator.OR;
            }

            return new BinaryOperationNode(start.getLine(), start.getCharPositionInLine(), op, left, right);
        }

        return visit(ctx.comparisonExpr(0));
    }

    @Override
    public ASTNode visitComparisonExpr(CoolParser.ComparisonExprContext ctx) {
        if (ctx.LT() != null || ctx.LE() != null || ctx.GT() != null ||
                ctx.GE() != null || ctx.EQUAL() != null || ctx.NE() != null) {

            Token start = ctx.getStart();
            ExpressionNode left = (ExpressionNode) visit(ctx.additiveExpr(0));
            ExpressionNode right = (ExpressionNode) visit(ctx.additiveExpr(1));

            BinaryOperationNode.Operator op;
            if (ctx.LT() != null) {
                op = BinaryOperationNode.Operator.LT;
            } else if (ctx.LE() != null) {
                op = BinaryOperationNode.Operator.LE;
            } else if (ctx.GT() != null) {
                op = BinaryOperationNode.Operator.GT;
            } else if (ctx.GE() != null) {
                op = BinaryOperationNode.Operator.GE;
            } else if (ctx.EQUAL() != null) {
                op = BinaryOperationNode.Operator.EQ;
            } else {
                op = BinaryOperationNode.Operator.NE;
            }

            return new BinaryOperationNode(start.getLine(), start.getCharPositionInLine(), op, left, right);
        }

        return visit(ctx.additiveExpr(0));
    }

    @Override
    public ASTNode visitAdditiveExpr(CoolParser.AdditiveExprContext ctx) {
        if (ctx.PLUS() != null || ctx.MINUS() != null) {
            Token start = ctx.getStart();
            ExpressionNode left = (ExpressionNode) visit(ctx.multiplicativeExpr(0));
            ExpressionNode right = (ExpressionNode) visit(ctx.multiplicativeExpr(1));

            BinaryOperationNode.Operator op;
            if (ctx.PLUS() != null) {
                op = BinaryOperationNode.Operator.PLUS;
            } else {
                op = BinaryOperationNode.Operator.MINUS;
            }

            return new BinaryOperationNode(start.getLine(), start.getCharPositionInLine(), op, left, right);
        }

        return visit(ctx.multiplicativeExpr(0));
    }

    @Override
    public ASTNode visitMultiplicativeExpr(CoolParser.MultiplicativeExprContext ctx) {
        if (ctx.MULT() != null || ctx.DIV() != null || ctx.MOD() != null) {
            Token start = ctx.getStart();
            ExpressionNode left = (ExpressionNode) visit(ctx.unaryExpr(0));
            ExpressionNode right = (ExpressionNode) visit(ctx.unaryExpr(1));

            BinaryOperationNode.Operator op;
            if (ctx.MULT() != null) {
                op = BinaryOperationNode.Operator.MULTIPLY;
            } else if (ctx.DIV() != null) {
                op = BinaryOperationNode.Operator.DIVIDE;
            } else {
                op = BinaryOperationNode.Operator.MOD;
            }

            return new BinaryOperationNode(start.getLine(), start.getCharPositionInLine(), op, left, right);
        }

        return visit(ctx.unaryExpr(0));
    }

    @Override
    public ASTNode visitUnaryExpr(CoolParser.UnaryExprContext ctx) {
        if (ctx.NOT() != null) {
            Token start = ctx.getStart();
            ExpressionNode operand = (ExpressionNode) visit(ctx.unaryExpr());

            return new UnaryOperationNode(
                    start.getLine(),
                    start.getCharPositionInLine(),
                    UnaryOperationNode.Operator.NOT,
                    operand
            );
        }

        return visit(ctx.primaryExpr());
    }

    @Override
    public ASTNode visitPrimaryExpr(CoolParser.PrimaryExprContext ctx) {
        Token start = ctx.getStart();

        if (ctx.LPAREN() != null && ctx.RPAREN() != null && ctx.expr() != null) {
            // Parenthesized expression
            return visit(ctx.expr(0)); // Use index 0 to get the first expression
        } else if (ctx.ID() != null && ctx.LPAREN() != null) {
            // Method call
            String methodName = ctx.ID().getText();
            MethodCallNode methodCall = new MethodCallNode(
                    start.getLine(),
                    start.getCharPositionInLine(),
                    null, // Call on self
                    methodName
            );

            // Add arguments - iterate through expressions using indices
            for (int i = 0; i < ctx.expr().size(); i++) {
                ExpressionNode arg = (ExpressionNode) visit(ctx.expr(i));
                methodCall.addArgument(arg);
            }

            return methodCall;
        } else if (ctx.ID() != null) {
            // Identifier
            String name = ctx.ID().getText();
            return new IdentifierNode(start.getLine(), start.getCharPositionInLine(), name);
        } else if (ctx.INT() != null) {
            // Integer literal
            int value = Integer.parseInt(ctx.INT().getText());
            return new IntegerLiteralNode(start.getLine(), start.getCharPositionInLine(), value);
        } else if (ctx.STRING() != null) {
            // String literal - remove the quotes
            String text = ctx.STRING().getText();
            String value = text.substring(1, text.length() - 1);
            return new StringLiteralNode(start.getLine(), start.getCharPositionInLine(), value);
        } else if (ctx.TRUE() != null) {
            // Boolean true
            return new BooleanLiteralNode(start.getLine(), start.getCharPositionInLine(), true);
        } else if (ctx.FALSE() != null) {
            // Boolean false
            return new BooleanLiteralNode(start.getLine(), start.getCharPositionInLine(), false);
        }

        // Default case (should not happen with valid COOL code)
        return null;
    }
}