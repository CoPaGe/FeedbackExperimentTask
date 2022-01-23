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
	
	public static Map<AST, Document> mapOfASTandDocument = new HashMap<>();//��Ϊ�����޸���
	public static Integer newFilesIndex = 1;
	public static Map<Integer, DataNode> mapOfIndexAndInsertStatements = new HashMap<>();
	public static Map<DataNode, List<DataNode>> mapOfInsertStatementsAndSameLogicNodes = new HashMap<>();
	
	public static String fileNameToFix = "";
	public static File currentFile = null;
	// ����������������й��Ĳ������֮���ӳ��
	public static Map<Integer, List<DataNode> > mapOfTestCaseAndInsertStatements = new HashMap<>();
	// ��������������������й������֮���ӳ��,������ͨ�������Ǹ���ӵõ���
	public static Map<Integer, List<DataNode> > mapOfTestCaseAndAllStatements = new HashMap<>();
	
	// �����������Ƿ�ͨ��֮���ӳ��
	public static Map<Integer, Boolean> mapOfTestCaseAndIfSucceed = new HashMap<>();
	
	// ���ɵ���ЩNodes�Ͳο�File֮���ӳ��
	public static Map<Map<Integer, DataNode>, String> mapOfNodesAndFile = new HashMap<>(); 
	
	// ����֮���⻻,keyΪ�ο���������� valueΪ���޸�Ŀ�����
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
		
		// ����Ĳ���
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
    		//�������֮��ӳ���ϵ
        	for(List<String> variables: variableRelations){
        		mapOfVariableNames.put(variables.get(2), variables.get(1));
        	}
    	}
    	
    	
		
		
		for(DataNode sourceNode: sourceNodeList){
			DataNode nodeToFix = sourceNode;
			//�Զ������Ľڵ�
			if(nodeToFix.node.toString().trim().startsWith("System.out.println(\"bitse207")){
				continue;
			}
			//ȥѰ�ҿ������޸��Ľڵ�
			    if(nodeToFix.numberOfToken2 > 30){//20190220����5�ĳ���30
			    	continue;
			    }
				for(Integer label: candidate.keySet()){
					DataNode nodeCandidate = candidate.get(label);// �ο������ÿ���ڵ�
					if(nodeCandidate.numberOfToken2 > 30){
						continue;
					}
				//	if(test.couldSubtitue(nodeToFix, nodeCandidate)){
					if(test.couldSubtitue2(nodeToFix, sourceNodes, nodeCandidate,candidate)){//20200723��
						//�����滻----------��node�滻dataNodeToFix
						
						CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(nodeToFix.node);
						
						
						Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
						Document documentTp = new Document(document.get());
//						System.out.println("document");
//						System.out.println(document.get());
						ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
						// �����޸�,�ȸ������滻
						ASTNode newNode = nodeToFix.node.copySubtree(nodeCandidate.node.getAST(), nodeCandidate.node);
						
						//��������,���ڵ�û��location in parent
						if(nodeToFix.node.getLocationInParent() == null){
							continue;
						}
						//System.out.println("ԭʼ��compilationUnit");
						//System.out.println(oriCompilationUnit);
						System.out.println("��Ҫ�޸��Ľڵ㣺" + nodeToFix.nodeType);
						System.out.println(nodeToFix.node);
						System.out.println("��ѡ�ڵ㣺" + nodeCandidate.nodeType);
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
				        System.out.println("���ɺ���³���new program:");
				        System.out.println("document\n"+  document.get());
				        System.out.println("********************************************");
				        mapOfASTandDocument.put(oriCompilationUnit.getAST(), documentTp);
				        //System.out.println(newFilesIndex++);
				        //���������ɵĳ���
				  
				        File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
				        if(newFileDir.exists())
				        	newFileDir.mkdir();
				        try {
							FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), document.get());
							
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        //������ȥ����Щ�Զ����������
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
		
		// ����Ĳ���
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
   		//�������֮��ӳ���ϵ
       	for(List<String> variables: variableRelations){
       		mapOfVariableNames.put(variables.get(2), variables.get(1));
       	}
   	}
   	
   	//20210425���Լ�д��
		int bugMainLabel = test.getMainBlockNum(sourceNodes);
		int candidateMainLabel  = test.getMainBlockNum(candidate);
		DataNode sourceNode = sourceNodes.get(bugMainLabel);
		DataNode nodeCandidate =  candidate.get(candidateMainLabel);
		
			DataNode nodeToFix = sourceNode;
						
			
			//�Զ������Ľڵ�
//			if(nodeToFix.node.toString().trim().startsWith("System.out.println(\"bitse207")){
//				continue;
//			}
			//ȥѰ�ҿ������޸��Ľڵ�
//			    if(nodeToFix.numberOfToken2 > 30){//20190220����5�ĳ���30
//			    	continue;
//			    }
			
//					if(nodeCandidate.numberOfToken2 > 30){
//						continue;
//					}
				//	if(test.couldSubtitue(nodeToFix, nodeCandidate)){
//					if(test.couldSubtitue2(nodeToFix, sourceNodes, nodeCandidate,candidate)){//20200723��
						//�����滻----------��node�滻dataNodeToFix
						
						CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(nodeToFix.node);
						
						
						Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
						Document documentTp = new Document(document.get());
//						System.out.println("document");
//						System.out.println(document.get());
						ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
						// �����޸�,�ȸ������滻
						ASTNode newNode = nodeToFix.node.copySubtree(nodeCandidate.node.getAST(), nodeCandidate.node);
						
						//��������,���ڵ�û��location in parent
//						if(nodeToFix.node.getLocationInParent() == null){
//							continue;
//						}
						//System.out.println("ԭʼ��compilationUnit");
						//System.out.println(oriCompilationUnit);
						System.out.println("��Ҫ�޸��Ľڵ㣺" + nodeToFix.nodeType);
						System.out.println(nodeToFix.node);
						System.out.println("��ѡ�ڵ㣺" + nodeCandidate.nodeType);
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
				        System.out.println("���ɺ���³���new program:");
				        System.out.println("document\n"+  document.get());
				        System.out.println("********************************************");
				        mapOfASTandDocument.put(oriCompilationUnit.getAST(), documentTp);
				        //System.out.println(newFilesIndex++);
				        //���������ɵĳ���
				  
				        File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
				        if(newFileDir.exists())
				        	newFileDir.mkdir();
				        try {
							FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), document.get());
							
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        //������ȥ����Щ�Զ����������
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
		
		// ����Ĳ���
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
   		//�������֮��ӳ���ϵ
       	for(List<String> variables: variableRelations){
       		mapOfVariableNames.put(variables.get(2), variables.get(1));
       	}
   	}
   	
   	
		
		
		for(DataNode sourceNode: sourceNodeList){
			DataNode nodeToFix = sourceNode;
			//�Զ������Ľڵ�
			if(nodeToFix.node.toString().trim().startsWith("System.out.println(\"bitse207")){
				continue;
			}
			//ȥѰ�ҿ������޸��Ľڵ�
//			    if(nodeToFix.numberOfToken2 > 30){//20190220����5�ĳ���30
//			    	continue;
//			    }
				for(Integer label: candidate.keySet()){
					DataNode nodeCandidate = candidate.get(label);// �ο������ÿ���ڵ�
//					if(nodeCandidate.numberOfToken2 > 30){
//						continue;
//					}
				//	if(test.couldSubtitue(nodeToFix, nodeCandidate)){
					if(test.couldSubtitue3(nodeToFix, sourceNodes, nodeCandidate,candidate)){//20200723��
						//�����滻----------��node�滻dataNodeToFix
						
						CompilationUnit oriCompilationUnit = ASTUtil.getCompilationUnit(nodeToFix.node);
						
						
						Document document = mapOfASTandDocument.get(oriCompilationUnit.getAST());
						Document documentTp = new Document(document.get());
//						System.out.println("document");
//						System.out.println(document.get());
						ASTRewrite rewriter = ASTRewrite.create(oriCompilationUnit.getAST());
						// �����޸�,�ȸ������滻
						ASTNode newNode = nodeToFix.node.copySubtree(nodeCandidate.node.getAST(), nodeCandidate.node);
						
						//��������,���ڵ�û��location in parent
						if(nodeToFix.node.getLocationInParent() == null){
							continue;
						}
						//System.out.println("ԭʼ��compilationUnit");
						//System.out.println(oriCompilationUnit);
						System.out.println("��Ҫ�޸��Ľڵ㣺" + nodeToFix.nodeType);
						System.out.println(nodeToFix.node);
						System.out.println("��ѡ�ڵ㣺" + nodeCandidate.nodeType);
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
				        System.out.println("���ɺ���³���new program:");
				        System.out.println("document\n"+  document.get());
				        System.out.println("********************************************");
				        mapOfASTandDocument.put(oriCompilationUnit.getAST(), documentTp);
				        //System.out.println(newFilesIndex++);
				        //���������ɵĳ���
				  
				        File newFileDir = new File(problemPath +"//newFiles//" + currentFile.getName() +"//"+ newFilesIndex);
				        if(newFileDir.exists())
				        	newFileDir.mkdir();
				        try {
							FileUtils.write(new File(newFileDir.getAbsolutePath()+"//" + fileNameToFix), document.get());
							
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        
				        //������ȥ����Щ�Զ����������
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
				//�ж��߼�
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
				node.setOperator(tempOperator); //���л�ԭ
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
	
	
	
	// ������-------------------------------------------------
	public static void fixByModifyingVariables(File fileToFix, File dir) throws IOException{
		CompilationUnit compilationUnit = ASTUtil.getCompilationUnit2(fileToFix.getAbsolutePath(), fileToFix.getName(), dir.getAbsolutePath(), true);
		Document document = mapOfASTandDocument.get(compilationUnit.getAST());
		Document documentTp = new Document(document.get());
		ASTRewrite rewriter = ASTRewrite.create(compilationUnit.getAST());
		TextEdit edits = rewriter.rewriteAST(document, null);
		
		//��������еı���
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
		
		//��ȥ�滻����
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(SimpleName node){
				if(fixed)
					return false;
				if(node.resolveBinding() != null){
					if(node.resolveBinding() instanceof IVariableBinding){
						IVariableBinding variableBinding = (IVariableBinding) node.resolveBinding();
						if(declaredVariableBindings.contains(variableBinding)){
							if(node.getParent() != null){
								//int n = scanner.nextInt();���������nȥ�������滻
								if(node.getParent() instanceof VariableDeclarationFragment){
									return true;
								}
							}
							
							// ȥ�滻������------------------------------
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
		//��������еı���
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
		data = data +  "\n  �ڼ���: " + dijige+"     end";
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
		data = data +  "\n  �ڼ���: " + dijige;
		data = data +  "\n  �ڼ���--�ļ���: " + dijigeString;
		if(fixed) {
		System.out.println(data);
		}
		FileUtils.write(new File(path), data);
		
		
	}
	
	public static void main(String[] args) throws MalformedTreeException, BadLocationException, IOException{
		System.out.println("hereTSE");
		
		Common.initInputList(problemPath);//ע������������¾�д��
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
			
			
//			//���е�һ���������޸Ĳ�����
			fixByModifyingOperators(filex, originalFileDir);
			if(fixed){
				createLog();
				continue;
			}
			//�޸ı���
			fixByModifyingVariables(filex, originalFileDir);
			if(fixed){
				createLog();
				continue;
			}
			//ɾ�����
			fixByDeleteStatements(filex, originalFileDir);
			if(fixed){
				createLog();
				continue;
			}
			
		
			MEndDate =  new Date();
			timeLog+="\nMutationRepairEnd: " + df.format(MEndDate);
			
			MEndTime = System.currentTimeMillis();
			timeLog +=  "\n Mutation time usage: "+ (MEndTime - startTime);
			
			//����Ӧ����һ�ѵ�map listɶ�ĳ�ʼ����� �Ȼ���˵
			mapOfIndexAndInsertStatements.clear();
			mapOfInsertStatementsAndSameLogicNodes.clear();
			mapOfTestCaseAndInsertStatements.clear();
			mapOfTestCaseAndAllStatements.clear();
			mapOfTestCaseAndIfSucceed.clear();
			mapOfVariableNames.clear();
			
			// �����Լ���������
			FileUtil.InsertStatementsForOriginalFile(problemPath, originalFile);
			
			File insertFile = new File(problemPath+ "//originalInsertFile//"+originalFile.getName());
			
			//��ʼ�������ɵĴ��޸����ļ�
			Map<Integer, DataNode> sourceNodes = initializeFileToBeFix(problemPath, insertFile);
			
			test.output(sourceNodes);
			for(Integer ii: sourceNodes.keySet()){
				DataNode node = sourceNodes.get(ii);
				//�ҳ���Щ�Լ���������
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
			
			
			
			
			//�ҵ��Ͳ�������ͬʱ���е���Щ�ڵ�
			getInsertStatementsAndSameLevelNodes(sourceNodes);
			
			//����ÿһ�������ļ���	ȥ���У���ͨ����ͨ����Ȼ��
			if(CMDUtil_new.complileAll(problemPath + "//originalInsertFile", originalFile)){
				// ����
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
				//��ÿһ��ԭʼ��java�ļ�,��ʵ��1��
				for(File file: originalFiles){
					if(file.getAbsolutePath().endsWith("java") && file.getName().equals(originalFile.getName())){
						String fileName = file.getName();
						fileName = fileName.substring(0, fileName.lastIndexOf("."));
						for(File testCase: testCaseFiles){//ÿһ�������ļ�
							String testCaseName = testCase.getName();
							testCaseName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
							String cmd="cmd.exe /c d: && cd " + originalCodePath +" && java " + fileName+" < " + testCase.getAbsolutePath() 
							+ " > " + outPutPath + "\\"+ testCaseName + ".txt";
							//System.out.println(cmd);
							CMDUtil_new.runCMD(cmd);
							String correctOutputFileDir = problemPath + "//Output//" + testCaseName + ".txt";
							String currentOutputFileDir = outPutPath + "//" + testCaseName + ".txt";
							Integer testCaseIndex = Integer.valueOf(testCaseName);
							System.out.println("��    " + testCaseName + "  ����������---------------------------");
							boolean isOk = false;
							if(CMDUtil_new.passOneTestCase(new File(correctOutputFileDir), new File(currentOutputFileDir), testCaseIndex)){
								System.out.println("�������� ͨ���˲������� ------------  ");
								mapOfTestCaseAndIfSucceed.put(testCaseIndex, true);
								isOk = true;
							}
							else{
								System.out.println("δͨ��-------------------- ");
								mapOfTestCaseAndIfSucceed.put(testCaseIndex, false);
							}
							
							//��ȡȫ���漰�������
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
							
							//��¼��Ϣ
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
			
			
			// ������Ϻ󣬼���ÿ���ڵ�ĸ���
			for(Integer it: sourceNodes.keySet()){
				DataNode node = sourceNodes.get(it);
				if(node.numberOfTotalTestCase == 0){
					node.score = 0.0;
				}
				else{
					node.score = 1.0 * node.numberOfWrongTestCase / node.numberOfTotalTestCase;
				}
				
				System.out.println("�ڵ�����\n"+node.node.toString());
				System.out.println("�ڵ����:   "+ node.numberOfWrongTestCase + " " + node.numberOfTotalTestCase + " " + node.score);
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
				
				System.out.println("****************"+filex+ "  try file   " + mapOfNodesAndFile.get(candidate) + " ȥ�޸�"+ "*****************************");
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
			
			//20210425 ��CLARA��˼·����refProgram��BuggyProgram�ṹһ���������帴�ƹ���
//			for(Map<Integer, DataNode> candidate: candidates){
//				
//				//�����if��ʾCLARA��Ҫ�󣬼�������ͼ��ͬ
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
//				System.out.println("JAVACLARA****************"+filex+ "  try file   " + mapOfNodesAndFile.get(candidate) + " ȥ�޸�"+ "*****************************");
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
			
		}//��ÿ���ļ����д���
		
		
		//����Ϊֹ-----------------------------------------------------------------------------------------
		
		
		
		
		
		
		
		
		

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
//		//ȥѰ�ҿ������޸��Ľڵ�
//		for(Map<Integer, DataNode> nodes: candidates){
//			for(Integer label: nodes.keySet()){
//				DataNode node = nodes.get(label);
//				if(test.couldSubtitue(dataNodeToFix, node)){
//					//�����滻----------��node�滻dataNodeToFix
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
//		// �����޸�,�ȸ������滻
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
