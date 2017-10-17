package proxy;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.mitre.dsmiley.httpproxy.ProxyServlet;

@WebServlet(initParams = { //
		@WebInitParam(name = "targetUri", value = ProxyList.GW2), //
		@WebInitParam(name = ProxyServlet.P_LOG, value = "true"), //
		@WebInitParam(name = ProxyServlet.P_HANDLEREDIRECTS, value = "true"),
		@WebInitParam(name = ProxyServlet.P_PRESERVECOOKIES, value="false"),
		@WebInitParam(name = ProxyServlet.P_PRESERVEHOST, value="false")//
}, loadOnStartup = 1, urlPatterns = "/ipfs/2/*")
@SuppressWarnings("serial")
public class Ipfs2 extends ProxyServlet {
}
