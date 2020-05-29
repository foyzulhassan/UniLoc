package edu.utsa.gradlediff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;

public class TypeUtil {
	
	public static int getExpressionType(ASTNode node)
	{
		int type=0;
		
		if(node instanceof MethodCallExpression)
		{
			type=2;
		}
		
		else if((node instanceof ArgumentListExpression))
		{
			type=3;
		}
		else if(node instanceof ClosureExpression)
		{
			type=4;
		}
		else if(node instanceof MapExpression && !(node instanceof NamedArgumentListExpression))
		{
			type=5;
		}
		else if(node instanceof BlockStatement)
		{
			type=1;
		}
		else if(node instanceof ForStatement)
		{
			type=6;
		}
		else if(node instanceof WhileStatement)
		{
			type=7;
		}
		else if(node instanceof DoWhileStatement)
		{
			type=8;
		}
		else if(node instanceof IfStatement)
		{
			type=9;
		}
		else if(node instanceof ExpressionStatement)
		{
			type=10;
		}
		else if(node instanceof ReturnStatement)
		{
			type=11;
		}
		else if(node instanceof AssertStatement)
		{
			type=12;
		}
		else if(node instanceof TryCatchStatement)
		{
			type=13;
		}
		else if(node instanceof SwitchStatement)
		{
			type=14;
		}
		else if(node instanceof CaseStatement)
		{
			type=15;
		}
		else if(node instanceof BreakStatement)
		{
			type=16;
		}
		else if(node instanceof ContinueStatement)
		{
			type=17;
		}
		else if(node instanceof ThrowStatement)
		{
			type=18;
		}
		else if(node instanceof SynchronizedStatement)
		{
			type=19;
		}
		else if(node instanceof CatchStatement)
		{
			type=20;
		}
		else if(node instanceof StaticMethodCallExpression)
		{
			type=21;
		}
		else if(node instanceof ConstructorCallExpression)
		{
			type=22;
		}		
		else if(node instanceof ElvisOperatorExpression)
		{
			type=24;
		}
		else if(node instanceof TernaryExpression)
		{
			type=23;
		}
		else if(node instanceof BinaryExpression  && !(node instanceof DeclarationExpression))
		{
			type=25;
		}
		else if(node instanceof PrefixExpression)
		{
			type=26;
		}
		else if(node instanceof PostfixExpression)
		{
			type=27;
		}
		else if((node instanceof BooleanExpression) && !(node instanceof NotExpression))
		{
			type=28;
		}
		else if(node instanceof TupleExpression)
		{
			type=29;
		}
		else if(node instanceof MapEntryExpression)
		{
			type=30;
		}
		else if(node instanceof ListExpression)
		{
			type=31;
		}
		else if(node instanceof RangeExpression)
		{
			type=32;
		}		
		else if(node instanceof AttributeExpression)
		{
			type=34;
		}
		else if(node instanceof PropertyExpression)
		{
			type=33;
		}
		else if(node instanceof FieldExpression)
		{
			type=35;
		}
		else if(node instanceof MethodPointerExpression)
		{
			type=36;
		}
		else if(node instanceof ConstantExpression)
		{
			type=37;
		}
		else if(node instanceof ClassExpression)
		{
			type=38;
		}
		else if(node instanceof VariableExpression)
		{
			type=39;
		}
		else if(node instanceof DeclarationExpression)
		{
			type=40;
		}
		else if(node instanceof GStringExpression)
		{
			type=41;
		}
		else if(node instanceof ArrayExpression)
		{
			type=42;
		}
		else if(node instanceof SpreadExpression)
		{
			type=43;
		}
		else if(node instanceof NotExpression)
		{
			type=44;
		}
		else if(node instanceof UnaryMinusExpression)
		{
			type=45;
		}
		else if(node instanceof UnaryPlusExpression)
		{
			type=46;
		}
		else if(node instanceof BitwiseNegationExpression)
		{
			type=47;
		}
		else if(node instanceof CastExpression)
		{
			type=48;
		}
		else if(node instanceof ClosureListExpression)
		{
			type=49;
		}
		else if(node instanceof BytecodeExpression)
		{
			type=50;
		}	
		
		else if(node instanceof NamedArgumentListExpression)
		{
			type=51;
		}	
		

		return type;
	}
	
	public static String getExpressionName(int type)
	{
		String typestring = "unknown";	
		
		if(type==1)
		{
			typestring="BlockStatement";
		}
		
		else if(type==2)
		{
			typestring="MethodCallExpression";
		}
		
		else if(type==3)
		{
			
			typestring="ArgumentListExpression";
		}
		else if(type==4)
		{
			
			typestring="ClosureExpression";
		}
		else if(type==5)
		{
		
			typestring="MapExpression";
		}
		
		else if(type==6)
		{
			
			typestring="ForStatement";

		}
		else if(type==7)
		{			
			typestring="WhileStatement";
		}
		else if(type==8)
		{
			
			typestring="DoWhileStatement";
		}
		else if(type==9)
		{
			
			typestring="IfStatement";
		}
		else if(type==10)
		{
			
			typestring="ExpressionStatement";
		}
		else if(type==11)
		{
			
			typestring="ReturnStatement";
		}
		else if(type==12)
		{
			
			typestring="AssertStatement";
		}
		else if(type==13)
		{
			typestring="TryCatchStatement";
		}
		else if(type==14)
		{
			
			typestring="SwitchStatement";
		}
		else if(type==15)
		{
			
			typestring="CaseStatement";
		}
		else if(type==16)
		{
			
			typestring="BreakStatement";
		}
		else if(type==17)
		{
			
			typestring="ContinueStatement";
		}
		else if(type==18)
		{
			
			typestring="ThrowStatement";
		}
		else if(type==19)
		{
			
			typestring="SynchronizedStatement";
		}
		else if(type==20)
		{
			
			typestring="CatchStatement";
		}
		else if(type==21)
		{
			
			typestring="StaticMethodCallExpression";
		}
		else if(type==22)
		{
			
			typestring="ConstructorCallExpression";
		}
		else if(type==23)
		{
			
			typestring="TernaryExpression";
		}
		else if(type==24)
		{
			
			typestring="ElvisOperatorExpression";
		}
		else if(type==25)
		{
			
			typestring="BinaryExpression";
		}
		else if(type==26)
		{
			
			typestring="PrefixExpression";
		}
		else if(type==27)
		{
			
			typestring="PostfixExpression";
		}
		else if(type==28)
		{
			
			typestring="BooleanExpression";
		}
		else if(type==29)
		{
			typestring="TupleExpression";
		}
		else if(type==30)
		{
			
			typestring="MapEntryExpression";
		}
		else if(type==31)
		{
			
			typestring="ListExpression";
		}
		else if(type==32)
		{
			
			typestring="RangeExpression";
		}
		else if(type==33)
		{
			
			typestring="PropertyExpression";
		}
		else if(type==34)
		{
			
			typestring="AttributeExpression";
		}
		else if(type==35)
		{
			
			typestring="FieldExpression";
		}
		else if(type==36)
		{
			
			typestring="MethodPointerExpression";
		}
		else if(type==37)
		{
			
			typestring="ConstantExpression";
		}
		else if(type==38)
		{
			
			typestring="ClassExpression";
		}
		else if(type==39)
		{
			
			typestring="VariableExpression";
		}
		else if(type==40)
		{
			
			typestring="DeclarationExpression";
		}
		else if(type==41)
		{
			
			typestring="GStringExpression";
		}
		else if(type==42)
		{
			
			typestring="ArrayExpression";
		}
		else if(type==43)
		{
			type=43;
			typestring="SpreadExpression";
		}
		else if(type==44)
		{
			
			typestring="NotExpression";
		}
		else if(type==45)
		{
			
			typestring="UnaryMinusExpression";
		}
		else if(type==46)
		{
			
			typestring="UnaryPlusExpression";
		}
		else if(type==47)
		{
			
			typestring="BitwiseNegationExpression";
		}
		else if(type==48)
		{
			
			typestring="CastExpression";
		}
		else if(type==49)
		{
			
			typestring="ClosureListExpression";
		}
		else if(type==50)
		{
			
			typestring="BytecodeExpression";
		}	
		
		else if(type==51)
		{
			
			typestring="NamedArgumentListExpression";
		}	
		

		return typestring;
	}

}
