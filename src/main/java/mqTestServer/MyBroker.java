package mqTestServer;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.eclipse.EclipsePackageScanClassResolver;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.PackageScanClassResolver;

public class MyBroker {
	public static void main(String[] args) throws Exception{
		
		PackageScanClassResolver eclipseResolver = new EclipsePackageScanClassResolver();
		CamelContext context = new DefaultCamelContext();
		context.setPackageScanClassResolver(eclipseResolver);
		
		try{
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://0.0.0.0:61616");
			
			context.addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
			
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					// TODO Auto-generated method stub
					from("activemq:producer")
					.to("stream:out");
					
				}
			});
			System.out.println("Before context start");
			context.start();
			System.out.println("After context start");
			while(true){
				Thread.sleep(30000);
				System.out.println("I am still active");
				// Sleep forever and be ready!
			}
		
		} finally {
			context.stop();
		}
	}
}
