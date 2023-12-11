package mods.thecomputerizer.scriptify.io.read;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.data.ByteArrayClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import stanhebben.zenscript.compiler.*;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionCallVirtual;
import stanhebben.zenscript.util.MethodOutput;
import stanhebben.zenscript.util.ZenPosition;

import java.util.List;
import java.util.Objects;

import static stanhebben.zenscript.util.ZenTypeUtil.internal;

public class ExpressionCallReader extends FileReader {

    private final IEnvironmentGlobal environment;
    private final Expression call;
    private IEnvironmentClass cachedClassEnv;
    private ClassWriter cachedClassWriter;
    private MethodOutput cachedOutput;
    private IEnvironmentMethod cachedMethodEnv;
    private String cachedClassName;

    public ExpressionCallReader(IEnvironmentGlobal environment, Expression expression) throws IllegalArgumentException {
        this.environment = environment;
        if(expression instanceof ExpressionCallStatic || expression instanceof ExpressionCallVirtual)
            this.call = expression;
        else throw new IllegalArgumentException("Expression is not a call type "+expression.getClass().getName());
    }

    private void clear() {
        this.cachedClassEnv = null;
        this.cachedClassWriter = null;
        this.cachedMethodEnv = null;
        this.cachedOutput = null;
    }

    @Override
    public void copy(List<String> lines) {

    }

    private void getClassEnvironment(String name) {
        if(Objects.isNull(this.cachedClassEnv)) {
            name = name.replace('.', '/');
            getWriter(name);
            EnvironmentClass classEnv = new EnvironmentClass(this.cachedClassWriter,this.environment);
            this.cachedClassWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,name,
                    null, internal(Object.class),new String[]{internal(Runnable.class)});
            this.cachedClassName = name;
            this.cachedClassEnv = classEnv;
        }
    }

    private void getOutput() {
        if(Objects.isNull(this.cachedOutput)) {
            this.cachedOutput = new MethodOutput(this.cachedClassWriter,Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "compileCTExpression","()V",null,null);
        }
    }

    private void getWriter(String name) {
        if(Objects.isNull(this.cachedClassWriter)) {
            ClassWriter clsScript = new ZenClassWriter(ClassWriter.COMPUTE_FRAMES);
            clsScript.visitSource(name,null);
            this.cachedClassWriter = clsScript;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void getMethodEnvironment(String name) {
        if(Objects.isNull(this.cachedMethodEnv)) {
            getClassEnvironment(name);
            getOutput();
            this.cachedMethodEnv = new EnvironmentMethod(this.cachedOutput,this.cachedClassEnv);
        }
    }

    /**
     * My first foray into ASM I guess
     */
    public Class<?> setPositionAndCompile(ZenPosition position) {
        getMethodEnvironment("test.scriptify.lol");
        this.cachedOutput.start();
        this.cachedOutput.position(position);
        this.call.compile(true,this.cachedMethodEnv);
        this.cachedOutput.ret();
        this.cachedOutput.end();
        this.cachedClassWriter.visitEnd();
        byte[] classBytes = this.cachedClassWriter.toByteArray();
        clear();
        return tryLoadingClass(classBytes);
    }

    private Class<?> tryLoadingClass(byte[] classBytes) {
        ScriptifyRef.LOGGER.error("NAME IS {} AND BYTES ARE {}",this.cachedClassName,classBytes);
        ByteArrayClassLoader.addClassByte(this.cachedClassName,classBytes);
        return new ByteArrayClassLoader().findClass(this.cachedClassName);
    }
}
