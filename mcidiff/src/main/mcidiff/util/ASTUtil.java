package mcidiff.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import mcidiff.model.CloneInstance;

public class ASTUtil {
	/**
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	public static int computeTypeDifferenceLevel(ASTNode node1, ASTNode node2){
		if(node1.getNodeType()==node2.getNodeType()){
			return 5;
		}
		else{
			Class<?> node1Type = node1.getClass();
			Class<?> node2Type = node2.getClass();
			
			ArrayList<Class<?>> superClassChain1 = findSuperClassChain(node1Type);
			ArrayList<Class<?>> superClassChain2 = findSuperClassChain(node2Type);
			
			int length = (superClassChain1.size() < superClassChain2.size()) ? superClassChain1.size() : superClassChain2.size();
			
			int similarLevel = 0;
			for(int i=2; i<length; i++){
				Class<?> class1 = superClassChain1.get(i);
				Class<?> class2 = superClassChain2.get(i);
				
				if(class1.equals(class2)){
					similarLevel++;
				}
			}
			
			return similarLevel;
		}
	}
	
	private static ArrayList<Class<?>> findSuperClassChain(Class<?> node1Type) {
		ArrayList<Class<?>> chain = new ArrayList<>();
		Class<?> superClass = node1Type.getSuperclass();
		while(superClass != null){
			chain.add(superClass);
			superClass = superClass.getSuperclass();
		}
		
		for(int i=0; i<chain.size()/2; i++){
			Class<?> tmp = chain.get(chain.size()-1);
			chain.set(chain.size()-1-i, chain.get(i));
			chain.set(i, tmp);
		}
		
		return chain;
	}

	public static ArrayList<String> parseTokens(String content){
		ArrayList<String> tokenList = new ArrayList<>();
		
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		scanner.setSource(content.toCharArray());
		
		while(true){
			try {
				int t = scanner.getNextToken();
				if(t == ITerminalSymbols.TokenNameEOF){
					break;
				}
				String tokenName = new String(scanner.getCurrentTokenSource());
				tokenList.add(tokenName);
				
			} catch (InvalidInputException e) {
				e.printStackTrace();
			}
			
		}
		
		return tokenList;
	}
	
	public static String retrieveContent(String absolutePath){
		String everything = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(absolutePath));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        everything = sb.toString();
	    } catch (Exception e) {
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
		return everything;
	}
	
	public static String retrieveContent(CloneInstance instance){
		String[] linesContent = instance.getFileContent().split("\n");
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<linesContent.length; i++){
			int lineNum = i+1;
			if(lineNum >= instance.getStartLine() && lineNum <= instance.getEndLine()){
				buffer.append(linesContent[i]);
				buffer.append("\n");
			}
			
		}
		
		return buffer.toString();
	}
	
	public static String retrieveContent(String absolutePath, int startLine, int endLine){
		
		if(startLine > endLine){
			System.err.print("start line is larger than end line");
			return null;
		}
		
		int count = 1;
		String everything = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(absolutePath));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        
	        while (line != null) {
	        	
	        	if(count >= startLine && count <= endLine){
	        		sb.append(line);
	        		sb.append(System.lineSeparator());
	        	}
	        	
	        	line = br.readLine();
	        	count++;
	        }
	        everything = sb.toString();
	    } catch (Exception e) {
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
		
		if(startLine > count || endLine > count){
			System.err.print("start line or end line is larger the total line number");
			return null;
		}
		
		return everything;
	}
	
	public static CompilationUnit generateCompilationUnit(String fileName, IJavaProject project){
		CloneInstance instance = new CloneInstance(fileName, -1, -1);
		return generateCompilationUnit(instance, project);
	}
	
	/**
	 * 
	 * @param rangeContent
	 * @param path
	 * @param project
	 * @return
	 */
	public static CompilationUnit generateCompilationUnit(CloneInstance instance, IJavaProject project){
		
		if(instance.getFileContent() == null){
			String content = retrieveContent(instance.getFileName());
			instance.setFileContent(content);
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		char[] content = instance.getFileContent().toCharArray();
		parser.setSource(content);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		if(project != null){
			String contentString = String.valueOf(content);
			int fromIndex = contentString.indexOf("package");
			String packageString = contentString.substring(fromIndex+8, contentString.indexOf(";", fromIndex));
			packageString = packageString.replace(".", "/");
			String path = instance.getFileName();
			String unitName = path.substring(path.indexOf(packageString));
			unitName = unitName.replace("\\", "/");
			unitName = "/" + unitName;
			
			parser.setProject(project);
			parser.setUnitName(unitName);
			parser.setResolveBindings(true);			
		}
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		return cu;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void sort(ArrayList list, Comparator comparator){
		for(int i=0; i<list.size(); i++){
			int targetPosition = list.size()-i-1;
			int replacePosition = 0;
			for(int j=1; j<=targetPosition; j++){
				Object targetingSet = list.get(j);
				Object replacingSet = list.get(replacePosition);
				
//				if(list.get(replacePosition).toString().contains("(") && targetingSet.toString().contains("name")){
//					System.currentTimeMillis();
//				}
//				
//				if(targetingSet.toString().contains("name")){
//					System.currentTimeMillis();//
//				}
				
				if(comparator.compare(replacingSet, targetingSet)<0){
					replacePosition = j;
				}
			}
			
			//System.out.println(list.get(replacePosition));
			
			Object tmp = list.get(replacePosition);
			list.set(replacePosition, list.get(targetPosition));
			list.set(targetPosition, tmp);
		}
	}
	
	public static boolean isSimpleNameDeclaration(IBinding binding, SimpleName name){
		if(binding.getKind() == IBinding.METHOD){
			ASTNode node = name.getParent();
			while(!(node instanceof MethodDeclaration || node instanceof MethodInvocation)){
				node = node.getParent();
				if(node == null){
					break;
				}
			}
			
			if(node != null){
				if(node instanceof MethodDeclaration){
					MethodDeclaration md = (MethodDeclaration)node;
					return md.getName().equals(name);
				}
			}
			return false;
		}
		else if(binding.getKind() == IBinding.TYPE){
			ASTNode node = name.getParent();
			while(!(node instanceof SimpleType || node instanceof TypeDeclaration)){
				node = node.getParent();
				if(node == null){
					break;
				}
			}
			
			if(node != null){
				if(node instanceof TypeDeclaration){
					TypeDeclaration td = (TypeDeclaration)node;
					return td.getName().equals(name);
				}
			}
			return false;
		}
		else if(binding.getKind() == IBinding.VARIABLE){
//			ASTNode node = name.getParent();
//			while(!(node instanceof VariableDeclaration)){
//				node = node.getParent();
//				if(node == null){
//					break;
//				}
//			}
//			
//			if(node != null){
//				if(node instanceof VariableDeclaration){
//					VariableDeclaration vd = (VariableDeclaration)node;
//					return vd.getName().equals(name);
//				}
//			}
			return false;
		}
		
		return false;
	}
}
