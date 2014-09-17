package com.weaselogic.getset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.MultiANewArrayInsnNode;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.weaselogic.getset.Compiler.Options;

public class ClassBuilder {
    
    
    private final ClassNode cn = new ClassNode();

    public ClassNode createClass(String name, String signature) {      
        
        return cn;
    }

    public void addAttribute(String attrName, String attrSignature, 
            List<String> genericParameters, 
            Set<Options> options) {
    }
}
