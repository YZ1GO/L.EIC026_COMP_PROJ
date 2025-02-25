grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
RETURN : 'return' ;

VARARGS : '...'; // TODO Check 1.3 for later modifications
INTEGER : [0-9] ;
ID : [a-zA-Z]+ ;
BOOL : 'true' | 'false';

SINGLE_COMMENT : '//' .*? '\n' -> skip ;
MULTI_COMMENT : '/*' .*? '*/' -> skip ;
WS : [ \t\n\r\f]+ -> skip ;

program
    :  (importDeclaration)* classDecl EOF
    ;

importDeclaration
    : 'import' name=ID ( '.' ID )* ';'
    ;

classDecl
    : CLASS name=ID ( 'extends' ID )?
        '{'
            ( varDecl )* ( methodDecl )*
        '}'
    ;

varDecl
    : type name=ID ';'
    | type name=ID op='[' op=']' ';'
    ;

type
    : 'int' '[' ']'
    | 'int' VARARGS
    | 'boolean'
    | name='int' // name required to pass initial tests
    | name=ID
    ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        '(' param ')'
        '{' varDecl* stmt* '}'
    // TODO check if needed
    // | ('public')? type name=ID '(' ( type name=ID ( ',' type name=ID )* )? ')' '{' ( varDecl)* ( stmt )* 'return' expr ';' '}'
    // | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' name=ID ')' '{' ( varDecl )* ( stmt )* '}'
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
    | 'return' expr ';' #ReturnStmt
    ;

expr
    : '(' expr ')' #ParentExpr
    | 'new' 'int' '[' expr ']' #NewIntArrayExpr
    | 'new' name=ID '(' ( expr ( ',' expr )* )? ')' #NewObjectExpr
    | expr '.' 'length' #LengthExpr
    | expr '.' name=ID '(' ( expr ( ',' expr )* )? ')' #MethodCallExpr
    | expr '[' expr ']' #ArrayAccessExpr
    | 'this' #ThisExpr
    | op='!' expr #BinaryExpr
    | expr op=('*' | '/') expr #BinaryExpr
    | expr op=('+' | '-') expr #BinaryExpr
    | expr op='&&' expr #BinaryExpr
    | expr op='||' expr #BinaryExpr
    | value=INTEGER #IntegerLiteral 
    | value=BOOL #BooleanLiteral
    | name=ID #VarRefExpr 
    | '[' (expr ('.' expr)* )? ']' #ArrayLiteral
    ;

