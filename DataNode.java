package llp;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jdt.core.dom.ASTNode;

public class DataNode {
	public ASTNode node; //所代表的的AST节点
	public int label; //编号
	public List<Integer> childrenLables = new ArrayList<>(); //直接的子节点的编号
	public List<ASTNode> childrenNodes = new ArrayList<>(); //直接的子节点
	public boolean isLeaf = false; //是否是叶子节点
	public String nodeType = "unknown";
	public String attachedStatementType = "null";
	public List<Integer> logicPoseList = new ArrayList<>();
	public static double score = -1;
	public int numberOfToken = 0;
	public int numberOfToken2 = 0;
	public int numberOfWrongTestCase = 0; //为了计算概率，运行测试用例失败的情况下，该节点运行的次数
	public int numberOfTotalTestCase = 0; //所有的测试用例，该节点运行了多少次

	
}
