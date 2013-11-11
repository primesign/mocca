package at.gv.egiz.bku.spring;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class InternalSSLSocketFactory extends SSLSocketFactory {

	private SSLSocketFactory proxy;
	private String[] suites;

	public InternalSSLSocketFactory(SSLSocketFactory socketFactory,
			String[] disabledSuites) {
		this.proxy = socketFactory;
		List<String> dSuites = Arrays.asList(disabledSuites);
		List<String> suites = new ArrayList<String>(Arrays.asList(proxy.getDefaultCipherSuites()));
		suites.removeAll(dSuites);
		this.suites = suites.toArray(new String[suites.size()]);
	}

	@Override
	public Socket createSocket(Socket s, String host, int port,
			boolean autoClose) throws IOException {
		Socket socket = proxy.createSocket(s, host, port, autoClose);
		setCipherSuites(socket);
		return socket;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return suites;
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return proxy.getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		Socket socket = proxy.createSocket(host, port);
		setCipherSuites(socket);
		return socket;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		Socket socket = proxy.createSocket(host, port);
		setCipherSuites(socket);
		return socket;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost,
			int localPort) throws IOException, UnknownHostException {
		Socket socket = proxy.createSocket(host, port, localHost,
				localPort);
		setCipherSuites(socket);
		return socket;
	}

	@Override
	public Socket createSocket(InetAddress address, int port,
			InetAddress localAddress, int localPort) throws IOException {
		Socket socket = proxy.createSocket(address, port, localAddress,
				localPort);
		setCipherSuites(socket);
		return socket;
	}

	private void setCipherSuites(Socket socket) {
		if (socket instanceof SSLSocket)
			((SSLSocket) socket).setEnabledCipherSuites(suites);
	}
}
