package com.weaselogic.getset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.weaselogic.getset.Compiler.Options;

public class ClassBuilder {
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
    	FieldNode fn = addField(attrName, attrSignature);
        addGetter(fn);
        addSetter(fn);
    }

	private FieldNode addField(String attrName,
			String attrSignature) {
		FieldNode fn = 
				new FieldNode(Opcodes.ACC_PRIVATE, 
						attrName, 
						attrSignature, 
						null, null);
		cn.fields.add(fn);
		return fn;
	}

	private void addGetter(FieldNode fn) {
		MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, 
				makeAccessorName("Z".equals(fn.desc) ? "is" : "get", fn.name), 
				"()" + fn.desc, null, null);
		
		mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, fn.name, fn.desc));
		mn.instructions.add(new InsnNode(getReturnOpcode(fn.desc)));
		
		mn.maxStack = getSlots(fn.desc);
		mn.maxLocals = getSlots(fn.desc);
		
		cn.methods.add(mn);
	}
	
	private int getSlots(String desc) {
	    return "J".equals(desc) || "D".equals(desc) ? 2 : 1;
	}
	
	private void addSetter(FieldNode fn) {
		MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, 
				makeAccessorName("set", fn.name), 
				String.format("(%s)V", fn.desc), null, null);
		
		mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		mn.instructions.add(new VarInsnNode(getLoadOpcode(fn.desc), 1));
		mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, fn.name, fn.desc));
		mn.instructions.add(new InsnNode(Opcodes.RETURN));
		
		mn.maxStack = getSlots(fn.desc) + 1;
		mn.maxLocals = getSlots(fn.desc) + 1;
		
		cn.methods.add(mn);
	}
	
	private int getLoadOpcode(String desc) {
	    return getTypedOpcode(PRIMITIVE_LOAD_MAP, desc);
	}

    private int getReturnOpcode(String desc) {
        return getTypedOpcode(PRIMITIVE_RETURN_MAP, desc);
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
