package proxy;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

@WebServlet(initParams = { //
		@WebInitParam(name = "targetUri", value = "https://ipfs.works/ipfs"), //
		@WebInitParam(name = ProxyServlet.P_LOG, value = "true"), //
		@WebInitParam(name = ProxyServlet.P_HANDLEREDIRECTS, value = "true"),
		@WebInitParam(name = ProxyServlet.P_PRESERVECOOKIES, value="true"),
		@WebInitParam(name = ProxyServlet.P_PRESERVEHOST, value="false")//
}, loadOnStartup = 1, urlPatterns = "/ipfs/*")
@SuppressWarnings("serial")
public class Proxy extends ProxyServlet {
	public Proxy() {
	}
	@Override
	protected void copyResponseHeader(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
			Header header) {
		log("HEADER: "+header.getName()+" = "+header.getValue());
		super.copyResponseHeader(servletRequest, servletResponse, header);
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
	}
}
