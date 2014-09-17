package com.weaselogic.getset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.weaselogic.getset.Compiler.Options;

public class ClassBuilder {
    private static final String NPE = "java/lang/NullPointerException";

    private static final String SETTER_SIGNATURE = "(%s)V";

    private static final String GETTER_SIGNATURE = "()%s";

    private static final String OBJECT_PRIMITIVE = ";;;";
    
    private static final Map<String, Integer> PRIMITIVE_LOAD_MAP = new HashMap<>();
    static {
        PRIMITIVE_LOAD_MAP.put("B", Opcodes.ILOAD);
        PRIMITIVE_LOAD_MAP.put("C", Opcodes.ILOAD);
        PRIMITIVE_LOAD_MAP.put("D", Opcodes.DLOAD);
        PRIMITIVE_LOAD_MAP.put("F", Opcodes.FLOAD);
        PRIMITIVE_LOAD_MAP.put("I", Opcodes.ILOAD);
        PRIMITIVE_LOAD_MAP.put("J", Opcodes.LLOAD);
        PRIMITIVE_LOAD_MAP.put("S", Opcodes.ILOAD);
        PRIMITIVE_LOAD_MAP.put("Z", Opcodes.ILOAD);
        PRIMITIVE_LOAD_MAP.put(OBJECT_PRIMITIVE, Opcodes.ALOAD);
    }
    
    private static final Map<String, Integer> PRIMITIVE_RETURN_MAP = new HashMap<>();
    static {
        PRIMITIVE_RETURN_MAP.put("B", Opcodes.IRETURN);
        PRIMITIVE_RETURN_MAP.put("C", Opcodes.IRETURN);
        PRIMITIVE_RETURN_MAP.put("D", Opcodes.DRETURN);
        PRIMITIVE_RETURN_MAP.put("F", Opcodes.FRETURN);
        PRIMITIVE_RETURN_MAP.put("I", Opcodes.IRETURN);
        PRIMITIVE_RETURN_MAP.put("J", Opcodes.LRETURN);
        PRIMITIVE_RETURN_MAP.put("S", Opcodes.IRETURN);
        PRIMITIVE_RETURN_MAP.put("Z", Opcodes.IRETURN);
        PRIMITIVE_RETURN_MAP.put(OBJECT_PRIMITIVE, Opcodes.ARETURN);
    }
    
    private final ClassNode cn = new ClassNode();
    private final MethodNode constructor = 
            new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);  
    private final LabelNode initReturn = new LabelNode();

    public ClassNode createClass(String name, String signature) {      
        cn.version = Opcodes.V1_7;
        cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;

        cn.name = name.replace('.', '/');
        cn.superName = "java/lang/Object";
        if (signature != null) {
            cn.signature = signature;
        }
        
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                "java/lang/Object", "<init>", "()V", false));
        constructor.instructions.add(initReturn);
        constructor.instructions.add(new InsnNode(Opcodes.RETURN));
        
        constructor.maxStack = 1;
        constructor.maxLocals = 1;
        
        cn.methods.add(constructor);
        
        return cn;
    }

    public void addAttribute(String attrName, String attrSignature, 
            List<String> genericParameters, 
            Set<Options> options) {
    	FieldNode fn = addField(cn, attrName, attrSignature);
        addGetter(cn, fn);
        addSetter(cn, fn, options);
    }

	private FieldNode addField(ClassNode cn, String attrName,
			String attrSignature) {
		FieldNode fn = 
				new FieldNode(Opcodes.ACC_PRIVATE, 
						attrName, 
						getErasure(attrSignature), 
						attrSignature.contains("<") || attrSignature.startsWith("T") ? attrSignature : null,
						null);
		cn.fields.add(fn);
		return fn;
	}
	
	private String getErasure(String signature) {
	    if (signature.startsWith("T")) {
	        return "Ljava/lang/Object;";
	    } else if (signature.contains("<")) {
	        return signature.substring(0, signature.indexOf('<')) + ";";
	    }
	    return signature;
	}

	private void addGetter(ClassNode cn, FieldNode fn) {
		MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, 
				makeAccessorName("Z".equals(fn.desc) ? "is" : "get", fn.name), 
                formatMethodSignature(GETTER_SIGNATURE, fn.desc),
                formatMethodSignature(GETTER_SIGNATURE, fn.signature),
				null);
		mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, fn.name, fn.desc));
		mn.instructions.add(getReturnInsnNode(fn.desc));
		
		mn.maxStack = getSlots(fn.desc);
		mn.maxLocals = getSlots(fn.desc);
		
		cn.methods.add(mn);
	}
	
	private int getSlots(String desc) {
	    return "J".equals(desc) || "D".equals(desc) ? 2 : 1;
	}
	
	private void addSetter(ClassNode cn, FieldNode fn, Set<Options> options) {
		MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, 
				makeAccessorName("set", fn.name),
                formatMethodSignature(SETTER_SIGNATURE, fn.desc),
                formatMethodSignature(SETTER_SIGNATURE, fn.signature),
				null);
		
		mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new VarInsnNode(getLoadOpcode(fn.desc), 1));
        
        final int xtraStack;
        
		if (options.contains(Options.NOT_NULL)) {
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            LabelNode label = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.IFNONNULL, label));
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, NPE));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, NPE, "<init>", "()V", false));
            mn.instructions.add(new InsnNode(Opcodes.ATHROW));
            mn.instructions.add(label);
            String pName = fn.desc.substring(1, fn.desc.length() - 1);
            mn.instructions.add(new FrameNode(Opcodes.F_FULL, 
                    2, new Object[] { cn.name, pName },
                    2, 
                    new Object[] { cn.name, pName }));
            
            xtraStack = 3;
		} else {
		    xtraStack = 1;
		}
		mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, fn.name, fn.desc));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
		
		mn.maxStack = getSlots(fn.desc) + xtraStack;
		mn.maxLocals = getSlots(fn.desc) + 1;
		
		cn.methods.add(mn);
	}
	
	private String formatMethodSignature(String format, String signature) {
	    if (signature == null) {
	        return null;
	    }
	    
	    return String.format(format, signature);
	}
	
	private int getLoadOpcode(String desc) {
	    return getTypedOpcode(PRIMITIVE_LOAD_MAP, desc);
	}

    private InsnNode getReturnInsnNode(String desc) {
        return new InsnNode(getTypedOpcode(PRIMITIVE_RETURN_MAP, desc));
    }

	private int getTypedOpcode(Map<String, Integer> primitiveOpcodeMap,
            String desc) {
	    if (primitiveOpcodeMap.containsKey(desc)) {
	        return primitiveOpcodeMap.get(desc);
	    }
        return getTypedOpcode(primitiveOpcodeMap, OBJECT_PRIMITIVE);
    }

    private String makeAccessorName(String prefix, String attrName) {
		return String.format("%s%S%s", prefix, 
				attrName.substring(0,1), 
				attrName.substring(1));
	}
}
