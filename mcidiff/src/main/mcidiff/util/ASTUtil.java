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
		parser.setSource(instance.getFileContent().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		if(project != null){
			String path = instance.getFileName();
			String unitName = path.substring(path.indexOf(project.getProject().getName()));
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
