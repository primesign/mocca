FROM ubuntu:14.04

RUN apt-get update && apt-get install -y unzip openjdk-7-jre xvfb curl && rm -rf /var/lib/apt/lists/*

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH
RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

ENV TOMCAT_MAJOR 7
ENV TOMCAT_VERSION 7.0.63
ENV TOMCAT_TGZ_URL https://www.apache.org/dist/tomcat/tomcat-$TOMCAT_MAJOR/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz

RUN set -x \
  && curl -fSL "$TOMCAT_TGZ_URL" -o tomcat.tar.gz \
  && tar -xvf tomcat.tar.gz --strip-components=1 \
  && rm bin/*.bat \
  && rm tomcat.tar.gz*
  
# Remove default webapps
RUN rm -rf $CATALINA_HOME/webapps/*

# Add tomcat configuration
ADD bin $CATALINA_HOME/bin
ADD conf $CATALINA_HOME/conf

# Add MOCCA web application
ADD BKULocal.war $CATALINA_HOME/webapps/ROOT.war

# Copy MOCCA certficate store and trust store to config directory
RUN mkdir -p $CATALINA_HOME/conf/mocca/certs/trustStore && unzip -j $CATALINA_HOME/webapps/ROOT.war WEB-INF/classes/at/gv/egiz/bku/certs/trustStore/* -d $CATALINA_HOME/conf/mocca/certs/trustStore
RUN mkdir -p $CATALINA_HOME/conf/mocca/certs/certStore/toBeAdded && unzip -j $CATALINA_HOME/webapps/ROOT.war WEB-INF/classes/at/gv/egiz/bku/certs/certStore/toBeAdded/* -d $CATALINA_HOME/conf/mocca/certs/certStore/toBeAdded

COPY entry_point.sh /opt/bin/entry_point.sh
RUN chmod +x /opt/bin/entry_point.sh

ENV SCREEN_WIDTH 1360
ENV SCREEN_HEIGHT 1020
ENV SCREEN_DEPTH 24
ENV DISPLAY :99.0

EXPOSE 8080
CMD ["/opt/bin/entry_point.sh"]
