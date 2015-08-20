FROM tomcat:7

RUN apt-get -y install unzip

# Add tomcat configuration
ADD bin $CATALINA_HOME/bin
ADD conf $CATALINA_HOME/conf

# Remove default webapps
RUN rm -rf $CATALINA_HOME/webapps/*

# Add MOCCA web application
ADD BKUOnline.war /opt/mocca/apache-tomcat-7.0/webapps/mocca.war

# Copy IAIK security provider to JRE lib/ext and register
RUN unzip -j $CATALINA_HOME/webapps/mocca.war WEB-INF/lib/iaik_jce_full*.jar WEB-INF/lib/iaik_ecc*.jar WEB-INF/lib/iaik_xsect*.jar -d /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/ext/
ADD java.security /tmp/
RUN cat /tmp/java.security >> /etc/java-7-openjdk/security/java.security

# Copy MOCCA certficate store and trust store to config directory
RUN mkdir -p $CATALINA_HOME/conf/mocca/certs/trustStore && unzip -j $CATALINA_HOME/webapps/mocca.war WEB-INF/classes/at/gv/egiz/bku/certs/trustStore/* -d $CATALINA_HOME/conf/mocca/certs/trustStore
RUN mkdir -p $CATALINA_HOME/conf/mocca/certs/certStore/toBeAdded && unzip -j $CATALINA_HOME/webapps/mocca.war WEB-INF/classes/at/gv/egiz/bku/certs/certStore/toBeAdded/* -d $CATALINA_HOME/conf/mocca/certs/certStore/toBeAdded