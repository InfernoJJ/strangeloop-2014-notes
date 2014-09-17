package com.weaselogic.getset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GetSetCompiler {

    public GetSetCompiler() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws IOException, CompileException {
        Path source = Paths.get(args[0]);
        Path dest = source.resolveSibling(source.getFileName().toString().replaceAll("(.+?)(\\.\\w+)?$", "$1.class"));
        
        Compiler cmp = new Compiler(source, new ClassBuilder());
        System.out.println(dest);
        Files.write(dest, cmp.compile(), StandardOpenOption.CREATE);
    }

}
