package com.weaselogic.getset;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.weaselogic.getset.Compiler.Options;

public class ClassBuilder {

    private final ClassNode cn = new ClassNode();
    private final MethodNode constructor = 
            new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);  

    public ClassNode createClass(String name, String signature) {      
        cn.version = Opcodes.V1_7;
        cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;

        cn.name = name.replace('.', '/');
        cn.superName = "java/lang/Object";
        
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        constructor.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                "java/lang/Object", "<init>", "()V", false));
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
				makeAccessorName("get", fn.name), 
				"()" + fn.desc, null, null);
		
		mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, fn.name, fn.desc));
		// TODO: handle non-int
		mn.instructions.add(new InsnNode(Opcodes.IRETURN));
		
		mn.maxStack = 1;
		mn.maxLocals = 1;
		
		cn.methods.add(mn);
	}
	
	private void addSetter(FieldNode fn) {
		MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, 
				makeAccessorName("set", fn.name), 
				String.format("(%s)V", fn.desc), null, null);
		
		// TODO: handle non-int
		mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
		mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, fn.name, fn.desc));
		mn.instructions.add(new InsnNode(Opcodes.RETURN));
		
		mn.maxStack = 2;
		mn.maxLocals = 2;
		
		cn.methods.add(mn);
	}


	private String makeAccessorName(String prefix, String attrName) {
		return String.format("%s%S%s", prefix, 
				attrName.substring(0,1), 
				attrName.substring(1));
	}
}
