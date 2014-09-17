package com.weaselogic.getset;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import com.weaselogic.getset.Compiler.Options;

public class ClassBuilder {

    private final ClassNode cn = new ClassNode();

    public ClassNode createClass(String name, String signature) {      
        cn.version = Opcodes.V1_7;
        cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;

        cn.name = name.replace('.', '/');
        cn.superName = "java/lang/Object";
        
        return cn;
    }

    
    public void addAttribute(String attrName, String attrSignature,
            List<String> genericParameters, 
            Set<Options> options) {
    }
}
