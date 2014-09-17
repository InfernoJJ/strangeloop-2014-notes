package com.weaselogic.getset;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
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
        // TODO Auto-generated method stub
        
    }
}
