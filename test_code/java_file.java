package com.de.base.tools;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.de.base.framework.cache.service.CacheService;
import com.de.base.framework.constant.GlobalConstants;
import com.de.base.framework.exception.DigiteAppException;
import com.de.base.preference.constant.PreferenceConstants;
import com.de.base.preference.service.PreferenceService;
import com.de.base.util.app.MessageLogger;
import com.de.base.util.app.SystemProperties;
import com.de.base.util.app.ZipHelper;
import com.de.base.util.connection.dataobject.DBTypes;
import com.de.base.util.connection.dataobject.QueryData;
import com.de.base.util.constant.CacheConstants;
import com.de.base.util.general.DateUtil;
import com.de.base.util.general.FileUtil;
import com.de.base.util.general.SQLUtil;
import com.de.base.util.general.StringUtil;
import com.de.base.util.general.WebUtil;
import com.de.base.websecurity.webhelper.WebSecHelper;
import com.de.enterprise.eform.user.dataobject.UserData;
import com.de.webservice.general.WSUtiltities;

public class AdminUtils 
{
	private static MessageLogger LOGGER = new MessageLogger(AdminUtils.class.getName());

	/**
	 * Retrieves the system properties of local server.
	 * @return
	 * @throws Exception
	 */
	
	public static JSONObject getSysPropData() throws Exception 
	{
	  HashSet<String> HIDEPROPS = new HashSet<String>();
	  HIDEPROPS.add("SMTP_PASSWORD");
	  SystemProperties w_Sys =new SystemProperties();
	  Class w_Cls = w_Sys.getClass();
	  Field[] w_Fields = w_Cls.getDeclaredFields();
	  HashMap w_FieldMap = new HashMap();
	  JSONObject w_jsonObj = new JSONObject();
	  for(int i=0; i < w_Fields.length; ++i)
	  {
	  	w_Fields[i].setAccessible(true);
	  	String w_propsName = w_Fields[i].getName();
	  	Object w_propsVal = w_Fields[i].get(w_Sys);
	   	w_FieldMap.put(w_propsName, w_propsVal);
	   	if(SystemProperties.isWildflyApp() && (w_propsName != null && !w_propsName.startsWith("m_")))
	   	{
	   		w_jsonObj.put(w_propsName, w_propsVal);
	   	}
	  }
	  Field w_Field = w_Cls.getDeclaredField("m_Props");
	  if(w_Field != null)
	  {
	   	w_Field.setAccessible(true);
		Properties w_Map = (Properties)w_Field.get(w_Sys);
		for (Enumeration e = w_Map.propertyNames(); e.hasMoreElements() ;)
		{
			String w_PropName  = (String)e.nextElement();
			Object w_Val = null;
			if(w_FieldMap.get(w_PropName) != null)
				w_Val = w_FieldMap.get(w_PropName);
			else
				w_Val = w_Map.getProperty(w_PropName);
			w_jsonObj.put(w_PropName, w_Val);
		 }
	  }
	  return w_jsonObj;
	}
	/**
	 * Reads the cluster nodes property and create JSON string for it.
	 * @return
	 */
	public static String getclusterServerIps(String a_key) 
	{
		JSONArray w_nameAddrMapStore = new JSONArray();
		String w_System = SystemProperties.getProperty("CLUSTER_NODES");
		if (w_System == null)
			return "{}";
		String[] w_serverIps = w_System.split(",");

		for (int i = 0; i < w_serverIps.length; i++) 
		{
			JSONObject w_nameAddrMap = new JSONObject();
			String w_serverName = ("server" + (i + 1));
			String w_serverIp = w_serverIps[i] + "/Request?Key="+a_key;
			w_nameAddrMap.put("serverName", w_serverName);
			w_nameAddrMap.put("serverIp", w_serverIp);
			w_nameAddrMapStore.put(w_nameAddrMap);
		}
		return w_nameAddrMapStore.toString();
	}
	
	/**
     * 
     * @param a_serverNameIp:json format with keys as server name and server ip
     * @param a_PName:parameter to change
     * @param a_PValue:new value of the parameter.
     * @param a_server:which server is making the request.
     * @param a_token:token used for authentication.
     * @return
     * @throws Exception
     */
    public static JSONObject getRemoteSysProp(JSONObject a_serverNameIp, String a_PName, String a_PValue,
			String a_server, String a_token, String a_isCFM)
	{
		StringBuilder w_applogPage = new StringBuilder();
		JSONObject w_json = null;
		HttpURLConnection w_urlCon = null;
		Scanner w_scanner = null;
		try 
		{
			StringBuilder w_serverUrl = new StringBuilder();
			w_serverUrl.append(a_serverNameIp.getString("serverIp"));
			w_serverUrl.append("&").append(GlobalConstants.TOKEN).append("=").append(a_token);
			if (a_PName != null)
			{		
				w_serverUrl.append("&PName=").append(a_PName);
			}
			if (a_server != null)
			{		
				w_serverUrl.append("&server=").append(a_server);
			}
			if (a_PValue != null)
			{		
				w_serverUrl.append("&PValue=").append(a_PValue);
			}	
			if (a_isCFM != null)
			{
				w_serverUrl.append("&isCFM=").append(a_isCFM);
			}
			URL w_url = new URL(w_serverUrl.toString());
			w_urlCon = (HttpURLConnection) w_url.openConnection();
			w_scanner = new Scanner(w_urlCon.getInputStream());
			while (w_scanner.hasNext()) 
			{
				w_applogPage.append(w_scanner.nextLine());
				w_applogPage.append("\n");
			}
		 	w_json = new JSONObject(w_applogPage.toString().trim());
		} 
		catch (Exception a_e)
		{
			a_e.printStackTrace();
		}
		finally
		{
			if (w_scanner != null)
			{
				w_scanner.close();
			}
			if (w_urlCon != null)
			{
				w_urlCon.disconnect();
			}
		}
		return w_json;
	}
    /**
	 * Checks the validity of passed token
	 * @param a_token:toke used for validating authenticity
	 * @return
	 * @throws Exception
	 */
	public static  boolean isClusterAutheticated(String a_token)throws Exception
	{
		if (a_token == null || !WebUtil.isClusterMode())
		{	
			return false;
		}
		boolean w_auth = false;
		int w_isValidToken = WSUtiltities.checkValidToken(a_token);
		
		if (w_isValidToken == -1)
		{
			w_auth = true;
		}
		return w_auth;
	}
	/**
	 * 
	 * @param LoginId:login id for whom the token needs to be generated.
	 * @param userId:database userid of the user.
	 * @return: access token
	 * @throws Exception
	 */
	public static String generateNewToken(String a_loginId, int a_userId) throws Exception
	{
		String w_token = String.valueOf(getSecureRandomInt());
		
		while (CacheService.get(CacheConstants.CACHEKEY.WEBSERVICE_CKEY_WS_TOKEN, w_token) != null)
		{
			w_token = String.valueOf(getSecureRandomInt());
		}
		
		CacheService.put(CacheConstants.CACHEKEY.WEBSERVICE_CKEY_WS_TOKEN, w_token, a_userId + "#" + System.currentTimeMillis(), true);

		return w_token;
	}
	
	public static int getSecureRandomInt()
	{
		SecureRandom w_sr = new SecureRandom();
		return w_sr.nextInt();
	}
	
	public static double getSecureRandomDouble()
	{
		SecureRandom w_sr = new SecureRandom();
		return w_sr.nextDouble();
	}
	
	/**
	 * 
	 * @return the names of the log file
	 */
	public static ArrayList<String> getFileName() 
	{
		if (SystemProperties.APP_SERVER.startsWith("JBoss")) 
		{
			return getFileNameJboss();
		} 
		return getFileNameWeb();
		
	}

	
	public static ArrayList<String> getFileNameWeb()
	{
		ArrayList<String> w_filenames = new ArrayList();
		w_filenames = dirSearch(System.getProperty("user.dir"));
		return w_filenames;
	}

	/**
	 * 
	 * @return the name of the .log file in which server log is stored.
	 */
	public static ArrayList<String> getFileNameJboss() 
	{
		ArrayList<String> w_filenames = new ArrayList<String>();
		if (System.getProperty("jboss.server.log.dir") != null
				|| "".equals(System.getProperty("jboss.server.log.dir"))) 
		{
			String w_path = System.getProperty("jboss.server.log.dir");
			w_filenames = dirSearch(w_path);
		}
		return w_filenames;
	}
	
	/**
	 * 
	 * @param a_dirname path of directory inside which .log files are store.
	 * @return returns .log file names for the given directory
	 */
	public static ArrayList<String> dirSearch(String a_dirname) 
	 {
	  File w_dir = com.de.base.util.general.FileUtil.getNewFile(a_dirname);
	  File[] w_files = w_dir.listFiles();
	  
	  Arrays.sort(w_files, new Comparator<File>(){
	      public int compare(File f1, File f2)
	      {
	          return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
	      } });

	  ArrayList<String> w_buf = new ArrayList<String>();
	  
	  try 
	  {
	   for (int j = 0; w_files != null && j < w_files.length; j++) 
	   {
	    if (w_files[j] != null && w_files[j].getName().indexOf(".log") != -1)
	     w_buf.add(w_files[j].getName());
	   }
	  } 
	  catch (Exception e) 
	  {
	   e.printStackTrace();
	  }
	  
	  
	  return w_buf;
	 }	
	
	/**
	 * 
	 * @param a_filename:name of .log file
	 * @param a_lineno:start line no. from which log needs to be searched.
	 * @return json object with the data field which contains data.
	 * @throws Exception
	 */
	public static JSONObject getApplogData(String a_filename, String a_lineno) throws Exception 
	{
		JSONObject w_json = new JSONObject();
		String w_lineno = a_lineno;
		int w_line = 0;
		String w_buffer = "";
		if(a_filename != null)
		{
//			String w_file = a_filename.split("~")[0].trim();
			RandomAccessFile w_rfr = new RandomAccessFile(a_filename.trim(), "r");
			try 
			{
				// For downloading the entire log file, a_lineno will be set to -1
				//if (!w_lineno.equals("-1"))
					w_line = (Integer.parseInt(w_lineno));
				long w_length  = w_rfr.length();
				long w_pos = w_length;
				byte w_scanByte = -1;
				if(w_pos-1 > 0)
				{
					int w_numBytes=0;
					w_pos = w_pos-1;
					w_rfr.seek(w_pos);
					int w_lineCount=0;
					while((w_scanByte=w_rfr.readByte()) != -1 && w_pos!=0)
					{
						if(isEndOfLine(w_scanByte))
						{
							w_lineCount++;
						}
						if(!"-1".equals(w_lineno) && w_lineCount > w_line)
						{
							break;
						}
						w_pos = w_pos-1;
						w_rfr.seek(w_pos);
						w_numBytes++;
					}
				if(w_pos+1 < w_length)
				{
					w_pos=w_pos+1;
					w_rfr.seek(w_pos);
				}
				byte w_byteBuffer[]= new byte[w_numBytes-1];
				w_rfr.read(w_byteBuffer,0, w_byteBuffer.length);	
				w_buffer = new String(w_byteBuffer);
				
			}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				throw (e);
			}
			finally 
			{
				w_rfr.close();
			}
			w_json.put("data", WebUtil.substituteInvalidXmlCharacters(w_buffer));
		}
		else
		{
			throw new DigiteAppException("File not found");
		}
			
		return w_json;
		
	}

	/**
	 * 
	 * @param a_pw:printer writer to which file needs to be written
	 * @param a_filename:name of log file.
	 * @throws Exception
	 */
	public static void downloadApplogFlie(PrintWriter a_pw,String a_filename) throws Exception 
	{
		String w_file = a_filename;
		FileInputStream w_fis = new FileInputStream(w_file);
		BufferedInputStream w_bis = new BufferedInputStream(w_fis);
		DataInputStream w_dis = new DataInputStream(w_bis);
		String w_check = null;
		try 
		{
			while((w_check = w_dis.readLine()) != null)
			{
				a_pw.println(w_check);
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw (e);
		}
		finally 
		{
			w_fis.close();
			w_bis.close();
			w_dis.close();
		}
	}
	
	/**
	 * 
	 * @param a_filename:name of .log file
	 * @param a_lineno:start line no. from which log needs to be searched.
	 * @param a_token:access token to retrieve information.
	 * @return json object with the data field which contains data.
	 */
	public static JSONObject getRemoteBootLog(String a_serverNameIp, String a_filename,
			String a_line, String a_token, boolean a_getFileNames, String a_isCFM) throws Exception
	{
		StringBuilder w_applogPage = new StringBuilder();
		HttpURLConnection w_urlCon = null;
		JSONObject w_json = null;
		Scanner w_scanner = null;
		
		if (GlobalConstants.YES.equalsIgnoreCase(getRestPref()))
		{
			w_json = getRemoteBootLog(a_serverNameIp, a_filename, a_line, a_token);
		}
		else
		{
			try 
			{
				if (a_serverNameIp != null)
				{
					StringBuilder w_serverUrl = new StringBuilder(); 
					w_serverUrl.append(a_serverNameIp);
					w_serverUrl.append("&").append(GlobalConstants.TOKEN).append("=").append(a_token);
					if (a_filename != null)
					{		
						w_serverUrl.append("&filename=").append(a_filename);
					}
					if (a_line != null)
					{		
						w_serverUrl.append("&lineno=").append(a_line);
					}
					if (a_isCFM != null)
					{
						w_serverUrl.append("&isCFM=").append(a_isCFM);
					}
					
					if (a_getFileNames)
					{
						w_serverUrl.append("&action=GETREMOTESERVERFILES");
					}
					URL w_url = new URL(w_serverUrl.toString());
					w_urlCon = (HttpURLConnection) w_url
							.openConnection();
					
					try
					{
						if (w_urlCon != null && w_urlCon.getResponseCode() != HttpURLConnection.HTTP_OK)
						{
							throw new DigiteAppException("Something went wrong");
						}
					}
					catch (Exception ae)
					{
					    if (!InetAddress.getByName(w_url.getHost()).isReachable(10000))
					     {
					    	throw new DigiteAppException("Something went wrong");
					     }
					}
					w_scanner = new Scanner(w_urlCon.getInputStream());
					while (w_scanner.hasNext()) 
					{
						w_applogPage.append(w_scanner.nextLine());
						w_applogPage.append("\n");
					}
					w_json = new JSONObject(w_applogPage.toString().trim());
				}
				else
				{
					throw new DigiteAppException("Invalid server");
				}
			} 
			catch (Exception a_exp) 
			{
				throw new DigiteAppException("Something went wrong");
			}
			finally
			{
				if (w_scanner != null)
				{
					w_scanner.close();
				}
				if (w_urlCon != null)
				{  
					w_urlCon.disconnect();
				}
			}
		}
		return w_json;
	}
	/**
	 * 
	 * @param a_byte:byte to be checked for end of line
	 * @return
	 */
	public static boolean isEndOfLine(byte a_byte)
	{
		boolean w_isEOL = false;
		for(byte w_tempByte : "\n".getBytes(Charset.defaultCharset()))
		{
			if(w_tempByte==a_byte)
			{
				w_isEOL = true;
			}
		}
		return w_isEOL;
	}
	
	/**
	 * 
	 * @param a_filePath : Name of app log file.
	 * @param tempFile   : temp folder directory to path
	 * @return File object : returns Zip File
	 * @throws IOException
	 */
	public static File getMinifiedLogFlies(String a_filePath,File tempFile) throws IOException
	{		
		
		File w_zipFile = null;
		File w_file = new File(a_filePath);
		FileInputStream w_fis = null;
		BufferedReader w_bufferReader = null;
		boolean w_isExceptionStarted = false;
		boolean w_isExDateLine = false;
		StringBuilder w_exceptionBody = null;
		ArrayList<ExceptionFilterDetails> exceptionList = new ArrayList<ExceptionFilterDetails>();
		HashSet<String> w_exceptionsSet = new HashSet<String>();
		ExceptionFilterDetails w_exFilterDetails = null;
		
		try 
		{
			w_fis = new FileInputStream(w_file);
			w_bufferReader = new BufferedReader(new InputStreamReader(w_fis));
			String w_Line=null;
			while ((w_Line = w_bufferReader.readLine()) != null)   
			{
				w_exFilterDetails= new ExceptionFilterDetails();
				 w_isExDateLine=false;	
				 w_Line = w_Line.replaceAll("&gt;", ">");
				 if(w_Line.contains("Exception===>"))
				 {
	//				 w_Line = AdminUtils.replacePattern(w_Line);				 
					 w_isExceptionStarted=true;
					 w_exceptionBody= new StringBuilder();
				 }
				 else
				 {
					 if(w_Line.matches(".*[0-9][0-9]:[0-9][0-9]:[0-9][0-9].*$") || w_Line.matches(".*[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9].*$"))
					 {
						 w_isExDateLine=true;
						 if (w_Line.contains("Values ===>") || w_Line.contains("No Arguments")) {
								 
								 w_exFilterDetails.setExceptionDate(w_Line.substring(0,20));
								 w_exFilterDetails.setExceptionValue(w_Line.replaceAll("^.*Values ===>(.*)$", "$1"));
						 }
					 }
				 }
				 
				 if (w_isExceptionStarted)
				 {
					 if(w_isExDateLine)
					 {   
						 w_isExceptionStarted = false;
					 	 w_exFilterDetails.setExceptionBody(w_exceptionBody.toString());
					 	 w_exceptionsSet.add(w_exceptionBody.toString());
					 	 w_exFilterDetails.setExceptionOccurance(1);
					 	 exceptionList.add(w_exFilterDetails); 	 
					 }
					 else if(w_Line.trim().matches("^at com\\.de.*$")||w_Line.matches("^.*[Ee]rror.*$")||w_Line.matches("^.*[Ee]xception.*$")||w_Line.matches("^.*ORA-.*$"))
							 w_exceptionBody.append("\n").append(w_Line); 
				 }
		 
			}
			File exceptionDetailsFile = getExceptionsDetails(exceptionList,tempFile);
			File exceptionFile=getExceptions(w_exceptionsSet,tempFile);
			ZipHelper w_ziphelper = new ZipHelper(tempFile+"_ex.zip");
			w_ziphelper.addFile(exceptionFile);
			w_ziphelper.addFile(exceptionDetailsFile);
			w_zipFile = com.de.base.util.general.FileUtil.getNewFile(tempFile+"_ex.zip");
			w_ziphelper.close();
				
		}catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if(w_bufferReader != null)
				w_bufferReader.close();
			if(w_fis != null)
				w_fis.close();
		}
		return w_zipFile;
	}
	
	/**
	 * 
	 * @param a_exceptionsSet : set of unique exceptions found in app log file
	 * @param tempFile : temp directory 
	 * @return File : returns text file containing  exceptions
	 * @throws Exception
	 */
	
	private static File getExceptions(HashSet<String> a_exceptionsSet,File tempFile) throws Exception {
		FileWriter w_fw = null;
		BufferedWriter bw = null;
		try
		{
		File w_file = new File(tempFile+"_exception.txt");
		
		if (!w_file.exists()) {
				w_file.createNewFile();
			}
	    w_fw = new FileWriter(w_file.getAbsoluteFile());
		bw = new BufferedWriter(w_fw);
		int w_exceptionCount = 0;
		for(String w_exBody:a_exceptionsSet)
		{
			if(w_exBody!= null && !w_exBody.isEmpty())
			{
				w_exBody = replacePattern(w_exBody);
				w_exceptionCount++;
				bw.write(w_exceptionCount+")"+w_exBody.trim()+"\n\n");
			}
		}
		   bw.write("**************** Processed succesfully");
		   return w_file;
		}
		
		finally
		{
			if(bw != null)
				bw.close();
			if(w_fw != null)
			w_fw.close();
		}
	}
	
	/**
	 * 
	 * @param a_exceptionsList : List of unique exceptions found in app log file
	 * @param tempFile : temp directory 
	 * @return :returns text file containing exceptions detail
	 * @throws Exception
	 */
	public static File getExceptionsDetails(ArrayList<ExceptionFilterDetails> a_exceptionsList,File tempFile) throws Exception
	{
		
		 int size = a_exceptionsList.size();
		 FileWriter w_fw = null;
		 BufferedWriter bw = null;
		 try
			{
				 File w_file = new File(tempFile+"_exceptionDetails.txt");
				 if (!w_file.exists()) {
						w_file.createNewFile();
					}
				 w_fw = new FileWriter(w_file.getAbsoluteFile());
				 bw = new BufferedWriter(w_fw);
				 
				 for(int i = 0; i < size; i++)
				 {
					 if(a_exceptionsList.get(i).getExceptionBody()!=null && !a_exceptionsList.get(i).getExceptionBody().isEmpty())
					 {
					 	 bw.write((i+1)+")"+a_exceptionsList.get(i).getExceptionBody().trim()+"\n");
					 
					 	 bw.write("\t\tDate="+a_exceptionsList.get(i).getExceptionDate()+
					 			 "-Values="+a_exceptionsList.get(i).getExceptionValue()+"\n");

					 	 for (int j = i + 1; j <size; j++) {
						
					 		 if (a_exceptionsList.get(j).getExceptionBody().trim().equals
									 (a_exceptionsList.get(i).getExceptionBody().trim()))
					 		 {
					            bw.write("\t\tDate="+a_exceptionsList.get(j).getExceptionDate()+"-Values="+a_exceptionsList.get(j).getExceptionValue()+"\n");
					            a_exceptionsList.remove(j);
					          
					            // decrease j because the array got re-indexed
					            j--;
					            size--;
					 		 }
						 }
					 	
						 bw.write("\n\n");
					 }
			}
			bw.write("**************** Processed succesfully");
			return w_file;
			}
		 finally
			{
			 	if(bw != null)
			 		bw.close();
			 	if(w_fw != null)
			 		w_fw.close();
			}
	}
	
	public static String replacePattern(String a_data)
	{
		StringBuilder w_builder = new StringBuilder();
		if (!"".equals(a_data) || a_data != null)
		{
			w_builder.append(a_data.replaceAll("ObjectMessage.ID.*>", "" ).
					replaceAll("EventLogData@.*\\]/", "" ).
					replaceAll("ActionFrameworkData@[0-9]+", "" ).
					replaceAll("a_actionFWData:com.de.base.scheduler.dataobject.* ", "" ).
					replaceAll("RUNID::.*::", "" ).
					replaceAll("response:weblogic.servlet.internal.ServletResponseImpl@.*\\[", "" ).
					replaceAll("#OID=[^#]*#", "#OID=1234#" ).
					replaceAll("#IID=[^#]*#", "#IID=1234#" ).
					replaceAll("RowName=[^#]*#", "#RowName=1234#" ).
					replaceAll("#RunID=[^#]*#", "#RunID=1234#" ).
					replaceAll("#ThreadID=[^#]*#", "#ThreadID=1234#" ).
					replaceAll("CurrentThreadID=[0-9][0-9]* ", "CurrentThreadID=1234 " ).
					replaceAll("EventType=[^#]*#", "#EventType=1234#" ).
					replaceAll("servlet.jsp.PageContextImpl@.* ", "" ).
					replaceAll("OwnerID=[0-9][0-9]*", "OwnerID=1234" ).
					replaceAll("ItemID=[0-9][0-9]*", "ItemID=1234" ).
					replaceAll("SelectedSegmentId=[0-9][0-9]*", "SelectedSegmentId=1234" ).
					replaceAll("ProjectId=[0-9][0-9]*", "ProjectId=1234" ).
					replaceAll("response:weblogic.servlet.internal.NestedServletResponse@.*\\[", "" ).
					replaceAll("dataobject.DynamicContentMethodParameters@[0-9][0-9a-zA-Z]*", "dataobject.DynamicContentMethodParameters@1234" ).
					replaceAll("[Ii][Tt][Ee][Mm][Ii][Dd] *= *[0-9][0-9]*", "itemid = 1234" ).
					replaceAll("[Ss][Tt][Aa][Gg][Ee][Ii][Dd] *= *[0-9][0-9]*", "stageid = 1234" ).
					replaceAll("Unparseable date: \".*\"", "Unparseable date: 1234" ).
					replaceAll("Transaction \\(Process ID .*\\) was deadlocked", "Transaction (Process ID 1111111) was deadlocked" ).
					replaceAll("===>a_dateStr:.*a_destTz:", "===>a_dateStr:xxxxa_destTz:" ).
					replaceAll("MODIFIEDDATE = [^,]*,", "MODIFIEDDATE = xxxx," ).
					replaceAll("CM_Name *= *'.*' ", "CM_Name = 'xxxx' " ).
					replaceAll("The duplicate key value is \\([0-9, ][0-9 ,]*\\)", "The duplicate key value is (1234)" ).
					replaceAll("Failed to insert data in prestaging table for itemtype [^ ][^ ]*", "Failed to insert data in prestaging table for itemtype XXX").
					replaceAll("m_ownerType:Prj m_SwiftALMItemType:[^ ][^ ]*", "m_ownerType:Prj m_SwiftALMItemType:XXXX").
					replaceAll("ItemType:[^ ][^ ]* PTCODE:[^ ][^ ]* OwnerCode:[^ ][^ ]*", "ItemType:XXXX PTCODE:YYYY OwnerCode:ZZZZ").
					replaceAll("CM_Description *= *'.*' ", "CM_Description = 'xxxx' " ).
					replaceAll("Transaction BEA.* not active anymore", "Transaction BEA-xxxx not active anymore" ).
					replaceAll("Transaction timed out after [1-9][0-9]* seconds", "Transaction timed out after xxx seconds" ).
					replaceAll("xp_readerrorlog", "xp_readerrorlog deadlock" ));
			return w_builder.toString();
		}
		return a_data;
	 }
	public static String getThreadDumps()
	{
		Map<Thread, StackTraceElement[]> w_TMap = Thread.getAllStackTraces();
		StringBuffer w_Buff = new StringBuffer();
		Iterator<Thread> w_It = w_TMap.keySet().iterator();
		while (w_It.hasNext())
		{
			w_Buff.append("\n");
			Thread w_Thread = (Thread) w_It.next();
			StackTraceElement[] w_Elems = (StackTraceElement[]) w_TMap.get(w_Thread);
			
			if (w_Elems.length <= 0 ) 
				continue;
			
			w_Buff.append("@@").append(WebUtil.getThreadDetails(w_Thread) + "\n");
			for (int w_idx = 0; w_idx < w_Elems.length; ++w_idx)
			{
				String w_Trace = w_Elems[w_idx].toString();
				w_Buff.append("\t\t\t" + w_Trace + "\n");
			}
		}
		return w_Buff.toString();
	}
	
	public static void downloadLogFile(HttpServletRequest a_request, ServletContext a_app, HttpServletResponse a_responce, 
			String a_serverIp, String a_serverNameIp, String a_serverNameIpJson, String a_textAreaData,
			String a_localIP, boolean a_isClusterMode, String a_filename, String a_line, String a_isCFM, String a_token) throws Exception
	{
		String w_data = "";
		String w_hostName = a_serverIp;
		String w_zipfilename = null;
		File w_File = null;
		PrintWriter w_FileWriter = null;
		BufferedWriter w_BufWriter = null;

		if (a_serverNameIp != null && a_isClusterMode && !(a_serverIp.equals(a_localIP))) 
		{
			JSONObject w_jsonRemote = null;
			if (a_request.getParameter("Download") != null)
			{
				w_jsonRemote = AdminUtils.getRemoteBootLog(a_serverNameIpJson, a_filename, a_line, a_token, false, a_isCFM);
			}
			else
			{
				w_jsonRemote = AdminUtils.getRemoteBootLog(a_serverNameIpJson, a_filename, "-1", a_token, false, a_isCFM);
			}
			w_data = w_jsonRemote.getString("data");
		} 
		else
		{
			if (a_request.getParameter("MinifyHidden").isEmpty())
			{
				w_data = a_textAreaData;
			}
			else
			{
				w_data = a_request.getParameter("MinifyHidden");
			}
		}
		java.util.Date w_dateObj = new java.util.Date();
		Timestamp w_today = new Timestamp(w_dateObj.getTime());
		String w_date = DateUtil.getUIString(w_today, "yyyyMMddhhmmss");
		
		if (a_request.getParameter("DownloadZip") != null) 
		{
			w_zipfilename = a_filename.substring(a_filename.lastIndexOf(SystemProperties.FILE_SEPERATOR) + 1, a_filename.lastIndexOf("."))
					+ "_" + w_hostName + "_" + w_date + ".zip";
			w_File = com.de.base.util.general.FileUtil.getNewFile(FileUtil.getTempDirectory(a_request, a_app)
							+ SystemProperties.FILE_SEPERATOR + a_filename.substring(a_filename.lastIndexOf(SystemProperties.FILE_SEPERATOR) + 1,
									a_filename.length()) + (a_filename.endsWith(".log") ? "" : ".log"));
			w_BufWriter = new BufferedWriter(new FileWriter(w_File));
			w_FileWriter = new PrintWriter(w_BufWriter);
			
			if (a_serverNameIp != null && a_isClusterMode && !(a_serverIp.equals(a_localIP))) 
			{
				w_FileWriter.println(w_data);
			}
			else
			{
				AdminUtils.downloadApplogFlie(w_FileWriter, a_filename);
			}
		} 
		else 
		{
			w_zipfilename = a_filename.substring(
					a_filename.lastIndexOf(SystemProperties.FILE_SEPERATOR) + 1,
					a_filename.lastIndexOf("."))
					+ "_area_" + w_date + ".zip";
			w_File = com.de.base.util.general.FileUtil.getNewFile(FileUtil.getTempDirectory(a_request, a_app) 
					+ SystemProperties.FILE_SEPERATOR + a_filename.substring(a_filename.lastIndexOf(SystemProperties.FILE_SEPERATOR) + 1, a_filename.lastIndexOf("."))
							+ "_area" + w_hostName + "_" + w_date + ".log");
			w_BufWriter = new BufferedWriter(new FileWriter(w_File));
			w_FileWriter = new PrintWriter(w_BufWriter);
			w_FileWriter.print(w_data);
		}
		if (w_BufWriter != null)
		{
			w_BufWriter.close();
		}
		if (w_FileWriter != null)
		{
			w_FileWriter.close();
		}

		if (a_request.getParameter("Zip") != null) 
		{
			ZipHelper w_ziphelper = new ZipHelper(FileUtil.getTempDirectory(a_request, a_app) + SystemProperties.FILE_SEPERATOR
							+ w_zipfilename);
			w_ziphelper.addFile(w_File);
			w_File.delete();
			File w_zipFile = com.de.base.util.general.FileUtil.getNewFile(FileUtil.getTempDirectory(a_request, a_app)
							+ SystemProperties.FILE_SEPERATOR + w_zipfilename);
			w_ziphelper.close();
			FileUtil.downloadFile(a_responce, w_zipFile, null);
			w_zipFile.delete();
		} 
		else
		FileUtil.downloadFile(a_responce, w_File, null);
		w_File.delete();
	}
	
	public static String getDefaultLogFile(ArrayList<String> a_fnames)
	{
		String w_logFile = null;
		if (SystemProperties.APP_SERVER.startsWith("JBoss"))
		{
			w_logFile = a_fnames.get(0);
		}
		else
		{
			String w_todayDate = DateUtil.getCurrentDate().toString();
			if (w_todayDate.indexOf(" ") != -1)
				w_todayDate = w_todayDate.substring(0, w_todayDate.indexOf(" "));
			String w_runningFileName = "app-" + w_todayDate + ".log";
			if (a_fnames.contains(w_runningFileName))
			{
				w_logFile = w_runningFileName;
			}
			else
			{
				w_logFile = a_fnames.get(0);
			}
		}
		return w_logFile;
	}
	
   public static Map<String, JSONObject> getLocalFiles(Map<String, JSONObject> a_map, String a_localIp, ArrayList<String> a_fnames)
	{
		JSONObject w_jsonRemote = new JSONObject();
		JSONArray w_array = new JSONArray(a_fnames);
		w_jsonRemote.put("REMOTESERVERFILES", w_array);
		
		if (a_map != null && !a_map.isEmpty())
		{
			a_map.put(a_localIp, w_jsonRemote);
		}
		else
		{
			Map<String, JSONObject> w_map = new HashMap<String, JSONObject>();
			w_map.put(a_localIp, w_jsonRemote);
			a_map = w_map; 
		}
		return a_map;
	}
   
   public static Map<String, JSONObject> getRemoteLogFiles( Map<String, JSONObject> a_map, String a_clusterIp, UserData a_userData, String a_localIp, ArrayList<String> a_fnames, String a_token)
   {
	   URLConnection w_urlCon = null;
	   Scanner w_scanner = null;
	   JSONObject w_json = null;
	   int w_statusCode = 0;
	   if (a_map == null || (a_map != null && a_map.isEmpty()))
	   {
		   a_map = new HashMap<String, JSONObject>();
	   }
		    
	   try
	   {
		   JSONArray w_serverIpStores = new JSONArray(a_clusterIp);
		   for (int w_idx = 0; w_idx < w_serverIpStores.length(); w_idx++)
		   {
			   String w_webSecUrl = WebSecHelper.generateKeyForURL(a_userData, "/digite/Request?Key=applog&action=GETREMOTESERVERFILESAJAX");
			   String w_serverIp = w_serverIpStores.getJSONObject(w_idx).getString("serverIp");
			   URL w_url = new URL(w_serverIp.split("digite")[0] + w_webSecUrl);
			   if (!w_serverIp.contains(a_localIp))
			   {
				   if (GlobalConstants.YES.equalsIgnoreCase(getRestPref()))
				   {
					   getRemoteLogFiles(a_map, w_serverIp, a_userData, a_token, a_localIp);
				   }
				   else
				   {
					   try
					   {
						   w_urlCon = (HttpURLConnection) w_url.openConnection();
						   w_statusCode = ((HttpURLConnection) w_urlCon).getResponseCode();
					   }
					   catch (Exception a_ex)
					   {
						   w_statusCode = 0;
					   }
					   if (w_statusCode == 200)
					   {
						   StringBuilder w_applogPage = new StringBuilder();
						   w_scanner = new Scanner(w_urlCon.getInputStream());
						   while (w_scanner.hasNext()) 
						   {
							   w_applogPage.append(w_scanner.nextLine()).append("\n");
						   }
						   w_json = new JSONObject(w_applogPage.toString().trim());
						   a_map.put(w_url.getHost() , w_json);
					   }
					   else
					   {
						   ArrayList<String> w_errMsg = new ArrayList<String>();
						   w_errMsg.add("\"SERVER_IS_DOWN\"");
						   a_map.put(w_url.getHost() , new JSONObject().put("REMOTESERVERFILES", w_errMsg));
					   }
				   }
			   }
			   else
			   {
				   a_map = getLocalFiles(a_map, a_localIp, a_fnames);
			   }
		   }
	   }
	   catch (Exception a_ex)
	   {
		   WebUtil.logExceptionTrace(a_ex, null);
	   }
	   finally
	   {
		   if (w_scanner != null)
		   {
			   w_scanner.close();
		   }
		   if (w_urlCon != null)
		   {  
			   ((HttpURLConnection) w_urlCon).disconnect();
		   }
	   }
	   
	   return a_map;
   }
   public static String getLogFolderPath()
   {
	   String w_path = "";
	   if (SystemProperties.APP_SERVER.startsWith("JBoss")) 
	   {
		   w_path = System.getProperty("jboss.server.log.dir");
	   } 
	   else 
	   {
		   w_path = System.getProperty("user.dir");
	   }
	   return w_path;
   }
   
   public static void downloadMinifiedLog(HttpServletRequest a_request, ServletContext a_app, HttpServletResponse a_responce, 
			String a_serverIp, String a_serverNameIp, String a_serverNameIpJson, String a_textAreaData,
			String a_localIP, boolean a_isClusterMode, String a_filename, String a_line, String a_isCFM, String a_token) throws Exception
   {
	   String w_data = "";

	   if (a_serverNameIp != null && a_isClusterMode && !(a_serverIp.equals(a_localIP))) 
	   {
		   JSONObject w_jsonRemote = getRemoteBootLog(a_serverNameIpJson, a_filename, a_line, a_token, false, a_isCFM);
		   w_data = w_jsonRemote.getString("data");
	   } 
	   else
	   {
		   if (a_request.getParameter("MinifyHidden").isEmpty())
		   {
			   w_data = a_textAreaData;
		   }
		   else
		   {
			   w_data = a_request.getParameter("MinifyHidden");
		   }
	   }
	   File w_tempfile = FileUtil.getTempDirectory(a_request, a_app);
	   PrintWriter w_printWriter = new PrintWriter(w_tempfile + "/temp.log");
	   w_printWriter.println(w_data);
	   w_printWriter.close();
	   File w_tempFile = new File(w_tempfile + SystemProperties.FILE_SEPERATOR + a_filename.substring(a_filename.lastIndexOf(SystemProperties.FILE_SEPERATOR) + 1,
			   a_filename.lastIndexOf(".")));
	   File w_file = null;
	   if (a_serverNameIp != null && a_isClusterMode && !(a_serverIp.equals(a_localIP))) 
	   {
		   w_file = getMinifiedLogFlies(w_tempfile + "\\temp.log", w_tempFile);
	   }
	   else
	   {
		   w_file = getMinifiedLogFlies(a_filename, w_tempFile);
	   }
	   FileUtil.downloadFile(a_responce, w_file, null);
   }
   
   public static String getMinifiedThreadDump(String a_threadDump)
   {
	   String[] w_threadDumpList = a_threadDump.split("@@");
	   StringBuilder w_minifiedThreadDump = new StringBuilder();
	   for (int w_idx = 0; w_idx < w_threadDumpList.length; w_idx++)
	   {
		   if (!(w_threadDumpList[w_idx].indexOf("ConcurrentUsersLogHandler.run") != -1 || w_threadDumpList[w_idx].indexOf("web.beans.BroadcastCommandHandler.run") != -1 ||
				   w_threadDumpList[w_idx].indexOf("web.servlets.AdaptorJobScheduler.run") != -1 || w_threadDumpList[w_idx].indexOf("web.servlets.WSTokenizer.run") != -1 ||
				   w_threadDumpList[w_idx].indexOf("ScheduledProcessHandler.run") != -1 || w_threadDumpList[w_idx].indexOf("DigiteConnectionThread.run") != -1 || 
				   w_threadDumpList[w_idx].indexOf("org.apache.jsp.base.tool.threaddump_jsp") != -1 || 
				   w_threadDumpList[w_idx].indexOf("WSTokenizer.run") != -1 || 
				   w_threadDumpList[w_idx].indexOf("DigiteAsyncHandlerThread.run") != -1 ))
		   {
			   if (w_threadDumpList[w_idx].indexOf("com.de") != -1 || w_threadDumpList[w_idx].indexOf("org.apache.jsp") != -1)
			   {
					String[] w_innerThreadDumpList = w_threadDumpList[w_idx].split("\n");
					w_minifiedThreadDump.append(w_innerThreadDumpList[0]).append("\n\t\t\t").
										 append(w_innerThreadDumpList[1]).append("\n\t\t\t");
					
					for (int w_idx1 = 1; w_idx1 < w_innerThreadDumpList.length - 1 ; w_idx1++)
					{
						if (w_innerThreadDumpList[w_idx1].indexOf("com.de") != -1 || w_innerThreadDumpList[w_idx1].indexOf("org.apache.jsp.") != -1)
							w_minifiedThreadDump.append(w_innerThreadDumpList[w_idx1]).append("\n\t\t\t");
					}
					w_minifiedThreadDump.append("\n");
				}				
			}			
		}
	   return w_minifiedThreadDump.toString();
   }
   
   public static String queryDataPreference(String a_key)
   {
	   String w_result = "";
	   JSONObject w_obj = null;
	   try
	   {
		   String w_prefVal = PreferenceService.getEnterprisePreference(-1, 
				   PreferenceConstants.ENTPREF.QUERYDATA_PREF);
		   
		   if (w_prefVal !=null)
		   {
			   w_obj = new JSONObject(w_prefVal);
			   if (w_obj.has(a_key))
			   {
				   w_result = (String) w_obj.get(a_key);
			   }
		   }
	   }
	   catch (ParseException w_parseExp)
	   {
		   LOGGER.fatal("AdminUtils.queryDataPreference: invalid value specified in DB");
		   HashMap<String, Object> w_map = new HashMap<String, Object>();
		   w_map.put("PrefValue", w_obj);
		   WebUtil.logExceptionTrace(w_parseExp, w_map);
		   w_result = "inproperValue specified";
	   }
	   catch (Exception a_ex)
	   {
		   WebUtil.logExceptionTrace(a_ex, null);
		   w_result = "Exception while fetching";
	   }
	   return w_result;
   }
   
   public static void checkQueryDataThreshold(QueryData a_queryData, int a_queryTimeOut) throws DigiteAppException, SQLException
   {
	   int w_prefVal = 0;
	   int w_selectCount = -1;
	   boolean w_throwExpn = false;
	   String w_queryString = a_queryData.getQuery();
	   try 
	   {
		   if (w_queryString != null && !StringUtil.containsString(".*(\\s+)(AVG|SUM|MIN|MAX|COUNT|STDEV|STDEVP|COUNT_BIG|DISTINCT)[(\\s+)[\\(]].*", w_queryString))
		   {
			   if (SystemProperties.DB_TYPE == DBTypes.SQL_SERVER && w_queryString.trim().toLowerCase().matches(".*select(\\s+)top(\\s+).*"))
			   {
				   int w_selectCount1 = -1;
				   Pattern w_pattern = Pattern.compile("-?\\d+");
				   Matcher w_matcher = w_pattern.matcher(w_queryString);
				   
				   if (w_matcher.find())
				   {
					   w_selectCount = Integer.parseInt(w_matcher.group());
				   }
				   w_selectCount1 = SQLUtil.getRowCountWrap(a_queryData, true, a_queryTimeOut);
				   
				   if (w_selectCount1 != -1 && w_selectCount1 < w_selectCount)
				   {
					   w_selectCount = w_selectCount1;
				   }

				   if (w_queryString.trim().toLowerCase().matches(".*(\\s+)rownum(\\s+).*"))
				   {
					   w_selectCount = -1;
				   }

			   }
			   else if (SystemProperties.DB_TYPE == DBTypes.ORACLE && w_queryString.trim().toLowerCase().matches(".*(\\s+)rownum(\\s+).*"))
			   {
				   int w_selectCount1 = -1;
				   Pattern w_pattern = Pattern.compile("-?\\d+");
				   Matcher w_matcher = w_pattern.matcher(w_queryString.split("rownum")[1]);
				   
				   if (w_matcher.find())
				   {
					   w_selectCount = Integer.parseInt(w_matcher.group());
				   }
				   
				   w_selectCount1 = SQLUtil.getRowCountWrap(a_queryData, true, a_queryTimeOut);
				   if (w_selectCount1 != -1 && w_selectCount1 < w_selectCount)
				   {
					   w_selectCount = w_selectCount1;
				   }
				   
				   if (w_queryString.trim().toLowerCase().matches(".*select(\\s+)top(\\s+).*"))
				   {
					   w_selectCount = -1;
				   }
			   }
			   else
			   {
				   w_selectCount = SQLUtil.getRowCountWrap(a_queryData, true, a_queryTimeOut);
			   }
		   }
		   w_prefVal = Integer.parseInt(queryDataPreference("ResultSize"));
		   
		   if (w_selectCount != -1 && w_prefVal != 0 && w_selectCount >= w_prefVal)
		   {
			   w_throwExpn = true;
		   }
	   }
	   catch (NumberFormatException a_ex)
	   {
		   LOGGER.fatal("AdminUtils.checkQueryDataThreshold: invalid value specified in DB");
		   WebUtil.logExceptionTrace(a_ex, null);
	   }
	   catch (SQLException a_ex)
	   {
		   if (a_ex.getMessage() != null && a_ex.getMessage().indexOf("Exception in getcount query") == -1)
		   {
			   if (a_ex instanceof SQLSyntaxErrorException || 
					   StringUtil.containsString(".*[(incorrect(\\s+)syntax)(invalid)].*", a_ex.getMessage()))
			   {
				   throw a_ex;
			   }
			   else
			   {
				   w_throwExpn = true;
			   }
		   }
	   }
	   catch (Exception e) 
	   {
		   WebUtil.logExceptionTrace(e, null);
	   }
	   finally
	   {
		   if (w_throwExpn)
		   {
			   throw new DigiteAppException("Cannot proceed");
		   }
	   }
   }
   
   public static void getRemoteLogFiles(Map<String, JSONObject> a_map, String a_nodeIp, UserData a_userData, String a_token, String a_localIP)
   {
	   URLConnection w_urlCon = null;
	   int w_statusCode = 0;
	   Scanner w_scanner = null;
	   JSONObject w_result = null;
	   URL w_url = null;
	   try 
	   {
		   w_url = new URL(a_nodeIp.split("digite")[0] + "rest/api/AdminUtilRest/logfilelist");
		   if (!a_nodeIp.isEmpty() && a_nodeIp.toLowerCase().indexOf("https:") != -1)
		   {
			   trustAll();
			   System.setProperty("https.protocols", "TLSv1.2");
			   w_urlCon = (HttpsURLConnection) w_url.openConnection();
			   w_urlCon.setRequestProperty("AuthorizationToken", a_token);
			   w_statusCode = ((HttpsURLConnection) w_urlCon).getResponseCode();
		   }
		   else
		   {
			   w_urlCon = (HttpURLConnection) w_url.openConnection();
			   w_urlCon.setRequestProperty("AuthorizationToken", a_token);
			   w_statusCode = ((HttpURLConnection) w_urlCon).getResponseCode();
		   }
		   if (w_statusCode == HttpURLConnection.HTTP_OK)
		   {
			   StringBuilder w_applogPage = new StringBuilder();
			   w_scanner = new Scanner(w_urlCon.getInputStream());
			   while (w_scanner.hasNext()) 
			   {
				   w_applogPage.append(w_scanner.nextLine()).append("\n");
			   }
			   w_result = new JSONObject(w_applogPage.toString().trim());
			   if (w_result.get("data") != null)
			   {
				   a_map.put(w_url.getHost(), new JSONObject().put("REMOTESERVERFILES", w_result.get("data")));
			   }
		   }
	   }
	   catch (Exception a_ex) 
	   {
		   WebUtil.logExceptionTrace(a_ex, null);
	   }
	   finally
	   {
			if (w_scanner != null)
			{
				w_scanner.close();
			}
			if (w_urlCon != null)
			{  
				if (w_urlCon instanceof HttpsURLConnection)
				{
					((HttpsURLConnection) w_urlCon).disconnect();	
				}
				else
				{
					((HttpURLConnection) w_urlCon).disconnect();
				}
			}
	   }
   }
  
   public static JSONObject getRemoteBootLog(String a_serverNameIp, String a_filename, String a_line, String a_token) throws Exception
   {
	   JSONObject w_result = null;
	   StringBuilder w_urlString = new StringBuilder();
	   StringBuilder w_applogData = new StringBuilder();
	   if (a_filename.contains("\\"))
	   {
		   a_filename = a_filename.substring(a_filename.lastIndexOf("\\") + 1);
	   }
	   w_urlString.append(a_serverNameIp.split("digite")[0]).append("rest/api/AdminUtilRest/logfiledata/").append(a_filename);
	   URLConnection w_urlCon = null;
	   Scanner w_scanner = null;
	   DataOutputStream w_out = null;
	   int w_statusCode = 0;
	   try 
	   {
		   URL w_url = new URL(w_urlString.toString().trim());
		   
		   if (w_urlString.toString().toLowerCase().indexOf("https:") != -1)
		   {
			   trustAll();
			   System.setProperty("https.protocols", "TLSv1.2");
			   w_urlCon = (HttpsURLConnection) w_url.openConnection();
			   w_urlCon.setRequestProperty("AuthorizationToken", a_token);
			   ((HttpsURLConnection) w_urlCon).setRequestMethod("POST");
			   w_urlCon.setDoOutput(true);
			   w_urlCon.connect();
			   w_out = new DataOutputStream(w_urlCon.getOutputStream());
			   w_out.writeBytes(a_line);
			   w_statusCode = ((HttpsURLConnection) w_urlCon).getResponseCode();
		   }
		   else
		   {
			   w_urlCon = (HttpURLConnection) w_url.openConnection();
			   w_urlCon.setRequestProperty("AuthorizationToken", a_token);
			   ((HttpURLConnection) w_urlCon).setRequestMethod("POST");
			   w_urlCon.setDoOutput(true);
			   w_urlCon.connect();
			   w_out = new DataOutputStream(w_urlCon.getOutputStream());
			   w_out.writeBytes(a_line);
			   w_statusCode = ((HttpURLConnection) w_urlCon).getResponseCode();
		   }

		   try
		   {
			   if ( w_statusCode != 0 && w_statusCode != HttpURLConnection.HTTP_OK)
			   {
				   throw new DigiteAppException("Something went wrong");
			   }
		   }
		   catch (Exception ae)
		   {
			   if (!InetAddress.getByName(w_url.getHost()).isReachable(10000))
			   {
				   throw new DigiteAppException("Something went wrong");
			   }
		   }
		   w_scanner = new Scanner(w_urlCon.getInputStream());
		   while (w_scanner.hasNext()) 
		   {
			   w_applogData.append(w_scanner.nextLine());
			   w_applogData.append("\n");
		   }
		   w_result = new JSONObject(w_applogData.toString().trim());
		   if (w_result.get("data") != null)
		   {
			   w_result = new JSONObject(w_result.get("data").toString());
		   }
		} 
		catch (Exception a_exp) 
		{
			throw new DigiteAppException("Something went wrong");
		}
		finally
		{
			if (w_scanner != null)
			{
				w_scanner.close();
			}
			if (w_urlCon != null)
			{  
				if (w_urlCon instanceof HttpsURLConnection)
				{
					((HttpsURLConnection) w_urlCon).disconnect();	
				}
				else
				{
					((HttpURLConnection) w_urlCon).disconnect();
				}
			}
			if (w_out != null)
			{
				w_out.close();
			}
		}

	   return w_result;
   }
   
   public static String getRestPref()
   {
	   String w_result = null;
	   try
	   {
		   String w_isApplogRestEnable = PreferenceService.getEnterprisePreference(-1, PreferenceConstants.ENTPREF.IS_APPLOG_REST_ENABLE);
		   w_result = w_isApplogRestEnable;
	   }
	   catch (Exception a_ex)
	   {
		   WebUtil.logExceptionTrace(a_ex, null);
	   }
	   return w_result;
   }
   
   public static boolean verifyHost()
   {
	   return true;
   }
   
	public static void trustAll() 
	{
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() 
		{
			public boolean verify(String a_hostname, SSLSession a_session) 
			{
				return verifyHost();
			}
		});
		try
		{
			SSLContext w_context = SSLContext.getInstance("SSL");
			w_context.init(null, new TrustManager[]
					{
							new X509TrustManager() 
							{
								public void checkClientTrusted(X509Certificate[] a_chain, String a_authType) { }
								public void checkServerTrusted(X509Certificate[] a_chain, String a_authType) { }
								public X509Certificate[] getAcceptedIssuers() 
								{
									return new X509Certificate[]{};
								}
							}
					},
					new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(w_context.getSocketFactory());
		}
		catch (GeneralSecurityException gse) 
		{
			throw new IllegalStateException(gse.getMessage());
		}
	}
} 