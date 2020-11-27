package ex2.proj;

import ast.*;

public class ExprVisitor implements Visitor {
    private String varDeclType;
    private String resReg;
    MethodContext methodContext; // to be initialized in every new method

    private void emit(String data) {
        //todo
    }

    private void appendWithIndent(String str) {
    }

    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        // examples: %sum = add i32 %a, %b
        //           %case = icmp slt i32 %a, %b


        // the result will be in resReg
        e.e1().accept(this);
        String e1Reg = resReg;

        // the result will be in resReg
        e.e2().accept(this);
        String e2Reg = resReg;

        String regTye = "";
        // case 1: && , + , - , *
        if (!infixSymbol.equals("<")) {
            // The result is the same type as the operands.
             regTye = methodContext.RegTypesMap.get(e1Reg);
        }
        // case 2: <
        else {
            // The result is the of type i1 (with value 1 or 0)
            regTye = "i1";
        }

        // save the result in a new temp reg
        String reg = methodContext.getNewReg();
        methodContext.RegTypesMap.put(reg,regTye);

        // comment for debug
        emit("\n\t; BinaryExpr: "+ infixSymbol);

        // get String matching the infixSymbol
        String infixSymbolStr = Utils.getStrForInfixSymbol(infixSymbol);

        // write the expression
        emit("\n\t" + reg + " = " + infixSymbolStr + " " + regTye + " " + e1Reg + ", " + e2Reg);

        // update resReg
        resReg = reg;
    }

    @Override
    public void visit(Program program) {
        program.mainClass().accept(this);

        for (ClassDecl classdecl : program.classDecls()) {

            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {

        if (classDecl.superName() != null) {

        }

        for (var fieldDecl : classDecl.fields()) {

            fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {

            methodDecl.accept(this);
        }

    }

    @Override
    public void visit(MainClass mainClass) {

        mainClass.mainStatement().accept(this);

    }

    @Override
    public void visit(MethodDecl methodDecl) {

        //todo: initialize methodContext

        methodDecl.returnType().accept(this);

        for (var formal : methodDecl.formals()) {

            formal.accept(this);
        }


        for (var varDecl : methodDecl.vardecls()) {

            varDecl.accept(this);
        }
        for (var stmt : methodDecl.body()) {

            stmt.accept(this);
        }

        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {

        formalArg.type().accept(this);

    }

    @Override
    public void visit(VarDecl varDecl) {
        // local variables

        // format the var name
        String localVarNameFormatted = Utils.FormatLocalVar(varDecl.name());

        // save the type string into varDeclType
        varDecl.type().accept(this);

        // get the allocation string for this type
        String typeAllocStr = Utils.getTypeStrForAlloc(varDeclType);

        // comment for debug
        emit("\n\t; local variable: name: " + varDecl.name() + ", type: "+ varDeclType + "%");

        // allocate on the stack
        emit("\n\t" + localVarNameFormatted + " = alloca " + typeAllocStr);

//        if (varDeclType.equals("classPointer")){
//            //TODO alloc Class On Stack
//        }

    }

    @Override
    public void visit(BlockStatement blockStatement) {

        for (var s : blockStatement.statements()) {
            s.accept(this);
        }

    }

    @Override
    public void visit(IfStatement ifStatement) {

        ifStatement.cond().accept(this);

        ifStatement.thencase().accept(this);

        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {

        whileStatement.cond().accept(this);

        whileStatement.body().accept(this);

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {

        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

        assignArrayStatement.index().accept(this);

        assignArrayStatement.rv().accept(this);
    }

    @Override
    public void visit(AndExpr e) {
        visitBinaryExpr(e, "&&");
    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e, "<");;
    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e, "+");;
    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e, "-");
    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e, "*");
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        e.arrayExpr().accept(this);

        e.indexExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);

    }

    @Override
    public void visit(MethodCallExpr e) {
        e.ownerExpr().accept(this);

        for (Expr arg : e.actuals()) {
            arg.accept(this);
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {

    }

    @Override
    public void visit(TrueExpr e) {

    }

    @Override
    public void visit(FalseExpr e) {

    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);

    }

    @Override
    public void visit(IntAstType t) {
        varDeclType = "int";
    }

    @Override
    public void visit(BoolAstType t) {
        varDeclType = "boolean";
    }

    @Override
    public void visit(IntArrayAstType t) {
        varDeclType = "array";
    }

    @Override
    public void visit(RefType t) {
        varDeclType = "classPointer";
    }
}
