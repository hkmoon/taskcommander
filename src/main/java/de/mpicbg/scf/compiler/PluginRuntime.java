package de.mpicbg.scf.compiler;

import de.mpicbg.scf.compiler.CachedCompiler;
import de.mpicbg.scf.compiler.CompilerUtils;

import java.io.File;
import java.util.Map;

/**
 * PluginRuntime provides a Class instance of compiled java code
 *
 * @author HongKee Moon
 * @version 0.1beta
 * @since 9/3/13
 */
public class PluginRuntime {

	public boolean compile(String className, String code)
	{
		CachedCompiler cc = CompilerUtils.CACHED_COMPILER;

		return cc.compileCheckFromJava(className, code);
	}

	public Class instanciate(String className, String code) throws
			ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		CachedCompiler cc = CompilerUtils.CACHED_COMPILER;

		Class pluginClass = cc.loadFromJava(className, code);

		return pluginClass;
	}

	public void storeClassFiles(String folder, String className, String code)
	{
		CachedCompiler cc = CompilerUtils.CACHED_COMPILER;

		Map<String, byte[]> bytesMap = cc.compileFromJava( className, code );

		for(Map.Entry<String, byte[]> entry : bytesMap.entrySet())
		{
			String className2 = entry.getKey();
			byte[] bytes = entry.getValue();
			String filename = className2.replaceAll("\\.", '\\' + File.separator) + ".class";
			String file = folder + "/" + filename;
			IOUtils.writeBytes(new File(file), bytes);
		}
	}
}
