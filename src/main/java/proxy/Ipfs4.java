package proxy;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.mitre.dsmiley.httpproxy.ProxyServlet;

@WebServlet(initParams = { //
		@WebInitParam(name = "targetUri", value = ProxyList.GW4), //
		@WebInitParam(name = ProxyServlet.P_LOG, value = "true"), //
		@WebInitParam(name = ProxyServlet.P_HANDLEREDIRECTS, value = "true"),
		@WebInitParam(name = ProxyServlet.P_PRESERVECOOKIES, value = "false"),
		@WebInitParam(name = ProxyServlet.P_PRESERVEHOST, value = "false")//
}, loadOnStartup = 1, urlPatterns = "/ipfs/4/*")
@SuppressWarnings("serial")
public class Ipfs4 extends ProxyServlet {
}
