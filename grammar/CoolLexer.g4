lexer grammar CoolLexer;

// Keywords (case-insensitive for COOL)
CLASS     : [Cc][Ll][Aa][Ss][Ss];
IF        : [Ii][Ff];
THEN      : [Tt][Hh][Ee][Nn];
ELSE      : [Ee][Ll][Ss][Ee];
FI        : [Ff][Ii];
WHILE     : [Ww][Hh][Ii][Ll][Ee];
LOOP      : [Ll][Oo][Oo][Pp];
POOL      : [Pp][Oo][Oo][Ll];
TRUE      : 't' 'r' 'u' 'e';
FALSE     : 'f' 'a' 'l' 's' 'e';
INHERITS  : [Ii][Nn][Hh][Ee][Rr][Ii][Tt][Ss];
RETURN    : [Rr][Ee][Tt][Uu][Rr][Nn];

// Symbols and Operators
PLUS      : '+';
MINUS     : '-';
MULT      : '*';
DIV       : '/';
MOD       : '%';
EQUAL     : '=';
NE        : '!=';
LT        : '<';
GT        : '>';
LE        : '<=';
GE        : '>=';
AND       : '&&';
OR        : '||';
NOT       : '!';
ASSIGN    : '<-';
PLUSASSIGN: '+=';
MINUSASSIGN:'-=';
MULTASSIGN:'*=';
DIVASSIGN : '/=';
LPAREN    : '(';
RPAREN    : ')';
LBRACE    : '{';
RBRACE    : '}';
SEMI      : ';';
COLON     : ':';
DOT       : '.';
COMMA     : ',';

// Identifiers
ID        : [a-zA-Z][a-zA-Z0-9_]*;

// Literals
INT       : [0-9]+;
STRING    : '"' (~["\r\n])* '"';

// Comments (ignored)
SINGLE_COMMENT : '--' ~[\r\n]* -> skip;
MULTI_COMMENT  : '(*' (MULTI_COMMENT | .)*? '*)' -> skip;

// Whitespace (ignored)
WS        : [ \t\r\n]+ -> skip;

// Catch invalid characters
ERROR     : . ;