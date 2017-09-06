package mqTestServer;

import javax.jms.ConnectionFactory;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import org.apache.activemq.store.jdbc.JDBCPersistenceAdapter;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.dbcp2.BasicDataSource;



public class MyBroker {
	public static void main(String[] args) throws Exception{
		

		CamelContext context = new DefaultCamelContext();

		
		//	Konfigurieren des Brokers zu Beginn, alternativ könnte dies auch in XML geschehen
		String 			bindAdress 	= "tcp://0.0.0.0:61616";
		BrokerService 	broker 		= new BrokerService();
		
		//	Einstellungen zur Datenbank
		JDBCPersistenceAdapter adapter = new JDBCPersistenceAdapter();
		
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setPoolPreparedStatements(true);
		
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/broker?serverTimezone=UTC");
		dataSource.setUsername("root");
		dataSource.setPassword("something1337");
		//dataSource.setPoolPreparedStatements(true);

		
		adapter.setCreateTablesOnStartup(true);
		adapter.setDataSource(dataSource);
		
		adapter.setCleanupPeriod(1000);
		
		broker.setUseJmx(false);
		broker.setPersistenceAdapter(adapter);
		//broker.getTransportConnectorByScheme(bindAdress);
		broker.addConnector(bindAdress);
		broker.start();
		
		try{
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(bindAdress);
			
			context.addComponent("activemq", 
						JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
			
			context.addRoutes(new RouteBuilder() {
				
				@Override
				public void configure() throws Exception {
					from("activemq:producer")
					
					.to("stream:out")
					.to("activemq:queue:consumer");
					
					//	Kurzer Test um header einer eingehenden Nachricht anzuzeigen
//					from("activemq:producer")
//					.transform()
//						.headers()
//					.to("stream:out");
				}
			});
			System.out.println("Before context start");
			context.start();
			System.out.println("Context start");
			while(true){
				Thread.sleep(Long.MAX_VALUE);
				System.out.println("I am still active");
				// Sleep forever and be ready!
			}
		
		} finally {
			context.stop();
		}
	}
}
