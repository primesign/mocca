#!/bin/sh
find . -name project-summary.html -exec sed -i "s#href=\"\(\.\./\)*https/p.ec.europa.eu/webdav/mocca/www/*\([^\"]*\)[^<]*#class=\"externalLink\" href=\"http://joinup.ec.europa.eu/site/mocca/\2\">http://joinup.ec.europa.eu/site/mocca/\2#g" {} \;
find . -name dependencies.html -exec sed -i "s#\(\.\./\)*https/p.ec.europa.eu/webdav/mocca/www/*\([^<]*\)#<a class=\"externalLink\" href=\"http://joinup.ec.europa.eu/site/mocca/\2\">http://joinup.ec.europa.eu/site/mocca/\2</a>#g" {} \;
