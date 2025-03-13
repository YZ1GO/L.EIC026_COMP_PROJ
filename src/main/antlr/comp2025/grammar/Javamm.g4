grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
STATIC : 'static' ;
RETURN : 'return' ;

STRING : '"' (ESC_SEQ | ~["\\\r\n])* '"' ;
fragment ESC_SEQ : '\\' ["\\bfnrt] ;

VARARGS : '...';
INTEGER : [0-9]+ ;
BOOLEAN : 'true' | 'false';
ID : [a-zA-Z_$] [a-zA-Z0-9_$]* ;

SINGLE_COMMENT : '//' .*? '\n' -> skip ;
MULTI_COMMENT : '/*' .*? '*/' -> skip ;
WS : [ \t\n\r\f]+ -> skip ;

program
    :  (importDecl)* classDecl EOF
    ;

importDecl
    : 'import' name=ID ( '.' ID )* ';'
    ;

classDecl
    : CLASS name=ID ( 'extends' parent=ID )?
        '{'
            ( varDecl )* ( methodDecl )*
        '}'
    ;

varDecl
    : type name=ID ';'
    ;

type locals[boolean isArray=false, boolean isVarArgs=false]
    : name=INT VARARGS {$isArray=true; $isVarArgs=true;}
    | name=INT '[' ']' {$isArray=true;}
    | name=ID '[' ']' {$isArray=true;}
    | name=INT
    | name='boolean'
    | name='void'
    | name=ID
    ;

methodDecl locals[boolean isPublic=false, boolean isStatic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        '(' (param (',' param)*)? ')'
        '{' varDecl* stmt* '}'  #RegularMethod
    | (PUBLIC {$isPublic=true;})? STATIC {$isStatic=true;}
        type name=ID
        '(' param ')'
        '{' varDecl* stmt* '}'  #MainMethod
    ;

param
    : type name=ID
    ;

stmt
    : '{' stmt* '}' #BlockStmt
    | 'if' '(' expr ')' stmt ('else' stmt)? #IfStmt
    | 'while' '(' expr ')' stmt #WhileStmt
    | 'System.out.println' '(' expr ')' ';' #PrintStmt
    | expr ';' #ExprStmt
    | type name=ID '=' expr ';' #VarDeclStmt
    | type name=ID '[' expr ']' '=' expr ';' #ArrayDeclStmt
    | name=ID '[' expr ']' '=' expr ';' #ArrayAssignStmt
    | name=ID '.' name=ID '=' expr ';' #FieldAssignStmt
    | expr '=' expr ';' #AssignStmt
    | RETURN expr ';' #ReturnStmt
    ;

expr
    : '(' expr ')' #ParentExpr
    | 'new' 'int' '[' expr ']' #NewIntArrayExpr
    | 'new' name=ID '(' ( expr ( ',' expr )* )? ')' #NewObjectExpr
    | expr '[' expr ']' #ArrayAccessExpr
    | expr '.' 'length' #LengthExpr
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #MethodCallExpr
    | 'this' #ThisExpr
    | op='!' expr #UnaryNotExpr
    | expr op=('*' | '/') expr #BinaryExpr  // Changing 'Binary' will fail initial tests
    | expr op=('+' | '-') expr #BinaryExpr
    | expr op=('<' | '>' | '<=' | '>=') expr #BinaryExpr
    | expr op=('==' | '!=') expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | value=BOOLEAN #BooleanLiteral
    | value=STRING #StringLiteral
    | name=ID #VarRefExpr
    | '[' (expr (',' expr)* )? ']' #ArrayInit
    ;