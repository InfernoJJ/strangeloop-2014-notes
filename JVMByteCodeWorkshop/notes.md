- JVM Basics
	- Everything is attached to a class. Even the floating lambas in new JDK's.
	- Instrictuions cannot see outside the operand stack.
	- Pure stack machine.
	- Variables feed into and out of method instructions
	- Method instructions feed into and out of operand stacks.
	- Operand stack feeds into the method instructions with an instruction pointer.
	- Cannot buffer overflow the jvm stack. Its limited in slots for variables, methods, and operands.
	- Example (doing crazy stuff)
		- void method(long x, int y){
			{ float f= y; }
	 		{ boolean z = x > 0; }
		}
		- Varialbes
			- 0: Object(this)
			- 1, 2: long(x) <- longs and doubles take 2 slots.
			- 3: int (y)
			- 4: Loca(f,z)
		- Bytecode
			//float f =y
			iload 3
			i2f
			fstore 4
		- ByteCode verifiyer, and ClassLoader are togehter.
			- It will not load the example because it will not allow the verification.
	- JVM likes integers
		- i2b truncate int to 8bit signed
		- i2c truncate int to 16bit unsigned
		- i2s trunc int to 16bit signed
	- The bytecode may be the same but the functions can be intepreted different because of the Type Descriptors.
		- boolean foo( byte ,b char c, int i) -> (BCI)Z
		- int foo(int b, int c, int i) -> (III)I
	- Descriptors and Signatures
		- List<String> <- type
		- Ljava/util/List; <- descriptor (compile doesnt care) erasure type
		- Ljava/util/List<Ljava/lang/String;>; <- signature (jvm does)
		- MapMap<K,V> extends AbstractMap
		- <K:Ljava/lang/Object;V:Ljava/lang/object;>Ljava/util/AbstractMap
		- Interfaces have 2 colons. (as part of above) X::Ljava.io.Serializable;

	- Requires packing up and running it through the ClassLoader.
	- Attribute section part of the clasfile structure basically allows them to add all of these features they keep adding without changing the original file format for class files.
	- Binary qualified Names 
		- fo.var.baz -> foo/bar/baz (for history reasons)
		- Forbidden . ; [ / < >
		- Except for <init> <clinit>
	- you can specify a method for something that doesnt exist in the code but in the tooling running the bytecode.
	- StackMapTable (JVM Critical)
		- The StackMapTable describes the data types live on the local variable and operand stack at keey bytecode offets. (branch targets)
	- ASM codes
		- invokevirtual (most common)
		- invokenonvirtual #4 (mostly used for calling a super target.)
		- dup (most used code because most instructions consume the top of the stack, dup duplicate the top of the stack to create 2 entries on the top. So if you dont want to consume the top item for an if check etc... you dup it before you call the if.
			- new <attribute object type>
			- dup
			- invokespecial <init>