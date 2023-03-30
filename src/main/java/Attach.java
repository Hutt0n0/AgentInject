
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

public class Attach {
    public static String CLASS_NAME = "org.apache.catalina.startup.Bootstrap";
    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String property = System.getProperty("user.dir");
        String agentPath = property+"/AgentInject-1.0-SNAPSHOT-jar-with-dependencies.jar";
        String toolspath = System.getProperty("java.home").replace("jre","lib") + java.io.File.separator + "tools.jar";
        System.out.println(toolspath);
        File file = new File(toolspath);
        java.net.URL url = file.toURI().toURL();
        java.net.URLClassLoader urlClassLoader = new java.net.URLClassLoader(new java.net.URL[]{url});
        Class<?> VirtualMachine = urlClassLoader.loadClass("com.sun.tools.attach.VirtualMachine");
        Class<?> VirtualMachineDescriptor = urlClassLoader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
        java.lang.reflect.Method listMethod = VirtualMachine.getMethod("list");
        java.util.List<Object> list = (java.util.List<Object>)listMethod.invoke(VirtualMachine);
        System.out.println(agentPath);
        for ( int i = 0 ; i < list.size() ; i++ ){
            Object o = list.get(i);
            java.lang.reflect.Method displayNameMethod = VirtualMachineDescriptor.getMethod("displayName");
            String display = (String) displayNameMethod.invoke(o);
            System.out.println(display);

            if (display.contains(CLASS_NAME)){
                java.lang.reflect.Method getId = VirtualMachineDescriptor.getDeclaredMethod("id");
                String jvmid = (String) getId.invoke(o);
                System.out.println(jvmid);
                Method attach = VirtualMachine.getDeclaredMethod("attach", new Class[]{java.lang.String.class});
                Object vm =  attach.invoke(o, new Object[]{jvmid});
                java.lang.reflect.Method loadAgent = VirtualMachine.getDeclaredMethod("loadAgent", new Class[]{java.lang.String.class});
                loadAgent.invoke(vm, new Object[]{agentPath});
                java.lang.reflect.Method detach = VirtualMachine.getDeclaredMethod("detach");
                detach.invoke(vm);
                System.out.println("Agent.jar Inject success!!");
                break;
            }
        }


    }
}