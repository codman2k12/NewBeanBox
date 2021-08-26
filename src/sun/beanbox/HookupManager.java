package sun.beanbox;

import java.util.*;
import java.beans.*;
import java.lang.reflect.*;

//import com.paragraph.gerasimov.beanbox.*;

class HookupHandler implements InvocationHandler {
   private static final Object[] EMPTY_OBJECTS = new Object[0];

   private final Object target;
   private final Method listenerMethod;
   private final Method targetMethod;

   HookupHandler(Object target, Method listenerMethod, Method targetMethod) {
      this.target = target;
      this.listenerMethod = listenerMethod;
      this.targetMethod = targetMethod;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Object result = null;
      if (method.equals(listenerMethod)){
            switch (targetMethod.getParameterTypes().length){
                case 0:
                    result = targetMethod.invoke(target, EMPTY_OBJECTS);
                    break;
                case 1:
                    result = targetMethod.invoke(target, args);
                    break;
                default:
                    System.err.println("it should not be happened...");
                    break;
            }
      }
      return result;
   }
}

public class HookupManager {
    static void hookup(EventSetDescriptor esd,
		Method listenerMethod,
		Wrapper sourceWrapper,
		Wrapper targetWrapper,
		Method targetMethod) 
	{
		Object target = targetWrapper.getBean();
		HookupHandler hookupHandler = new HookupHandler(target, listenerMethod, targetMethod);
		Class[] interfaces = new Class[2];
		interfaces[0] = esd.getListenerType();
		interfaces[1] = java.io.Serializable.class;
		Object proxy = Proxy.newProxyInstance(null, interfaces, hookupHandler);
		sourceWrapper.addEventTarget(esd.getName(), targetWrapper, proxy);
    }
    static String[] getHookupFiles() {
		String back[] = new String[javaFiles.size()];
		Enumeration e = javaFiles.elements();
		for (int i=0; e.hasMoreElements(); i++) {
			back[i] = (String) e.nextElement();
		}
		return back;
    }

    static String getId() {
		java.util.Date now = new java.util.Date();
		long id = now.getTime()/10;
		return Long.toHexString(id);
    }

    static String shortPackageName = "sunw.beanbox";
    static String shortTmpDir = BeanBoxFrame.getTmpDir();
    static String packageName;
    static String tmpDir;
    static Vector javaFiles = new Vector();

    static {
		packageName = shortTmpDir.replace(java.io.File.separatorChar, '.') + "." + shortPackageName;
		tmpDir = packageName.replace('.', java.io.File.separatorChar);
    }
}

