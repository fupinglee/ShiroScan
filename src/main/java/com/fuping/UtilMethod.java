package com.fuping;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class UtilMethod {

	public static CloseableHttpClient getHttpClient(){


		SSLContext sslcontext = SSLContextFactory.getSSLContext(); //设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER))
				.build();


		RequestConfig.Builder builder =
				RequestConfig.custom()
						.setConnectTimeout(50000)
						.setSocketTimeout(5000)
						.setConnectionRequestTimeout(5000);


		//把代理设置到请求配置
		RequestConfig requestConfig = builder
				.build();


		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connManager)
				//.disableRedirectHandling()
				.build();

		return client;
	}

	public static CloseableHttpResponse doHttpRequest(String url, String cookie, String labValue){
		CloseableHttpResponse response = null;
		HttpRequestBase requestBase = new HttpGet(url);
		try{
			requestBase.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.113 Safari/537.36");
			requestBase.setHeader("labHeader",labValue);
			requestBase.setHeader("Cookie",cookie);
			response = getHttpClient().execute(requestBase); //获取结果实体
		}catch (Exception e){

		}
		return response;

	}
	public static CloseableHttpResponse doHttpRequest(String url, String cookie, Map<String, String> headers){
		CloseableHttpResponse response = null;
		HttpRequestBase requestBase = new HttpGet(url);
		try{
			if(headers != null && headers.size()>0){
				Iterator iterator = headers.keySet().iterator();
				while (iterator.hasNext()){
					Object key = iterator.next();
					requestBase.setHeader(key.toString(),headers.get(key));
				}
			}

			if(cookie !=null){
				requestBase.setHeader("Cookie",cookie);
			}

			response = getHttpClient().execute(requestBase); //获取结果实体
		}catch (Exception e){

		}
		return response;

	}

	public static CloseableHttpResponse doHttpRequest(String url, String cookie){
		Map<String, String> headers = new HashMap<>();
		return doHttpRequest(url,cookie,headers);

	}


	public static JSONObject getDomain(){
		Random random=new Random();
		double t = random.nextDouble();
		Map<String, String> headers = new HashMap<>();
		String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
		String getDomainURL = "http://www.dnslog.cn/getdomain.php?t="+t;
		headers.put("Referer","http://www.dnslog.cn/");
		headers.put("Accept-Encoding","gzip, deflate");
		headers.put("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8");
		headers.put(HttpHeaders.USER_AGENT,ua);
		HttpResponse response  = UtilMethod.doHttpRequest(getDomainURL,null,headers);

		String cookie;
		String domain;
		JSONObject jsonObject = null;
		try{
			cookie = response.getLastHeader("Set-Cookie").getValue();
			domain = EntityUtils.toString(response.getEntity());
			jsonObject = new JSONObject();
			jsonObject.put("Cookie",cookie);
			jsonObject.put("domain",domain);
		}catch (Exception e){

		}


		return jsonObject;
	}

	public static String getRecords(String cookie){
		Random random=new Random();
		double t = random.nextDouble();
		Map<String, String> headers = new HashMap<>();
		String getRecordsURL = "http://www.dnslog.cn/getrecords.php?t=" + t;
		headers.put("Referer","http://www.dnslog.cn/");
		headers.put("Accept-Encoding","gzip, deflate");
		headers.put("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8");
		String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36";
		headers.put(HttpHeaders.USER_AGENT,ua);
		HttpResponse response = UtilMethod.doHttpRequest(getRecordsURL,cookie,headers);
		String result = null;
		try{
			result = EntityUtils.toString(response.getEntity());
			JSONArray jsonArray = JSONArray.fromObject(result);

			StringBuilder sb = new StringBuilder();;
			if(jsonArray.size()>0){
				for(int i =0;i<jsonArray.size();i++){
					sb.append(jsonArray.getString(i)).append("\r\n");
				}
				result = sb.toString();
			}else{
				result = "未找到正确的Key";
			}
		}catch (Exception e){
			result = e.getMessage();
		}
		return result;

	}
	public static String formatUrl(String oldUrl){
		StringBuffer url = new StringBuffer();
		String result = oldUrl;
		if((!oldUrl.startsWith("http://"))&&(!oldUrl.startsWith("https://"))){
			url.append("http://");
			url.append(oldUrl);
			result = url.toString();
		}
		return result;
	}

	public static String getUrl(String url){
		StringBuffer sb = new StringBuffer();
		if(url.startsWith("http://")||(url.startsWith("https://"))){
			sb.append(url.split("/")[0]);
			sb.append("//");
			sb.append(url.split("/")[2]);
		}else{
			sb.append("http://");
			sb.append(url.split("/")[0]);
		}
		return sb.toString();
	}
	
	public static String getTime(){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		return sdf.format(new Date());
	}
	public static boolean isExists(String path){
		if(path!=null){

			File file = new File(path);
			return file.exists();
		}else{
			return false;
		}
		
	}
	public static List<String> readFile(String path) {
		List<String> lt = new ArrayList<String>();
		File file = new File(path);
		Reader reader = null;
		BufferedReader bf = null;
		if (file.exists()) {
			try {
				reader = new InputStreamReader(new FileInputStream(file),"GBK");
				bf = new BufferedReader(reader);
				String line = null;
				while ((line = bf.readLine()) != null) {
					if (line.trim().length() > 0) {
						lt.add(line.trim());
					}
				}
				bf.close();
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lt;
	}
	public static String getFDate(){
		String result  = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmsss");
		result = sdf.format(new Date());
		return result;
	}
	public static boolean writeResult(String path, String content, boolean flag){
		
		File f = new File(path);
		FileWriter fw = null;
		try{
			if (!f.exists()) {
				f.createNewFile();		
			}
			fw = new FileWriter(f,flag);
			fw.write(content+"\r\n");
			fw.flush();
			fw.close();
			
		}catch(IOException e){
			
		}
		return f.exists();
	}
	
	public static void delFile(String path){
		
		File f = new File(path);
		if (f.exists()) {
			f.delete();		
		}
	}

}
