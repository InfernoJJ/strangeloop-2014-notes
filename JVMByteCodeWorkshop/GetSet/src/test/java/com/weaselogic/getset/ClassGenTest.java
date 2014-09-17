package com.weaselogic.getset;

import static org.junit.Assert.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

@RunWith(MockitoJUnitRunner.class)
public class ClassGenTest {

    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void Excercise0_testCreateClass() {
        ClassBuilder cb = new ClassBuilder();
                
        ClassNode cn = cb.createClass("Test", null);
        assertEquals(Opcodes.V1_7, cn.version);
    }

    @Test
    public void Exercise1_testNothing() throws Exception {
        Class<?> c = compileAndLoad("pkg1", "Nothing");
        standardClassChecks(c, 0);
    }
    
	@Test
	public void Exercise2_testIntHolder() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "IntHolder");
        Object o = standardClassChecks(c, 2);
        
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle get;
        MethodHandle set;
        
        get = lookup.findVirtual(c, "getValue", MethodType.methodType(int.class));
        set = lookup.findVirtual(c, "setValue", MethodType.methodType(void.class, int.class));
        assertEquals(0, get.invoke(o));
        set.invoke(o, 123);
        assertEquals(123, get.invoke(o));
        
	}

    @Test
    public void Exercise3_testPrimitiveHolder() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "PrimitiveHolder");
        Object o = standardClassChecks(c, 16);
        
        exerciseMethod(c, o, "getValue", "setValue", int.class, 0, 123);
        exerciseMethod(c, o, "isValue", "setValue", boolean.class, false, true);
        exerciseMethod(c, o, "getValue", "setValue", byte.class, (byte)0, (byte)-123);
        exerciseMethod(c, o, "getValue", "setValue", short.class, (short)0, (short)-1230);
        exerciseMethod(c, o, "getValue", "setValue", char.class, (char)0, 'X');
        exerciseMethod(c, o, "getValue", "setValue", long.class, 0L, Long.MAX_VALUE);      
        exerciseMethod(c, o, "getValue", "setValue", float.class, 0.0f, Float.MAX_VALUE);      
        exerciseMethod(c, o, "getValue", "setValue", double.class, 0.0d, Double.MAX_VALUE);      
    }
    
    @Test
    public void Exercise4_testObjectHolder() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "ObjectHolder");
        Object o = standardClassChecks(c, 4);
        
        exerciseMethod(c, o, "getDate", "setDate", Date.class, null, new Date());
        exerciseMethod(c, o, "getString", "setString", String.class, null, "Hello!");
    }
    
    @Test
    public void Exercise5_testGenericObjectHolder() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "GenericObjectHolder");
        Object o = standardClassChecks(c, 6);
        
        exerciseMethod(c, o, "getString", "setString", String.class, null, "Goodbye!");
        exerciseMethod(c, o, "getList", "setList", List.class, null, Arrays.asList(1, 2, 3));
        exerciseMethod(c, o, "getMap", "setMap", Map.class, null, new HashMap<>());
        
        Method m = c.getMethod("getList");
        assertEquals("java.util.List<java.lang.Integer>", m.getGenericReturnType().toString());
        
        m = c.getMethod("setList", List.class);
        assertEquals(1, m.getGenericParameterTypes().length);
        assertEquals("java.util.List<java.lang.Integer>", m.getGenericParameterTypes()[0].toString());
        
    }
    
    @Test
    public void Exercise6_testBasicGenericHolder() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "BasicGenericHolder");
        Object o = standardClassChecks(c, 4);
        
        exerciseMethod(c, o, "getThing", "setThing", Object.class, null, new Object());
        exerciseMethod(c, o, "getList", "setList", List.class, null, Arrays.asList(1, "test", new Object()));
        
        Method m = c.getMethod("getList");
        assertEquals("java.util.List<L>", m.getGenericReturnType().toString());
        
        m = c.getMethod("setList", List.class);
        assertEquals(1, m.getGenericParameterTypes().length);
        assertEquals("java.util.List<L>", m.getGenericParameterTypes()[0].toString());
        
        m = c.getMethod("getThing");
        assertEquals("T", m.getGenericReturnType().toString());
        
        m = c.getMethod("setThing", Object.class);
        assertEquals(1, m.getGenericParameterTypes().length);
        assertEquals("T", m.getGenericParameterTypes()[0].toString());
        
    }
    
    @Test
    public void Exercise7_testNotNull() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "NotNull");
        Object o = standardClassChecks(c, 2);
        
        exerciseMethod(c, o, "getString", "setString", String.class, null, "NotNull!");
        
        MethodHandle set = MethodHandles.lookup().findVirtual(c, "setString", MethodType.methodType(void.class, String.class));
        
        try { 
            set.invoke(o, null);
            fail("Expected NPE");
        } catch(NullPointerException x) {
            // success
        }

    }
    
    @Test
    public void Exercise8_testNeverNull() throws Throwable {
        Class<?> c = compileAndLoad("pkg1", "NeverNull");
        Object o = standardClassChecks(c, 2);
        
        exerciseMethod(c, o, "getString", "setString", String.class, "", "NeverNull!");
        
        MethodHandle set = MethodHandles.lookup().findVirtual(c, "setString", MethodType.methodType(void.class, String.class));
        
        try { 
            set.invoke(o, null);
            fail("Expected NPE");
        } catch(NullPointerException x) {
            // success
        }

    }
    
    @Test
    public void Exercise9_testNeverNullArray() throws Throwable {
    	String[][][] init = new String[0][0][0];
    	String[][][] testVal = new String[1][2][3];
    	
    	int[][] intInit = new int[0][0];
    	int[][] intTestVal = new int[3][4];
    
    	testVal[0][0] = new String[] { "foo", "bar", "baz" };
    	intTestVal[0] = new int[] { 1, 3, 3, 7 };
    	
    	
        Class<?> c = compileAndLoad("pkg1", "NeverNullArray");
        Object o = standardClassChecks(c, 4);
        
        exerciseMethod(c, o, "getStrings", "setStrings", init.getClass(), init, testVal);
        exerciseMethod(c, o, "getInts", "setInts", intInit.getClass(), intInit, intTestVal);
        
        MethodHandle set = MethodHandles.lookup().findVirtual(c, "setStrings", 
        		MethodType.methodType(void.class, init.getClass()));
        
        try { 
            set.invoke(o, null);
            fail("Expected NPE");
        } catch(NullPointerException x) {
            // success
        }
        
        set = MethodHandles.lookup().findVirtual(c, "setInts", 
        		MethodType.methodType(void.class, intInit.getClass()));
        
        try { 
            set.invoke(o, null);
            fail("Expected NPE");
        } catch(NullPointerException x) {
            // success
        }

    }
    
    private Class<?> compileAndLoad(String pkg, String cName) throws Exception {
        Path p = Paths.get("src", "test", "resources", pkg, cName + ".gs");
        
        Compiler cmp = new Compiler(p, new ClassBuilder());
        
        return loadBinaryClass(pkg + '.' + cName, cmp.compile());
    }
    

    
    private void exerciseMethod(Class<?> c, Object o, String getMethod, String setMethod,
            Class<?> attrType, Object initialValue, Object value) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle get;
        MethodHandle set;

        get = lookup.findVirtual(c, getMethod, MethodType.methodType(attrType));
        set = lookup.findVirtual(c, setMethod, MethodType.methodType(void.class, attrType));
        if (initialValue != null && initialValue.getClass().isArray()) {
        	assertArrayEquals((Object[])initialValue, (Object[])get.invoke(o));
        } else {
        	assertEquals(initialValue, get.invoke(o));
        }
        set.invoke(o, value);
        assertEquals(value, get.invoke(o));
    }

    private Class<?> loadBinaryClass(String className, byte[] code) throws Exception {
        ThingyClassLoader tcl = new ThingyClassLoader(className, code);
        Thread.currentThread().setContextClassLoader(tcl);
        return tcl.loadClass(className);
    }
    
    private Object standardClassChecks(Class<?> c, int numMethods) throws Exception {
        Object o = c.newInstance();
        assertEquals(numMethods, c.getDeclaredMethods().length);
        assertEquals(1, c.getDeclaredConstructors().length);
    	assertEquals(1, c.getModifiers());
    	
    	return o;
    }

    public static class ThingyClassLoader extends ClassLoader {
        private final String name;
        private final byte[] code;
        
        public ThingyClassLoader(String name, byte[] code) {
            super();
            this.name = name;
            this.code = code;
        }

        @Override
        public Class<?> loadClass(String cname) throws ClassNotFoundException {
            if (cname.equals(name)) {
                return defineClass(cname, code, 0, code.length);
            }
            return super.loadClass(cname);
        }
        
    }
}
