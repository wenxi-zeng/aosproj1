package util;

/**
 * Created by Yongtao on 9/17/2015.
 *
 * MISC utils.
 */
public class Utils{
	public static final long kilo=0x400;
	public static final long mega=0x100000;
	public static final long giga=0x40000000;

	/**
	 * If you need to parse ini setting and setting file contains number with unit, this can help.
	 *
	 * @param str Your size in string
	 * @return Size in number.
	 */
	public static long parseLong(String str){
		long num;
		try{
			num=Long.parseLong(str);
		}catch(NumberFormatException e){
			num=Long.parseLong(str.substring(0,str.length()-1));
			char u=Character.toLowerCase(str.charAt(str.length()-1));
			switch(u){
				case 'k':
					num*=kilo;
					break;
				case 'm':
					num*=mega;
					break;
				case 'g':
					num*=giga;
					break;
				default:
					throw new NumberFormatException("str cannot be parsed, str: "+str);
			}
		}
		return num;
	}

	private static String[] unknown={"Unknown class","Unknown method"};

	/**
	 * http://stackoverflow.com/a/11306854
	 * Get caller's class and method name.
	 *
	 * @param className Callee's class name
	 * @return String[0]: class name, String[1]: method name
	 */
	public static String[] getCaller(String className){
		StackTraceElement[] stElements=Thread.currentThread().getStackTrace();
		for(int i=2;i<stElements.length;i++){
			StackTraceElement ste=stElements[i];
			if(!ste.getClassName().equals(className) && ste.getClassName().indexOf("java.lang.Thread")!=0){
				String[] result=new String[2];
				result[0]=ste.getClassName();
				result[1]=ste.getMethodName();
				return result;
			}
		}
		return unknown;
	}
}