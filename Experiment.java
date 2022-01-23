package llp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.AbstractDocument.Content;
import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.formula.functions.Fixed;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.core.DocumentAdapter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;

import com.graphbuilder.math.Expression;

import TestCaseUtile.ASTUtil;
import TestCaseUtile.ASTUtil.visitor;
import data.ResultData;
import ui.SearchResultView;

public class Experiment {
	
//	public static String problemPath = "D:\\llptsemulinput\\263A";
//	public static String problemPath = "D:\\llptsemulinput\\4A";
//	public static String problemPath = "D:\\llptseCLARA\\1A";
//	public static String problemPath = "D:\\llptsedisM\\1A";
//	public static String problemPath = "D:\\llptse2\\339A";
//	public static String problemPath = "D:\\llptse3\\118A";
//	public static String problemPath = "D:\\llptse2\\4A";
//	public static String problemPath = "D:\\llptse3\\secondround\\281A";
//	public static String problemPath = "D:\\llptsespeed\\4A";
	
	public static String problemPath = "D:\\llptsetimelog\\263A";
	
	public static Map<AST, Document> mapOfASTandDocument = new HashMap<>();//是为了做修改用
	public static Integer newFilesIndex = 1;
	public static Map<Integer, DataNode> mapOfIndexAndInsertStatements = new HashMap<>();
	public static Map<DataNode, List<DataNode>> mapOfInsertStatementsAndSameLogicNodes = new HashMap<>();
	
	public static String fileNameToFix = "";
	public static File currentFile = null;
	// 保存测试用例和运行过的插入语句之间的映射
	public static Map<Integer, List<DataNode> > mapOfTestCaseAndInsertStatements = new HashMap<>();
	// 保存测试用例和所有运行过的语句之间的映射,下面是通过上面那个间接得到的
	public static Map<Integer, List<DataNode> > mapOfTestCaseAndAllStatements = new HashMap<>();
	
	// 测试用例和是否通过之间的映射
	public static Map<Integer, Boolean> mapOfTestCaseAndIfSucceed = new HashMap<>();
	
	// 生成的那些Nodes和参考File之间的映射
	public static Map<Map<Integer, DataNode>, String> mapOfNodesAndFile = new HashMap<>(); 
	
	// 变量之间题换,key为参考程序变量， value为待修复目标变量
	public static Map<String, String> mapOfVariableNames = new HashMap<>();
	public static boolean fixed = false;
	
	// the following three variables are used in the function fixByDeleteStatements
	public static int numberOfExpression = 0;//number of expressionStatement node
	public static int thExpression = 0;//for count 
	public static int k = 1;
	
	public static long  startTime = -1;
	public static long endTime = -1;
	
	public static long MEndTime = -1;
	public static long SBFLEndTime = -1;
	public static long LBeginTime = -1;
	public static long LEndTime = -1;
	
	public static Date startDate;
	public static Date endDate;
	
	public static Date MEndDate;
	public static Date SBFLEndDate;
//	public static Date REndDate;
	
	
	public static Date LBeginDate;
	public static Date LEndDate;
	
	public static int dijige = 0;
	public static String dijigeString="unknown";
	
	public static String timeLog = "";
	
	//initialize the file to be fix, path = D:\\LLP\\915A
	public static Map<Integer, DataNode> initializeFileToBeFix(String path, File source){
		File originalFileDir = new File(path + "//originalInsertFile");
		File sourceFile = source;
		Map<Integer, DataNode> sourceNodes = new HashMap<>();
		String absolutePath = sourceFile.getAbsolutePath();
		String dirPah = absolutePath.substring(0, absolutePath.lastIndexOf('\\'));
		
		try {
			CompilationUnit unit = ASTUtil.getCompilationUnit2(absolutePath, sourceFile.getName(), dirPah, true);
			test.ID = 0;
			test.getDirectChildren(unit, 0, sourceNodes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sourceNodes;
	}
	
	// Initialize all candidate files
	public static List<Map<Integer, DataNode>> initializeCandidatesFile(String path){
		List<Map<Integer, DataNode>> candidates = new ArrayList<>();
		File candidatesFileDir = new File(path + "//candidatesFile");
		File[] candidatesFile = candidatesFileDir.listFiles();
		for(File candidate: candidatesFile){
			Map<Integer, DataNode> candidateNodes = new HashMap<>();
			String absolutePath = candidate.getAbsolutePath();
			String dirPah = absolutePath.substring(0, absolutePath.lastIndexOf('\\'));
			try {
				CompilationUnit unit = ASTUtil.getCompilationUnit2(absolutePath, candidate.getName(), dirPah, true);
				test.ID = 0;
				test.getDirectChildren(unit, 0, candidateNodes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			candidates.add(candidateNodes);
			mapOfNodesAndFile.put(candidateNodes, candidate.getAbsolutePath());
		}
		return candidates;
	}
	
	
	public static void fix(Map<Integer, DataNode> sourceNodes, Map<Integer, DataNode> candidate, List<List<String> > variableRelations){
		
		 fixed = false;
		List<DataNode> sourceNodeList = new ArrayList<>();
		for(DataNode node: sourceNodes.values()){
			sourceNodeList.add(node);
		}
		
		// 排序的部分
    	Collections.sort(sourceNodeList, new Comparator<DataNode>() {
    		
			@Override
			public int compare(DataNode o1, DataNode o2) {
				int num1 = o1.numberOfToken2 - o2.numberOfToken2;
				if(num1 == 0){
					return (int) (o2.score - o1.score);
				}
				return num1;
			}
		});
    	
    	
    	if(variableRelations != null){
    		//处理变量之间映射关系
        	for(List<String> variables: variableRelations){
        		mapOfVariableNames.put(variables.get(2), variables.get(1));
        	}
    	}
    	
    	
		
		
		for(DataNode sourceNode: sourceNodeList){
			DataNode nodeToFix = sourceNode;
			//自定义插入的节点
			if(nodeToFix.node.toString().trim().startsWith("System.out.println(\"bitse207")){
				continue;
			}
			//去寻找可用来修复的节点
			    if(nodeToFix.numberOfToken2 > 30){//20190220，把5改成了30
			    	continue;
			    }
				for(Integer label: candidate.keySet()){
					DataNode nodeCandidate = candidate.get(label);// 参考程序的每个节点
					if(nodeCandidate.numberOfToken2 > 30){
						continue;
					}
				//	if(test.couldSubtitue(nodeToFix, nodeCandidate)){
					if(test.couldSubtitue2(nodeToFix, sourceNodes, nodeCandidate,candidate)){//20200723改
						//进行替换----------用node替换dataNodeToFix
						
						CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(nodeToFix.node);
						
						
						Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
						Document documentTp = new Document(document.get());
//						System.out.println("document");
//						System.out.println(document.get());
						ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
						// 进行修改,先复制再替换
						ASTNode newNode = nodeToFix.node.copySubtree(nodeCandidate.node.getAST(), nodeCandidate.node);
						
						//限制条件,根节点没有location in parent
						if(nodeToFix.node.getLocationInParent() == null){
							continue;
						}
						//System.out.println("原始的compilationUnit");
						//System.out.println(oriCompilationUnit);
						System.out.println("需要修复的节点：" + nodeToFix.nodeType);
						System.out.println(nodeToFix.node);
						System.out.println("候选节点：" + nodeCandidate.nodeType);
						System.out.println(newNode);
						
						
						rewriter.replace(nodeToFix.node, newNode, null);
						
						
						
						newNode.accept(new ASTVisitor() {
							
							public boolean visit(SimpleName node){
								
								String name = node.getIdentifier();
								if(mapOfVariableNames.get(name) != null){
									System.out.println("SRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
									node.setIdentifier(mapOfVariableNames.get(name));
								}
							
								return true;
							}
							
							
						});
						
						
						TextEdit edits = rewriter.rewriteAST(document, null);
				        try {
							edits.apply(document);
						} catch (MalformedTreeException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						} catch (BadLocationException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
				        System.out.println("生成后的新程序new program:");
				        System.out.println("document\n"+  document.get());
				        System.out.println("********************************************");
				        mapOfASTandDocument.put(oriCompilationUnit.getAST(), documentTp);
				        //System.out.println(newFilesIndex++);
				        //保存新生成的程序
				  
				        File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
				        if(newFileDir.exists())
				        	newFileDir.mkdir();
				        try {
							FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), document.get());
							
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        //在这里去掉那些自定义插入的语句
				        try {
				        	CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(newFileDir.getAbsolutePath()+"//" + fileNameToFix, fileNameToFix, newFileDir.getAbsolutePath(), true);
				        	compilationUnit.accept(new ASTVisitor() {
				        		public boolean visit(ExpressionStatement node){
				        			if(node.toString().trim().startsWith("System.out.println(\"bitse207")){
				        				node.delete();
				        			}
				        			return true;
				        		}
							});
				        	FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), compilationUnit.toString());
				        	 mapOfASTandDocument.remove(compilationUnit.getAST());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        newFilesIndex++;
				        System.out.println("-------------------index ------------   "+(newFilesIndex - 1));
				        if(CMDUtil_new.complileAll(newFileDir.getAbsolutePath(), currentFile)){
				        	File newFileOutputDir = new File(newFileDir+ "//Output");
							
							if(!newFileOutputDir.exists()){
								newFileOutputDir.mkdir();
							}
							
//				        	CMDUtil_new.runAllForExperiment(problemPath, newFileDir.getAbsolutePath());
							try {
								if(CMDUtil_new.runAllForExperimentWithoutPass(problemPath, newFileDir.getAbsolutePath())){
									fixed = true;
									System.out.println("* pass all test cases!");
									break;
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				        
				       // FileUtils.write(new File("D:\\LLP\\CFnew.java"), document.get());
					}
				}//for
				if(fixed)
					break;
			
		}
	}
	
	public static boolean CLARAfix(Map<Integer, DataNode> sourceNodes, Map<Integer, DataNode> candidate, List<List<String> > variableRelations){
		
		 fixed = false;
		List<DataNode> sourceNodeList = new ArrayList<>();
		for(DataNode node: sourceNodes.values()){
			sourceNodeList.add(node);
		}
		
		// 排序的部分
   	Collections.sort(sourceNodeList, new Comparator<DataNode>() {
   		
			@Override
			public int compare(DataNode o1, DataNode o2) {
				int num1 = o1.numberOfToken2 - o2.numberOfToken2;
				if(num1 == 0){
					return (int) (o2.score - o1.score);
				}
				return num1;
			}
		});
   	
   	
   	if(variableRelations != null){
   		//处理变量之间映射关系
       	for(List<String> variables: variableRelations){
       		mapOfVariableNames.put(variables.get(2), variables.get(1));
       	}
   	}
   	
   	//20210425我自己写的
		int bugMainLabel = test.getMainBlockNum(sourceNodes);
		int candidateMainLabel  = test.getMainBlockNum(candidate);
		DataNode sourceNode = sourceNodes.get(bugMainLabel);
		DataNode nodeCandidate =  candidate.get(candidateMainLabel);
		
			DataNode nodeToFix = sourceNode;
						
			
			//自定义插入的节点
//			if(nodeToFix.node.toString().trim().startsWith("System.out.println(\"bitse207")){
//				continue;
//			}
			//去寻找可用来修复的节点
//			    if(nodeToFix.numberOfToken2 > 30){//20190220，把5改成了30
//			    	continue;
//			    }
			
//					if(nodeCandidate.numberOfToken2 > 30){
//						continue;
//					}
				//	if(test.couldSubtitue(nodeToFix, nodeCandidate)){
//					if(test.couldSubtitue2(nodeToFix, sourceNodes, nodeCandidate,candidate)){//20200723改
						//进行替换----------用node替换dataNodeToFix
						
						CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(nodeToFix.node);
						
						
						Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
						Document documentTp = new Document(document.get());
//						System.out.println("document");
//						System.out.println(document.get());
						ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
						// 进行修改,先复制再替换
						ASTNode newNode = nodeToFix.node.copySubtree(nodeCandidate.node.getAST(), nodeCandidate.node);
						
						//限制条件,根节点没有location in parent
//						if(nodeToFix.node.getLocationInParent() == null){
//							continue;
//						}
						//System.out.println("原始的compilationUnit");
						//System.out.println(oriCompilationUnit);
						System.out.println("需要修复的节点：" + nodeToFix.nodeType);
						System.out.println(nodeToFix.node);
						System.out.println("候选节点：" + nodeCandidate.nodeType);
						System.out.println(newNode);
						
						
						rewriter.replace(nodeToFix.node, newNode, null);
						
						
						
						newNode.accept(new ASTVisitor() {
							
							public boolean visit(SimpleName node){
								
								String name = node.getIdentifier();
								if(mapOfVariableNames.get(name) != null){
									System.out.println("SRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
									node.setIdentifier(mapOfVariableNames.get(name));
								}
							
								return true;
							}
							
							
						});
						
						
						TextEdit edits = rewriter.rewriteAST(document, null);
				        try {
							edits.apply(document);
						} catch (MalformedTreeException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						} catch (BadLocationException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
				        System.out.println("生成后的新程序new program:");
				        System.out.println("document\n"+  document.get());
				        System.out.println("********************************************");
				        mapOfASTandDocument.put(oriCompilationUnit.getAST(), documentTp);
				        //System.out.println(newFilesIndex++);
				        //保存新生成的程序
				  
				        File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
				        if(newFileDir.exists())
				        	newFileDir.mkdir();
				        try {
							FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), document.get());
							
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        //在这里去掉那些自定义插入的语句
				        try {
				        	CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(newFileDir.getAbsolutePath()+"//" + fileNameToFix, fileNameToFix, newFileDir.getAbsolutePath(), true);
				        	compilationUnit.accept(new ASTVisitor() {
				        		public boolean visit(ExpressionStatement node){
				        			if(node.toString().trim().startsWith("System.out.println(\"bitse207")){
				        				node.delete();
				        			}
				        			return true;
				        		}
							});
				        	FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), compilationUnit.toString());
				        	 mapOfASTandDocument.remove(compilationUnit.getAST());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        newFilesIndex++;
				        System.out.println("-------------------index ------------   "+(newFilesIndex - 1));
				        if(CMDUtil_new.complileAll(newFileDir.getAbsolutePath(), currentFile)){
				        	File newFileOutputDir = new File(newFileDir+ "//Output");
							
							if(!newFileOutputDir.exists()){
								newFileOutputDir.mkdir();
							}
							
//				        	CMDUtil_new.runAllForExperiment(problemPath, newFileDir.getAbsolutePath());
							try {
								if(CMDUtil_new.runAllForExperimentWithoutPass(problemPath, newFileDir.getAbsolutePath())){
									fixed = true;
									System.out.println("* pass all test cases!");
								
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				        
				       // FileUtils.write(new File("D:\\LLP\\CFnew.java"), document.get());
					
			
			
			
	//	}
					return fixed;
	}
	
	
	public static void CLARAfix2(Map<Integer, DataNode> sourceNodes, Map<Integer, DataNode> candidate, List<List<String> > variableRelations){
		
		 fixed = false;
		List<DataNode> sourceNodeList = new ArrayList<>();
		for(DataNode node: sourceNodes.values()){
			sourceNodeList.add(node);
		}
		
		// 排序的部分
   	Collections.sort(sourceNodeList, new Comparator<DataNode>() {
   		
			@Override
			public int compare(DataNode o1, DataNode o2) {
				int num1 = o1.numberOfToken2 - o2.numberOfToken2;
				if(num1 == 0){
					return (int) (o2.score - o1.score);
				}
				return num1;
			}
		});
   	
   	
   	if(variableRelations != null){
   		//处理变量之间映射关系
       	for(List<String> variables: variableRelations){
       		mapOfVariableNames.put(variables.get(2), variables.get(1));
       	}
   	}
   	
   	
		
		
		for(DataNode sourceNode: sourceNodeList){
			DataNode nodeToFix = sourceNode;
			//自定义插入的节点
			if(nodeToFix.node.toString().trim().startsWith("System.out.println(\"bitse207")){
				continue;
			}
			//去寻找可用来修复的节点
//			    if(nodeToFix.numberOfToken2 > 30){//20190220，把5改成了30
//			    	continue;
//			    }
				for(Integer label: candidate.keySet()){
					DataNode nodeCandidate = candidate.get(label);// 参考程序的每个节点
//					if(nodeCandidate.numberOfToken2 > 30){
//						continue;
//					}
				//	if(test.couldSubtitue(nodeToFix, nodeCandidate)){
					if(test.couldSubtitue3(nodeToFix, sourceNodes, nodeCandidate,candidate)){//20200723改
						//进行替换----------用node替换dataNodeToFix
						
						CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(nodeToFix.node);
						
						
						Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
						Document documentTp = new Document(document.get());
//						System.out.println("document");
//						System.out.println(document.get());
						ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
						// 进行修改,先复制再替换
						ASTNode newNode = nodeToFix.node.copySubtree(nodeCandidate.node.getAST(), nodeCandidate.node);
						
						//限制条件,根节点没有location in parent
						if(nodeToFix.node.getLocationInParent() == null){
							continue;
						}
						//System.out.println("原始的compilationUnit");
						//System.out.println(oriCompilationUnit);
						System.out.println("需要修复的节点：" + nodeToFix.nodeType);
						System.out.println(nodeToFix.node);
						System.out.println("候选节点：" + nodeCandidate.nodeType);
						System.out.println(newNode);
						
						
						rewriter.replace(nodeToFix.node, newNode, null);
						
						
						
						newNode.accept(new ASTVisitor() {
							
							public boolean visit(SimpleName node){
								
								String name = node.getIdentifier();
								if(mapOfVariableNames.get(name) != null){
									System.out.println("SRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
									node.setIdentifier(mapOfVariableNames.get(name));
								}
							
								return true;
							}
							
							
						});
						
						
						TextEdit edits = rewriter.rewriteAST(document, null);
				        try {
							edits.apply(document);
						} catch (MalformedTreeException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						} catch (BadLocationException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
				        System.out.println("生成后的新程序new program:");
				        System.out.println("document\n"+  document.get());
				        System.out.println("********************************************");
				        mapOfASTandDocument.put(oriCompilationUnit.getAST(), documentTp);
				        //System.out.println(newFilesIndex++);
				        //保存新生成的程序
				  
				        File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
				        if(newFileDir.exists())
				        	newFileDir.mkdir();
				        try {
							FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), document.get());
							
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        //在这里去掉那些自定义插入的语句
				        try {
				        	CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(newFileDir.getAbsolutePath()+"//" + fileNameToFix, fileNameToFix, newFileDir.getAbsolutePath(), true);
				        	compilationUnit.accept(new ASTVisitor() {
				        		public boolean visit(ExpressionStatement node){
				        			if(node.toString().trim().startsWith("System.out.println(\"bitse207")){
				        				node.delete();
				        			}
				        			return true;
				        		}
							});
				        	FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), compilationUnit.toString());
				        	 mapOfASTandDocument.remove(compilationUnit.getAST());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        newFilesIndex++;
				        System.out.println("-------------------index ------------   "+(newFilesIndex - 1));
				        if(CMDUtil_new.complileAll(newFileDir.getAbsolutePath(), currentFile)){
				        	File newFileOutputDir = new File(newFileDir+ "//Output");
							
							if(!newFileOutputDir.exists()){
								newFileOutputDir.mkdir();
							}
							
//				        	CMDUtil_new.runAllForExperiment(problemPath, newFileDir.getAbsolutePath());
							try {
								if(CMDUtil_new.runAllForExperimentWithoutPass(problemPath, newFileDir.getAbsolutePath())){
									fixed = true;
									System.out.println("* pass all test cases!");
									break;
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				        
				       // FileUtils.write(new File("D:\\LLP\\CFnew.java"), document.get());
					}
				}//for
				if(fixed)
					break;
			
		}
	}
	
	
	
	public static boolean sameLogic(DataNode node1, DataNode node2){
		int len1 = node1.logicPoseList.size();
		int len2 = node2.logicPoseList.size();
		if(len1 != len2){
			return false;
		}
		for(int i = 0; i < len1; i++){
			if(node1.logicPoseList.get(i) != node2.logicPoseList.get(i))
				return false;
		}
		return true;
	}
	
	public static boolean sameLogic2(DataNode node1, DataNode node2){
		int len1 = node1.logicPoseList.size();
		int len2 = node2.logicPoseList.size();
		
		
		
		if(len1 != len2 ){
			return false;
		}
		
		//System.out.println("aaa: \n"+node1.node.toString());
		//System.out.println("bbb: \n"+node2.node.toString());
		for(int i = 0; i < len1; i++){
			//System.out.println("a: "+node1.logicStepList.get(i));
			//System.out.println("b: "+node2.logicStepList.get(i));
			if(node1.logicPoseList.get(i) != node2.logicPoseList.get(i) )
				return false;
			if(node1.logicPoseList.get(i) == 1){
				if(!node1.node.getParent().equals(node2.node.getParent())){
					return false;
				}
			}
			
		}
		return true;
	}
	
	public static void getInsertStatementsAndSameLevelNodes(Map<Integer, DataNode> sourcesNodes){
		for(Integer integer: mapOfIndexAndInsertStatements.keySet()){
			DataNode insertNode = mapOfIndexAndInsertStatements.get(integer);
			List<DataNode> sameGroupDataNodes = new ArrayList<>();
			for(Integer otherInteger: sourcesNodes.keySet()){
				DataNode anotherDataNode = sourcesNodes.get(otherInteger);
				if(anotherDataNode.equals(insertNode))
					continue;
				//判断逻辑
				if(sameLogic2(insertNode, anotherDataNode)){
					sameGroupDataNodes.add(anotherDataNode);
				}
			}
			
			mapOfInsertStatementsAndSameLogicNodes.put(insertNode, sameGroupDataNodes);
		}
		
	}
	
	public static List<Operator> getOtherOperators(InfixExpression node){
		List<Operator> operators = new ArrayList<>();
		Operator origOperator = node.getOperator();
		
		List<Operator> boolOperators = new ArrayList<>(); // ==  !=
		boolOperators.add(Operator.NOT_EQUALS);  boolOperators.add(Operator.EQUALS);
		
		List<Operator> baseOperators = new ArrayList<>();  // + - * /
		baseOperators.add(Operator.PLUS); baseOperators.add(Operator.MINUS);
		baseOperators.add(Operator.TIMES); baseOperators.add(Operator.DIVIDE);
		
		List<Operator> compareOperators = new ArrayList<>(); // < > <=  >=
		compareOperators.add(Operator.LESS); compareOperators.add(Operator.GREATER);
		compareOperators.add(Operator.LESS_EQUALS); compareOperators.add(Operator.GREATER_EQUALS);
		
		if(boolOperators.contains(origOperator)){
			for(Operator op: boolOperators){
				if(op != origOperator){
					operators.add(op);
				}
			}
			return operators;
		}
		
		if(baseOperators.contains(origOperator)){
			for(Operator op: baseOperators){
				if(op != origOperator){
					operators.add(op);
				}
			}
			return operators;
		}
		
		if(compareOperators.contains(origOperator)){
			for(Operator op: compareOperators){
				if(op != origOperator){
					operators.add(op);
				}
			}
			return operators;
		}
		return operators;
		
		
	}
	
	public static void output(String str){
		System.out.println(str);
	}
	
	public static void fixByModifyingOperators(File fileToFix, File dir) throws IOException{
		CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(fileToFix.getAbsolutePath(), fileToFix.getName(), dir.getAbsolutePath(), true);
		Document document = mapOfASTandDocument.get(compilationUnit.getAST());
		Document documentTp = new Document(document.get());
		ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
		TextEdit edits = rewriter.rewriteAST(document, null);
		compilationUnit.accept(new ASTVisitor() {
			
			public boolean visit(InfixExpression node){
				//System.out.println("operator:  "  + node.toString());
				if(fixed)
					return false;
				Operator tempOperator = node.getOperator();
				List<Operator> otherOperators = getOtherOperators(node);
				for(Operator op: otherOperators){
					node.setOperator(op);
					//now already create a new program
					//D:\LLP\58A\newFiles\A.java\1
					File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
					if(newFileDir.exists())
			        	newFileDir.mkdir();
			        try {
			        	//D:\LLP\58A\newFiles\A.java\1\A.java
						FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), compilationUnit.toString());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        
			        newFilesIndex++;
			        System.out.println("-------------------index ------------   "+(newFilesIndex - 1));
			        if(CMDUtil_new.complileAll(newFileDir.getAbsolutePath(), currentFile)){
			        	File newFileOutputDir = new File(newFileDir+ "//Output");
						
						if(!newFileOutputDir.exists()){
							newFileOutputDir.mkdir();
						}
						
						//CMDUtil_new.runAllForExperiment(problemPath, newFileDir.getAbsolutePath());
						try {
							if(CMDUtil_new.runAllForExperimentWithoutPass(problemPath, newFileDir.getAbsolutePath())){
								fixed = true;
								System.out.println("* pass all test cases!");
								break;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			        
			        if(fixed){
			        	break;
			        }
			        
			        
				}
				node.setOperator(tempOperator); //进行还原
//				node.setOperator(Operator.PLUS);
//				System.out.println("new Program:\n"+compilationUnit);
				
				return true;
			}
		});
        try {
			edits.apply(document);
		} catch (MalformedTreeException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (BadLocationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
       //System.out.println("original program:\n" + compilationUnit);
	}
	
	
	
	// 待补充-------------------------------------------------
	public static void fixByModifyingVariables(File fileToFix, File dir) throws IOException{
		CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(fileToFix.getAbsolutePath(), fileToFix.getName(), dir.getAbsolutePath(), true);
		Document document = mapOfASTandDocument.get(compilationUnit.getAST());
		Document documentTp = new Document(document.get());
		ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
		TextEdit edits = rewriter.rewriteAST(document, null);
		
		//保存的现有的变量
		List<IVariableBinding> declaredVariableBindings = new ArrayList<>();
		
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationStatement node ){
				List<VariableDeclarationFragment> frags = node.fragments();
				for(VariableDeclarationFragment frag : frags){
					if(frag.resolveBinding() != null){
						IVariableBinding v = frag.resolveBinding();
						if(!v.isField()){
							declaredVariableBindings.add(v);
						}
						
					}
				}
				return true;
			}
		});
		
//		for(IVariableBinding var: declaredVariableBindings){
//			output("var: "+var.getName());
//		}
		
		//再去替换变量
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(SimpleName node){
				if(fixed)
					return false;
				if(node.resolveBinding() != null){
					if(node.resolveBinding() instanceof IVariableBinding){
						IVariableBinding variableBinding = (IVariableBinding) node.resolveBinding();
						if(declaredVariableBindings.contains(variableBinding)){
							if(node.getParent() != null){
								//int n = scanner.nextInt();这种情况将n去掉，不替换
								if(node.getParent() instanceof VariableDeclarationFragment){
									return true;
								}
							}
							
							// 去替换变量了------------------------------
							//output("current var: "+node.toString());
							String oirignalName = node.getIdentifier();
							for(IVariableBinding var: declaredVariableBindings){
								
								if(!var.equals(variableBinding) 
										&& !var.getName().equals(variableBinding.getName())
										&& var.getType().getName().equals(variableBinding.getType().getName())){
									//can subtitute  
									
									//output("candidate vars: "+var.getName());
									node.setIdentifier(var.getName());
									
									//output("new Programs ----------------------------------------:\n"+compilationUnit);
									//already crate a new program
									File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
									if(newFileDir.exists())
							        	newFileDir.mkdir();
							        try {
							        	//D:\LLP\58A\newFiles\A.java\1\A.java
										FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), compilationUnit.toString());
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
							        
							        newFilesIndex++;
							        System.out.println("-------------------index ------------   "+(newFilesIndex - 1));
							        if(CMDUtil_new.complileAll(newFileDir.getAbsolutePath(), currentFile)){
							        	File newFileOutputDir = new File(newFileDir+ "//Output");
										
										if(!newFileOutputDir.exists()){
											newFileOutputDir.mkdir();
										}
										
										//CMDUtil_new.runAllForExperiment(problemPath, newFileDir.getAbsolutePath());
										try {
											if(CMDUtil_new.runAllForExperimentWithoutPass(problemPath, newFileDir.getAbsolutePath())){
												fixed = true;
												System.out.println("* pass all test cases!");
												break;
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
							        node.setIdentifier(oirignalName);
							        
							        if(fixed){
							        	break;
							        }
								}
							}// candidate vars tried end
							
						}
					}
				}
				return true;
			}// visit simpleNode end
			
		});
		
	}
	
	public static void fixByDeleteStatements(File fileToFix, File dir) throws IOException{
		CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(fileToFix.getAbsolutePath(), fileToFix.getName(), dir.getAbsolutePath(), true);
		Document document = mapOfASTandDocument.get(compilationUnit.getAST());
		Document documentTp = new Document(document.get());
		ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
		TextEdit edits = rewriter.rewriteAST(document, null);
		output(compilationUnit.toString());
		//保存的现有的变量
		List<IVariableBinding> declaredVariableBindings = new ArrayList<>();
		numberOfExpression = 0;
		
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(ExpressionStatement node){
				System.out.println("there:  "+node);
				numberOfExpression++;
				return true;
			}
		});
		
		for(k = 1; k <= numberOfExpression; k++){
			CompilationUnit cUnit= ASTUtil.getCompilationUnit2(fileToFix.getAbsolutePath(), fileToFix.getName(), dir.getAbsolutePath(), true);
			thExpression = 0;
			cUnit.accept(new ASTVisitor() {
				public boolean visit(ExpressionStatement node){
					if(fixed)
						return false;
					thExpression++;
					if(thExpression == k){
						System.out.println(node.getParent());
						System.out.println(node);
						try {
							node.delete();
							//generate a new program 
							File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
							if(newFileDir.exists())
					        	newFileDir.mkdir();
					        try {
					        	//D:\LLP\58A\newFiles\A.java\1\A.java
								FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), cUnit.toString());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					        
					        newFilesIndex++;
					        System.out.println("-------------------index ------------   "+(newFilesIndex - 1));
					        if(CMDUtil_new.complileAll(newFileDir.getAbsolutePath(), currentFile)){
					        	File newFileOutputDir = new File(newFileDir+ "//Output");
								
								if(!newFileOutputDir.exists()){
									newFileOutputDir.mkdir();
								}
								
								//CMDUtil_new.runAllForExperiment(problemPath, newFileDir.getAbsolutePath());
								try {
									if(CMDUtil_new.runAllForExperimentWithoutPass(problemPath, newFileDir.getAbsolutePath())){
										fixed = true;
										System.out.println("* pass all test cases!");
										return true;
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (IllegalArgumentException e) {
							// TODO: handle exception
							return true;
						}
						
				        
				        if(fixed){
				        	return true;
				        }
					}
					return true;
				}
			});
			if(fixed)
				break;
		}
	}
	
	public static void createLog() throws IOException{
		String path = problemPath + "//logs//" + fileNameToFix + ".txt";
		File file = new File(path);
		endTime = System.currentTimeMillis();
		endDate = new 	Date();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String data = "Data start: " + df.format(startDate) +"   Data end: " + df.format(endDate);
		 data =  data + "   time usage: "+ (endTime - startTime);
		if(fixed){
			data = data + "\n  fixed: YES";
		}
		else{
			data = data + "\n  fixed: NO";
		}
		data = data +  "\n  第几个: " + dijige+"     end";
		FileUtils.write(new File(path), data);
		
		
	}
	
	public static void createNewLog(String fileName) throws IOException{
		System.out.println("kekekeke");
		String[] fileNameArr = fileName.split("\\\\");
		String path = problemPath + "//logs//" + fileNameToFix+"//"+dijige + ".txt";
		System.out.println(path);
		File file = new File(path);
		endTime = System.currentTimeMillis();
		endDate = new 	Date();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String data = fileNameToFix+"\nData start: " + df.format(startDate) +"   Data end: " + df.format(endDate);
		 data =  data + "   time usage: "+ (endTime - startTime);
		if(fixed){
			data = data + "\n  fixed: YES";
		}
		else{
			data = data + "\n  fixed: NO";
		}
		data = data +  "\n  第几个: " + dijige;
		data = data +  "\n  第几个--文件名: " + dijigeString;
		if(fixed) {
		System.out.println(data);
		}
		FileUtils.write(new File(path), data);
		
		
	}
	
	public static void main(String[] args) throws MalformedTreeException, BadLocationException, IOException{
		System.out.println("hereTSE");
		
		Common.initInputList(problemPath);//注：这两行是李柯君写的
		Common.initOutputList(problemPath);
		
		
		File originalFileDir = new File(problemPath + "//originalFile");
		File[] listFiles = originalFileDir.listFiles();
		List<Map<Integer, DataNode>> candidates = initializeCandidatesFile(problemPath);
		File originalFile = null;
		for(File filex: listFiles){
			if(!filex.getName().endsWith(".java")){
				continue;
			}
			fixed = false;
			
			newFilesIndex = 1;
			originalFile = filex;
			currentFile = filex;
			fileNameToFix = originalFile.getName();
			
			// start time
			startTime = System.currentTimeMillis();
			startDate = new Date();
			
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			timeLog = "";
			timeLog+="\nMutationRepairBegin: " + df.format(startDate);
			
			
//			//进行第一个操作，修改操作符
			fixByModifyingOperators(filex, originalFileDir);
			if(fixed){
				createLog();
				continue;
			}
			//修改变量
			fixByModifyingVariables(filex, originalFileDir);
			if(fixed){
				createLog();
				continue;
			}
			//删除语句
			fixByDeleteStatements(filex, originalFileDir);
			if(fixed){
				createLog();
				continue;
			}
			
		
			MEndDate =  new Date();
			timeLog+="\nMutationRepairEnd: " + df.format(MEndDate);
			
			MEndTime = System.currentTimeMillis();
			timeLog +=  "\n Mutation time usage: "+ (MEndTime - startTime);
			
			//这里应该有一堆的map list啥的初始化语句 等会再说
			mapOfIndexAndInsertStatements.clear();
			mapOfInsertStatementsAndSameLogicNodes.clear();
			mapOfTestCaseAndInsertStatements.clear();
			mapOfTestCaseAndAllStatements.clear();
			mapOfTestCaseAndIfSucceed.clear();
			mapOfVariableNames.clear();
			
			// 插入自己定义的语句
			FileUtil.InsertStatementsForOriginalFile(problemPath, originalFile);
			
			File insertFile = new File(problemPath+ "//originalInsertFile//"+originalFile.getName());
			
			//初始化新生成的待修复的文件
			Map<Integer, DataNode> sourceNodes = initializeFileToBeFix(problemPath, insertFile);
			
			test.output(sourceNodes);
			for(Integer ii: sourceNodes.keySet()){
				DataNode node = sourceNodes.get(ii);
				//找出那些自己插入的语句
				if(FileUtil.isInsertStatement(node.node)){
					System.out.println("content  "+ node.toString());
					String content = node.node.toString();
					String printContent = "";
					int index = 0;
					for(; index < content.length(); index++){
						if(content.charAt(index) == '"'){
							break;
						}
					}
					index++;
					for(; index < content.length(); index++){
						if(content.charAt(index) != '"'){
							printContent = printContent + content.charAt(index);
						}
						else{
							break;
						}
					}
					String[] strs = printContent.split(" ");
					System.out.println("SSS  "+printContent);
					Integer integer = Integer.valueOf(strs[1]);
					Experiment.mapOfIndexAndInsertStatements.put(integer, node);
				}
			}
			
			
			
			
			//找到和插入语句会同时运行的那些节点
			getInsertStatementsAndSameLevelNodes(sourceNodes);
			
			//对于每一个测试文件，	去运行，看通过不通过，然后
			if(CMDUtil_new.complileAll(problemPath + "//originalInsertFile", originalFile)){
				// 运行
				String originalCodePath = problemPath + "//originalInsertFile";
				String testCasePath = problemPath + "//Input";
				String outPutPath = problemPath + "//originalInsertFileOutput//"+ originalFile.getName();
				File output = new File(outPutPath);
				if(!output.exists()){
					output.mkdir();
				}
				
				File originalCodeFile = new File(originalCodePath);
				File[] originalFiles = originalCodeFile.listFiles(); 
				
				File testCaseFile = new File(testCasePath);
				File[] testCaseFiles = testCaseFile.listFiles();
				System.out.println("* running testcase --- ");
				//对每一个原始的java文件,其实就1个
				for(File file: originalFiles){
					if(file.getAbsolutePath().endsWith("java") && file.getName().equals(originalFile.getName())){
						String fileName = file.getName();
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
						for(File testCase: testCaseFiles){//每一个测试文件
							String testCaseName = testCase.getName();
							testCaseName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
							String cmd="cmd.exe /c d: && cd " + originalCodePath +" && java " + fileName+" < " + testCase.getAbsolutePath() 
							+ " > " + outPutPath + "\\"+ testCaseName + ".txt";
							//System.out.println(cmd);
							CMDUtil_new.runCMD(cmd);
							String correctOutputFileDir = problemPath + "//Output//" + testCaseName + ".txt";
							String currentOutputFileDir = outPutPath + "//" + testCaseName + ".txt";
							Integer testCaseIndex = Integer.valueOf(testCaseName);
							System.out.println("第    " + testCaseName + "  个测试用例---------------------------");
							boolean isOk = false;
							if(CMDUtil_new.passOneTestCase(new File(correctOutputFileDir), new File(currentOutputFileDir), testCaseIndex)){
								System.out.println("本次运行 通过了测试用例 ------------  ");
								mapOfTestCaseAndIfSucceed.put(testCaseIndex, true);
								isOk = true;
							}
							else{
								System.out.println("未通过-------------------- ");
								mapOfTestCaseAndIfSucceed.put(testCaseIndex, false);
							}
							
							//获取全部涉及到的语句
							for(DataNode nd: mapOfTestCaseAndInsertStatements.get(testCaseIndex)){
								if(mapOfTestCaseAndAllStatements.get(testCaseIndex) == null){
									List<DataNode> allStatements = new ArrayList<>();
									if(!allStatements.contains(nd))
										allStatements.add(nd);
									mapOfTestCaseAndAllStatements.put(testCaseIndex, allStatements);
								}
								for(DataNode all: mapOfInsertStatementsAndSameLogicNodes.get(nd)){
									if(!mapOfTestCaseAndAllStatements.get(testCaseIndex).contains(all)){
										mapOfTestCaseAndAllStatements.get(testCaseIndex).add(all);
									}
									
								}
							}
							
							//记录信息
							for(DataNode node: mapOfTestCaseAndAllStatements.get(testCaseIndex)){
								if(isOk == false){
									node.numberOfWrongTestCase++;
								}
								node.numberOfTotalTestCase++;
							}
							
							
							System.out.println();
							
							
						}
					}
				}
				System.out.println("* running complete --- ");
			}
			
			
			// 运行完毕后，计算每个节点的概率
			for(Integer it: sourceNodes.keySet()){
				DataNode node = sourceNodes.get(it);
				if(node.numberOfTotalTestCase == 0){
					node.score = 0.0;
				}
				else{
					node.score = 1.0 * node.numberOfWrongTestCase / node.numberOfTotalTestCase;
				}
				
				System.out.println("节点内容\n"+node.node.toString());
				System.out.println("节点概率:   "+ node.numberOfWrongTestCase + " " + node.numberOfTotalTestCase + " " + node.score);
			}
			
			
			dijige = 0;
			
			
			SBFLEndDate =  new Date();
			timeLog+="\nRefRepairBegin: " + df.format(SBFLEndDate);
			SBFLEndTime = System.currentTimeMillis();
			timeLog +=  "\n SBFL time usage: "+ (SBFLEndTime - MEndTime);
			
			for(Map<Integer, DataNode> candidate: candidates){
				dijige = dijige +1;
				
				// start time
				startTime = System.currentTimeMillis();
				startDate = new Date();
				
				System.out.println("****************"+filex+ "  try file   " + mapOfNodesAndFile.get(candidate) + " 去修复"+ "*****************************");
				List<List<String> > variableRelations = llpmethod.getVariablesRelation(sourceNodes, candidate, 0);
				fix(sourceNodes, candidate, variableRelations);	
				dijigeString = llptest.getID(candidate);
				createNewLog(mapOfNodesAndFile.get(candidate));
				if(fixed){
					break;
				}
			}	
			
			LBeginDate =  new Date();
			timeLog+="\nRefRepairEnd: " + df.format(LBeginDate);
			LBeginTime = System.currentTimeMillis();
			timeLog +=  "\n ReferenceBasedRepair time usage: "+ ( LBeginTime - SBFLEndTime);
			
			//20210425 用CLARA的思路，若refProgram与BuggyProgram结构一样，则整体复制过来
//			for(Map<Integer, DataNode> candidate: candidates){
//				
//				//这里的if表示CLARA的要求，即控制流图相同
//				if(!test.SameASTStructure
//						(test.getASTStructure(sourceNodes),test.getASTStructure(candidate)))
//				{continue;}
//				
//				
//				dijige = dijige +1;				
//				// start time
//				startTime = System.currentTimeMillis();
//				startDate = new Date();
//				
//				System.out.println("JAVACLARA****************"+filex+ "  try file   " + mapOfNodesAndFile.get(candidate) + " 去修复"+ "*****************************");
//				List<List<String> > variableRelations = llpmethod.getVariablesRelation(sourceNodes, candidate, 0);
//				CLARAfix2(sourceNodes, candidate, variableRelations);	
//				createNewLog(mapOfNodesAndFile.get(candidate));
//				if(fixed){
//					break;
//				}
//			}	
			
			LEndDate =  new Date();
			timeLog+="\nLastResortEnd: " + df.format(LEndDate);
			
			String TotalLogPath = problemPath + "//logs//" + fileNameToFix+"//"+"TotalLog" + ".txt";
			File file = new File(TotalLogPath);
			FileUtils.write(new File(TotalLogPath), timeLog);
			
		}//对每个文件进行处理
		
		
		//到此为止-----------------------------------------------------------------------------------------
		
		
		
		
		
		
		
		
		

//		
////		for(Integer it: sourceNodes.keySet()){
////			DataNode dNode = sourceNodes.get(it);
////			if(dNode.node instanceof SimpleName){
////				SimpleName simpleName = (SimpleName)dNode.node;
////				System.out.println(simpleName.toString()+ (simpleName.resolveBinding() instanceof IVariableBinding));
////				
////			}
////		}
//		
////		for(Integer t: mapOfIndexAndInsertStatements.keySet()){
////			System.out.println(t +"   "+ mapOfIndexAndInsertStatements.get(t).node.toString());
////		}
//		

//		
////		for(DataNode insertNode: mapOfInsertStatementsAndSameLogicNodes.keySet()){
////			System.out.println("");
////			System.out.println("________________________________________________________");
////			System.out.println(insertNode.node.toString());
////			for(DataNode node: mapOfInsertStatementsAndSameLogicNodes.get(insertNode)){
////				System.out.println("node****    " +node.nodeType);
////				System.out.println(node.node.toString());
////			}
////		}
		
	
//		CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(sourceNodes.get(new Integer(0)).node);
//		Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());

		
		//DataNode targetNode = test.getMostSimiarNode(dataNodeToFix, candidates);

		//System.out.println(dataNodeToFix.node.getClass() +"  "+dataNodeToFix.node.getParent().getClass());
		

		
//		Map<Integer, DataNode> SourceNodes = new HashMap<>();
//		List<Map<Integer, DataNode>> candidates = new ArrayList<Map<Integer,DataNode>>();
//		CompilationUnit unit = ASTUtil.getCompilationUnit2("D:\\LLP\\CF.java", "CF.java", "D:\\LLP", true);
//		test.ID = 0;
//		test.getDirectChildren(unit, 0, SourceNodes);
//		//test.output(SourceNodes);
//		
//		
//		Map<Integer, DataNode> candidateNodes1 = new HashMap<>();
//		CompilationUnit unit1 = ASTUtil.getCompilationUnit("D:\\LLP\\CF1.java", "CF1.java", "D:\\LLP");
//		test.ID = 0;
//		test.getDirectChildren(unit1, 0, candidateNodes1);
//		candidates.add(candidateNodes1);
//		
//		Map<Integer, DataNode> candidateNodes2 = new HashMap<>();
//		CompilationUnit unit2 = ASTUtil.getCompilationUnit("D:\\LLP\\CF2.java", "CF2.java", "D:\\LLP");
//		test.ID = 0;
//		test.getDirectChildren(unit2, 0, candidateNodes2);
//		candidates.add(candidateNodes2);
//		
//		Map<Integer, DataNode> candidateNodes3 = new HashMap<>();
//		CompilationUnit unit3 = ASTUtil.getCompilationUnit("D:\\LLP\\CF3.java", "CF3.java", "D:\\LLP");
//		test.ID = 0;
//		test.getDirectChildren(unit3, 0, candidateNodes3);
//		candidates.add(candidateNodes3);
//		
//		Map<Integer, DataNode> candidateNodes4 = new HashMap<>();
//		CompilationUnit unit4 = ASTUtil.getCompilationUnit("D:\\LLP\\CF4.java", "CF4.java", "D:\\LLP");
//		test.ID = 0;
//		test.getDirectChildren(unit4, 0, candidateNodes4);
//		candidates.add(candidateNodes4);
//		
//		Map<Integer, DataNode> candidateNodes5 = new HashMap<>();
//		CompilationUnit unit5 = ASTUtil.getCompilationUnit("D:\\LLP\\CF5.java", "CF5.java", "D:\\LLP");
//		test.ID = 0;
//		test.getDirectChildren(unit5, 0, candidateNodes5);
//		candidates.add(candidateNodes5);
//		
//		
//		DataNode dataNodeToFix = null;
//		for(Integer label: SourceNodes.keySet()){
//			DataNode n = SourceNodes.get(label);
//			//System.out.println("sr  " + n.node);
//			if(n.node instanceof IfStatement){
//				dataNodeToFix = n;
//				break;
//			}
//		}
//		
//		System.out.println(ASTUtil.getCompilationUnit(dataNodeToFix.node));
//		System.out.println(dataNodeToFix.node);
//		test.output(SourceNodes);
//		
//		
//		
//		
//		//去寻找可用来修复的节点
//		for(Map<Integer, DataNode> nodes: candidates){
//			for(Integer label: nodes.keySet()){
//				DataNode node = nodes.get(label);
//				if(test.couldSubtitue(dataNodeToFix, node)){
//					//进行替换----------用node替换dataNodeToFix
//				}
//			}
//		}
//		
//		
//		DataNode targetNode = test.getMostSimiarNode(dataNodeToFix, candidates);
////		System.out.println(targetNode.node);
//
//		//System.out.println(dataNodeToFix.node.getClass() +"  "+dataNodeToFix.node.getParent().getClass());
//		
//		CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(dataNodeToFix.node);
//		Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
//		ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
//		
//		// 进行修改,先复制再替换
//		ASTNode newNode = dataNodeToFix.node.copySubtree(targetNode.node.getAST(), targetNode.node);
//		rewriter.replace(dataNodeToFix.node, newNode, null);
//		
//		
//		
//		TextEdit edits = rewriter.rewriteAST(document, null);
////		TextEdit edits = oriCompilationUnit.rewrite(document, null);
//        edits.apply(document);
//        System.out.println(document.get());
//        FileUtils.write(new File("D:\\LLP\\CFnew.java"), document.get());

	}

}
