parser grammar CoolParser;

options { tokenVocab=CoolLexer; }

program
    : classDef+ EOF
    ;

classDef
    : CLASS ID (INHERITS ID)? LBRACE (feature | statement)* RBRACE SEMI?
    ;

feature
    : ID COLON ID (ASSIGN expr)? SEMI
    | ID LPAREN (formal (COMMA formal)*)? RPAREN COLON ID LBRACE statement* RBRACE SEMI
    ;

formal
    : ID COLON ID
    ;

statement
    : expr SEMI
    | ID ASSIGN expr SEMI
    | IF expr THEN expr ELSE expr FI SEMI?
    | WHILE expr LOOP statement POOL
    ;

// Expression rules with proper precedence and associativity
expr
    : assignExpr
    ;

assignExpr
    : logicalExpr (ASSIGN | PLUSASSIGN | MINUSASSIGN | MULTASSIGN | DIVASSIGN) assignExpr  // Right-associative
    | logicalExpr
    ;

logicalExpr
    : comparisonExpr (AND | OR) comparisonExpr  // Left-associative
    | comparisonExpr
    ;

comparisonExpr
    : additiveExpr (LT | LE | GT | GE | EQUAL | NE) additiveExpr  // Left-associative
    | additiveExpr
    ;

additiveExpr
    : multiplicativeExpr (PLUS | MINUS) multiplicativeExpr  // Left-associative
    | multiplicativeExpr
    ;

multiplicativeExpr
    : unaryExpr (MULT | DIV | MOD) unaryExpr  // Left-associative
    | unaryExpr
    ;

unaryExpr
    : NOT unaryExpr  // Unary operator (highest precedence)
    | primaryExpr
    ;

primaryExpr
    : LPAREN expr RPAREN
    | ID LPAREN (expr (COMMA expr)*)? RPAREN
    | ID
    | INT
    | STRING
    | TRUE
    | FALSE
    ;