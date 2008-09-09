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
