package com.wp.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

/**
 * Servlet implementation class PhotoTest
 */
@WebServlet("/PhotoTest")
public class PhotoTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletConfig config;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PhotoTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		System.out.println("访问了上传文件服务器");  
		getServletContext().log("访问了上传文件服务器");

		request.setCharacterEncoding("utf-8");  
		
        //获得磁盘文件条目工厂。  
        DiskFileItemFactory factory = new DiskFileItemFactory();  
        
        //获取文件上传需要保存的路径，upload文件夹需存在。这是服务器的保存地址
        String path = request.getSession().getServletContext().getRealPath("/upload"); 
        
        //设置暂时存放文件的存储室，这个存储室可以和最终存储文件的文件夹不同。因为当文件很大的话会占用过多内存所以设置存储室。  
        factory.setRepository(new File(path));  
        
        //设置缓存的大小，当上传文件的容量超过缓存时，就放到暂时存储室。  
        factory.setSizeThreshold(1024*1024);  
        
        //上传处理工具类（高水平API上传处理？）  
        ServletFileUpload upload = new ServletFileUpload(factory);  
          
        try{  
            //调用 parseRequest（request）方法  获得上传文件 FileItem 的集合list 可实现多文件上传。  
            List<FileItem> list = (List<FileItem>)upload.parseRequest(new ServletRequestContext(request));  
            
            for(FileItem item:list){  
            	
                //获取表单属性名字。  
                String name = item.getFieldName();  
                
                //如果获取的表单信息是普通的文本信息。即通过页面表单形式传递来的字符串。  
                if(!name.equals("upload")){  // 不是 upload
                	
                    //获取用户具体输入的字符串，  
                    String value = item.getString();  
                    request.setAttribute(name, value);  
                    
                } else{   //upload 如果传入的是非简单字符串，而是图片，音频，视频等二进制文件。  
                	
                    //获取路径名  
                    String value = item.getName();  
                    
                    //取到最后一个反斜杠。  
                    int start = value.lastIndexOf("\\");  
                    
                    //截取上传文件的 字符串名字。+1是去掉反斜杠。  
                    String filename = value.substring(start+1);  
                    
                    request.setAttribute(name, filename);  
                      
                    /*第三方提供的方法直接写到文件中。 
                     * item.write(new File(path,filename));*/  
                    
                    //收到 写到接收的文件中。  
                    OutputStream out = new FileOutputStream(new File(path,filename));  
                    InputStream in = item.getInputStream();  
                      
                    int length = 0;  
                    byte[] buf = new byte[1024];  
                    
                    System.out.println("获取文件总量的容量:"+ item.getSize());  
                    getServletContext().log("文件名:"+ filename + "，文件总量的容量:"+ item.getSize());
                    
                    response.getWriter().print("ok:" + filename );
                    
                    while((length = in.read(buf))!=-1){  
                        out.write(buf,0,length);  
                    }  
                    in.close();  
                    out.close();  
                }  
            }  
            
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  
}
