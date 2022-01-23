package llp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import javax.xml.soap.Text;

import org.apache.poi.hslf.dev.SlideAndNotesAtomListing;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import TestCaseUtile.ASTUtil;




public class test {
	
	//public static Map<Integer, DataNode> Nodes = new HashMap<Integer, DataNode>();
	public static int ID = 0;//节点遍历时给节点编号，从0开始，注意ID必须得是个全局变量
	
	//获取该节点的直接子节点，也就是深度为1的子节点，参数为节点以及节点的编号
	public static void getDirectChildren(ASTNode node, int label, Map<Integer, DataNode> Nodes){
		
		//先创建一个节点数据结构
		DataNode myNode = new DataNode();
		Nodes.put(label, myNode);
		myNode.label = label;
		myNode.node = node;
		myNode.numberOfToken = getNumberOfTokens(node);
		myNode.nodeType = node.getClass().toString();
		myNode.attachedStatementType = getAttachedStatementInformation(node);
		getContraolInformation(myNode);
		List listProperty = node.structuralPropertiesForType();
		

		boolean hasChildren = false;
		for(int i = 0; i < listProperty.size(); i++){
			StructuralPropertyDescriptor propertyDescriptor = (StructuralPropertyDescriptor) listProperty.get(i);
			if(propertyDescriptor instanceof ChildListPropertyDescriptor){//ASTNode列表
				ChildListPropertyDescriptor childListPropertyDescriptor = (ChildListPropertyDescriptor)propertyDescriptor;
				Object children = node.getStructuralProperty(childListPropertyDescriptor);
				List<ASTNode> childrenNodes = (List<ASTNode>)children;
				for(ASTNode childNode: childrenNodes){
					//获取所有节点
					if(childNode == null)
						continue;
					hasChildren = true;
					myNode.childrenNodes.add(childNode);
					myNode.childrenLables.add((++ID));
					getDirectChildren(childNode, ID, Nodes);//继续递归
					//System.out.println("childrenList:   "+childNode+"   "+childNode.getClass());
				}
				
			}else if(propertyDescriptor instanceof ChildPropertyDescriptor){//一个ASTNode
				ChildPropertyDescriptor childPropertyDescriptor = (ChildPropertyDescriptor)propertyDescriptor;
				Object child = node.getStructuralProperty(childPropertyDescriptor);
				ASTNode childNode = (ASTNode)child;
				if(childNode == null)
					continue;
				hasChildren = true;
				//获取了这个节点
				myNode.childrenNodes.add(childNode);
				myNode.childrenLables.add((++ID));
				getDirectChildren(childNode, ID, Nodes);//继续递归
				
				//System.out.println("child:   "+childNode +"  "+childNode.getClass());
			}
		}
		if(hasChildren){
			//进行递归子节点
			myNode.isLeaf = false;
			int cnt = 0;
			for(Integer it: myNode.childrenLables){
				DataNode dataNode = Nodes.get(it);
				cnt += dataNode.numberOfToken2;
			}
			myNode.numberOfToken2 = cnt;
		}
		else{
			//结束，是叶子结点
			myNode.isLeaf = true;
			myNode.numberOfToken2 = 1;
		}
	}
	
	public static int getNumberOfTokens(ASTNode node){
		String string = node.toString();
		int cnt = 0;
		for(int i = 0; i < string.length(); i++){
			int value = Integer.valueOf(string.charAt(i));
			if(value >= 1 && value <= 127){
				cnt++;
			}
		}
		return cnt;
	}
	
	

	
	
	
	// 获取节点的control信息, if, enhancedfor, for, do, while,保存在logicPostList中
	public static void getContraolInformation(DataNode dataNode){
		ASTNode ansNode = null;
		ASTNode tp = dataNode.node;
		//System.out.println(node.getParent());
		while(tp.getParent() != null && !(tp.getParent() instanceof CompilationUnit)){
			tp = tp.getParent();
			if(tp instanceof IfStatement){
				dataNode.logicPoseList.add(1);
			}
			else if(tp instanceof EnhancedForStatement){
				dataNode.logicPoseList.add(2);
				
			}else if(tp instanceof ForStatement){
				dataNode.logicPoseList.add(3);
			}
			else if(tp instanceof DoStatement){
				dataNode.logicPoseList.add(4);
			}
			else if(tp instanceof WhileStatement){
				dataNode.logicPoseList.add(5);
			}
		}
		dataNode.logicPoseList.add(6);//这个是我改的，6代表最外围.2018-11-28
	}
	
	
	
	// 输出节点的一些信息
	public static void output(Map<Integer, DataNode> nodes){
		System.out.println("节点的总个数为:  "+nodes.size());
		System.out.println("print  all ndoes----------------start");
		for(Integer it: nodes.keySet()){
			DataNode dn = nodes.get(it);
			System.out.println("label  :"+dn.label);
			System.out.println("节点类型   ："+ dn.node.getClass());
			System.out.println("节点内容   : "+dn.node);
			System.out.println();
			System.out.println();
		}
		System.out.println("print  all ndoes----------------end");
		System.out.println("print relation -----------------start ");
		for(Integer it: nodes.keySet()){
			DataNode dn = nodes.get(it);
			System.out.println("current label  :"+dn.label);
			System.out.println("节点类型   ："+ dn.node.getClass());
			System.out.println("节点内容   : "+dn.node);
			System.out.println("节点token1数量   : "+ dn.numberOfToken);
			System.out.println("节点token2数量   : "+ dn.numberOfToken2);
			System.out.println("是否是叶子节点 ： "+dn.isLeaf);
			System.out.println("子节点编号及内容:");
			for(Integer itt: dn.childrenLables){
				System.out.println(itt);
				System.out.println(nodes.get(itt).node);
			}
//			System.out.println("逻辑关系");
//			if(dn.logicPoseList.size() > 0){
//				for(Integer i: dn.logicPoseList){
//					if(i.intValue() == 1){
//						System.out.print("if ");
//					}
//					else if(i.intValue() == 2){
//						System.out.println("enfor ");
//					}
//					else if(i.intValue() == 3){
//						System.out.print("for ");
//					}else if(i.intValue() == 4){
//						System.out.print("do ");
//					}
//				}
//				System.out.println();
//			}
			System.out.println("节点类型： "+ dn.nodeType);
			System.out.println("附属statement类型: "+ dn.attachedStatementType);
			System.out.println();
			System.out.println();
		}
		
	}
	
	public static double cosSimilarity(List<Integer> list1, List<Integer> list2){
		int len1 = list1.size();
		int len2 = list2.size();
		if(len1 == 0 || len2 == 0)
			return 0;
		int minLen = Math.min(len1, len2);
		
		double upper = 0.0;
		for(int i = 0; i < minLen; i++){
			upper += (list1.get(i) * list2.get(i));
		}
		
		
		double down = 0.0;
		double absList1 = 0.0;
		for(int i = 0; i < len1; i++){
			absList1 += (list1.get(i) * list1.get(i)); 
		}
		absList1 = Math.sqrt(absList1);
		
		double absList2 = 0.0;
		for(int i = 0; i < len2; i++){
			absList2 += (list2.get(i) * list2.get(i));
		}
		absList2 = Math.sqrt(absList2);
		
		down = absList1 * absList2;
		
		
		return (1.0 * upper / down);
	}
	
	
	public static double getScore(DataNode node1, DataNode node2){
		if(!node1.nodeType.equals(node2.nodeType)){
			return 0;
		}
		double score = 0.0;
		
		//logic similarity 
		score += cosSimilarity(node1.logicPoseList, node2.logicPoseList);
		
		//subtree similarity
		//-------------------------
		return score;
	}
	
	
	// 返回statement的类型名称，不是statement的话返回"null"
	public  static String getParentStatementInformation(ASTNode node){
		if((node instanceof AssertStatement) || (node instanceof BreakStatement)
				|| (node instanceof ConstructorInvocation) || (node instanceof ContinueStatement)
				|| (node instanceof DoStatement) || (node instanceof EmptyStatement)
				|| (node instanceof EnhancedForStatement) || (node instanceof ExpressionStatement)
				|| (node instanceof ForStatement) || (node instanceof IfStatement)
				|| (node instanceof LabeledStatement) || (node instanceof ReturnStatement)
				||  (node instanceof SuperConstructorInvocation) || (node instanceof SwitchCase)
				||  (node instanceof SwitchStatement) || (node instanceof SynchronizedStatement)
				|| (node instanceof ThrowStatement) || (node instanceof TryStatement)
				|| (node instanceof TypeDeclarationStatement) || (node instanceof VariableDeclarationStatement)
				|| (node instanceof WhileStatement )){
			return node.getClass().toString();
		}
		return "null";
	}
	
	
	//找到node所属的statement类型，返回类型的名字，找不到返回"null"
	public static String getAttachedStatementInformation(ASTNode node){
		String result = getParentStatementInformation(node);
		if(!result.equals("null")){
			return result;
		}
		ASTNode tp = node;
		//System.out.println(node.getParent());
		while(tp.getParent() != null && !(tp.getParent() instanceof CompilationUnit)){
			tp = tp.getParent();
			result = getParentStatementInformation(tp);
			if(!result.equals("null")){
				return result;
			}
		}
		return "null";
	}
	
	
	//找到最相似的那个节点
	public static DataNode  getMostSimiarNode(DataNode ori, List<Map<Integer, DataNode>> candiates){
		double maxScore = -1;
		DataNode targetNode = null;
		for(Map<Integer, DataNode> nodes: candiates){
			for(Integer label: nodes.keySet()){
				DataNode node = nodes.get(label);
				node.score = getScore(ori, node);
				if(maxScore < node.score){
					maxScore = node.score;
					targetNode = node;
				}
			}
		}
		return targetNode;
	}
	
	
	// 判断是否有相同的control信息， if while for，比较前6位
	public static boolean hasSameControlInformation(DataNode node1, DataNode node2){
		int len1 = node1.logicPoseList.size();
		int len2 = node2.logicPoseList.size();
		if(len1 > 6) len1 = 6;
		if(len2 > 6) len2 = 6;
		if(len1 != len2)
			return false;
		for(int i = 0; i < len1; i++){
			if(node1.logicPoseList.get(i) != node2.logicPoseList.get(i)){
				return false;
			}
		}
		return true;
	}
	
	// 判断能否用node2来替换node1生成新程序
	// 判断条件1： node本身的节点类型相同
	// 判断条件2： node所属的statement类型相同
	// 判断条件3： control信息相同
	public static boolean couldSubtitue(DataNode node1, DataNode node2)
	{
		
		if(node1.nodeType.equals(node2.nodeType))//如果type都不一样就不匹配了
		{
//			if(node1.node.toString().trim().equals(node2.node.toString().trim()))
//			{
//				return false;//完全一样自然没必要匹配了
//			}
//			else
//			{}
			
//			if(!node1.node.toString().trim().equals("b * c"))
//			{
//				return false;
//			}						
//			if(!node1.nodeType.toString().trim().equals("class org.eclipse.jdt.core.dom.InfixExpression"))
//			{
//				return false;
//			}
//			else
//			{
////				System.out.println(node1.node.toString());
////				System.out.println(node2.node.toString());
//			}//这一部分是加速测试用的
//			
			
			if(node1.nodeType.toString().trim().equals("class org.eclipse.jdt.core.dom.InfixExpression")||node1.nodeType.toString().trim().equals("class org.eclipse.jdt.core.dom.NumberLiteral"))
			{
				return true;//算式和数字可以全场匹配
			}
			else
			{}
			
			if(node1.nodeType.toString().trim().equals("class org.eclipse.jdt.core.dom.SimpleName"))
			{
				return false;//不管变量了
//				if(llpmethod.sameOrNot(Experiment.globalVariablesRelation, node1.node.toString().trim(),node2.node.toString().trim()))
//				{
//					if(Experiment.tmpVariables.contains(node2.node.toString().trim()))//如果已经用这个变量尝试过替换了，就没必要了
//					{
//						return false;
//					}
//					else
//					{
//						Experiment.tmpVariables.add(node2.node.toString().trim());
//						return true;//同类型的变量全场匹配，这个地方有待修改，因为会出现大量重复的尝试！
//					}
//					
//				}
//				else
//				{
//					return false;
//				}
//				
//				if(llpmethod.sameOrNot(Experiment.globalVariablesRelation, node1.nodeType.toString().trim(),node2.nodeType.toString().trim()))
//				{
//					return true;//同类型的变量全场匹配，这个地方有待修改，因为会出现大量重复的尝试！//20181221孙锐改好了，变量和算符用老方法遍历替换
//				}
//				else
//				{
//					return false;
//				}
				
			}
			else
			{}
			
			if(node1.attachedStatementType.equals(node2.attachedStatementType))
			{
				//判断逻辑信息
				if(hasSameControlInformation(node1, node2) && !hasSameContent(node1.node.toString(), node2.node.toString()))
					{
//						if(node1.nodeType.equals("class org.eclipse.jdt.core.dom.SimpleName")&&Experiment.oricount!=Experiment.cancount)
////					    if(node1.nodeType.equals("class org.eclipse.jdt.core.dom.SimpleName"))
//						{
//							return false;
//						}
//						else
//						{
//							return true;
//						}
					return true;
					
					}
				else 
				{
					return false;
				}
			}			
		
		   else
		   {
			return false;
		   }
		}
		else
		{
		  return false;
		}
		
	}
	
	public static boolean hasSameContent(String str1, String str2){
		String[] strs1 = str1.split(" ");
		String[] strs2 = str2.split(" ");
		String finalStr1 = "";
		String finalStr2 = "";
		for(String str: strs1){
			if(str.length() > 0)
				finalStr1 = finalStr1 + str;
		}
		
		for(String str: strs2){
			if(str.length() > 0){
				finalStr2 = finalStr2 + str;
			}
		}
		
		if(finalStr1.equals(finalStr2)){
			return true;
		}
		
		return false;
	}
	
	//2020062029改成图匹配的算法，新增几个函数
	public static DataNode getParentNode(DataNode nd,Map<Integer, DataNode> sourceNodes)
	{
		for(int i =0;i< sourceNodes.size();i++)
		{
			List<Integer> childrenLables = sourceNodes.get(i).childrenLables;
			for(int j = 0; j< childrenLables.size();j++)
			{
				if (childrenLables.get(j)==nd.label)
				{
					return sourceNodes.get(i);
				}
			}
		}
		return null;
	}
	
	

	
	
	
	public static List<String> GetAncestorType(DataNode nd,Map<Integer, DataNode> sourceNodes)
	{
		 List<String> NodeTypeList = new ArrayList<>();
		while(getParentNode(nd,sourceNodes)!=null)
		{
			nd = getParentNode(nd,sourceNodes);
			//这里我不要block了，因为if后面有没有{}不影响语义，但是会多出来一层block节点
			if(!nd.nodeType.toString().equals("class org.eclipse.jdt.core.dom.Block")
					&&(!nd.nodeType.toString().equals("class org.eclipse.jdt.core.dom.TryStatement"))
					){
			NodeTypeList.add(nd.nodeType);
			}
		}
		return NodeTypeList;
	}
	
	
	public static int GetStartLabel(Map<Integer, DataNode> sourceNodes)
	{
		
		for(int i = 0 ; i< sourceNodes.size();i++){
			if(sourceNodes.get(i).nodeType.equals("class org.eclipse.jdt.core.dom.Block"))
			{return i;}
		}
		return 0;
	}
	
	
	public static boolean SameTreeOrNot(DataNode nd1, Map<Integer, DataNode> sourceNodes1, DataNode nd2, Map<Integer, DataNode> sourceNodes2)
	{
		//20210311经过初步试验后决定修改一下：把InfixExpression看成一个点，亦即，如果两个节点都是表达式，则认为二者是一样的
				if(nd1.nodeType.contains("InfixExpression")&&nd2.nodeType.contains("InfixExpression"))
				{
					return true;
				}
		
		
		if(nd1.numberOfToken2!=nd2.numberOfToken2)
			return false;
//		DataNode nd1 = sourceNodes1.get(0);
//		DataNode nd2 = sourceNodes2.get(0);
		
		Boolean result = true;
		int c1 = nd1.childrenLables.size();
		int c2 = nd2.childrenLables.size();		
		
		if(c1==c2)
		{
                for(int i = 0 ; i < c1;i++)
                {
                	result = result&&SameTreeOrNot(sourceNodes1.get(nd1.childrenLables.get(i)),sourceNodes1,sourceNodes2.get(nd2.childrenLables.get(i)),sourceNodes2);
                }					
		}
		else
		{
			return false;
		}		
		return result;
	}
	
	
	
	public static  List<DataNode> GetSubTreeNodes (DataNode nd,Map<Integer, DataNode> sourceNodes,  List<DataNode> ndlist)
	{
		 ndlist.add(nd);
		 int c = nd.childrenLables.size();
		 for(int i = 0 ; i < c; i ++)
		 {
			 DataNode tmp = sourceNodes.get(nd.childrenLables.get(i));
			
			 ndlist = GetSubTreeNodes (tmp,sourceNodes, ndlist);
		 }
		 return ndlist;
	}
	
	public static boolean SameTreeSubOrNot(DataNode nd1, Map<Integer, DataNode> sourceNodes1, DataNode nd2, Map<Integer, DataNode> sourceNodes2)
	{	
		//20210311经过初步试验后决定修改一下：把InfixExpression看成一个点，亦即，如果两个节点都是表达式，则认为二者是一样的
		if(nd1.nodeType.contains("InfixExpression")&&nd2.nodeType.contains("InfixExpression"))
		{
			return true;
		}
		
		if(nd1.numberOfToken2 >= nd2.numberOfToken2)
		{return false;}
		
		
		 List<DataNode> nd2list = new ArrayList<>();
		 nd2list = GetSubTreeNodes(nd2,sourceNodes2,nd2list);
		 for(int i=0;i<nd2list.size();i++)
		 {
			 DataNode temp = nd2list.get(i);
			 if(SameTreeOrNot(nd1,sourceNodes1,temp,sourceNodes2))
			 {
				 return true;
			 }
			 else
			 {
				 //什么也不做
			 }
		 }
		 
		 
	     return false;
	}
	
	
//	public static int SameTreeExcOneOrNot(DataNode nd1, Map<Integer, DataNode> sourceNodes1, DataNode nd2, Map<Integer, DataNode> sourceNodes2,int dif)
//	{
//		
//		if(dif>1)//这行代码貌似是死的，在这个位置永远不会出现dif大于2的情况
//			{return dif;}
//		
//		int c1 = nd1.childrenLables.size();
//		int c2 = nd2.childrenLables.size();	
//		
//		
//			if(c1==c2)
//			{
//				for(int i = 0 ; i<c1;i++)
//				{
//					dif += SameTreeExcOneOrNot(sourceNodes1.get(nd1.childrenLables.get(i)),sourceNodes1,sourceNodes2.get(nd2.childrenLables.get(i)),sourceNodes2,dif);
//			  	}				
//				
//			}
//			else
//			{
//				if(dif==1)
//				{
//				return dif+1;	
//				}
//				else
//				{
//					dif++;
//				}
//			}
//		return dif;
//	}
	
	public static boolean SameTreeExcThisOrNot(DataNode nd1, Map<Integer, DataNode> sourceNodes1, DataNode nd2, Map<Integer, DataNode> sourceNodes2,int Exclabel)
	{
		//20210311经过初步试验后决定修改一下：把InfixExpression看成一个点，亦即，如果两个节点都是表达式，则认为二者是一样的
				if(nd1.nodeType.contains("InfixExpression")&&nd2.nodeType.contains("InfixExpression"))
				{
					return true;
				}
		
		
        
		if(nd1.label==Exclabel)//这个函数和上面那个差距就再对特定的node网开一面，其他的地方还是照旧要求一模一样
		{
			return true;
		}
		else{
		
		Boolean result = true;
		int c1 = nd1.childrenLables.size();
		int c2 = nd2.childrenLables.size();		
		
		if(c1==c2)
		{
                for(int i = 0 ; i < c1;i++)
                {
                	result = result&&SameTreeExcThisOrNot(sourceNodes1.get(nd1.childrenLables.get(i)),sourceNodes1,sourceNodes2.get(nd2.childrenLables.get(i)),sourceNodes2,Exclabel);
                }					
		}
		else
		{
			return false;
		}		
		return result;
		
		}
	
	}
	
	
	
	public static boolean SameTreeExcOneOrNot(DataNode nd1, Map<Integer, DataNode> sourceNodes1, DataNode nd2, Map<Integer, DataNode> sourceNodes2)
	{
		if(SameTreeOrNot(nd1,sourceNodes1,nd2,sourceNodes2))//排除完全一样的情况
		{return false;}
		
		
		
		boolean result = true;
		boolean temp = false;
		
		 List<DataNode> Nodelist = new ArrayList<>();
		 Nodelist = GetSubTreeNodes(nd1,sourceNodes1,Nodelist);
		
		 for(int x=0;x<Nodelist.size();x++)
		 {
			
				if(SameTreeExcThisOrNot(nd1,sourceNodes1,nd2,sourceNodes2,Nodelist.get(x).label))
				{
					return true;
				}
				else
				{}
			 
			 
		 }
		 
	return false;
	}
	

	public static boolean couldSubtitue2(DataNode node1,Map<Integer, DataNode> sourceNodes1, DataNode node2,Map<Integer, DataNode> sourceNodes2)
	{
		if(node1.node.toString().equals(node2.node.toString()))
		{
		 return false;//20210304：如果文本完全一样就不匹配了
		}
		
		if(!node1.nodeType.equals(node2.nodeType))
		{
		 return false;//如果type都不一样就不匹配了
		}
		
//		if(node1.nodeType.toString().trim().contains("Literal"))
//		{
//			return true;//字符串和数字等常量可以全场匹配
//		}

		if(node1.nodeType.toString().trim().equals("class org.eclipse.jdt.core.dom.SimpleName"))
		{
			return false;//不管变量了
		}
			
		if(!GetAncestorType(node1, sourceNodes1).equals(GetAncestorType(node2, sourceNodes2)))
		{
			return false;//如果祖先不一样就把不匹配了。这里省略了block这一层。这个规则替换了原来的要求句子类型一样
		}
	
		if(
				!(
						SameTreeOrNot(node1, sourceNodes1, node2,sourceNodes2)
						||
						SameTreeSubOrNot(node1, sourceNodes1, node2,sourceNodes2)   
						||
						SameTreeSubOrNot(node2, sourceNodes2, node1,sourceNodes1)   
				        ||
				        SameTreeExcOneOrNot(node1, sourceNodes1, node2,sourceNodes2)  
				)
			)//这是新算法的灵魂。上面这几个情况，满足任意一个都算true
		{
			return false;
		}
			
		
		return true;
	}
	
	public static int getMainBlockNum(Map<Integer, DataNode> sourceNodes)
	{
		for(int i=0;i<sourceNodes.size();i++)
		{
			DataNode nd = sourceNodes.get(i);
			if(nd.nodeType.toString().contains("MethodDeclaration")
					&&nd.node.toString().contains("main"))
			{
				for(int x=0;x<nd.childrenLables.size();x++)
				{
					int index = nd.childrenLables.get(x);
					DataNode b = sourceNodes.get(index);
					if(b.nodeType.toString().contains("Block"))
					{return index;}
					else
					{continue;}
				}
				
			}
			else
			{continue;}		
		}
		return -1;
	}
	
	public static List<String> getASTControlList(Map<Integer, DataNode> sourceNodes)
	{
		 List<String> ret = new ArrayList<String>();
		for(int i=0;i<sourceNodes.size();i++)
		{
			DataNode nd = sourceNodes.get(i);
			String thistype = nd.nodeType.trim();
			if(thistype.contains("IfStatement"))
			{
				ret.add("Conditional");
			}
			else if(thistype.contains("ForStatement"))
			{
				ret.add("Looping");
			}
			else if(thistype.contains("While"))
			{
				ret.add("Looping");
			}
			else if(thistype.contains("VariableDeclarationStatement"))
			{
				ret.add("VariableDeclarationStatement");
			}
			else if(thistype.contains("ExpressionStatement"))
			{
				ret.add("ExpressionStatement");
			}
		}
		return ret;
	}
	
	public static int getSubtreeEndLabel(Map<Integer, DataNode> sourceNodes,DataNode node)
	{
		int x = node.childrenLables.size();
		if(x>0)
		{
			return getSubtreeEndLabel(sourceNodes,
					sourceNodes.get(node.childrenLables.get(x-1)));
		}
		else
		{return node.label;}
		
	}
	
	
	
	public static List<String> getASTStructure(Map<Integer, DataNode> sourceNodes)
	{
		//注：这里应该构造一个栈，用来储存有哪些if、for、while结构等等。
		 List<String> Zhan = new ArrayList<String>();
		 List<Integer> ZhanNum =  new ArrayList<Integer>();
		 
		 List<String> ret = new ArrayList<String>();
		 
		 int tmpbegin =0;
		 int tmpend = 0;
		for(int i=0;i<sourceNodes.size();i++)
		{
			DataNode nd = sourceNodes.get(i);
			
			while(ZhanNum.size()>0&&nd.label>=ZhanNum.get(ZhanNum.size()-1))
			//if(nd.label>=ZhanNum.get(ZhanNum.size()-1))
			{
				ret.add(Zhan.get(Zhan.size()-1)+"End");
				
				Zhan.remove(Zhan.size()-1);
				ZhanNum.remove(ZhanNum.size()-1);
			}
			
			
			
			String thistype = nd.nodeType.trim();
			if(thistype.contains("IfStatement"))
			{
				ZhanNum.add(getSubtreeEndLabel(sourceNodes,nd));

				Zhan.add("Conditional");
				ret.add("Conditional");
			}
			else if(thistype.contains("ForStatement"))
			{
				ZhanNum.add(getSubtreeEndLabel(sourceNodes,nd));
				
				Zhan.add("Looping");
				ret.add("Looping");
			}
			else if(thistype.contains("WhileStatement"))
			{
				ZhanNum.add(getSubtreeEndLabel(sourceNodes,nd));
				
				Zhan.add("Looping");
				ret.add("Looping");
			}
		}
		return ret;
	}
	
	public static boolean SameASTStructure(List<String> L1, List<String> L2)
	{		
		if(L1.size()!=L2.size())
		{ return false;}
		else
		{
			for(int i=0;i<L1.size();i++)
			{
				if(L1.get(i).trim().equals(L2.get(i).trim()))
				{}
				else
				{return false;}				
			}	
			
		}
		return true;
	}
	
	public static boolean couldSubtitue3(DataNode node1,Map<Integer, DataNode> sourceNodes1, DataNode node2,Map<Integer, DataNode> sourceNodes2)
	{
		//20210419尝试整体替换
		if(node1.label== getMainBlockNum(sourceNodes1)&&node2.label== getMainBlockNum(sourceNodes2))
		{
		 return true;//20210419这里预留一些可以增加条件的地方。
		}
		else
		{
			return false;
		}
		
	}
	
	public static void main(String[] args){
		String t = "cou;";
		String[] strs = t.split(" ");
		for(String s: strs){
			System.out.println(s+"   "+s.length());
		}
	}
}
