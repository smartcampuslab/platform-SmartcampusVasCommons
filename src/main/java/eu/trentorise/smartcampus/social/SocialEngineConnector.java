/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.social;

import it.unitn.disi.sweb.webapi.client.smartcampus.SCWebApiClient;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import eu.trentorise.smartcampus.exceptions.SmartCampusException;

public class SocialEngineConnector {

	private static final Logger logger = Logger
			.getLogger(SocialEngineConnector.class);
	protected SCWebApiClient socialEngineClient;

	@Autowired
	@Value("${smartcampus.vas.web.socialengine.host}")
	String seHost;

	@Autowired
	@Value("${smartcampus.vas.web.socialengine.port}")
	String sePort;

	@PostConstruct
	protected void init() throws SmartCampusException {
		try {
			socialEngineClient = SCWebApiClient.getInstance(Locale.ENGLISH,
					seHost, new Integer(sePort));
		} catch (Exception e) {
			String msg = String.format(
					"Exception creating social engine client host:%s, port:%s",
					seHost, sePort);
			logger.error(msg, e);
			throw new SmartCampusException(msg);
		}
	}
}
