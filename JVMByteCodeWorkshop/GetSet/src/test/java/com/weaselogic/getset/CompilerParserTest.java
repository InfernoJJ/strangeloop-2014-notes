package com.weaselogic.getset;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

@RunWith(MockitoJUnitRunner.class)
public class CompilerParserTest {
	@Captor
	ArgumentCaptor<String> signatureCaptor;
	
	@Captor
	ArgumentCaptor<String> nameCaptor;
	
    @Captor
    ArgumentCaptor<String> attrNameCaptor;
    
    @Captor
    ArgumentCaptor<String> attrSigCaptor;
	
	@Mock
	ClassBuilder cb;

	@Before
	public void setUp() throws Exception {
		when(cb.createClass(any(String.class), any(String.class))).thenReturn(makeDummyClassNode());
	}

    @Test
    public void testNothing() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "Nothing.gs");

        Compiler cmp = new Compiler(p, cb);

        cmp.compile();

        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals(null, signatureCaptor.getValue());

        assertEquals("pkg1.Nothing", nameCaptor.getValue());

        verifyNoMoreInteractions(cb);
    }


    @Test
    public void testBasicGeneric() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "BasicGenericHolder.gs");

        Compiler cmp = new Compiler(p, cb);

        cmp.compile();

        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals("<L:Ljava/lang/Object;T:Ljava/lang/Object;>Ljava/lang/Object;", signatureCaptor.getValue());

        assertEquals("pkg1.BasicGenericHolder", nameCaptor.getValue());

        verify(cb, times(2)).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(), 
                any(List.class), any(Set.class));

        assertEquals("thing", attrNameCaptor.getAllValues().get(0));
        assertEquals("TT;", attrSigCaptor.getAllValues().get(0));
        assertEquals("list", attrNameCaptor.getAllValues().get(1));
        assertEquals("Ljava/util/List<TL;>;", attrSigCaptor.getAllValues().get(1));
    }


	@Test
	public void testIntHolder() throws Exception {
		Path p = Paths.get("src", "test", "resources", "pkg1", "IntHolder.gs");
		
		Compiler cmp = new Compiler(p, cb);
		
		cmp.compile();
		
		verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
		
		assertEquals("pkg1.IntHolder", nameCaptor.getValue());
		
	    assertEquals(null, signatureCaptor.getValue());

		verify(cb).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(),
                any(List.class), any(Set.class));

		assertEquals("value", attrNameCaptor.getValue());
		assertEquals("I", attrSigCaptor.getValue());
	}

    @Test
    public void testIntArrayHolder() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "IntArrayHolder.gs");
        
        Compiler cmp = new Compiler(p, cb);
        
        cmp.compile();
        
        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals("pkg1.IntArrayHolder", nameCaptor.getValue());
        
	    assertEquals(null, signatureCaptor.getValue());

        verify(cb).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(),
                any(List.class), any(Set.class));

        assertEquals("valueArray", attrNameCaptor.getValue());
        assertEquals("[[[I", attrSigCaptor.getValue());

    }
    
    @Test
    public void testDateHolder() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "DateHolder.gs");
        
        Compiler cmp = new Compiler(p, cb);
        
        cmp.compile();
        
        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals("pkg1.DateHolder", nameCaptor.getValue());
        
	    assertEquals(null, signatureCaptor.getValue());

        verify(cb).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(),
                any(List.class), any(Set.class));

        assertEquals("date", attrNameCaptor.getValue());
        assertEquals("Ljava/util/Date;", attrSigCaptor.getValue());

    }

    @Test
    public void testImportedDateHolder() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "ImportedDateHolder.gs");
        
        Compiler cmp = new Compiler(p, cb);
        
        cmp.compile();
        
        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals("pkg1.ImportedDateHolder", nameCaptor.getValue());
        
	    assertEquals(null, signatureCaptor.getValue());

        verify(cb).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(),
                any(List.class), any(Set.class));

        assertEquals("date", attrNameCaptor.getValue());
        assertEquals("Ljava/util/Date;", attrSigCaptor.getValue());

    }

    @Test
    public void testStringToIntegerHolder() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "StringToIntegerHolder.gs");
        
        Compiler cmp = new Compiler(p, cb);
        
        cmp.compile();
        
        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals("pkg1.StringToIntegerHolder", nameCaptor.getValue());
        
	    assertEquals(null, signatureCaptor.getValue());

        verify(cb).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(),
                any(List.class), any(Set.class));

        assertEquals("map", attrNameCaptor.getValue());
        assertEquals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;", attrSigCaptor.getValue());

    }
    
    @Test
    public void testNeverNullArray() throws Exception {
        Path p = Paths.get("src", "test", "resources", "pkg1", "NeverNullArray.gs");
        
        Compiler cmp = new Compiler(p, cb);
        
        cmp.compile();
        
        verify(cb).createClass(nameCaptor.capture(), signatureCaptor.capture());
        
        assertEquals("pkg1.NeverNullArray", nameCaptor.getValue());
        
	    assertEquals(null, signatureCaptor.getValue());

        verify(cb).addAttribute(attrNameCaptor.capture(), attrSigCaptor.capture(),
                any(List.class), any(Set.class));

        assertEquals("strings", attrNameCaptor.getValue());
        assertEquals("[[[Ljava/lang/String;", attrSigCaptor.getValue());

    }
    
    private ClassNode makeDummyClassNode() {
        ClassNode cn = new ClassNode();
        
        cn.version = Opcodes.V1_8;
        cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;

        cn.name = "test.Test";
        cn.superName = "java/lang/Object";
        return cn;
    }
 }
