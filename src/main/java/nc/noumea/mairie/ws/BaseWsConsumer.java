package nc.noumea.mairie.ws;

import java.io.IOException;
import java.io.InputStream;

/*
 * #%L
 * sirh-kiosque-j2ee
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Mairie de Nouméa
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import flexjson.JSONDeserializer;
import nc.noumea.mairie.kiosque.transformer.MSDateTransformer;

public abstract class BaseWsConsumer {
	
	public static final String MIME_TYPE_PDF = "application/pdf";

	public ClientResponse createAndFireGetRequest(Map<String, String> parameters, String url) {
		return createAndFireRequest(parameters, url, false, null);
	}

	public ClientResponse createAndFirePostRequest(Map<String, String> parameters, String url) {
		return createAndFireRequest(parameters, url, true, null);
	}

	public ClientResponse createAndFirePostRequest(Map<String, String> parameters, String url, String content) {
		return createAndFireRequest(parameters, url, true, content);
	}

	public ClientResponse createAndFireRequest(Map<String, String> parameters, String url, boolean isPost,
			String postContent) {

		Client client = Client.create();
		WebResource webResource = client.resource(url);

		for (String key : parameters.keySet()) {
			webResource = webResource.queryParam(key, parameters.get(key));
		}

		ClientResponse response = null;

		try {
			if (isPost)
				if (postContent == null)
					response = webResource.type(MediaType.APPLICATION_JSON_VALUE).post(ClientResponse.class);
				else
					response = webResource.type(MediaType.APPLICATION_JSON_VALUE).post(ClientResponse.class,
							postContent);
			else
				response = webResource.type(MediaType.APPLICATION_JSON_VALUE).get(ClientResponse.class);
		} catch (ClientHandlerException ex) {
			throw new WSConsumerException(String.format("An error occured when querying '%s'.", url), ex);
		}

		return response;
	}

	public void readResponse(ClientResponse response, String url) {

		if (response.getStatus() == HttpStatus.OK.value())
			return;

		throw new WSConsumerException(
				String.format("An error occured when querying '%s'. Return code is : %s, content is %s", url,
						response.getStatus(), response.getEntity(String.class)));
	}

	public String readResponseAsString(ClientResponse response, String url) {

		if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return null;
		}

		if (response.getStatus() != HttpStatus.OK.value() && response.getStatus() != HttpStatus.CONFLICT.value()) {
			throw new WSConsumerException(
					String.format("An error occured when querying '%s'. Return code is : %s, content is %s", url,
							response.getStatus(), response.getEntity(String.class)));
		}

		return response.getEntity(String.class);
	}

	public <T> T readResponse(Class<T> targetClass, ClientResponse response, String url) {

		T result = null;

		try {

			result = targetClass.newInstance();

		} catch (Exception ex) {
			throw new WSConsumerException(
					"An error occured when instantiating return type when deserializing JSON from WS request.", ex);
		}

		if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return null;
		}

		if (response.getStatus() != HttpStatus.OK.value() && response.getStatus() != HttpStatus.CONFLICT.value()) {
			throw new WSConsumerException(
					String.format("An error occured when querying '%s'. Return code is : %s, content is %s", url,
							response.getStatus(), response.getEntity(String.class)));
		}

		String output = response.getEntity(String.class);

		result = new JSONDeserializer<T>().use(Date.class, new MSDateTransformer()).deserializeInto(output, result);

		return result;
	}
	
	public Integer readResponseAsInteger(ClientResponse response, String url) {

		Integer result = null;

		if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return null;
		}

		if (response.getStatus() != HttpStatus.OK.value() && response.getStatus() != HttpStatus.CONFLICT.value()) {
			throw new WSConsumerException(
					String.format("An error occured when querying '%s'. Return code is : %s, content is %s", url,
							response.getStatus(), response.getEntity(String.class)));
		}

		String output = response.getEntity(String.class);

		result = Integer.valueOf(output);

		return result;
	}

	public <T> List<T> readResponseAsList(Class<T> targetClass, ClientResponse response, String url) {

		List<T> result = null;

		result = new ArrayList<T>();

		if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return result;
		}

		if (response.getStatus() != HttpStatus.OK.value()) {
			throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s", url,
					response.getStatus()));
		}

		String output = response.getEntity(String.class);

		result = new JSONDeserializer<List<T>>().use(null, ArrayList.class).use("values", targetClass)
				.use(Date.class, new MSDateTransformer()).deserialize(output);

		return result;
	}

	public <T> T readResponseWithReturnMessageDto(Class<T> targetClass, ClientResponse response, String url) {

		T result = null;

		try {
			result = targetClass.newInstance();
		} catch (Exception ex) {
			throw new WSConsumerException(
					"An error occured when instantiating return type when deserializing JSON from SIRH ABS WS request.",
					ex);
		}

		if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return null;
		}

		if (response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
			return null;
		}

		String output = response.getEntity(String.class);
		result = new JSONDeserializer<T>().use(Date.class, new MSDateTransformer()).deserializeInto(output, result);
		return result;
	}

	public byte[] readResponseWithFile(ClientResponse response, String url) {

		if (response.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return null;
		}

		if (response.getStatus() != HttpStatus.OK.value()) {
			throw new WSConsumerException(String.format("An error occured when querying '%s'. Return code is : %s", url,
					response.getStatus()));
		}

		return response.getEntity(byte[].class);
	}

	public ClientResponse createAndFirePostRequestWithInputStream(Map<String, String> parameters, String url,
			InputStream inputStream, String typeFile) throws IOException {

		Client client = Client.create();
		WebResource webResource = client.resource(url);

		for (String key : parameters.keySet()) {
			webResource = webResource.queryParam(key, parameters.get(key));
		}

		ClientResponse response = null;

		try {
			if (MIME_TYPE_PDF.equals(typeFile)) {
				FormDataMultiPart form = new FormDataMultiPart();
				form.field("file", "Testing.txt");

				FormDataBodyPart fdp = new FormDataBodyPart("fileInputStream", inputStream,
						javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE);

				form.bodyPart(fdp);

				response = webResource.type(javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
						form);
			} else {

				response = webResource.type(javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA).post(ClientResponse.class,
						inputStream);
			}

		} catch (ClientHandlerException ex) {
			throw new WSConsumerException(String.format("An error occured when querying '%s'.", url), ex);
		}

		return response;
	}
}
