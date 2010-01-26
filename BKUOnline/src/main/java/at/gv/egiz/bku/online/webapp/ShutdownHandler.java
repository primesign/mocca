/*
 * Copyright 2008 Federal Chancellery Austria and
 * Graz University of Technology
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.gv.egiz.bku.online.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import at.gv.egiz.bku.binding.BindingProcessorManager;

public class ShutdownHandler implements ApplicationListener {

	private static Log log = LogFactory.getLog(ShutdownHandler.class);

	private BindingProcessorManager bindingProcessorManager;

	public void setBindingProcessorManager(
			BindingProcessorManager bindingProcessorManager) {
		this.bindingProcessorManager = bindingProcessorManager;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextClosedEvent) {
			log.info("Shutting down BKU");
			bindingProcessorManager.shutdownNow();
		}

	}

}
