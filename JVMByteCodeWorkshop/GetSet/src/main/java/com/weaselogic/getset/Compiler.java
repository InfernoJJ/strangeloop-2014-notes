package com.weaselogic.getset;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class Compiler {
    public enum Options {
        NOT_NULL, NEVER_NULL
    }
    
	private static class TypeDeclaration {
		final String rawType;
		final String generic;
		final boolean isGeneric;
		
		public TypeDeclaration(String typeDecl) {
			typeDecl = typeDecl.trim();
			if (typeDecl.contains("<")) {
                generic = typeDecl.substring(typeDecl.indexOf('<'));
                rawType = typeDecl.substring(0, typeDecl.indexOf('<'));
                isGeneric = true;
            } else {
                generic = "";
                rawType = typeDecl;
                isGeneric = false;
            }
		}
		
		public TypeDeclaration(String rawType, String generic) {
			this.rawType = rawType;
			this.generic = generic;
			isGeneric = !generic.isEmpty();
		}
	}
	
    public static final String COMMENT = "#";

    public static final String PACKAGE = ">>";

    public static final Pattern ATTRIBUTE_LINE = 
            Pattern.compile("^\\s*([^:!]+)\\s*(:|!{1,2})\\s*([^:!]+)\\s*$");

    public static final String IMPORT = "<<";
    
    private static final Map<String, String> PRIMITIVE_DESCRIPTOR_MAP = new HashMap<>();
    static {
        PRIMITIVE_DESCRIPTOR_MAP.put("byte", "B");
        PRIMITIVE_DESCRIPTOR_MAP.put("char", "C");
        PRIMITIVE_DESCRIPTOR_MAP.put("double", "D");
        PRIMITIVE_DESCRIPTOR_MAP.put("float", "F");
        PRIMITIVE_DESCRIPTOR_MAP.put("int", "I");
        PRIMITIVE_DESCRIPTOR_MAP.put("long", "J");
        PRIMITIVE_DESCRIPTOR_MAP.put("short", "S");
        PRIMITIVE_DESCRIPTOR_MAP.put("boolean", "Z");
    }
    
    private final Path src;
    private final Map<String, String> importMap = new HashMap<>();
    private final ClassBuilder cBuilder;
    private final List<String> genericParameters = new ArrayList<>();
    
    private int sourceLine = 0;
    private String pkg;
    private String className;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    
    public Compiler(Path src, ClassBuilder cBuilder) {
        this.src = src;
        this.cBuilder = cBuilder;
    }
    
    public byte[] compile() throws CompileException {
        try(BufferedReader br = Files.newBufferedReader(src, Charset.defaultCharset())) {
            String line = readSourceLine(br);
            
            pkg = processPackage(line);
            
            while((line = readSourceLine(br)) != null) {
                line = line.trim();
                if (!addToImportMap(line)) {
                    break;
                }
            }
            
            processClassName(line);
            
            ClassNode cn = 
            		cBuilder.createClass(pkg.isEmpty() ? className : pkg + "." + className,
            				generateSignature());
            
            cn.sourceFile = src.getFileName().toString();
            
            while((line = readSourceLine(br)) != null) {
                line = line.trim();
                addAttribute(line);
            }
            
            try {
            	ClassWriter cw = new ClassWriter(0);
            	cn.accept(cw);
            	return cw.toByteArray();
            } catch(Exception x) {
            	throw new CompileException("Error compiling binary class", x);
            }
            
        } catch (IOException e) {
            throw new CompileException(String.format("Unable to read file %s", src), e);
        } catch(NullPointerException npx) {
            throw new CompileException(String.format("Unexpected end of file after line", sourceLine), npx);
        }
    }
    
    private String generateSignature() {
    	if (genericParameters.isEmpty()) {
    		return null;
    	}
    	
    	try(Formatter fmt = new Formatter()) {
    	    fmt.format("<");
    	    for(String parm : genericParameters) {
    	        fmt.format("%s:Ljava/lang/Object;", parm);
    	    }
    	    fmt.format(">Ljava/lang/Object;");
    	    return fmt.toString();
    	}
    }

    private void addAttribute(String line) throws CompileException {
        Matcher m = ATTRIBUTE_LINE.matcher(line);
    	if (!m.matches()) {
    		throwSyntaxError(String.format("Expected name : type on line %d", line));
    	}

    	// TODO: make sure null constraint options are only applied to object types
    	// TODO: never-null requires object with default constructor
    	cBuilder.addAttribute(m.group(1).trim(), 
    	        resolveSignature(m.group(3).trim(), true), 
    	        genericParameters, getOptionsFromOperator(m.group(2)));
	}

	private Set<Options> getOptionsFromOperator(String group) {
        switch(group) {
        case "!":
            return EnumSet.of(Options.NOT_NULL);
        case "!!":
            return EnumSet.of(Options.NEVER_NULL);
        }
        return EnumSet.noneOf(Options.class);
    }

    private String resolveSignature(String rawType, boolean allowPrimitive) {
	    final int arrayDimensions;
	    
	    if (rawType.contains("[")) {
	        arrayDimensions = countArrayDimensions(rawType);
	        rawType = rawType.substring(0, rawType.indexOf("["));
	    } else {
	        arrayDimensions = 0;
	    }
	    
	    final String internalType = resolveInternal(rawType, allowPrimitive || (arrayDimensions > 0));
	    
	    return makeArray(internalType, arrayDimensions);
    }

    private String resolveInternal(String rawType, boolean allowPrimitive) {
        if (allowPrimitive && PRIMITIVE_DESCRIPTOR_MAP.containsKey(rawType)) {
            return PRIMITIVE_DESCRIPTOR_MAP.get(rawType);
        } else {
        	TypeDeclaration typeDecl = new TypeDeclaration(rawType);
            if (importMap.containsKey(typeDecl.rawType)) {
                return internalObjectForm(new TypeDeclaration(importMap.get(typeDecl.rawType), typeDecl.generic));
            } else {
                try {
                    Class.forName(typeDecl.rawType);
                    return internalObjectForm(typeDecl);
                } catch (ClassNotFoundException e) {
                	try {
                		Class<?> c = Class.forName("java.lang." + typeDecl.rawType);
                		return internalObjectForm(new TypeDeclaration(c.getName(), typeDecl.generic));
                	} catch(ClassNotFoundException cfne) {
                	    if (genericParameters.contains(rawType)) {
                	        return String.format("T%s;", rawType);
                	    }
                		throw new UnsupportedOperationException("Type not resolved: " + rawType);
                	}
                }
            }
        }
    }

    private String internalObjectForm(TypeDeclaration typeDecl) {
        String erasureType = "L" + typeDecl.rawType.replace('.', '/') + ";";
        if (typeDecl.isGeneric) {
        	String generic = typeDecl.generic.substring(1, typeDecl.generic.length() - 1);
        	String[] parts = generic.split(",");
        	StringBuilder sb = new StringBuilder(erasureType);
        	sb.setLength(sb.length() - 1); // remove trailing ;
        	sb.append('<');
        	for(String genType : parts) {
        		sb.append(resolveSignature(genType.trim(), false));
        	}
        	erasureType = sb.append(">;").toString();
        }
        return erasureType;
    }

    private int countArrayDimensions(String rawType) {
        int dims = 0;
        int pos = 0;
        
        while(rawType.indexOf("[]", pos) >= 0) {
            dims++;
            pos = rawType.indexOf(']', pos + 1);
        }
        
        return dims;
    }

    private String makeArray(String internalType, int arrayDimensions) {
        if (arrayDimensions == 0) {
            return internalType;
        }
        StringBuilder sb = new StringBuilder(internalType.length() + arrayDimensions);
        sb.append(internalType);
        while(arrayDimensions-- > 0) {
            sb.insert(0, '[');
        }
        return sb.toString();
    }

    private void processClassName(String classDeclaration) {
    	int genParmIndex = classDeclaration.indexOf('<');
    	if (genParmIndex > 0) {
    		String[] genParms = 
    				classDeclaration.substring(genParmIndex + 1, classDeclaration.length() - 1).split(",");
    		for(String parm : genParms) {
    			genericParameters.add(parm.trim());
    		}
    		classDeclaration = classDeclaration.substring(0, genParmIndex);
    	}
        className = classDeclaration;
    }

    private boolean addToImportMap(String line) {
        if (line.startsWith(IMPORT)) {
            processImport(line.substring(2).trim());
            return true;
        }
        return false;
    }

    private void processImport(String fqn) {
        if (fqn.contains(".")) {
            importMap.put(fqn.substring(fqn.lastIndexOf('.') + 1), fqn);
        } else {
            importMap.put(fqn, fqn);
        }
    }

    private String processPackage(String line) throws CompileException {
        if (line.startsWith(PACKAGE)) {
            String p = line.substring(2).trim();
            checkPackageSyntax();
            return p;
        }
        return throwSyntaxError("Expected " + PACKAGE + " package");
    }

    private void checkPackageSyntax() throws CompileException {
        // TODO Auto-generated method stub
    }

    private <T> T throwSyntaxError(String detail) throws CompileException {
        throw new CompileException(String.format("Syntax error on line %d: %s", sourceLine, detail));
    }

    private String readSourceLine(BufferedReader br) throws IOException {
        String line = null;
        while((line = br.readLine()) != null) {
            sourceLine++;
            line = line.trim();
            if (!line.startsWith(COMMENT)  && !line.isEmpty()) {
                return line;
            }
        }
        return null;
    }

}
