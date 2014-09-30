package nc.noumea.mairie.kiosque.cmis;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nc.noumea.mairie.ws.WSConsumerException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class SharepointService implements ISharepointService {

	@Override
	public List<SharepointDto> getAllEae(Integer idAgent) throws Exception {
		String xml = recupereEaeSharepoint(idAgent);
		return transformeXmlEnListUrl(xml);
	}

	public List<SharepointDto> transformeXmlEnListUrl(String xml) {
		List<SharepointDto> result = new ArrayList<SharepointDto>();
		try {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("m:properties");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				NodeList sousListe = nNode.getChildNodes();

				SharepointDto dto = new SharepointDto();

				for (int temp2 = 0; temp2 < sousListe.getLength(); temp2++) {

					Node nNode2 = sousListe.item(temp2);

					if (nNode2.getNodeName().equals("d:ValeurDIDDeDocument")) {
						dto.setId(sousListe.item(temp2).getTextContent());
					} else if (nNode2.getNodeName().equals("d:AnnéeEAE")) {
						dto.setAnnee(sousListe.item(temp2).getTextContent());
					} else if (nNode2.getNodeName().equals("d:Nom")) {
						dto.setUrl(sousListe.item(temp2).getTextContent());
					}
				}
				result.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	private String recupereEaeSharepoint(Integer idAgent) throws Exception {

		String urlRestSharepoint = "/kiosque-rh/_vti_bin/ListData.svc/EAE?$filter=MatriculeAgent+eq+'" + idAgent + "'";

		HashMap<String, String> params = new HashMap<>();
		params.put("userSharepoint", "sirhDroitEAE");
		params.put("userPwdSharepoint", "fum2ndo5");
		params.put("domainSharepoint", "SITE-MAIRIE");
		params.put("urlSharepoint", "svq-sp");
		params.put("portSharepoint", "80");
		params.put("urlSharepointComplete", urlRestSharepoint);

		HttpResponse res = null;
		try {
			res = createAndFireRequest(params);
		} catch (ClientProtocolException e) {
			throw new WSConsumerException(e.getMessage(), e);
		} catch (IOException e) {
			throw new WSConsumerException(e.getMessage(), e);
		}

		return readResponse(String.class, res, params.get("urlSharepointComplete"));
	}

	public HttpResponse createAndFireRequest(Map<String, String> parameters) throws ClientProtocolException,
			IOException {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());

		httpclient.getCredentialsProvider().setCredentials(
				new AuthScope(parameters.get("urlSharepoint"), Integer.valueOf(parameters.get("portSharepoint"))),
				new NTCredentials(parameters.get("userSharepoint"), parameters.get("userPwdSharepoint"), "", parameters
						.get("domainSharepoint")));

		HttpHost target = new HttpHost(parameters.get("urlSharepoint"), Integer.valueOf(parameters
				.get("portSharepoint")), "http");

		// Make sure the same context is used to execute logically related
		// requests
		HttpContext localContext = new BasicHttpContext();

		// Execute a cheap method first. This will trigger NTLM authentication

		HttpGet httpget = new HttpGet(parameters.get("urlSharepointComplete"));

		HttpResponse response1 = httpclient.execute(target, httpget, localContext);

		return response1;
	}

	public <T> String readResponse(Class<T> targetClass, HttpResponse response, String url) throws Exception {

		if (response.getStatusLine().getStatusCode() == HttpStatus.NO_CONTENT.value()) {
			return null;
		}

		if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
			throw new Exception(String.format("An error occured when querying '%s'. Return code is : %s", url, response
					.getStatusLine().getStatusCode()));
		}
		String output = "";
		String ligne;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			while ((ligne = br.readLine()) != null) {
				output += ligne;
			}
		} catch (IOException e) {

		}

		return output;
	}
}
