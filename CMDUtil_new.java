package llp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class CMDUtil_new {

	// 执行命令行，输入为命令行字符串
	public static String runCMDWithoutPass(String cmd) {
		String result = "";
		try {
			// process = rt.exec("cmd.exe /c javac d:\\test\\tp\\a.java && javac
			// d:\\test\\tp\\tpp\\b.java");
			Process process = Runtime.getRuntime().exec(cmd);

			boolean exitStatus = false;
			try {
				exitStatus = process.waitFor(2, TimeUnit.SECONDS);
//						exitStatus = process.waitFor();

				if (!exitStatus) {
					System.out.println("cmd execute failed!");
				} else {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
					String line = null;
					while ((line = br.readLine()) != null) {
						result += line + "\n";
					}

					br.close();
					process.waitFor();
					return result;
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			process.destroy(); // 销毁子进程
			process = null;
			String command = "taskkill /f /im java.exe";
			Process process2 = Runtime.getRuntime().exec(command);
			try {
				process2.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static Boolean runAllForExperimentWithoutPass(String problemPath, String sourcePath) {
		String originalCodePath = sourcePath;
		String outPutPath = sourcePath + "\\Output";

		File originalCodeFile = new File(originalCodePath);
		File[] originalFiles = originalCodeFile.listFiles();

		System.out.println("* running testcase --- ");
		// 对每一个原始的java文件,其实就1个
		for (File file : originalFiles) {
			if (file.getAbsolutePath().endsWith("java")) {
				String fileName = file.getName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				
				for (int i = 0; i < Common.inputList.size(); i++) {// 每一个测试文件

					String str = Common.inputList.get(i);

				//	String cmd = "cmd.exe /c d: && cd " + originalCodePath + " && echo " + str + "| java " + fileName;

					//20210506改成灵活数量输入参数
					String cmd = "cmd.exe /c d: && cd " + originalCodePath + " && " + str + "| java " + fileName;

					
					 System.out.println(cmd);
					String resultString = runCMDWithoutPass(cmd).trim();
					
					
					
					if (!resultString.equals(Common.outputList.get(i))) {
						System.out.println("Expected: "+Common.outputList.get(i)+"     find: "+resultString);
						System.out.println("* can not pass " + i + "th  test case");
						return false;
					}

				}

			}
		}
		System.out.println("* running complete --- ");
		return true;
	}

	// 执行命令行，输入为命令行字符串
	public static void runCMD(String cmd) {
//		Runtime rt = Runtime.getRuntime();
//		Process process;
//		try {
//			//process = rt.exec("cmd.exe /c javac d:\\test\\tp\\a.java && javac d:\\test\\tp\\tpp\\b.java");
//			process = rt.exec(cmd);
//			System.out.println(process.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		Runtime rt = Runtime.getRuntime();
		Process process;
		try {
			// process = rt.exec("cmd.exe /c javac d:\\test\\tp\\a.java && javac
			// d:\\test\\tp\\tpp\\b.java");
			process = rt.exec(cmd);
			System.out.println(process.toString());

			boolean exitStatus = false;
			try {
				exitStatus = process.waitFor(2, TimeUnit.SECONDS);
//				exitStatus = process.waitFor();
				if (!exitStatus) {
					System.out.println("cmd execute failed!");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			process.destroy(); // 销毁子进程
			process = null;
			String command = "taskkill /f /im java.exe";
			Process process2 = Runtime.getRuntime().exec(command);
			try {
				process2.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 编译所有的文件， 输入为源文件所在的文件夹， 比如 D:\LLP\915A\originalFile
	public static boolean complileAll(String path, File currentFile) {
		// 先对里面的originalCode进行编译
		File originalCodeFile = new File(path);
		File[] originalFiles = originalCodeFile.listFiles();

		List<String> originalCodePaths = new ArrayList<String>();
		for (File file : originalFiles) {
			if (file.getName().equals(currentFile.getName()))
				originalCodePaths.add(file.getAbsolutePath());
		}

		String cmd = "cmd.exe /c javac " + originalCodePaths.get(0);
		for (int i = 1; i < originalCodePaths.size(); i++) {
			cmd = cmd + " && javac " + originalCodePaths.get(i);
		}
		System.out.println("* compling -----");
		runCMD(cmd);

//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		// System.out.println("compling all source code-----complete");

		originalFiles = originalCodeFile.listFiles();
		for (File file : originalFiles) {
			String name1 = file.getName().substring(0, file.getName().indexOf('.'));
			String name2 = currentFile.getName().substring(0, currentFile.getName().indexOf('.'));
			System.out.println("FileName: "+  file.getName()    +"   NAME1:  "+name1+"    NAME2:  "+name2);
			if (file.getName().endsWith(".class") && name1.equals(name2)) {
				
				System.out.println("* compling succeed!");
				return true;
			}
		}

		System.out.println("* compling failed!");
		for (File file2 : originalCodeFile.listFiles()) {
			file2.delete();
		}
		originalCodeFile.delete();
		return false;
	}

	// 运行所有的测试用例， 输入为题目地址， 比如 // D:\LLP\915A
	public static void runAllForExperiment(String problemPath, String sourcePath) {
		String originalCodePath = sourcePath;
		String testCasePath = problemPath + "\\Input";
		String outPutPath = sourcePath + "\\Output";

		File originalCodeFile = new File(originalCodePath);
		File[] originalFiles = originalCodeFile.listFiles();

		File testCaseFile = new File(testCasePath);
		File[] testCaseFiles = testCaseFile.listFiles();
		System.out.println("* running testcase --- ");
		// 对每一个原始的java文件,其实就1个
		for (File file : originalFiles) {
			if (file.getAbsolutePath().endsWith("java")) {
				String fileName = file.getName();
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				for (File testCase : testCaseFiles) {// 每一个测试文件
					String testCaseName = testCase.getName();
					testCaseName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
					String cmd = "cmd.exe /c d: && cd " + originalCodePath + " && java " + fileName + " < "
							+ testCase.getAbsolutePath() + " > " + outPutPath + "\\" + testCaseName + ".txt";
					// System.out.println(cmd);
					runCMD(cmd);

				}
			}
		}
//		try {
//			Thread.sleep(4000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("* running complete --- ");
	}

	// 判断是否能通过所有的测试用例， 输入为题目的文件夹路径，比如 D:\LLP\915A
	public static boolean passAllTestCase(String path, String OutputDir) throws IOException {
		String outputDirPath = path + "//Output//";
		String newOutputDirPath = OutputDir + "//";
		File outputDir = new File(outputDirPath);
		File newOutputDir = new File(newOutputDirPath);
		File[] outputsFiles = outputDir.listFiles();
		File[] newOutputsFiles = newOutputDir.listFiles();

		int num = outputsFiles.length;
		for (int i = 1; i <= num; i++) {
			File oneFile = new File(outputDirPath + i + ".txt");
			// String file1Content = FileUtils.readFileToString(oneFile).trim();
			// String file1Content = readFileExcludeSomething(oneFile);
			// System.out.println("one "+file1Content);

			File twoFile = new File(newOutputDirPath + i + ".txt");
//			String file2Content = FileUtils.readFileToString(twoFile).trim();
			// String file2Content = readFileExcludeSomething(twoFile);
			// System.out.println("two "+ file2Content);
//			if(!file1Content.equals(file2Content)){
//				System.out.println("* can not pass "+ i+"th  test case");
//				return false;
//			}
			if (!twoFilesAreSame(oneFile, twoFile)) {
				System.out.println("* can not pass " + i + "th  test case");
				return false;
			}

		}
		return true;
	}

	public static boolean twoFilesAreSame(File oneFile, File twoFile) {
		FileInputStream fis1 = null;
		FileInputStream fis2 = null;
		InputStreamReader isr1 = null;
		InputStreamReader isr2 = null;
		BufferedReader br1 = null;
		BufferedReader br2 = null;

		try {
			String str1 = "-1";
			String str2 = "-1";
			fis1 = new FileInputStream(oneFile.getAbsolutePath());// FileInputStream
			fis2 = new FileInputStream(twoFile.getAbsolutePath());// FileInputStream
			// 从文件系统中的某个文件中获取字节
			isr1 = new InputStreamReader(fis1);// InputStreamReader 是字节流通向字符流的桥梁,
			isr2 = new InputStreamReader(fis2);// InputStreamReader 是字节流通向字符流的桥梁,
			br1 = new BufferedReader(isr1);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			br2 = new BufferedReader(isr2);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			// System.out.println((br1.readLine() != null) + " "+(br2.readLine() != null));
			
			String str2TotalString = "";
			while((str2 = br2.readLine())!=null) {
				str2TotalString += str2;
			}
			
			String str1TotalString = "";
			while((str1 = br1.readLine())!=null) {
				str1TotalString += str1;
			}
			
			if(str1TotalString.equals(str2TotalString)) {
				return true;
			} else {
				return false;
			}

		} catch (FileNotFoundException e) {
			System.out.println("找不到指定文件");
		} catch (IOException e) {
			System.out.println("读取文件失败");
		} finally {
			try {
				br1.close();
				isr1.close();
				fis1.close();
				// 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public static String readFileExcludeSomething(File oneFile) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String str1 = "";
		try {
			String str = "";
			fis = new FileInputStream(oneFile.getAbsolutePath());// FileInputStream
			// 从文件系统中的某个文件中获取字节
			isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
			br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			while ((str = br.readLine()) != null) {
				str = str.trim();
				if (str.startsWith("bitse207")) {
					continue;
				}

				str1 += str + "\n";
//			    System.out.println(str);
//			    for(int i = 0; i < str.length(); i++){
//			    	System.out.println(i+" "+str.charAt(i)+" "+Integer.valueOf(str.charAt(i)));
//			    }
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
		return str1;
	}

	// 第几个测试用例是否通过
	public static boolean passOneTestCase(File oneFile, File anotherFile, Integer index) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String str1 = "";
		String str2 = "";
		try {
			String str = "";
			fis = new FileInputStream(oneFile.getAbsolutePath());// FileInputStream
			// 从文件系统中的某个文件中获取字节
			isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
			br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			while ((str = br.readLine()) != null) {
				str = str.trim();
				if (str.startsWith("bitse207")) {
					int indexInserStatement = Integer.valueOf(str.split(" ")[1].trim());
					DataNode insertStatement = Experiment.mapOfIndexAndInsertStatements.get(indexInserStatement);
					if (Experiment.mapOfTestCaseAndInsertStatements.get(indexInserStatement) == null) {
						List<DataNode> statements = new ArrayList<>();
						statements.add(insertStatement);
					} else {
						if (!Experiment.mapOfTestCaseAndInsertStatements.get(indexInserStatement)
								.contains(insertStatement)) {
							Experiment.mapOfTestCaseAndInsertStatements.get(indexInserStatement).add(insertStatement);
						}
					}
					continue;
				}

				str1 += str + "\n";
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

		try {
			String str = "";
			fis = new FileInputStream(anotherFile.getAbsolutePath());// FileInputStream
			// 从文件系统中的某个文件中获取字节
			isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
			br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			while ((str = br.readLine()) != null) {
				str = str.trim();
				if (str.startsWith("bitse207")) {
					int indexInserStatement = Integer.valueOf(str.split(" ")[1].trim());
//					   for(String ss: str.split(" ")){
//						   System.out.println(ss+ " "+ss.length());
//					   }
					DataNode insertStatement = Experiment.mapOfIndexAndInsertStatements.get(indexInserStatement);
					if (Experiment.mapOfTestCaseAndInsertStatements.get(index) == null) {
						List<DataNode> statements = new ArrayList<>();
						statements.add(insertStatement);
						Experiment.mapOfTestCaseAndInsertStatements.put(index, statements);
					} else {
						if (!Experiment.mapOfTestCaseAndInsertStatements.get(index).contains(insertStatement)) {
							Experiment.mapOfTestCaseAndInsertStatements.get(index).add(insertStatement);
						}
					}
					continue;
				}

				str2 += str + "\n";
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
//		  System.out.println("正确的答案是   \n"+str1);
//		  System.out.println("当前运行的答案是  \n"+ str2);
		System.out.println("本次运行涉及到的插入语句有  \n");

		for (DataNode node : Experiment.mapOfTestCaseAndInsertStatements.get(index)) {
			System.out.println(node == null);
			System.out.println(node.node.toString());
		}
		if (str1.equals(str2)) {
			return true;
		}

		return false;

	}

	public static String readMy(File oneFile) {
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String str1 = "";
		try {
			String str = "";
			fis = new FileInputStream(oneFile.getAbsolutePath());// FileInputStream
			// 从文件系统中的某个文件中获取字节
			isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
			br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new InputStreamReader的对象
			while ((str = br.readLine()) != null) {
				if (str.startsWith("Main.main")) {
					str = str.substring(10);
					String newString = "";
					for (int i = 0; i < str.length(); i++) {
						if (str.charAt(i) == ')') {
							break;
						}
						newString = newString + str.charAt(i);
					}
					String s1 = newString.split(",")[0].trim();
					String s2 = newString.split(",")[1].trim();
					if (s1.equals(s2)) {
						System.out.println(newString);
						break;
					}
				}
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
		System.out.println("done !!!!!!");
		return str1;
	}

	public static void main(String[] args) {
		File file1 = new File("D:\\LLP\\266A\\newFiles\\CF266A4.java\\1\\Output\\1.txt");
		File file2 = new File("D:\\LLP\\266A\\newFiles\\CF266A4.java\\1\\Output\\2.txt");
		System.out.println(twoFilesAreSame(file1, file2));
	}

}
