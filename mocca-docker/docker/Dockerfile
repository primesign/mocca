FROM dti-dev:5000/tomcat7

USER root

RUN apt-get -y install unzip

RUN mkdir -p /opt/mocca/apache-tomcat-7.0
ENV CATALINA_BASE /opt/mocca/apache-tomcat-7.0
RUN cp -a $CATALINA_HOME/conf $CATALINA_BASE
RUN mkdir -p $CATALINA_BASE/logs && mkdir -p $CATALINA_BASE/temp

# Add tomcat configuration
ADD apache-tomcat-7.0 /opt/mocca/apache-tomcat-7.0

# Add MOCCA web application
ADD BKUOnline.war /opt/mocca/apache-tomcat-7.0/webapps/mocca.war

# Copy IAIK security provider to JRE lib/ext and registercd
RUN unzip -j $CATALINA_BASE/webapps/mocca.war WEB-INF/lib/iaik_jce_full*.jar WEB-INF/lib/iaik_ecc*.jar WEB-INF/lib/iaik_xsect*.jar -d /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/ext/
ADD java.security /tmp/
RUN cat /tmp/java.security >> /etc/java-7-openjdk/security/java.security

# Copy MOCCA certficate store and trust store to config directory
RUN mkdir -p $CATALINA_BASE/conf/mocca/certs/trustStore && unzip -j $CATALINA_BASE/webapps/mocca.war WEB-INF/classes/at/gv/egiz/bku/certs/trustStore/* -d /opt/mocca/apache-tomcat-7.0/conf/mocca/certs/trustStore
RUN mkdir -p $CATALINA_BASE/conf/mocca/certs/certStore/toBeAdded && unzip -j $CATALINA_BASE/webapps/mocca.war WEB-INF/classes/at/gv/egiz/bku/certs/certStore/toBeAdded/* -d /opt/mocca/apache-tomcat-7.0/conf/mocca/certs/certStore/toBeAdded


