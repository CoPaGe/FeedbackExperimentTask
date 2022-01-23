package llp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.omg.CORBA.PUBLIC_MEMBER;


public class FileUtil {
	public static File dirFrom;
	public static File dirTo;
	public static List<String> simpleNameOfSelectedFiles = new ArrayList<>(); 
	public static int statementID = 0; // 用来个语句编号
	
	public static void main(String[] args) throws IOException{

		//InsertStatementsForOriginalFile("D:\\LLP\\916A");
	}
	
	public static ExpressionStatement createInsertStatement(AST ast){
		//ExpressionStatement myExpressionStatement = ast.newE
		QualifiedName qualifiedName = ast.newQualifiedName(ast.newName("System"), ast.newSimpleName("out"));
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(qualifiedName);
		methodInvocation.setName(ast.newSimpleName("println"));
		StringLiteral literal = ast.newStringLiteral();
		String value = "bitse207 "+ (++statementID);
		literal.setLiteralValue(value);
		methodInvocation.arguments().add(literal);
		ExpressionStatement myExpressionStatement = ast.newExpressionStatement(methodInvocation);
		return myExpressionStatement;
		
	}
	
	public static boolean isInsertStatement(ASTNode node){
		if((node instanceof ExpressionStatement) && (node.toString().startsWith("System.out.println(\"bitse207"))){
			return true;
		}
		return false;
	}
	
	//e.g. D:\LLP\916A
	public static void InsertStatementsForOriginalFile(String path, File sourceFile) throws IOException{

				File originalFile = sourceFile;
				//  start--------------------------------------------------
				String  source = FileUtils.readFileToString(originalFile);
		        Document document = new Document(source);
		        System.out.println("document-------------\n"+document.get());
		        
				Map<String, String> options = JavaCore.getOptions(); 
		        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8); 
		        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, 
		        		JavaCore.VERSION_1_8); 
		        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8); 
		        
		        ASTParser astParser = ASTParser.newParser(AST.JLS4);
		        
		        
		        astParser.setSource(document.get().toCharArray());
		        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		        astParser.setEnvironment( // apply classpath
		                new String[] { "D:\\Program Files\\Java\\jdk1.8.0_181\\src.zip" }, //
		                new String[]{path+"\\originalFile"}, new String[] { "UTF-8" }, true);
		        
		        astParser.setBindingsRecovery(true); 
		        astParser.setResolveBindings(true); 
		        astParser.setStatementsRecovery(true); 
		        astParser.setBindingsRecovery(true); 
		        astParser.setUnitName(originalFile.getName()); 
		        astParser.setCompilerOptions(options); 
		        CompilationUnit compilationUnit = (CompilationUnit) (astParser.createAST(null));  
		        compilationUnit.recordModifications();
		        
		        System.out.println("SSSSSSSSSSSSSSSSSSSS");
		        System.out.println(compilationUnit);
		        //进行修改
		        MethodDeclaration mainMethodDeclaration = null;
		        List<AbstractTypeDeclaration > absTyeps = compilationUnit.types();
		        for(AbstractTypeDeclaration ty: absTyeps){
		        	if(ty instanceof TypeDeclaration){
		        		TypeDeclaration typeDeclaration = (TypeDeclaration)ty;
		        		for(MethodDeclaration md: typeDeclaration.getMethods()){
		        			//if(md.getName().toString().equals("main")){
		        				mainMethodDeclaration = md;
		        				md.accept(new ASTVisitor() {
		        					//进行修改
		        					
		        					
		        					//处理 for
		        					public boolean visit(ForStatement node){
		        						if(!(node.getBody() instanceof Block)){
		        							Block  block = compilationUnit.getAST().newBlock();
		        							ASTNode newNode = block.copySubtree(compilationUnit.getAST(), node.getBody());
		        							block.statements().add(newNode);
		        							//node.getBody().delete();
		        							node.setBody(block);
		        						}
		        						return true;
		        					}
		        					
		        					
		        					//处理if
		        					public boolean visit(IfStatement node){
		        						if(node.getElseStatement() != null && !(node.getElseStatement() instanceof Block)){
		        							Block block = compilationUnit.getAST().newBlock();
		        							ASTNode newNode = node.copySubtree(compilationUnit.getAST(), node.getElseStatement());
		        							block.statements().add(newNode);
		        							node.setElseStatement(block);
		        						}
		        						if(node.getThenStatement() != null && !(node.getThenStatement() instanceof Block)){
		        							Block block = compilationUnit.getAST().newBlock();
		        							ASTNode newNode = node.copySubtree(compilationUnit.getAST(), node.getThenStatement());
		        							block.statements().add(newNode);
		        							node.setThenStatement(block);
		        						}
		        						return true;
		        					}
		        					
		        					public boolean visit(WhileStatement node){
		        						if(!(node.getBody() instanceof Block)){
		        							Block  block = compilationUnit.getAST().newBlock();
		        							ASTNode newNode = block.copySubtree(compilationUnit.getAST(), node.getBody());
		        							block.statements().add(newNode);
		        							//node.getBody().delete();
		        							node.setBody(block);
		        						}
		        						return true;
		        					}
		        					
		        					
								});
		        			//}
		        		}
		        	}
		        }
		        
		        // 到此为止都加入了括号
		        // 下面开始插入语句
		        statementID = 0;
		        System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT1");
		        System.out.println(compilationUnit);
		       // if(mainMethodDeclaration != null){
		        compilationUnit.accept(new ASTVisitor() {
		        		
		        		//先处理main函数
		        		public boolean visit(MethodDeclaration node){
		        			Block block = node.getBody();
		        			if(block != null){
		        				block.statements().add(0, createInsertStatement(compilationUnit.getAST()));
		        			}
		        			return true;
		        		}
		        		
		        		public boolean visit(ForStatement node){
		        			Block block = (Block) node.getBody();
		        			if(block != null){
		        				block.statements().add(0, createInsertStatement(compilationUnit.getAST()));
		        			}
		        			return true;
		        			
		        		}
		        		
		        		public boolean visit(IfStatement node){
		        			if(node.getThenStatement() != null){
		        				Block block = (Block) node.getThenStatement();
		        				if(block != null){
		            				block.statements().add(0, createInsertStatement(compilationUnit.getAST()));
		            			}
		        			}
		        			if(node.getElseStatement() != null){
		        				Block block = (Block) node.getElseStatement();
		        				if(block != null){
		            				block.statements().add(0, createInsertStatement(compilationUnit.getAST()));
		            			}
		        			}
		        			return true;
		        		}
		        		
		        		public boolean visit(WhileStatement node){
		        			Block block = (Block) node.getBody();
		        			if(block != null){
		        				block.statements().add(0, createInsertStatement(compilationUnit.getAST()));
		        			}
		        			return true;
		        		}
		        		
					});
		       // }
		        System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT2");
		        System.out.println(compilationUnit);

		        FileUtils.write(new File(path + "//originalInsertFile//"+ originalFile.getName()), compilationUnit.toString());
		        System.out.println("插入语句完毕------" + originalFile.getName());
		        //  end--------------------------------------------------

	}
	
	
	static void getrandProblem() throws FileNotFoundException{
		FileInputStream fis = null;
		  InputStreamReader isr = null;
		  BufferedReader br = null;
		  List<String> problems = new ArrayList<>(); 

			   String str = "";
			   String str1 = "";
			   fis = new FileInputStream("D:\\test\\problems.txt");// FileInputStream
			   // 从文件系统中的某个文件中获取字节
			    isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
			    br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			   try {
				while ((str = br.readLine()) != null) {
//				   if(index == 7)
//				   System.out.println(str);
				    str = str.trim();
				    if(!str.equals("") && !problems.contains(str)){
				    	problems.add(str);
				    }
				   }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   
		int len = problems.size();
//		for(String strr:  problems){
//			System.out.println(strr);
//		}
//		System.out.println(len);
		
		List<String> finalProblemList  = new ArrayList<>();
		
		for(int i = 0; i < 2000; i++){
			Random random = new Random();
			int index = random.nextInt();
			if(!finalProblemList.contains(problems.get(index))){
				System.out.println(problems.get(index));
				finalProblemList.add(problems.get(index));
				
				FileWriter fw;
				try {
					fw = new FileWriter("D:\\test\\problems.txt", true);
					BufferedWriter bw = new BufferedWriter(fw);

						bw.append(problems.get(index));
			           bw.append("\r\n");
			       
			            bw.close();
			            fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
			}
		}
		System.out.println(finalProblemList.size());
		
	}
	
	static void getRandom() throws IOException{
		File file = new File("D:\\test\\javaSelected6");
		File[] problems = file.listFiles();
		int len = problems.length;
		List<String> titles = new ArrayList<>();
		for(File f: problems){
			titles.add(f.getName());
		}
		
		for(String  s: titles)
			System.out.println(s);
		
		System.out.println(titles.size());
	}
	
	
	public static void getTotalTestCase(String path){
		File[] allFiles = new File(path).listFiles();
		List<String> allTestCases = new ArrayList<>(); 
		for(File file : allFiles){
			if(!file.getName().endsWith("edit"))
				continue;
			File[] innerFiles = file.listFiles();
			for(File inner: innerFiles){
				if(!inner.getName().equals("testCase"))
					continue;
				File[] cases = inner.listFiles();
				for(File c: cases){
					String str = "";
					String content = "";
					// read
					FileInputStream fis = null;
					  InputStreamReader isr = null;
					  BufferedReader br = null;
					
						   try {
							   System.out.println(c.getAbsolutePath());
							fis = new FileInputStream(c.getAbsolutePath());
							 isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
							    br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
							   while ((str = br.readLine()) != null) {
								   content += str;
							   }
							   System.out.println(content);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}// FileInputStream
						   // 从文件系统中的某个文件中获取字节
						   catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						  finally {
							   try {
							     br.close();
							     isr.close();
							     fis.close();
							    // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
							   } catch (IOException e) {
							    e.printStackTrace();
							   }
						  }
						   
			
					if(!allTestCases.contains(content)){
						allTestCases.add(content);
					}
				}
			}
		}
		
		//写
		int cnt = -1;
		for(String c: allTestCases){
			cnt++;
		 FileWriter fw;
			try {
				fw = new FileWriter(path + "\\totalTestCase\\"+cnt+".txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.append(c);
		            bw.close();
		            fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
		}
		
	}
	
	public static void analyzeContentOfFiles2(String path){
		String resultPath = path + "\\result2";
		//第几个测试用例，以及对应的输出文件
		Map<Integer, List<File> > mapOfIndexAndFiles = new HashMap<Integer, List<File> >();
		Set<Integer> indexs = new HashSet<>();
		
		File resultDir = new File(resultPath);
		File[] resultFiles = resultDir.listFiles();
		
		for(File file: resultFiles){ // 每个输出文件
			String name = file.getName();
			//System.out.println("name  "+name +"   "+name.indexOf("_"));
			int index = Integer.parseInt(name.substring(0, name.indexOf("_")));
			//System.out.println("index s "+index);
			if(mapOfIndexAndFiles.get(index) == null){
				List<File> files = new ArrayList<File>();
				files.add(file);
				mapOfIndexAndFiles.put(index, files);
			}
			else{
				List<File> files = mapOfIndexAndFiles.get(index); 
				files.add(file);
				mapOfIndexAndFiles.put(index, files);
			}
			indexs.add(index);
		}
		Map<String, Integer> mapOfContentAndTimes = new HashMap<>();
		System.out.println(indexs.size());
		for(Integer index: indexs){// 处理每一个题
			//System.out.println("index  "+index);
			mapOfContentAndTimes.clear();
			for(File file: mapOfIndexAndFiles.get(index)){
//				if(index == 7){
//					System.out.println("file --- "+file.getName());
//				}
				FileInputStream fis = null;
				  InputStreamReader isr = null;
				  BufferedReader br = null;
				try {
					   String str = "";
					   String str1 = "";
					   fis = new FileInputStream(file.getAbsolutePath());// FileInputStream
					   // 从文件系统中的某个文件中获取字节
					    isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
					    br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
					   while ((str = br.readLine()) != null) {
//						   if(index == 15)
//						   System.out.println(str);
					    str1 += str.trim() + "\n";
					   }
					  
					   
					   
					   // 当读取的一行不为空时,把读到的str的值赋给str1
					  // System.out.println(str1);// 打印出str1
//					   if(index == 7){
//						   System.out.println("file name  "+file.getAbsolutePath());
//						   System.out.println(str1+str1.length());
//					   }
					   if(mapOfContentAndTimes.get(str1) == null){
						   mapOfContentAndTimes.put(str1, 1);
					   }
					   else{
						   mapOfContentAndTimes.put(str1, mapOfContentAndTimes.get(str1) + 1);
					   }
					   
					  } catch (FileNotFoundException e) {
					   System.out.println("找不到指定文件");
					  } catch (IOException e) {
					   System.out.println("读取文件失败");
					  } finally {
					   try {
					     br.close();
					     isr.close();
					     fis.close();
					    // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
					   } catch (IOException e) {
					    e.printStackTrace();
					   }
					  
				}
				
			}
		    //System.out.println(index +"  " +   mapOfContentAndTimes.size());
	        if(mapOfContentAndTimes.size() > 1){
	        	System.out.println("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR   " +index);
	        	for(String str: mapOfContentAndTimes.keySet()){
	        		System.out.println(str+"  "+mapOfContentAndTimes.get(str));
	        	}
	        	//return;
	        }
		}
		System.out.println("analyze --- complete   the output are all same");
			
	}
	
	
	// 分析测试用例输出的结果  D:\test\tp\5D， 找出输出不一致的某个测试用例
	public static void analyzeContentOfFiles(String path){
		String resultPath = path + "\\result";
		//第几个测试用例，以及对应的输出文件
		Map<Integer, List<File> > mapOfIndexAndFiles = new HashMap<Integer, List<File> >();
		Set<Integer> indexs = new HashSet<>();
		
		File resultDir = new File(resultPath);
		File[] resultFiles = resultDir.listFiles();
		
		for(File file: resultFiles){ // 每个输出文件
			String name = file.getName();
			//System.out.println("name  "+name +"   "+name.indexOf("_"));
			int index = Integer.parseInt(name.substring(0, name.indexOf("_")));
			if(mapOfIndexAndFiles.get(index) == null){
				List<File> files = new ArrayList<File>();
				files.add(file);
				mapOfIndexAndFiles.put(index, files);
			}
			else{
				List<File> files = mapOfIndexAndFiles.get(index); 
				files.add(file);
				mapOfIndexAndFiles.put(index, files);
			}
			indexs.add(index);
		}
		Map<String, Integer> mapOfContentAndTimes = new HashMap<>();
		
		for(Integer index: indexs){// 处理每一个题
			mapOfContentAndTimes.clear();
			for(File file: mapOfIndexAndFiles.get(index)){
//				if(index == 7){
//					System.out.println("file --- "+file.getName());
//				}
				FileInputStream fis = null;
				  InputStreamReader isr = null;
				  BufferedReader br = null;
				try {
					   String str = "";
					   String str1 = "";
					   fis = new FileInputStream(file.getAbsolutePath());// FileInputStream
					   // 从文件系统中的某个文件中获取字节
					    isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
					    br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
					   while ((str = br.readLine()) != null) {
//						   if(index == 7)
//						   System.out.println(str);
					    str1 += str.trim() + "\n";
					   }
					  
					   
					   
					   // 当读取的一行不为空时,把读到的str的值赋给str1
					  // System.out.println(str1);// 打印出str1
//					   if(index == 7){
//						   System.out.println("file name  "+file.getAbsolutePath());
//						   System.out.println(str1+str1.length());
//					   }
					   if(mapOfContentAndTimes.get(str1) == null){
						   mapOfContentAndTimes.put(str1, 1);
					   }
					   else{
						   mapOfContentAndTimes.put(str1, mapOfContentAndTimes.get(str1) + 1);
					   }
					   
					  } catch (FileNotFoundException e) {
					   System.out.println("找不到指定文件");
					  } catch (IOException e) {
					   System.out.println("读取文件失败");
					  } finally {
					   try {
					     br.close();
					     isr.close();
					     fis.close();
					    // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
					   } catch (IOException e) {
					    e.printStackTrace();
					   }
					  
				}
				
			}
		    //System.out.println(index +"  " +   mapOfContentAndTimes.size());
	        if(mapOfContentAndTimes.size() > 1){
	        	System.out.println("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR   " +index);
	        	for(String str: mapOfContentAndTimes.keySet()){
	        		System.out.println(str+"  "+mapOfContentAndTimes.get(str));
	        	}
	        	//return;
	        }
		}
		System.out.println("analyze --- complete   the output are all same");
			
	}
	
	public static void analyzeContentOfFilesForSingleAnswer(String path){
		String resultPath = path + "\\result";
		//第几个测试用例，以及对应的输出文件
		Map<Integer, List<File> > mapOfIndexAndFiles = new HashMap<Integer, List<File> >();
		Set<Integer> indexs = new HashSet<>();
		
		File resultDir = new File(resultPath);
		File[] resultFiles = resultDir.listFiles();
		
		for(File file: resultFiles){ // 每个输出文件
			String name = file.getName();
			//System.out.println("name  "+name +"   "+name.indexOf("_"));
			int index = Integer.parseInt(name.substring(0, name.indexOf("_")));
			if(mapOfIndexAndFiles.get(index) == null){
				List<File> files = new ArrayList<File>();
				files.add(file);
				mapOfIndexAndFiles.put(index, files);
			}
			else{
				List<File> files = mapOfIndexAndFiles.get(index); 
				files.add(file);
				mapOfIndexAndFiles.put(index, files);
			}
			indexs.add(index);
		}
		Map<String, Integer> mapOfContentAndTimes = new HashMap<>();
		
		for(Integer index: indexs){// 处理每一个题
			mapOfContentAndTimes.clear();
			for(File file: mapOfIndexAndFiles.get(index)){
//				if(index == 7){
//					System.out.println("file --- "+file.getName());
//				}
				FileInputStream fis = null;
				  InputStreamReader isr = null;
				  BufferedReader br = null;
				try {
					   String str = "";
					   String str1 = "";
					   fis = new FileInputStream(file.getAbsolutePath());// FileInputStream
					   // 从文件系统中的某个文件中获取字节
					    isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
					    br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
					   while ((str = br.readLine()) != null) {
//						   if(index == 7)
//						   System.out.println(str);
					    str1 += str.trim() + "\n";
					   }
					   
					   
					   // 当读取的一行不为空时,把读到的str的值赋给str1
					  // System.out.println(str1);// 打印出str1
//					   if(index == 7){
//						   System.out.println("file name  "+file.getAbsolutePath());
//						   System.out.println(str1+str1.length());
//					   }
					   if(mapOfContentAndTimes.get(str1) == null){
						   mapOfContentAndTimes.put(str1, 1);
					   }
					   else{
						   mapOfContentAndTimes.put(str1, mapOfContentAndTimes.get(str1) + 1);
					   }
					   
					  } catch (FileNotFoundException e) {
					   System.out.println("找不到指定文件");
					  } catch (IOException e) {
					   System.out.println("读取文件失败");
					  } finally {
					   try {
					     br.close();
					     isr.close();
					     fis.close();
					    // 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
					   } catch (IOException e) {
					    e.printStackTrace();
					   }
					  
				}
				
			}
		    //System.out.println(index +"  " +   mapOfContentAndTimes.size());
	        if(mapOfContentAndTimes.size() > 1){
	        	System.out.println("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR   " +index);
	        	for(String str: mapOfContentAndTimes.keySet()){
	        		System.out.println(str+"  "+str.length());
	        	}
	        }
		}
		System.out.println("analyze --- complete   the output are all same");
			
	}

	
	// 对应题目处理流程的第三步和第四步
	public static void collectOriginalAndEditCode(String path){
		File problem = new File(path);
		File[] allFiles = problem.listFiles();
		for(File dir: allFiles){
			if(dir.getName().endsWith("edit")){
				FileUtil.dirFrom = dir;
				FileUtil.dirTo = new File(problem.getAbsolutePath() + "\\editCode");
				listFileInDir2(FileUtil.dirFrom);
			}
			else if(!dir.getName().equals("editCode") && !dir.getName().equals("originalCode")
					&& !dir.getName().equals("testCase") && !dir.getName().equals("parameters.json")
					&& !dir.getName().equals("result") && !dir.getName().endsWith("txt")
					&& !dir.getName().equals("WrongAnswer")){
				FileUtil.dirFrom = dir;
				FileUtil.dirTo = new File(problem.getAbsolutePath() + "\\originalCode");
				listFileInDir2(FileUtil.dirFrom);
			}
		}
	}
	
	
	//为每个题目创建传统方法的必要的文件夹， paht的格式为  D:\test\tp\5D\9912327edit
	public static void createFoldersForTraditionalExperiment(String path){
		File file1 = new File(path + "\\result");
		File file2 = new File(path + "\\testCase");
		
		if(!file1.exists())
			file1.mkdir();
		if(!file2.exists())
			file2.mkdir();
	}
	
	
	//为每个题目创建文件夹 ，这是为了保存我们改进的实验的D:\test\tp\5E,
	public static void createFoldersForOurExperiment (String path){
		
		File file1 = new File(path+"\\editCode");
		File file2 = new File(path+"\\originalCode");
		File file3 = new File(path+"\\testCase");
		File file4 = new File(path+"\\result");
		if(!file1.exists())
			file1.mkdir();
		if(!file2.exists())
			file2.mkdir();
		if(!file3.exists())
			file3.mkdir();
		if(!file4.exists())
			file4.mkdir();
		//D:\test\tp\5D\9912327edit 在这样的目录下生成必要的文件夹，为了传统方法用
		File[] files = new File(path).listFiles();
		for(File f: files){
			if(f.getName().endsWith("edit")){
				createFoldersForTraditionalExperiment(f.getAbsolutePath());
			}
		}
		System.out.println("complelte ");
	}
	
	public static void cooopy(String path){
		File outest = new File(path);// javatp
		File[] files = outest.listFiles();
		for(File answersDir : files){//javatp//1A
			File[] answerSingleDir = answersDir.listFiles();//javatp//1A//10010
			for(File answerFile: answerSingleDir){
				System.out.println(answerFile.getAbsolutePath());
				if(!answerFile.getName().endsWith("edit")){
					FileUtil.dirFrom = answerFile;
					FileUtil.dirTo = new File(answerFile.getAbsolutePath()+"edit");
					listFileInDir2(FileUtil.dirFrom);
				}
			}
		}
	}
	
	//拷贝文件，传入的是源文件夹， dirFrom也是源文件夹目录， dirTo是目标文件夹，把源文件夹的所有文件拷贝到目标文件夹
	public static void listFileInDir2(File file) {
		System.out.println("----file  "+file.getAbsolutePath());
        File[] files = file.listFiles();   
       for (File f : files) {
            String tempfrom = f.getAbsolutePath();   
            String tempto = tempfrom.replace(dirFrom.getAbsolutePath(),   
                    dirTo.getAbsolutePath()); // 后面的路径 替换前面的路径名   
           if (f.isDirectory()) {   
                File tempFile = new File(tempto);   
                tempFile.mkdirs();   
                listFileInDir2(f);   
            } else {   
                System.out.println("源文件:" + f.getAbsolutePath());   
               //   
               int endindex = tempto.lastIndexOf("\\");// 找到"/"所在的位置   
                String mkdirPath = tempto.substring(0, endindex);   
                File tempFile = new File(mkdirPath);   
                tempFile.mkdirs();// 创建立文件夹   
                System.out.println("目标点:" + tempto);   
                copy(tempfrom, tempto);   
            }   
        }   
    }   
	//源目标文件夹
	public static void listFileInDir(File file, int cnt) {   
        File[] files = file.listFiles();   
       for (File f : files) {
    	    if(f.isDirectory() && cnt == 0  && !simpleNameOfSelectedFiles.contains(f.getName())){
    	    	continue;
    	    }
            String tempfrom = f.getAbsolutePath();   
            String tempto = tempfrom.replace(dirFrom.getAbsolutePath(),   
                    dirTo.getAbsolutePath()); // 后面的路径 替换前面的路径名   
           if (f.isDirectory()) {   
                File tempFile = new File(tempto);   
                tempFile.mkdirs();   
                listFileInDir(f, cnt + 1);   
            } else {   
                System.out.println("源文件:" + f.getAbsolutePath());   
               //   
               int endindex = tempto.lastIndexOf("\\");// 找到"/"所在的位置   
                String mkdirPath = tempto.substring(0, endindex);   
                File tempFile = new File(mkdirPath);   
                tempFile.mkdirs();// 创建立文件夹   
                System.out.println("目标点:" + tempto);   
                copy(tempfrom, tempto);   
            }   
        }   
    }   
	
	public static  void copy(String from, String to) {   
        try {   
             InputStream in = new FileInputStream(from);   
             OutputStream out = new FileOutputStream(to);   
   
            byte[] buff = new byte[1024];   
            int len = 0;   
            while ((len = in.read(buff)) != -1) {   
                 out.write(buff, 0, len);   
             }   
             in.close();   
             out.close();   
         } catch (FileNotFoundException e) {   
             e.printStackTrace();   
         } catch (IOException e) {   
             e.printStackTrace();   
         }   
     }   
	
	
	
	
	public static void readFileDir(String path){
		LinkedList<File> dirList = new LinkedList<>();
		LinkedList<String> fileList = new LinkedList<>();
		File dir = new File(path); //指定路径
		File[] files = dir.listFiles(); //所有文件
		int problemsCnt = 0;
		int javaFileCnt = 0;
		for(File file: files){//每个题目
			//设计成了这里的每个file都是diretory, 
			problemsCnt++;
			System.out.println(file.getAbsolutePath());
			File[] singleAnswerDirs = file.listFiles(); //每个题解java文件都有一个固定的文件夹,每个文件夹下有一个java文件
			for(File singleAnswerDir : singleAnswerDirs){
				
				File[] finalFiles = singleAnswerDir.listFiles(); // 最内层的每个文件

				for(File finalFile: finalFiles){
					javaFileCnt++;
					System.out.println(finalFile.getAbsolutePath()+"    "+finalFile.getName());
				}
				
			}
		}
		
		//统计数量信息
		System.out.println("题目总个数 :   "+ problemsCnt);
		System.out.println("文件总个数:   "+javaFileCnt);
	}
}
