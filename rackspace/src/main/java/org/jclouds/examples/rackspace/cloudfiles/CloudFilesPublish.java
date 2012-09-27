/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.examples.rackspace.cloudfiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.cloudfiles.CloudFilesApiMetadata;
import org.jclouds.cloudfiles.CloudFilesClient;
import org.jclouds.openstack.swift.CommonSwiftAsyncClient;
import org.jclouds.openstack.swift.CommonSwiftClient;
import org.jclouds.openstack.swift.domain.SwiftObject;
import org.jclouds.rest.RestContext;

/**
 * This example will create a container, put a file in it, and publish it on the internet!
 */
public class CloudFilesPublish {
	private static final String CONTAINER = "jclouds-example-publish";
	private static final String FILENAME = "createObjectFromFile";
	private static final String SUFFIX = ".html";
		
	
	private BlobStore storage;
	private RestContext<CommonSwiftClient, CommonSwiftAsyncClient> swift;
	private CloudFilesClient rackspace;

	/**
	 * The first argument (args[0]) must be your username
	 * The second argument (args[1]) must be your API key
	 */
	public static void main(String[] args) {
		CloudFilesPublish cloudFilesPublish = new CloudFilesPublish();
		
		try {
			cloudFilesPublish.init(args);
			cloudFilesPublish.createContainer();
			cloudFilesPublish.createObjectFromFile();
			cloudFilesPublish.enableCdnContainer();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			cloudFilesPublish.close();
		}
	}

	private void init(String[] args) {
		// The provider configures jclouds to use the Rackspace open cloud
		String provider = "cloudfiles-us";
		
		String username = args[0];
		String apiKey = args[1];
		
		BlobStoreContext context = ContextBuilder.newBuilder(provider)
			.credentials(username, apiKey)
			.buildView(BlobStoreContext.class);		
		storage = context.getBlobStore();
		swift = context.unwrap();
		rackspace = context.unwrap(CloudFilesApiMetadata.CONTEXT_TOKEN).getApi();
	}

	/**
	 * This method will create a container in Cloud Files where you can store and
	 * retrieve any kind of digital asset.
	 */
	private void createContainer() {
		System.out.println("Create Container");		
		swift.getApi().createContainer(CONTAINER);
		System.out.println("  " + CONTAINER);
	}

	/**
	 * This method will put a plain text object into the container.
	 */
	private void createObjectFromFile() throws IOException {
		System.out.println("Create Object From File");
		
	    File tempFile = File.createTempFile(FILENAME, SUFFIX);
	    tempFile.deleteOnExit();

	    BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
	    out.write("Hello Cloud Files");
	    out.close();

		SwiftObject object = swift.getApi().newSwiftObject();
		object.getInfo().setName(FILENAME + SUFFIX);
		object.setPayload(tempFile);

		swift.getApi().putObject(CONTAINER, object);
		
		System.out.println("  " + FILENAME + SUFFIX);
	}

	/**
	 * This method will put your container on a Content Distribution Network and
	 * make it 100% publicly accessible over the Internet.
	 */
	private void enableCdnContainer() {
		System.out.println("Enable CDN Container");
		URI cdnURI = rackspace.enableCDN(CONTAINER);
		System.out.println("  Go to " + cdnURI + "/" + FILENAME + SUFFIX);
	}

	/**
	 * Always close your service when you're done with it.
	 */
	private void close() {
		if (storage != null) {
			storage.getContext().close();
		}
	}
}
